package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.Board;
import com.example.airplanning.domain.entity.Comment;
import com.example.airplanning.domain.entity.Review;
import com.example.airplanning.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByUser(User user, Pageable pageable);
    Page<Comment> findAllByBoard(Board board, Pageable pageable);
    Page<Comment> findAllByReview(Review review, Pageable pageable);
}
