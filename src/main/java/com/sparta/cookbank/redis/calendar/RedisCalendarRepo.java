package com.sparta.cookbank.redis.calendar;

import org.springframework.data.repository.CrudRepository;

public interface RedisCalendarRepo extends CrudRepository<RedisCalendar, Long> {
}
