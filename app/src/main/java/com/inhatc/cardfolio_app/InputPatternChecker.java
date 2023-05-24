package com.inhatc.cardfolio_app;

import java.util.regex.Pattern;

public class InputPatternChecker {
    private static InputPatternChecker inputPatternChecker = new InputPatternChecker();

    private InputPatternChecker() {}

    public static InputPatternChecker getInputPatternChecker() {
        return inputPatternChecker;
    }

    // 숫자 검사기
    public boolean isNumeric(String str) {
        return Pattern.matches("^[0-9]*$", str);
    }

    // 영어 검사기
    public boolean isAlpha(String str) {
        return Pattern.matches("^[a-zA-Z]*$", str);
    }

    // 한국어 혹은 영어 검사기
    public boolean isAlphaNumeric(String str) {
        return Pattern.matches("[a-zA-Z0-9]*$", str);
    }

    // 한국어 검사기
    public boolean isKorean(String str) {
        return Pattern.matches("[가-힣]*$", str);
    }

    // 대문자 검사기
    public boolean isUpper(String str) {
        return Pattern.matches("^[A-Z]*$", str);
    }

    // 소문자 검사기
    public boolean isDowner(String str) {
        return Pattern.matches("^[a-z]*$", str);
    }

    // 이메일 검사기
    public boolean isEmail(String str) {
        return Pattern.matches("^[a-z0-9A-Z._-]*@[a-z0-9A-Z]*.[a-zA-Z.]*$", str);
    }

    // URL 검사기 (Http, Https 제외)
    public boolean isURL(String str) {
        return Pattern.matches("^[^((http(s?))\\:\\/\\/)]([0-9a-zA-Z\\-]+\\.)+[a-zA-Z]{2,6}(\\:[0-9]+)?(\\/\\S*)?$",
                str);
    }



    // phone number checker
    // xxx-xxx-xxxx (형식만 비교)
    // - 은 없어야 함.
    public boolean isMob(String str) {
        return Pattern.matches("^\\d{2,3}\\d{3,4}\\d{4}$", str);
    }

    // 일반 전화 검사기
    public boolean isPhone(String str) {
        return Pattern.matches("^\\d{2,3}\\d{3,4}\\d{4}$", str);
    }

    // IP 검사기
    public boolean isIp(String str) {
        return Pattern.matches("([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})", str);
    }

    // 우편번호 검사기
    public boolean isPost(String str) {
        return Pattern.matches("^\\d{3}\\d{2}$", str);
    }

    // 주민등록번호 검사기
    public boolean isPersonalID(String str) {
        return Pattern.matches("^(?:[0-9]{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[1,2][0-9]|3[0,1]))-[1-4][0-9]{6}$", str);
    }
}
