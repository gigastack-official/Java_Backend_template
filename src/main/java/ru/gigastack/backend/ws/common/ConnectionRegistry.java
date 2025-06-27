package ru.gigastack.backend.ws.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionRegistry {
    private static final Logger log = LoggerFactory.getLogger(ConnectionRegistry.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void add(WebSocketSession session) {
        sessions.add(session);
        log.info("WS connected: {}", session.getId());
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session);
        log.info("WS disconnected: {}", session.getId());
    }

    public void send(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException e) {
            log.error("WS send error → closing {}", session.getId(), e);
            closeSilently(session);
        }
    }

    public void broadcast(String payload) {
        sessions.forEach(s -> send(s, payload));
    }

    private void closeSilently(WebSocketSession session) {
        try { session.close(); } catch (IOException ignore) {}
    }
}