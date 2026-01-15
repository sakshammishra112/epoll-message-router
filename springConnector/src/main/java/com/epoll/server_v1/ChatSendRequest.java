package com.epoll.server_v1;

public class ChatSendRequest {
    private int to;
    private String text;

    public ChatSendRequest() {}

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

