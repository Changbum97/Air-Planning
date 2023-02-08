package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.chat.ChatMessageDto;
import com.example.airplanning.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final ChatService chatService;

    @MessageMapping(value = "/chat/enter")
    public void enter(ChatMessageDto chatMessage) {
        chatService.sendChatMessage(chatMessage);
    }

    @MessageMapping(value = "/chat/message")
    public void send(ChatMessageDto chatMessage) {
        chatService.sendChatMessage(chatMessage);
    }

    @MessageMapping(value = "/chat/read")
    public void read(ChatMessageDto chatMessage) {
        //chatService.readChatMessage(chatMessage);
        chatService.sendChatMessage(chatMessage);
    }
}
