package com.sparta.cookbank.service;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.ingredient.Ingredient;
import com.sparta.cookbank.domain.ingredient.dto.AutoIngredientResponseDto;
import com.sparta.cookbank.domain.ingredient.dto.IngredientResponseDto;
import com.sparta.cookbank.domain.ingredient.dto.IngredientsRatioResponseDto;
import com.sparta.cookbank.domain.ingredient.dto.TotalIngredientResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.myingredients.dto.*;
import com.sparta.cookbank.redis.ingredient.RedisIngredient;
import com.sparta.cookbank.redis.ingredient.RedisIngredientRepo;
import com.sparta.cookbank.repository.IngredientsRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.MyIngredientsRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientsRepository ingredientsRepository;
    private final MemberRepository memberRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final TokenProvider tokenProvider;
    private final RedisIngredientRepo redisIngredientRepo;

    @Transactional(readOnly = true)
    public ResponseDto<?> findAutoIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        // 분기 처리.
        // 빈배열이면 fail
        if(food_name.isEmpty()){
            return ResponseDto.fail(null,"내용을 입력하세요.");
        }

        //한글이 아닐시 fail
        String pattern = "^[가-힣]*$";
        boolean result = Pattern.matches(pattern,food_name);
        if(!result){
            return ResponseDto.fail(null, "한글만 입력가능합니다.");
        }

        //해당 검색어 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContainingOrderByMarkName(food_name);
        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();

        // 검색내용이 없다면 예외처리리
       if (ingredients.isEmpty()){
            return ResponseDto.success(null, "검색 내용이 없습니다.");
        }
        // 5개만 보여주기
        for (Ingredient ingredient : ingredients) {
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredient.getId())
                    .food_name(ingredient.getFoodName())
                    .group_name(ingredient.getFoodCategory())
                    .build());
        }

        AutoIngredientResponseDto responseDto = AutoIngredientResponseDto.builder()
                .auto_complete(dtoList)
                .build();



        return ResponseDto.success(responseDto, "자동완성 리스트 제공에 성공하였습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> findIngredient(String food_name, HttpServletRequest request, Pageable pageable) {

        // Token 유효성 검사 없음

        // 분기 처리
        // 빈배열이면 fail
        if(food_name.isEmpty()){
            return ResponseDto.fail(null,"내용을 입력하세요.");
        }

        //한글이 아닐시 fail
        String pattern = "^[가-힣]*$";
        boolean result = Pattern.matches(pattern,food_name);
        if(!result){
            return ResponseDto.fail(null, "한글만 입력가능합니다.");
        }



        //해당 검색 찾기
        Page<Ingredient> ingredientPage = ingredientsRepository.findAllByFoodNameIsContaining(food_name, pageable);
        List<IngredientResponseDto> dtoList = new ArrayList<>();
        for(Ingredient ingredient : ingredientPage){
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredient.getId())
                    .food_name(ingredient.getFoodName())
                    .group_name(ingredient.getFoodCategory())
                    .build());
        }

        TotalIngredientResponseDto responseDto = TotalIngredientResponseDto.builder()
                .current_page_num(ingredientPage.getPageable().getPageNumber()+1)
                .total_page_num(ingredientPage.getTotalPages())
                .search_list(dtoList)
                .build();
        return ResponseDto.success(responseDto,"식재료 검색에 성공하였습니다.");
    }
    @Transactional
    public ResponseDto<?> saveMyIngredient(IngredientRequestDto requestDto, HttpServletRequest request) throws ParseException {

        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        //재료 id 검사
        if(requestDto.getId() == null){
            return ResponseDto.fail("214","재료를 선택해주세요!");
        }

        // Storage 검사
        switch (requestDto.getStorage()) {
            case "freeze":
                break;
            case "room_temp":
                break;
            case "refrigerated":
                break;
            default:
                throw new IllegalArgumentException("보관방법을 선택해주세요!");
        }

        //입주날짜 검사
        if(requestDto.getIn_date().isEmpty()||requestDto.getExp_date().isEmpty()){
            throw new IllegalArgumentException("입주날짜 혹은 유통기한을 추가해주세요!");
        }

        //입주 유통기한 비교
        Date inPutDay = new SimpleDateFormat("yyyy-MM-dd").parse(requestDto.getIn_date());
        Date expDay = new SimpleDateFormat("yyyy-MM-dd").parse( requestDto.getExp_date());
        int result = expDay.compareTo(inPutDay);
        // result가 0보다 작다면 유통기한이 입주날짜보다 앞선 날짜라는것이됨..
        if(result<0){
            throw new IllegalArgumentException("입주날짜가 유통기한보다 이전날짜입니다.");
        }


        //재료찾기
        Ingredient ingredient = ingredientsRepository.findById(requestDto.getId()).orElseThrow(
                () -> new IllegalArgumentException("해당 음식 재료가 존재 하지 않습니다.")
        );

        MyIngredients myIngredients = MyIngredients.builder()
                .member(member)
                .ingredient(ingredient)
                .storage(Storage.valueOf(requestDto.getStorage()))
                .inDate(requestDto.getIn_date())
                .expDate(requestDto.getExp_date())
                .build();
        myIngredientsRepository.save(myIngredients);
        // 레디스 캐시 초기화.
        String redisStorage = member.getEmail()+requestDto.getStorage();
        redisIngredientRepo.deleteById(redisStorage);
        return ResponseDto.success("","작성완료");
    }


    @Transactional(readOnly = true)
    public ResponseDto<?> getMyIngredient(String storage, HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();
        //전체 갯수 조회
        List<MyIngredients> totalMyIngredients = myIngredientsRepository.findAllByMemberIdOrderByExpDate(member.getId());
        List<MyIngredientResponseDto> dtoList = new ArrayList<>();
        long total_nums = totalMyIngredients.size();


        // 없을시 empty false 로 응답.
        if(totalMyIngredients.isEmpty()){
            StorageResponseDto responseDto = StorageResponseDto.builder()
                    .empty(true)
                    .total_nums(0)
                    .storage(dtoList)
                    .build();
            return ResponseDto.success(responseDto,"리스트제공에 성공하였습니다.");
        }

        // 나의 재료 전체조회
        if(storage.equals("")){

            StorageResponseDto responseDto = getStorageResponseDto(totalMyIngredients, dtoList,total_nums);

            return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
        }else {
            // Storage별 조회
            String redisStorage = member.getEmail()+storage;
            Optional<RedisIngredient> ingredientList = redisIngredientRepo.findById(redisStorage);

            // 캐시에서 확인, 만약 없을시 DB에서 검색후 캐시저장.
            if(ingredientList.isEmpty()){
                Storage storage1 = Storage.valueOf(storage);
                List<MyIngredients> myIngredients = myIngredientsRepository.findByMemberIdAndStorageOrderByExpDate(member.getId(), storage1);
                StorageResponseDto responseDto = getStorageResponseDto(myIngredients, dtoList,total_nums);
                //레디스 캐시에 저장..
                RedisIngredient redisIngredient = RedisIngredient.builder()
                        .id(redisStorage)
                        .empty(false)
                        .total_nums(total_nums)/// 여기에 저장
                        .storageList(responseDto)
                        .build();
                //레디스 캐시에 저장..
                redisIngredientRepo.save(redisIngredient);

                return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
            }else {   // 캐시에 있을시 캐시를 출력.
                RedisIngredient redisIngredient = ingredientList.get();
                StorageResponseDto responseDto = redisIngredient.getStorageList();

                return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
            }

        }


    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getMyWarningIngredient(HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        List<MyIngredients> myIngredients = myIngredientsRepository.findAllByMemberIdOrderByExpDate(member.getId());
        List<MyIngredientResponseDto> outList = new ArrayList<>();
        List<InHurryIngredientDto> hurryList = new ArrayList<>();
        List<String> stringHurryList = new ArrayList<>();

        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        for (MyIngredients myIngredient : myIngredients){
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec= (outDay.getTime()-nowDay.getTime())/1000;
            Long diffDays = diffSec / (24*60*60);
            String d_day;
            if(diffDays < 0){  // 유통기한 넘을시 추가..
                diffDays = -diffDays;
                d_day ="+"+diffDays;
                outList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .mark_name(myIngredient.getIngredient().getMarkName())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .build());

            }else if(diffDays < 5) {     // 7일 미만 HurryList 추가.
                hurryList.add(InHurryIngredientDto.builder()
                        .mark_name(myIngredient.getIngredient().getMarkName())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .build());
            }
        }

        if (hurryList.isEmpty()){
            WarningResponseDto responseDto = WarningResponseDto.builder()
                    .empty(true)
                    .out_dated_num(outList.size())
                    .in_hurry_num(0)
                    .out_dated(outList)
                    .in_hurry(hurryList)
                    .build();

            return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다");
        }


        // 중복된 임박재료 리스트 set으로 제거  @EqualsAndHashCode  사용..
        LinkedHashSet<InHurryIngredientDto> set = new LinkedHashSet<>(hurryList);
        List<InHurryIngredientDto> setHurryList = new ArrayList<>(set);


        WarningResponseDto responseDto = WarningResponseDto.builder()
                .empty(false)
                .out_dated_num(outList.size())
                .in_hurry_num(hurryList.size())
                .out_dated(outList)
                .in_hurry(setHurryList)
                .build();

        return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다");
    }

    @Transactional
    public ResponseDto<?> deleteMyIngredient(Long myIngredientId, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();



        //재료 유효성 검사
        MyIngredients myIngredients = myIngredientsRepository.findById(myIngredientId).orElseThrow(
                () -> new IllegalArgumentException("이미 삭제된 재료입니다.")
        );

        //로그인한 멤버 id와 작성된 재료의 멤버 id와 다를시 예외처리
        if(!getMember().getId().equals(myIngredients.getMember().getId())){
            throw new RuntimeException("타인의 식재료를 삭제할 수 없습니다.");
        }

        myIngredientsRepository.delete(myIngredients);
        // 레디스 캐시 초기화.
        String redisStorage = member.getEmail()+myIngredients.getStorage();
        redisIngredientRepo.deleteById(redisStorage);
        return ResponseDto.success("","재료 삭제가 성공하였습니다.");
    }

    private void extracted(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        token = resolveToken(token);
        tokenProvider.validateToken(token);
    }

    private Member getMember() {
        return memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
    }


    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid token !!");
    }

    private StorageResponseDto getStorageResponseDto(List<MyIngredients> myIngredients, List<MyIngredientResponseDto> dtoList,
                                                     long total_nums  ) throws ParseException {
        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        for (MyIngredients myIngredient : myIngredients) {
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec = (outDay.getTime()-nowDay.getTime())/1000;  //밀리초로 나와서 1000을 나눠야지 초 차이로됨
            Long diffDays = diffSec / (24*60*60); // 일자수 차이
            String d_day;
            if(diffDays < 0){  //"유통기간만료"를 출력.
                diffDays = -diffDays;
                dtoList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .mark_name(myIngredient.getIngredient().getMarkName())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("기한 만료")
                        .build());

            }else if(diffDays == 0){ // 당일 재료는 "D-DAY"로출력
                dtoList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .mark_name(myIngredient.getIngredient().getMarkName())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D-Day")
                        .build());
            }else {  //유통기간낸는 "D-남은날짜"
                d_day ="-"+diffDays.toString();

                dtoList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .mark_name(myIngredient.getIngredient().getMarkName())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .build());

            }

        }

        return StorageResponseDto.builder()
                .empty(false)
                .total_nums(total_nums)// 넣기
                .storage(dtoList)
                .build();
    }

    // 나만의 냉장고 상태 표시
    @Transactional(readOnly = true)
    public IngredientsRatioResponseDto MyRefrigeratorState() {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인 한 유저를 찾을 수 없습니다.");
        });

        boolean empty = false;

        List<MyIngredients> myIngredientsList = myIngredientsRepository.findAllByMemberId(member.getId());
        if (myIngredientsList.isEmpty()) {
            empty = true;
        }

        List<Integer> countList = new ArrayList<>();

        int warningCount = 0;
        int inHurryCount = 0;
        int fineCount = 0;

        // 5일 미만이면 hurry, < 0 warning
        for (MyIngredients myIngredients : myIngredientsList) {
            String match = "[^0-9]";
            int exp_date = Integer.parseInt(myIngredients.getExpDate().replaceAll(match,""));
            int in_date = Integer.parseInt(myIngredients.getInDate().replaceAll(match,""));
            if ((exp_date - in_date) < 5 && (exp_date - in_date) >= 0) { // 남은 유통기한이 5일 미만일 때 = inHurry
                inHurryCount++;
            }  else if ((exp_date - in_date) >= 5) { // 남은 유통기한이 5일 이상일때
                fineCount++;
            } else if ((exp_date - in_date) < 0) { // 유통기한이 지난 재료
                warningCount++;
            }
        }

        countList.add(inHurryCount);
        countList.add(warningCount);
        countList.add(fineCount);

        return IngredientsRatioResponseDto.builder()
                .empty(empty)
                .count(countList)
                .build();
    }

    // 제품류 나눠서 보여주기
    @Transactional(readOnly = true)
    public IngredientsRatioResponseDto ingredientsByCategory() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인 한 유저를 찾을 수 없습니다.");
        });

        boolean empty = false;

        // 농산물
        int produceNum = 0;
        // 축산물
        int livestockNum = 0;
        // 수산물
        int marineNum = 0;
        // 음료류
        int drinkNum = 0;
        // 기타
        int etcNum = 0;

        // 개수를 담을 list 생성
        List<Integer> countList = new ArrayList<>();
        List<MyIngredients> myIngredientsList = myIngredientsRepository.findAllByMemberId(member.getId());
        if (myIngredientsList.isEmpty()) {
            empty = true;
        }
        // 카테고리별 재료 분류
        for (MyIngredients myIngredients : myIngredientsList) {
            switch (myIngredients.getIngredient().getFoodCategory()){
                // 농산물
                case 전분류: case 견과류: case 곡류: case 과실류: case 두류: case 버섯류: case 채소류:
                    produceNum++;
                    break;
                // 축산물
                case 난류: case 육류:
                    livestockNum++;
                    break;
                // 수산물
                case 어패류: case 해조류:
                    marineNum++;
                    break;
                // 음료류
                case 음료류: case 주류: case 차류:
                    drinkNum++;
                    break;
                // 기타
                case 기타: case 당류: case 유제품류: case 조리가공품류: case 유지류: case 조미료류:
                    etcNum++;
                    break;
            }
        }
        countList.add(produceNum);
        countList.add(livestockNum);
        countList.add(marineNum);
        countList.add(drinkNum);
        countList.add(etcNum);


        return IngredientsRatioResponseDto.builder()
                .empty(empty)
                .count(countList)
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getAllMyIngredient(HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        List<MyIngredients> myIngredients = myIngredientsRepository.findAllByMemberIdOrderByExpDate(member.getId());
        List<TotalMyIngredientDto> dtoList = new ArrayList<>();

        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        // 유통기한지난거 빼서 보내주기
        for (MyIngredients myIngredient : myIngredients) {
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec= (outDay.getTime()-nowDay.getTime())/1000;  //밀리초로 나와서 1000을 나눠야지 초 차이로됨
            Long diffDays = diffSec / (24*60*60); // 일자수 차이
            String d_day;
            if(diffDays == 0){
                d_day ="D-Day";
                dtoList.add(TotalMyIngredientDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date(d_day)
                        .category(myIngredient.getStorage())
                        .build());
            }else if(diffDays > 0){
                d_day ="-"+diffDays.toString();
                dtoList.add(TotalMyIngredientDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .category(myIngredient.getStorage())
                        .build());
            }

        }


        ListTotalMyIngredientsDto responseDto = ListTotalMyIngredientsDto.builder()
                .ingredients_num(dtoList.size())
                .storage(dtoList)
                .build();

        return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
    }

    public ResponseDto<?> getMyCategoryIngredient(String category, HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        //Storage별 분류
        if( category.equals("freeze") ||category.equals("refrigerated")||category.equals("room_temp")) {

            String redisStorage = member.getEmail()+category;
            Optional<RedisIngredient> ingredientList = redisIngredientRepo.findById(redisStorage);

            // 캐시에서 확인, 만약 없을시 DB에서 검색후 캐시저장.
            if(ingredientList.isEmpty()){
                Storage storage1 = Storage.valueOf(category);
                List<MyIngredients> myIngredients = myIngredientsRepository.findByMemberIdAndStorageOrderByExpDate(member.getId(), storage1);
                long total_nums = 0;
                List<MyIngredientResponseDto> dtoList1 = new ArrayList<>();


                StorageResponseDto responseDto = getStorageResponseDto(myIngredients, dtoList1,total_nums);
                //레디스 캐시에 저장..
                RedisIngredient redisIngredient = RedisIngredient.builder()
                        .id(redisStorage)
                        .empty(false)
                        .total_nums(total_nums)/// 여기에 저장
                        .storageList(responseDto)
                        .build();
                //레디스 캐시에 저장..
                redisIngredientRepo.save(redisIngredient);

                return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
            }else {   // 캐시에 있을시 캐시를 출력.
                RedisIngredient redisIngredient = ingredientList.get();
                StorageResponseDto responseDto = redisIngredient.getStorageList();

                return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
            }

        }




        if(category.isEmpty()){

            //전체 갯수 조회
            List<MyIngredients> totalMyIngredients = myIngredientsRepository.findAllByMemberIdOrderByExpDate(member.getId());
            List<MyIngredientResponseDto> dtoList = new ArrayList<>();
            long total_nums = totalMyIngredients.size();

            if(totalMyIngredients.isEmpty()){
                StorageResponseDto responseDto = StorageResponseDto.builder()
                        .empty(true)
                        .total_nums(0)
                        .storage(dtoList)
                        .build();
                return ResponseDto.success(responseDto,"리스트제공에 성공하였습니다.");
            }

            StorageResponseDto responseDto = getStorageResponseDto(totalMyIngredients, dtoList,total_nums);

            return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");

        }



        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        //분류별 리스트
        List<MyIngredients> produceList = new ArrayList<>();
        List<MyIngredients> livestockList = new ArrayList<>();
        List<MyIngredients> marineList = new ArrayList<>();
        List<MyIngredients> drinkList = new ArrayList<>();
        List<MyIngredients> etcList = new ArrayList<>();




        List<MyIngredients> myIngredientsList = myIngredientsRepository.findAllByMemberId(member.getId());
        List<TotalMyIngredientDto> dtoList = new ArrayList<>();


    // 카테고리별 재료 분류
        for (MyIngredients myIngredients : myIngredientsList) {
            switch (myIngredients.getIngredient().getFoodCategory()){
                // 농산물
                case 전분류: case 견과류: case 곡류: case 과실류: case 두류: case 버섯류: case 채소류:
                    produceList.add(myIngredients);
                    break;
                // 축산물
                case 난류: case 육류:
                    livestockList.add(myIngredients);
                    break;
                // 수산물
                case 어패류: case 해조류:
                    marineList.add(myIngredients);
                    break;
                // 음료류
                case 음료류: case 주류: case 차류:
                    drinkList.add(myIngredients);
                    break;
                // 기타
                case 기타: case 당류: case 유제품류: case 조리가공품류: case 유지류: case 조미료류:
                    etcList.add(myIngredients);
                    break;
            }
        }


        switch (category){
            case "produce":
                getMyIngredientWithDday(produceList, dtoList, nowString);
                break;
            case "livestock":
                getMyIngredientWithDday(livestockList, dtoList, nowString);
                break;
            case "marine":
                getMyIngredientWithDday(marineList, dtoList, nowString);
                break;
            case "drink":
                getMyIngredientWithDday(drinkList, dtoList, nowString);
                break;
            case "etc":
                getMyIngredientWithDday(etcList, dtoList, nowString);
                break;
        }

        CategoryIngredientDto categoryIngredientDto = CategoryIngredientDto.builder()
                .empty(false)
                .storage(dtoList)
                .build();


        return ResponseDto.success(categoryIngredientDto,"리스트 제공에 성공하였습니다.");

    }

    private void getMyIngredientWithDday(List<MyIngredients> myIngredients, List<TotalMyIngredientDto> dtoList, String nowString) throws ParseException {
        for (MyIngredients myIngredient : myIngredients) {
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec= (outDay.getTime()-nowDay.getTime())/1000;  //밀리초로 나와서 1000을 나눠야지 초 차이로됨
            Long diffDays = diffSec / (24*60*60); // 일자수 차이
            String d_day;
            if(diffDays < 0){
                d_day ="기한 만료";
                dtoList.add(TotalMyIngredientDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date(d_day)
                        .category(myIngredient.getStorage())
                        .build());
            }else if(diffDays == 0){
                d_day ="D-Day";
                dtoList.add(TotalMyIngredientDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date(d_day)
                        .category(myIngredient.getStorage())
                        .build());
            }else{
                d_day ="-"+diffDays.toString();
                dtoList.add(TotalMyIngredientDto.builder()
                        .id(myIngredient.getId())
                        .icon_image(myIngredient.getIngredient().getIconImage())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .category(myIngredient.getStorage())
                        .build());
            }

        }
    }
}
