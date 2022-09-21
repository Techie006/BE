package com.sparta.cookbank.redis.calendar;

import com.sparta.cookbank.domain.calendar.dto.CalendarListResponseDto;
import com.sparta.cookbank.domain.calendar.dto.CalendarMealsDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Getter
@Builder
@RedisHash(value = "dailyCalendar", timeToLive = 600) // 600s
public class RedisDayCalendar {
    @Id
    private String id;
    private CalendarListResponseDto meals;


}
