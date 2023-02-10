package com.example.airplanning.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user1Id;
    private Long user2Id;
    private Long lastMessageId;

    @OneToMany(mappedBy = "chatRoom", orphanRemoval = true)
    @JsonIgnore
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public void update(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
}
