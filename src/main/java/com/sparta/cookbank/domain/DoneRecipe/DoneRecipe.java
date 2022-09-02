package com.sparta.cookbank.domain.DoneRecipe;

import com.sparta.cookbank.domain.Member.Member;
import com.sparta.cookbank.domain.Recipe.Recipe;
import com.sparta.cookbank.domain.Timestamped;
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
public class DoneRecipe extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public DoneRecipe(Member member, Recipe recipe){
        this.member = member;
        this.recipe = recipe;
    }
}
