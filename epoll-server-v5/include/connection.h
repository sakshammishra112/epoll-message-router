#pragma once
#include <string>

struct Connection {
    int user_id = -1;
    std::string in_buffer;
};
