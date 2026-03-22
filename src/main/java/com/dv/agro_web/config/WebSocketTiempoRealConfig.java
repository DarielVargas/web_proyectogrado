package com.dv.agro_web.config;

import com.dv.agro_web.websocket.TiempoRealWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketTiempoRealConfig implements WebSocketConfigurer {

    private final TiempoRealWebSocketHandler tiempoRealWebSocketHandler;

    public WebSocketTiempoRealConfig(TiempoRealWebSocketHandler tiempoRealWebSocketHandler) {
        this.tiempoRealWebSocketHandler = tiempoRealWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(tiempoRealWebSocketHandler, "/ws/tiempo-real")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }
}
