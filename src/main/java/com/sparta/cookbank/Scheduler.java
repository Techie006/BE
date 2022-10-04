package com.sparta.cookbank;


import com.sparta.cookbank.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component // 스프링이 필요 시 자동으로 생성하는 클래스 목록에 추가합니다.
public class Scheduler {

    private final ChatService chatService;



    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 0 * *")
    public void deleteRoom(){
        chatService.DailyRemoveClass();
        log.info("안쓰는 방이 사라졌습니다.");
    }



}
