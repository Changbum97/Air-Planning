package com.example.airplanning.service;

import com.example.airplanning.domain.dto.chat.ChatMessageDto;
import com.example.airplanning.domain.dto.chat.CreateChatRoomRequest;
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
import java.util.List;

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
            // 읽었다는 메세지가 아니고 입장 메세지도 아니면 저장
            ChatMessage chatMessage = dto.toEntity(chatRoomRepository.findById(dto.getRoomId()).get());
            ChatMessage saved = chatMessageRepository.save(chatMessage);
            dto.setCreatedAt(saved.getCreatedAt());
            dto.setId(saved.getId());
            dto.setIsRead(saved.getIsRead());
            log.info("TALK");
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
            log.info("ENTER");
        } else {
            // 읽었다는 메세지라면 타겟 메세지만 읽었다고 수정 후 전송
            ChatMessage targetChatMessage = chatMessageRepository.findById(dto.getTargetMessageId()).get();
            targetChatMessage.read();
            chatMessageRepository.save(targetChatMessage);
            log.info("READ");

        }
        template.convertAndSend("/sub/chat/room" + dto.getRoomId(), dto);
    }

    public void createChatRoom(CreateChatRoomRequest request) {

        // 작은 Id가 user1Id, 큰 Id가 user2Id
        Long user1Id = request.getUser1Id();
        Long user2Id = request.getUser2Id();
        if (user1Id > user2Id) {
            Long temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .user1Id(user1Id)
                .user2Id(user2Id)
                .build();

        chatRoomRepository.save(chatRoom);
    }

    public List<ChatRoom> findMyRooms(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUNDED));
        Long userId = user.getId();
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
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
}
