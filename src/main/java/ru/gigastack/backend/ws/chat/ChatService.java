package ru.gigastack.backend.ws.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.gigastack.backend.ws.common.ConnectionRegistry;

@Service
public class ChatService {

    private final ConnectionRegistry registry;
    private final ObjectMapper mapper;

    public ChatService(ConnectionRegistry registry, ObjectMapper mapper) {
        this.registry = registry;
        this.mapper = mapper;
    }

    public void broadcast(ChatMessage msg) {
        try {
            registry.broadcast(mapper.writeValueAsString(msg));
        } catch (JsonProcessingException ignore) {}
    }
}