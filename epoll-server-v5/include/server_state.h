#pragma once
#include <unordered_map>
#include <unordered_set>
#include <string>
#include <cstdint>
#include "connection.h"

struct PendingMessage {
    int to;
    int from;
    std::string payload;
};

extern uint64_t NEXT_MSG_ID;

extern std::unordered_map<uint64_t, PendingMessage> pending;
extern std::unordered_map<int, std::unordered_set<uint64_t>> pending_by_user;
extern std::unordered_map<int, int> user_to_fd;
extern std::unordered_map<int, Connection> connections;
