package org.lang.config;

import org.lang.handler.DefaultWebSocketHandler;
import org.lang.interceptor.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    private WebSocketInterceptor webSocketInterceptor;

    @Autowired
    private DefaultWebSocketHandler defaultWebSocketHandler;

//    @Bean
//    public DefaultWebSocketHandler defaultWebSocketHandler() {
//        return new DefaultWebSocketHandler();
//    }

//    @Bean
//    public IMyWebSocket myWebSocket() {
//        return new MyWebSocketImpl();
//    }

//    @Bean
//    public WebSocketInterceptor webSocketInterceptor() {
//        return new WebSocketInterceptor();
//    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(defaultWebSocketHandler, "ws/message")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");

    }
}

