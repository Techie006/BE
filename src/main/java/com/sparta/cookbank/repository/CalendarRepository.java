package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.calendar.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    List<Calendar> findAllByMealDayAndMember_Id(String day,Long id);
}
