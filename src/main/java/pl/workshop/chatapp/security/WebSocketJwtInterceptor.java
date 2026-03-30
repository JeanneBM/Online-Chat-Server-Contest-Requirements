package pl.workshop.chatapp.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import pl.workshop.chatapp.service.SessionService;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final SessionService sessionService;

    public WebSocketJwtInterceptor(JwtService jwtService, SessionService sessionService) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String username = jwtService.extractUsername(token);
                    String sessionId = jwtService.extractSessionId(token);
                    if (jwtService.isTokenValid(token, username)
                            && sessionId != null
                            && sessionService.isSessionActive(username, sessionId)) {
                        accessor.setUser(new UsernamePasswordAuthenticationToken(username, null));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return message;
    }
}
