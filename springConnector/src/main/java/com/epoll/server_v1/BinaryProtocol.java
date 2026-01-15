package com.epoll.server_v1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class BinaryProtocol {

    public static final short LOGIN = 1;
    public static final short SEND  = 2;
    public static final short MSG   = 3;
    public static final short ACK   = 4;

    public static ByteBuffer loginFrame(int userId) {
        ByteBuffer buf = ByteBuffer.allocate(4 + 2 + 8 + 4);
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putInt(2 + 8 + 4);
        buf.putShort(LOGIN);
        buf.putLong(0);
        buf.putInt(userId);

        buf.flip();
        return buf;
    }

    public static ByteBuffer sendFrame(int to, String text) {
        byte[] msg = text.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer.allocate(4 + 2 + 8 + 4 + msg.length);
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putInt(2 + 8 + 4 + msg.length);
        buf.putShort(SEND);
        buf.putLong(0);
        buf.putInt(to);
        buf.put(msg);

        buf.flip();
        return buf;
    }

    public static ByteBuffer ackFrame(long msgId) {
        ByteBuffer buf = ByteBuffer.allocate(4 + 2 + 8);
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putInt(2 + 8);
        buf.putShort(ACK);
        buf.putLong(msgId);

        buf.flip();
        return buf;
    }
}

