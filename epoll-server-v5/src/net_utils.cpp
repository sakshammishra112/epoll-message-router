#include "net_utils.h"
#include <fcntl.h>

void make_non_blocking(int fd) {
    int flag = fcntl(fd, F_GETFL, 0);
    fcntl(fd, F_SETFL, flag | O_NONBLOCK);
}
