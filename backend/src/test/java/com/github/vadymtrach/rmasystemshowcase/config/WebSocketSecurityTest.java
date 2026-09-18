package com.github.vadymtrach.rmasystemshowcase.config;

import com.github.vadymtrach.rmasystemshowcase.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketSecurityTest extends IntegrationTest {

    private static final String TOPIC = "/topic/rma-updates";

    @Autowired
    private AuthorizationManager<Message<?>> messageAuthorizationManager;

    private final Authentication user = new TestingAuthenticationToken("user", "password", "ROLE_ADMIN");

    @Test
    void authenticatedClientsCanSubscribeToTopics() {
        assertThat(isGranted(user, SimpMessageType.SUBSCRIBE, TOPIC)).isTrue();
    }

    @Test
    void clientsCannotPublishToTopics() {
        // Only the server broadcasts; a client SEND would reach every connected browser.
        assertThat(isGranted(user, SimpMessageType.MESSAGE, TOPIC)).isFalse();
    }

    @Test
    void anonymousClientsCannotSubscribe() {
        assertThat(isGranted(null, SimpMessageType.SUBSCRIBE, TOPIC)).isFalse();
    }

    private boolean isGranted(Authentication authentication, SimpMessageType type, String destination) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(type);
        headers.setDestination(destination);
        Message<?> message = MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
        var decision = messageAuthorizationManager.authorize(() -> authentication, message);
        return decision != null && decision.isGranted();
    }
}
