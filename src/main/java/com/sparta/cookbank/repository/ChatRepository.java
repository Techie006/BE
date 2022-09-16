package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.chat.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByRoom_id(Long id);
}
