package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.chat.CreateChatRoomRequest;
import com.example.airplanning.domain.entity.ChatMessage;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.service.ChatService;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // 채팅방 개설
    @ResponseBody
    @PostMapping("/room")
    public String createChatRoom(@RequestBody CreateChatRoomRequest request) {
        chatService.createChatRoom(request);
        return "채팅방 생성 완료";
    }

    // 채팅방 리스트 출력
    @GetMapping("/rooms")
    public String getAllRooms(Model model, Principal principal) {
        model.addAttribute("rooms", chatService.findMyRooms(principal.getName()));
        return "chat/list";
    }

    // 채팅방 조회
    @GetMapping("/room/{roomId}")
    public String getChatRoom(@PathVariable Long roomId, Model model, Principal principal,
                              @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        Long userId = userService.findUser(principal.getName()).getId();
        model.addAttribute("userId", userId);
        model.addAttribute("room", chatService.findRoomById(roomId));

        // 안 읽은 메세지 리스트
        List<ChatMessage> notReadMessages = chatService.findNotReadMessages(roomId, userId);
        model.addAttribute("notReadMessages", notReadMessages);

        // 채팅방 메세지 페이징 작업을 위해 첫번째 메세지 아이디 저장
        Long firstMessageId = Long.MAX_VALUE;
        if(notReadMessages.size() != 0) {
            firstMessageId = notReadMessages.get(0).getId();
        }

        // 안 읽은 메세지를 제외한 전에 있던 메세지 리스트
        Page<ChatMessage> moreMessages = chatService.findMoreMessages(roomId, firstMessageId, pageable);
        model.addAttribute("moreMessages", moreMessages);
        model.addAttribute("noMoreMessages", moreMessages.getTotalElements() <= 20 ? true : false);
        return "chat/room";
    }

    // 채팅 메세지 더 가져오기
    @GetMapping("/more/{roomId}")
    @ResponseBody
    public Page<ChatMessage> getMoreMessages(@PathVariable Long roomId, @RequestParam Long firstMessageId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        return chatService.findMoreMessages(roomId, firstMessageId, pageable);
    }
}
