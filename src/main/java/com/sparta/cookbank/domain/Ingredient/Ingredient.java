package com.sparta.cookbank.domain.Ingredient;

import com.sparta.cookbank.domain.Ingredient.enums.FoodCategory;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String foodName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    FoodCategory foodCategory;
}
