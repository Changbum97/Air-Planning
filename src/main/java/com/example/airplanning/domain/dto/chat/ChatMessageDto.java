package com.example.airplanning.domain.dto.chat;

import com.example.airplanning.domain.entity.ChatMessage;
import com.example.airplanning.domain.entity.ChatRoom;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {

    private Long id;
    private LocalDateTime createdAt;
    private String message;
    private Long writerId;
    private Boolean isRead;   // 상대가 읽었는지 체크
    private Long roomId;
    private Long targetMessageId;
    private String messageType; // ENTER, TALK, READ, DISCONNECTION

    public ChatMessage toEntity(ChatRoom chatRoom) {
        return ChatMessage.builder()
                .message(message)
                .writerId(writerId)
                .isRead(false)
                .chatRoom(chatRoom)
                .build();
    }
}
