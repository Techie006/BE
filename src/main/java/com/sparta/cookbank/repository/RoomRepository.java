package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

}