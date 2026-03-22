package com.dv.agro_web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TiempoRealWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sesiones.add(session);
        enviar(session, new EventoTiempoRealDto("connected", null));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.remove(session);
    }

    public void broadcast(EventoTiempoRealDto evento) {
        sesiones.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sesiones) {
            try {
                enviar(session, evento);
            } catch (IOException ignored) {
                sesiones.remove(session);
            }
        }
    }

    private void enviar(WebSocketSession session, EventoTiempoRealDto evento) throws IOException {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(evento)));
        } catch (JsonProcessingException ex) {
            throw new IOException("No se pudo serializar el evento websocket", ex);
        }
    }

    public record EventoTiempoRealDto(String type, Object payload) {}
}