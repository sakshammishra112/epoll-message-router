package com.epoll.server_v1;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Component
public class EngineTcpClient {

    private SocketChannel channel;

    @PostConstruct
    public void init() throws IOException {
        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 9000));
        channel.configureBlocking(true);

        System.out.println("Connected to C++ engine");
    }

    public synchronized void send(ByteBuffer frame) throws IOException {
        while (frame.hasRemaining()) {
            channel.write(frame);
        }
    }

    public SocketChannel channel() {
        return channel;
    }
}
