package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Page<ChatRoom> findByUser1IdOrUser2Id(Long user1Id, Long user2Id, Pageable pageable);
}
