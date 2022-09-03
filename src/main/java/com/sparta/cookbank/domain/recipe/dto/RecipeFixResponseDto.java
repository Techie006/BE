package com.sparta.cookbank.domain.recipe.dto;

import com.sparta.cookbank.domain.recipe.Recipe;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeFixResponseDto {
    Long nextRecipeNum;
    String nextRecipeName;
    String nextRecipeIngredients;

    public RecipeFixResponseDto(Recipe nextRecipe) {
        this.nextRecipeNum = nextRecipe.getId();
        this.nextRecipeName = nextRecipe.getRCP_NM();
        this.nextRecipeIngredients = nextRecipe.getRCP_PARTS_DTLS();
    }
}
