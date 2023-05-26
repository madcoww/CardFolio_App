package com.inhatc.cardfolio_app;

import java.util.Random;

public class RandomCodeMaker {
    private static RandomCodeMaker randomCodeMaker = new RandomCodeMaker();
    private static String code;
    public RandomCodeMaker() {
        this.code = generateRandomCode();
    }
    public static RandomCodeMaker getRandomCodeMaker() {
        return randomCodeMaker;
    }
    public static String getCode() {
        return code;
    }

    //랜덤 코드 생성
    private static String generateRandomCode() {
        int codeLength = 6; // 회원 코드 길이
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }
}
