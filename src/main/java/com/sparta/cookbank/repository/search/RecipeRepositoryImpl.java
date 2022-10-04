package com.sparta.cookbank.repository.search;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeByCategoryRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeSearchRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        List<Recipe> recipes = Objects.requireNonNull(this.getQuerydsl()).applyPagination(pageable, query).fetch();
        return new PageImpl<>(recipes, pageable, query.fetchCount());
    }

    @Override
    public List<Recipe> findByRecommendRecipeOption(String baseName) {
        JPQLQuery<Recipe> query = queryFactory
                .selectFrom(recipe)
                .where(eqBaseName(baseName));

        return query.fetch();
    }

    @Override
    public Page<Recipe> findByCategoryRecipeOption(RecipeByCategoryRequestDto requestDto, Pageable pageable) {
        JPQLQuery<Recipe> query = queryFactory
                .selectFrom(recipe)
                .where(eqCategory(requestDto));

        List<Recipe> recipeList = Objects.requireNonNull(this.getQuerydsl()).applyPagination(pageable, query).fetch();
        return new PageImpl<>(recipeList, pageable, query.fetchCount());
    }

    private BooleanExpression eqCategory(RecipeByCategoryRequestDto requestDto) {
        if (requestDto.getType() == null || requestDto.getType().isEmpty() || requestDto.getCategory() == null || requestDto.getCategory().isEmpty()) {
            throw new InvalidDataAccessApiUsageException("잘못된 요청입니다!");
        }

        Boolean condition = null;
        if (requestDto.getType().equals("방법")) {
            condition = true;
        } else if (requestDto.getType().equals("종류")) {
            condition = false;
        }
        return (Boolean.TRUE.equals(condition))? recipe.RCP_WAY2.eq(requestDto.getCategory()) : recipe.RCP_PAT2.eq(requestDto.getCategory());
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
