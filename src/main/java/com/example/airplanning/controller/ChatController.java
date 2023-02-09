package com.example.airplanning.controller;

import com.example.airplanning.domain.dto.chat.ChatMessageDto;
import com.example.airplanning.domain.dto.chat.ChatRoomDto;
import com.example.airplanning.domain.dto.chat.CreateChatRoomRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.ChatMessage;
import com.example.airplanning.domain.entity.ChatRoom;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public String getAllRooms(Model model, Principal principal, @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long loginUserId = userService.findUser(principal.getName()).getId();
        Page<ChatRoom> chatRooms = chatService.findMyRooms(loginUserId, pageable);

        List<ChatRoomDto> chatRoomDtos = chatRooms.stream()
                .map(chatRoom -> {
                    UserDto otherUser;
                    if(chatRoom.getUser1Id() == loginUserId) {
                        otherUser = userService.findUserById(chatRoom.getUser2Id());
                    } else {
                        otherUser = userService.findUserById(chatRoom.getUser1Id());
                    }
                    String otherUserNickname = otherUser.getNickname();
                    String image = otherUser.getImage();

                    ChatMessage lastChatMessage = chatService.findMessageById(chatRoom.getLastMessageId());
                    String lastMessage = lastChatMessage.getMessage();
                    String updatedAt = chatRoom.getUpdatedAt().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
                    Boolean hasNew = false;
                    // 내가 쓴 글이 아닌데 읽지 않았다면 => 새로운 메세지가 온 것으로 판단
                    if(lastChatMessage.getWriterId() != loginUserId && lastChatMessage.getIsRead() == false) {
                        hasNew = true;
                    }
                    return new ChatRoomDto(chatRoom.getId(), otherUserNickname, lastMessage, updatedAt, hasNew, image);
                })
                .collect(Collectors.toList());

        model.addAttribute("rooms", chatRoomDtos);
        return "chat/list";
    }

    // 채팅방 조회
    @GetMapping("/room/{roomId}")
    public String getChatRoom(@PathVariable Long roomId, Model model, Principal principal,
                              @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        ChatRoom chatRoom = chatService.findRoomById(roomId);
        Long userId = userService.findUser(principal.getName()).getId();

        // 채팅방에 속한 사람이 아니라면 접근 불가
        if(chatRoom.getUser1Id() != userId && chatRoom.getUser2Id() != userId) {
            model.addAttribute("nextPage", "/chat/rooms");
            model.addAttribute("msg", "해당 채팅방에 접근할 수 없습니다");
            return "error/redirect";
        }

        // 채팅방 이름 => "상대 유저 닉네임"과의 채팅방
        if(chatRoom.getUser1Id() == userId) {
            model.addAttribute("roomName", userService.findUserById(chatRoom.getUser2Id()).getNickname() + "님과의 채팅방");
        } else {
            model.addAttribute("roomName", userService.findUserById(chatRoom.getUser1Id()).getNickname() + "님과의 채팅방");
        }

        model.addAttribute("userId", userId);
        model.addAttribute("room", chatRoom);

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

    // 채팅방 삭제
    @ResponseBody
    @DeleteMapping("/room/{roomId}")
    public String createChatRoom(@PathVariable Long roomId) {
        chatService.deleteRoom(roomId);
        return "채팅방 삭제 완료";
    }
}
