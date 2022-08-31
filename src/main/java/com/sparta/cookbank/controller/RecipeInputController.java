package com.sparta.cookbank.controller;


import com.sparta.cookbank.domain.Recipe.Recipe;
import com.sparta.cookbank.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@RequiredArgsConstructor
@RestController
public class RecipeInputController {

    private final RecipeRepository recipeRepository;

    @GetMapping("api/save")  // 공공api 레시피 저장
    public void saveTotalRecipe() throws IOException {
        String result = "";

        try {
            URL url = new URL("http://openapi.foodsafetykorea.go.kr/api/31d29db0e20c4009848a/COOKRCP01/json/1001/1061");

            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            result = bf.readLine();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(result);  // 스트링값을 JSON 객체로 만들어준다.
            JSONObject COOKRCP01 = (JSONObject)jsonObject.get("COOKRCP01"); // 키값으로 value 추출..
            JSONArray row = (JSONArray)COOKRCP01.get("row");  // 키값으로 value 행렬을 추출..


            for(int i = 0 ; i<row.size();i++){
//                System.out.println("row.get(0) = " + row.get(i));
                JSONObject jsonRecipe = (JSONObject)row.get(i);
                Recipe recipe = Recipe.builder()
                        .RCP_NM(jsonRecipe.get("RCP_NM").toString())
                        .RCP_WAY2(jsonRecipe.get("RCP_WAY2").toString())
                        .RCP_PAT2(jsonRecipe.get("RCP_PAT2").toString())
                        .INFO_ENG(jsonRecipe.get("INFO_ENG").toString())
                        .INFO_CAR(jsonRecipe.get("INFO_CAR").toString())
                        .INFO_PRO(jsonRecipe.get("INFO_PRO").toString())
                        .INFO_FAT(jsonRecipe.get("INFO_FAT").toString())
                        .INFO_NA(jsonRecipe.get("INFO_NA").toString())
                        .ATT_FILE_NO_MAIN(jsonRecipe.get("ATT_FILE_NO_MAIN").toString())
                        .ATT_FILE_NO_MK(jsonRecipe.get("ATT_FILE_NO_MK").toString())
                        .RCP_PARTS_DTLS(jsonRecipe.get("RCP_PARTS_DTLS").toString())
                        .MANUAL01(jsonRecipe.get("MANUAL01").toString())
                        .MANUAL_IMG01(jsonRecipe.get("MANUAL_IMG01").toString())
                        .MANUAL02(jsonRecipe.get("MANUAL02").toString())
                        .MANUAL_IMG02(jsonRecipe.get("MANUAL_IMG02").toString())
                        .MANUAL03(jsonRecipe.get("MANUAL03").toString())
                        .MANUAL_IMG03(jsonRecipe.get("MANUAL_IMG03").toString())
                        .MANUAL04(jsonRecipe.get("MANUAL04").toString())
                        .MANUAL_IMG04(jsonRecipe.get("MANUAL_IMG04").toString())
                        .MANUAL05(jsonRecipe.get("MANUAL05").toString())
                        .MANUAL_IMG05(jsonRecipe.get("MANUAL_IMG05").toString())
                        .MANUAL06(jsonRecipe.get("MANUAL06").toString())
                        .MANUAL_IMG06(jsonRecipe.get("MANUAL_IMG06").toString())
                        .MANUAL07(jsonRecipe.get("MANUAL07").toString())
                        .MANUAL_IMG07(jsonRecipe.get("MANUAL_IMG07").toString())
                        .MANUAL08(jsonRecipe.get("MANUAL08").toString())
                        .MANUAL_IMG08(jsonRecipe.get("MANUAL_IMG08").toString())
                        .build();

//                recipeRepository.save(recipe);     저장완료로 주석
            }
       }catch (Exception e){
            e.printStackTrace();
        }
    }
}
