package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatRepository extends JpaRepository<Chat, Long> {

}
