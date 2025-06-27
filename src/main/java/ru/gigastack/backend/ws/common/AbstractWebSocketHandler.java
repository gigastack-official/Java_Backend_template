package ru.gigastack.backend.ws.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Базовый хендлер, который:
 * – регистрирует / удаляет сессии в ConnectionRegistry,
 * – конвертирует JSON→T и отправляет в абстрактный handleMessage().
 */
public abstract class AbstractWebSocketHandler<T extends WebSocketMessage> extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractWebSocketHandler.class);

    private final ConnectionRegistry registry;
    private final ObjectMapper mapper;
    private final Class<T> type;

    protected AbstractWebSocketHandler(ConnectionRegistry registry, ObjectMapper mapper, Class<T> type) {
        this.registry = registry;
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        registry.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        registry.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WS transport error {}", session.getId(), exception);
        registry.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            T payload = mapper.readValue(message.getPayload(), type);
            handleMessage(session, payload);
        } catch (JsonProcessingException e) {
            log.warn("WS invalid JSON → {}", e.getMessage());
            registry.send(session, "{\"event\":\"ERROR\",\"detail\":\"bad_json\"}");
        }
    }

    protected abstract void handleMessage(WebSocketSession session, T message);
}