package com.sparta.cookbank.domain.Recipe;

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
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    String RCP_NM;

    @Column
    String RCP_WAY2;//방법 ex)끓이기

    @Column
    String RCP_PAT2;//요리종류 ex)반찬

    @Column
    String INFO_ENG;//열량

    @Column
    String INFO_CAR;//탄수화물

    @Column
    String INFO_PRO;//단백질

    @Column
    String INFO_FAT;//지방

    @Column
    String INFO_NA;//나트륨

    @Column
    String ATT_FILE_NO_MAIN;//이미지경로 소

    @Column
    String ATT_FILE_NO_MK;//이미지경로 대(썸내일)

    @Column
    String RCP_PARTS_DTLS;//재료정보

    @Column
    Long likeCount;//북마크수
}
