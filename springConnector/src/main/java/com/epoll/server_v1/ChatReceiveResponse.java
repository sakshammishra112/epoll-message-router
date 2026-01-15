package com.epoll.server_v1;

public class ChatReceiveResponse {
    private long from;
    private String text;

    public ChatReceiveResponse(long from, String text) {
        this.from = from;
        this.text = text;
    }

    public long getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }
}

