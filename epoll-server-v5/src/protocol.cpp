#include "protocol.h"
#include <arpa/inet.h>
#include <cstring>

void write_u32(std::string& out, uint32_t v) {
    v = htonl(v);
    out.append(reinterpret_cast<char*>(&v), 4);
}

void write_u16(std::string& out, uint16_t v) {
    v = htons(v);
    out.append(reinterpret_cast<char*>(&v), 2);
}

void write_u64(std::string& out, uint64_t v) {
    v = htobe64(v);
    out.append(reinterpret_cast<char*>(&v), 8);
}

std::string build_msg(uint64_t msg_id, int to, int from, const std::string& payload) {
    std::string frame;

    uint32_t length = 2 + 8 + 4 + 4 + payload.size();
    write_u32(frame, length);
    write_u16(frame, MsgType::MSG);
    write_u64(frame, msg_id);

    uint32_t to_net = htonl(to);
    uint32_t from_net = htonl(from);

    frame.append(reinterpret_cast<char*>(&to_net), 4);
    frame.append(reinterpret_cast<char*>(&from_net), 4);
    frame.append(payload);

    return frame;
}

bool try_parse_frame(
    Connection& c,
    uint16_t& type,
    uint64_t& msg_id,
    std::string& payload
) {
    if (c.in_buffer.size() < 4) return false;

    uint32_t len;
    memcpy(&len, c.in_buffer.data(), 4);
    len = ntohl(len);

    if (c.in_buffer.size() < 4 + len) return false;

    memcpy(&type, c.in_buffer.data() + 4, 2);
    type = ntohs(type);

    memcpy(&msg_id, c.in_buffer.data() + 6, 8);
    msg_id = be64toh(msg_id);

    payload.assign(c.in_buffer.data() + 14, len - 10);
    c.in_buffer.erase(0, 4 + len);

    return true;
}
