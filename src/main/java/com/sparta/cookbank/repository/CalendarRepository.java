package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.calendar.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
}
