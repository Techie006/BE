package com.sparta.cookbank.repository.search;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeSearchRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.sparta.cookbank.domain.recipe.QRecipe.recipe;

public class RecipeRepositoryImpl extends QuerydslRepositorySupport implements RecipeRepositoryCustom {

    @Autowired
    private JPAQueryFactory queryFactory; // JPAQueryFactory 빈 주입

    public RecipeRepositoryImpl() {
        super(Recipe.class);
    }

    @Override // 검색쿼리
    public Page<Recipe> findBySearchOption(RecipeSearchRequestDto searchRequestDto, Pageable pageable) {
        JPQLQuery<Recipe> query = queryFactory
                .selectFrom(recipe)
                .where(eqName(searchRequestDto));
        List<Recipe> recipes = this.getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<Recipe>(recipes, pageable, query.fetchCount());
    }

    @Override
    public List<Recipe> findByRecommendRecipeOption(String baseName) {
        JPQLQuery<Recipe> query = queryFactory
                .selectFrom(recipe)
                .where(eqBaseName(baseName));

        List<Recipe> recipeList = query.fetch();
        return recipeList;
    }

    // 검색 조건
    private BooleanExpression eqName(RecipeSearchRequestDto requestDto) {
        if (requestDto.getRecipe_name() == null || requestDto.getRecipe_name().isEmpty()) {
            throw new InvalidDataAccessApiUsageException("검색어를 입력해주세요!");
        }

        return recipe.RCP_NM.containsIgnoreCase(requestDto.getRecipe_name());
    }

    // 추천 레시피 Baes 조건
    private BooleanExpression eqBaseName(String baseName) {
        if (baseName == null || baseName.isEmpty()) {
            return null;
        }
        return recipe.RCP_PARTS_DTLS.containsIgnoreCase(baseName);
    }
}
