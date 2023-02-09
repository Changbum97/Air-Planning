package com.example.airplanning.service;

import com.example.airplanning.domain.dto.chat.ChatMessageDto;
import com.example.airplanning.domain.dto.chat.ChatRoomDto;
import com.example.airplanning.domain.dto.chat.CreateChatRoomRequest;
import com.example.airplanning.domain.dto.user.UserDto;
import com.example.airplanning.domain.entity.ChatMessage;
import com.example.airplanning.domain.entity.ChatRoom;
import com.example.airplanning.domain.entity.User;
import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import com.example.airplanning.repository.ChatMessageRepository;
import com.example.airplanning.repository.ChatRoomRepository;
import com.example.airplanning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // 특정 Broker로 메세지 전달
    private final SimpMessagingTemplate template;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendChatMessage(ChatMessageDto dto) {
        if(dto.getMessageType().equals("TALK")) {
            // 읽었다는 메세지가 아니고 입장 메세지도 아니면 메세지 저장
            ChatMessage chatMessage = dto.toEntity(chatRoomRepository.findById(dto.getRoomId()).get());
            ChatMessage saved = chatMessageRepository.save(chatMessage);
            dto.setCreatedAt(saved.getCreatedAt());
            dto.setId(saved.getId());
            dto.setIsRead(saved.getIsRead());

            // 메세지 저장 후 채팅방의 lastMessageId update
            ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).get();
            chatRoom.update(saved.getId());
            chatRoomRepository.save(chatRoom);

        } else if(dto.getMessageType().equals("ENTER")) {
            // 입장이라면 본인이 작성하지 않은 글 중 읽지 않은 글들을 불러와 읽었다고 수정
            List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdAndWriterIdNotAndIsRead(dto.getRoomId(), dto.getWriterId(), false);
            for (ChatMessage beforeChatMessage :chatMessages) {
                if(beforeChatMessage.getIsRead() == true) {
                    break;
                } else {
                    beforeChatMessage.read();
                    chatMessageRepository.save(beforeChatMessage);
                }
            }
        } else {
            // 읽었다는 메세지라면 타겟 메세지만 읽었다고 수정 후 전송
            ChatMessage targetChatMessage = chatMessageRepository.findById(dto.getTargetMessageId()).get();
            targetChatMessage.read();
            chatMessageRepository.save(targetChatMessage);
            log.info("READ");

        }
        template.convertAndSend("/sub/chat/room" + dto.getRoomId(), dto);
    }

    @Transactional
    public Long createChatRoom(CreateChatRoomRequest request, String userName) {

        // 작은 Id가 user1Id, 큰 Id가 user2Id
        Long user1Id = request.getUser1Id();
        Long user2Id = request.getUser2Id();
        if (user1Id > user2Id) {
            Long temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        // 이미 채팅방이 존재하면 채팅방으로 redirect
        Optional<ChatRoom> optChatRoom = chatRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id);
        if(optChatRoom.isPresent()) {
            return optChatRoom.get().getId();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .user1Id(user1Id)
                .user2Id(user2Id)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        // 채팅방에 "채팅방이 개설되었습니다"라는 메세지 추가
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        ChatMessage chatMessage = ChatMessage.builder()
                .message("채팅방이 개설되었습니다.")
                .isRead(false)
                .writerId(user.getId())
                .chatRoom(savedChatRoom)
                .build();

        savedChatRoom.update(chatMessageRepository.save(chatMessage).getId());
        return savedChatRoom.getId();
    }

    public Page<ChatRoom> findMyRooms(Long loginUserId, Pageable pageable) {
        return chatRoomRepository.findByUser1IdOrUser2Id(loginUserId, loginUserId, pageable);
    }

    public List<ChatRoomDto> findMyRooms(String userName, Pageable pageable) {
        User user =  userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));

        Long loginUserId = user.getId();

        return chatRoomRepository.findByUser1IdOrUser2Id(loginUserId, loginUserId, pageable)
                .stream().map(chatRoom -> {
                User otherUser;
                if(chatRoom.getUser1Id() == loginUserId) {
                    otherUser = userRepository.findById(chatRoom.getUser2Id())
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
                } else {
                    otherUser = userRepository.findById(chatRoom.getUser1Id())
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
                }

                String otherUserNickname = otherUser.getNickname();
                String image = otherUser.getImage();

                ChatMessage lastChatMessage = chatMessageRepository.findById(chatRoom.getLastMessageId())
                        .orElseThrow(() -> new AppException(ErrorCode.CHAT_MESSAGE_NOT_FOUNDED));
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
    }

    public ChatRoom findRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_ROOM_NOT_FOUNDED));
        return chatRoom;
    }

    public List<ChatMessage> findNotReadMessages(Long roomId, Long userId) {
        return chatMessageRepository.findByChatRoomIdAndWriterIdNotAndIsRead(roomId, userId, false);
    }

    public Page<ChatMessage> findMoreMessages(Long roomId, Long firstMessageId, Pageable pageable) {
        return chatMessageRepository.findByChatRoomIdAndIdLessThan(roomId, firstMessageId, pageable);
    }

    public void deleteRoom(Long roomId) {
        chatRoomRepository.deleteById(roomId);
    }
}
