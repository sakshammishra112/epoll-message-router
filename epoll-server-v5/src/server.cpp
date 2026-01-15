#include <netinet/in.h>
#include <errno.h>
#include <unistd.h>
#include <sys/epoll.h>
#include <sys/socket.h>
#include <iostream>
#include <cstring>   


#include "protocol.h"
#include "net_utils.h"
#include "server_state.h"

constexpr int PORT = 9000;
constexpr int MAX_EVENTS = 1024;


uint64_t NEXT_MSG_ID = 1;

std::unordered_map<uint64_t, PendingMessage> pending;
std::unordered_map<int, std::unordered_set<uint64_t>> pending_by_user;
std::unordered_map<int, int> user_to_fd;
std::unordered_map<int, Connection> connections;

void cleanup_client(int epfd, int fd) {
    auto it = connections.find(fd);
    if (it != connections.end()) {
        int uid = it->second.user_id;
        if (uid != -1) {
            user_to_fd.erase(uid);
            std::cout << "User " << uid << " disconnected\n";
        }
        connections.erase(fd);
    }
    epoll_ctl(epfd, EPOLL_CTL_DEL, fd, nullptr);
    close(fd);
}

void resend_pending(int user_id) {
    if (!pending_by_user.count(user_id)) return;

    int fd = user_to_fd[user_id];
    for (uint64_t mid : pending_by_user[user_id]) {
        const auto& pm = pending[mid];
        auto frame = build_msg(mid, pm.to, pm.from, pm.payload);
        write(fd, frame.data(), frame.size());
    }
}

void handle_ack(uint64_t msg_id) {
    auto it = pending.find(msg_id);
    if (it == pending.end()) return;

    int to = it->second.to;
    pending.erase(it);
    pending_by_user[to].erase(msg_id);

    std::cout << "Delivered msg " << msg_id << "\n";
}

void send_ack(int fd, uint64_t msg_id) {
    std::string frame;

    uint32_t len = 2 + 8;
    write_u32(frame, len);
    write_u16(frame, MsgType::ACK);
    write_u64(frame, msg_id);

    write(fd, frame.data(), frame.size());
}

void handle_frames(int fd) {
    Connection& conn = connections[fd];

    uint16_t type;
    uint64_t msg_id;
    std::string payload;

    while (try_parse_frame(conn, type, msg_id, payload)) {

        if (type == MsgType::LOGIN) {
            int uid;
            memcpy(&uid, payload.data(), sizeof(int));
            uid = ntohl(uid);

            conn.user_id = uid;
            user_to_fd[uid] = fd;

            std::cout << "User logged in (id = " << uid << ")\n";

            send_ack(fd, msg_id);
            resend_pending(uid);
        }

        else if (type == MsgType::SEND) {
            if (conn.user_id == -1) continue;

            int to;
            memcpy(&to, payload.data(), sizeof(int));
            to = ntohl(to);

            std::string text(payload.data() + 4, payload.size() - 4);

            uint64_t mid = NEXT_MSG_ID++;
            pending[mid] = {to, conn.user_id, text};
            pending_by_user[to].insert(mid);

            send_ack(fd, msg_id);

            if (user_to_fd.count(to)) {
                int to_fd = user_to_fd[to];
                auto frame = build_msg(mid, to_fd, conn.user_id, text);
                write(to_fd, frame.data(), frame.size());
            }
        }

        else if (type == MsgType::ACK) {
            handle_ack(msg_id);
        }
    }
}

void run_server() {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(PORT);
    addr.sin_addr.s_addr = INADDR_ANY;

    bind(server_fd, (sockaddr*)&addr, sizeof(addr));
    listen(server_fd, SOMAXCONN);

    make_non_blocking(server_fd);

    int epfd = epoll_create1(0);

    epoll_event sev{};
    sev.events = EPOLLIN;
    sev.data.fd = server_fd;
    epoll_ctl(epfd, EPOLL_CTL_ADD, server_fd, &sev);

    std::cout << "epoll server listening on port 9000\n";

    epoll_event events[MAX_EVENTS];

    while (true) {
        int n = epoll_wait(epfd, events, MAX_EVENTS, -1);

        for (int i = 0; i < n; i++) {
            int fd = events[i].data.fd;

            if (fd == server_fd) {
                while (true) {
                    int client_fd = accept(server_fd, nullptr, nullptr);
                    if (client_fd == -1) {
                        if (errno == EAGAIN) break;
                        perror("accept");
                        break;
                    }

                    make_non_blocking(client_fd);

                    epoll_event cev{};
                    cev.events = EPOLLIN;
                    cev.data.fd = client_fd;
                    epoll_ctl(epfd, EPOLL_CTL_ADD, client_fd, &cev);

                    connections[client_fd] = Connection{};
                    std::cout << "New Client fd = " << client_fd << "\n";
                }
            }
            else {
                char buff[1024];

                while (true) {
                    ssize_t bytes = read(fd, buff, sizeof(buff));
                    if (bytes > 0) {
                        connections[fd].in_buffer.append(buff, bytes);
                        handle_frames(fd);
                    }
                    else if (bytes == -1 && errno == EAGAIN) {
                        break;
                    }
                    else {
                        cleanup_client(epfd, fd);
                        break;
                    }
                }
            }
        }
    }
}
