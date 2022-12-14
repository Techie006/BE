package com.sparta.cookbank.domain.myingredients;

import com.sparta.cookbank.domain.ingredient.Ingredient;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyIngredients {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Storage storage;

    @Column(nullable = false)
    private String inDate;

    @Column(nullable = false)
    private String expDate;

}
