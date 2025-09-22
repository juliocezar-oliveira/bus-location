package com.projeto.bus_location.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita o processamento de mensagens WebSocket via STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. Define o prefixo para tópicos de destino (o cliente se inscreve aqui)
        config.enableSimpleBroker("/topic");

        // 2. Define o prefixo para os endpoints dos controllers (se houver, para receber mensagens)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 3. Define o endpoint que os clientes usarão para conectar.
        // O cliente se conectará a 'ws://localhost:8080/ws-connect'
        registry.addEndpoint("/ws-connect").setAllowedOriginPatterns("*").withSockJS();
    }
}