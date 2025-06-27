package ru.gigastack.backend.ws.chat;

import ru.gigastack.backend.ws.common.WebSocketMessage;

public record ChatMessage(String event, String from, String text) implements WebSocketMessage {}