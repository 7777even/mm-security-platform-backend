package com.sinopec.mmsecurity.config;

import com.sinopec.mmsecurity.websocket.AlarmWebSocketHandler;
import com.sinopec.mmsecurity.websocket.RealtimeAuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AlarmWebSocketHandler alarmWebSocketHandler;
    private final RealtimeAuthHandshakeInterceptor realtimeAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alarmWebSocketHandler, "/ws/alarm")
                .setAllowedOriginPatterns("*")
                .addInterceptors(realtimeAuthHandshakeInterceptor);
    }
}
