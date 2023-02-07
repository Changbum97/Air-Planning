package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);
}
