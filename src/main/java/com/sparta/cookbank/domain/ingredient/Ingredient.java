package com.sparta.cookbank.domain.ingredient;

import com.sparta.cookbank.domain.ingredient.enums.FoodCategory;
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

    @Column
    String markName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    FoodCategory foodCategory;

    @Column
    String iconImage;
}
