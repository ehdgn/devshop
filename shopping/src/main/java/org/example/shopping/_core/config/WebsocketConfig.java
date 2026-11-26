package org.example.shopping._core.config;

import lombok.RequiredArgsConstructor;
import org.example.shopping.chatRoom.ChatRoomGuard;
import org.example.shopping.users.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String CHAT_TOPIC = "/sub/chat/";

    private final ChatRoomGuard chatRoomGuard;

    // 배포 시 실제 도메인을 쉼표로 추가 (예: https://devshop.example.com)
    @Value("${app.allowed-origins:http://localhost:8080}")
    private String[] allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    // 채팅방 구독은 그 방에 접근할 수 있는 사람만: 방 번호만 알면 남의 대화를 받아볼 수 있었음
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String destination = accessor.getDestination();
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                        && destination != null && destination.startsWith(CHAT_TOPIC)) {
                    Map<String, Object> attrs = accessor.getSessionAttributes();
                    User user = attrs == null ? null : (User) attrs.get("sessionUser");
                    if (!chatRoomGuard.canAccess(user, parseRoomId(destination))) {
                        throw new MessagingException("채팅방에 접근할 수 없습니다.");
                    }
                }
                return message;
            }
        });
    }

    private static Long parseRoomId(String destination) {
        try {
            return Long.valueOf(destination.substring(CHAT_TOPIC.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
