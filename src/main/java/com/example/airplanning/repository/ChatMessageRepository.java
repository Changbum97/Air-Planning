package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdAndWriterIdNotAndIsRead(Long roomId, Long writerId, Boolean isRead);
    Page<ChatMessage> findByChatRoomIdAndIdLessThan(Long roomId, Long id, Pageable pageable);
}
