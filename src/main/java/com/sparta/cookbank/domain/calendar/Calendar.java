package com.sparta.cookbank.domain.calendar;

import com.sparta.cookbank.domain.calendar.dto.CalendarRequestDto;
import com.sparta.cookbank.domain.calendar.enums.MealCategory;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Calendar{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    MealCategory mealDivision;

    @Column(nullable = false)
    private String mealDay;

    public void update(CalendarRequestDto requestDto, Recipe recipe) {
        this.recipe = recipe;
        this.mealDivision = requestDto.getCategory();
        this.mealDay=requestDto.getDay();

    }
}
