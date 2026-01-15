#pragma once
#include <string>
#include <cstdint>
#include "connection.h"

enum MsgType : uint64_t {
    LOGIN = 1,
    SEND = 2,
    MSG = 3,
    ACK = 4
};

void write_u32(std::string& out, uint32_t v);
void write_u16(std::string& out, uint16_t v);
void write_u64(std::string& out, uint64_t v);

std::string build_msg(
    uint64_t msg_id,
    int to,
    int from,
    const std::string& payload
);

bool try_parse_frame(
    Connection& c,
    uint16_t& type,
    uint64_t& msg_id,
    std::string& payload
);
