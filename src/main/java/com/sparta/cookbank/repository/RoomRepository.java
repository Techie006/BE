package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRedisClassId(String roomId);
}