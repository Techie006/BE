package com.sparta.cookbank;

import com.sparta.cookbank.domain.chat.ChatMessage;

import java.util.Comparator;

public class MiniComparator implements Comparator<ChatMessage> {
    @Override
    public int compare(ChatMessage first, ChatMessage second){
        String firstTime = first.getCreateAt();
        String secondTime = second.getCreateAt();

        return firstTime.compareTo(secondTime);
    }
}
