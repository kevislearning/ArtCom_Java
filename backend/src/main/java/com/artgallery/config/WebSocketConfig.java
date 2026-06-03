package com.artgallery.config;

import com.artgallery.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> origins = new ArrayList<>();
        if (clientUrl != null && !clientUrl.trim().isEmpty()) {
            for (String url : clientUrl.split(",")) {
                origins.add(url.trim());
            }
        }
        if (!origins.contains("http://localhost:5173")) {
            origins.add("http://localhost:5173");
        }

        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins.toArray(new String[0]))
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            String token = servletRequest.getServletRequest().getParameter("token");
                            if (token != null) {
                                attributes.put("token", token);
                            }
                        }
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {}
                })
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String jwt = null;
                    
                    // 1. Try to read from native headers
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String bearer = authHeaders.get(0);
                        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                            jwt = bearer.substring(7);
                        }
                    }

                    // 2. Try to read from query params mapped to Session Attributes by HandshakeInterceptor
                    if (!StringUtils.hasText(jwt) && accessor.getSessionAttributes() != null) {
                        Object tokenAttr = accessor.getSessionAttributes().get("token");
                        if (tokenAttr != null) {
                            jwt = tokenAttr.toString();
                        }
                    }

                    // 3. Try to read from custom native header passcode
                    if (!StringUtils.hasText(jwt)) {
                        List<String> tokenHeaders = accessor.getNativeHeader("token");
                        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                            jwt = tokenHeaders.get(0);
                        }
                    }

                    if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                        UUID userId = tokenProvider.getUserIdFromToken(jwt);
                        
                        Principal principal = new Principal() {
                            @Override
                            public String getName() {
                                return userId.toString();
                            }
                        };
                        
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, Collections.emptyList());
                        
                        accessor.setUser(authentication);
                        System.out.println("[WebSocketConfig] Securely established WS principal for: " + userId);
                    }
                }
                return message;
            }
        });
    }
}
