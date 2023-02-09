package com.example.airplanning.domain.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ChatRoomDto {

    private Long id;
    private String otherUserNickname;   // 채팅 상대 유저 닉네임
    private String lastMessage;         // 마지막 메세지 => 리스트에서도 출력
    private String updatedAt;           // 마지막 메세지 생성 시간
    private Boolean hasNew;             // 내가 안 읽은 메세지가 있는지
    private String image;               // 상대 유저 프로필 이미지
}
