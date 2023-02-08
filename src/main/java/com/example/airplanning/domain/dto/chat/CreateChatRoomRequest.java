package com.example.airplanning.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomRequest {

    private Long user1Id;
    private Long user2Id;
}
