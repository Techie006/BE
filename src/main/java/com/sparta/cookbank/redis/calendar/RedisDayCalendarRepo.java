package com.sparta.cookbank.redis.calendar;

import com.sparta.cookbank.redis.ingredient.RedisIngredient;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RedisDayCalendarRepo extends CrudRepository<RedisDayCalendar, String> {
    Optional<RedisDayCalendar> findById(String day);
}
