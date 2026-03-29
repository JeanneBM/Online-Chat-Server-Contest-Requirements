package pl.workshop.chatapp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import pl.workshop.chatapp.model.ChatMessage;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        return chatMessage;
    }
}
