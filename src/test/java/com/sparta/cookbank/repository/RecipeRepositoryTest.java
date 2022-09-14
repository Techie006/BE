package com.sparta.cookbank.repository;

import com.sparta.cookbank.config.TestQueryDslConfig;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeSearchRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test1")
// JpaQueryFactory가 PersistenceLayer가 아니여서 Config 를 만들어서 빈 등록
@Import(TestQueryDslConfig.class)
class RecipeRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RecipeRepository recipeRepository;

    @Nested
    @DisplayName("SearchRecipe")
    class SearchTest{
        private Recipe setRecipe() {
            Recipe setRecipe = Recipe.builder()
                    .RCP_NM("고등어")
                    .build();

            return recipeRepository.save(setRecipe);
        }

        @Test
        @DisplayName("레시피 검색 테스트")
        void RecipeSearch_Normal() {
            // given
            setRecipe();
            entityManager.clear();
            String recipe_name = "고등어";
            Pageable pageable = Pageable.ofSize(5);
            RecipeSearchRequestDto requestDto = new RecipeSearchRequestDto(recipe_name);

            //when
            Page<Recipe> recipePage = recipeRepository.findBySearchOption(requestDto, pageable);

            //then
            List<Recipe> recipeList = recipePage.getContent();
            assertThat(recipeList.get(0).getRCP_NM()).contains(recipe_name);
        }

        @Test
        @DisplayName("레시피 검색 실패 테스트")
        void RecipeSearch_Fail() {
            // given
            setRecipe();
            entityManager.clear();
            String recipe_name = "";
            Pageable pageable = Pageable.ofSize(5);
            RecipeSearchRequestDto requestDto = new RecipeSearchRequestDto(recipe_name);

            // when
            Exception exception = assertThrows(InvalidDataAccessApiUsageException.class, () -> {
                recipeRepository.findBySearchOption(requestDto, pageable);
            });

            // then
            assertEquals("검색어를 입력해주세요!", exception.getMessage());
        }
    }


}