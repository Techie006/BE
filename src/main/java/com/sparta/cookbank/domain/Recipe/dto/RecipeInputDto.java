package com.sparta.cookbank.domain.Recipe.dto;

import lombok.*;

import javax.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeInputDto {

    private String RCP_NM; // 이름
    private String RCP_WAY2;//방법 ex)끓이기
    private String RCP_PAT2;//요리종류 ex)반찬
    private String INFO_ENG;//열량
    private String INFO_CAR;//탄수화물
    private String INFO_PRO;//단백질
    private String INFO_FAT;//지방
    private String INFO_NA;//나트륨
    private String ATT_FILE_NO_MAIN;//이미지경로 소
    private String ATT_FILE_NO_MK;//이미지경로 대(썸내일)
    private String RCP_PARTS_DTLS;//재료정보
    private Long likeCount;//북마크수
    // 조리법 설명 및 그림
    private String MANUAL01;
    private String MANUAL_IMG01;
    private String MANUAL02;
    private String MANUAL_IMG02;
    private String MANUAL03;
    private String MANUAL_IMG03;
    private String MANUAL04;
    private String MANUAL_IMG04;
    private String MANUAL05;
    private String MANUAL_IMG05;
    private String MANUAL06;
    private String MANUAL_IMG06;
    private String MANUAL07;
    private String MANUAL_IMG07;
    private String MANUAL08;
    private String MANUAL_IMG08;


}
