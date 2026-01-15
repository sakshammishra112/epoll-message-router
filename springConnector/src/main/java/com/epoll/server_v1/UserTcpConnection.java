package com.epoll.server_v1;

import java.nio.channels.SocketChannel;

public class UserTcpConnection {
    final SocketChannel channel;
    final Thread readerThread;

    public UserTcpConnection(SocketChannel channel, Thread readerThread) {
        this.channel = channel;
        this.readerThread = readerThread;
    }
}

