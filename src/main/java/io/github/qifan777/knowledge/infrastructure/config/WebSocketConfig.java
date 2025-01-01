package io.github.qifan777.knowledge.infrastructure.config;//package io.github.qifan777.knowledge.infrastructure.config;
//
//import io.github.qifan777.knowledge.chatRoom.WebSocketChatServer;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(new WebSocketChatServer(), "/ws/chat")
//                .setAllowedOrigins("*");
//    }
//}
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}