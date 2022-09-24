package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRedis_class_id(String roomId);
}