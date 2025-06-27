package ru.gigastack.backend.ws.common;

public interface WebSocketMessage {
    String event();   // тип события: "CHAT_MESSAGE", "JOIN", "LEAVE" ...
}