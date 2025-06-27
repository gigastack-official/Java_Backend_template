package ru.gigastack.backend.ws.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.gigastack.backend.ws.common.AbstractWebSocketHandler;
import ru.gigastack.backend.ws.common.ConnectionRegistry;

@Component
public class ChatWebSocketHandler extends AbstractWebSocketHandler<ChatMessage> {

    private final ChatService chatService;

    public ChatWebSocketHandler(ConnectionRegistry registry,
                                ObjectMapper mapper,
                                ChatService chatService) {
        super(registry, mapper, ChatMessage.class);
        this.chatService = chatService;
    }

    @Override
    protected void handleMessage(WebSocketSession session, ChatMessage message) {
        chatService.broadcast(message);
    }
}