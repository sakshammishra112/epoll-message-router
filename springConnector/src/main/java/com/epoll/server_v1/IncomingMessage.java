package com.epoll.server_v1;

public class IncomingMessage {

    private String from;   // email
    private String text;

    public IncomingMessage(String from, String text) {
        this.from = from;
        this.text = text;
    }

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }
}
