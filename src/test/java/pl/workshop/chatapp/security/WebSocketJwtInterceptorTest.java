package pl.workshop.chatapp.security;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import pl.workshop.chatapp.service.SessionService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketJwtInterceptorTest {
    @Test
    void connectPreservesPrincipalAndSessionOnTheOriginalMessageAccessor() {
        JwtService jwt = mock(JwtService.class);
        SessionService sessions = mock(SessionService.class);
        when(jwt.extractEmail("token")).thenReturn("alice@local.test");
        when(jwt.extractSessionId("token")).thenReturn("session");
        when(jwt.isTokenValid("token", "alice@local.test")).thenReturn(true);
        when(sessions.isSessionActive("alice@local.test", "session")).thenReturn(true);
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer token");
        headers.setSessionAttributes(new HashMap<>());
        headers.setLeaveMutable(true);
        Message<byte[]> original = MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
        Message<?> result = new WebSocketJwtInterceptor(jwt, sessions).preSend(original, null);
        assertSame(original, result);
        StompHeaderAccessor actual = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(actual.getUser());
        assertEquals("alice@local.test", actual.getUser().getName());
        assertEquals("session", actual.getSessionAttributes().get("authSessionId"));
    }

    @Test
    void connectWithoutAuthenticationIsRejected() {
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
        var interceptor = new WebSocketJwtInterceptor(mock(JwtService.class), mock(SessionService.class));
        assertThrows(BadCredentialsException.class, () -> interceptor.preSend(message, null));
    }
}
