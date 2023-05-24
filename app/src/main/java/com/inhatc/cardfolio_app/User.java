package com.inhatc.cardfolio_app;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
* 최초 작성자 : 정다운
* 최초 작성일 : 2023-05-10
* 목적 : DataBase 회원 정보 관리
* 개정 이력 : 정다운, 2023-05-10
* */
public class User implements Serializable {
    private String idToken;     // 고유 토큰 정보
    private String user_email;  // 이메일(ID)   (UNIQUE KEY)
    private String user_pw;    // 비밀번호
    private String user_name;        // 이름
    private String phone_num;   // 휴대폰 번호

    public User(){}

    public User(String idToken, String user_email, String user_pw, String user_name, String phone_num) {
        this.idToken = idToken;
        this.user_email = user_email;
        this.user_pw = user_pw;
        this.user_name = user_name;
        this.phone_num = phone_num;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_pw() {
        return user_pw;
    }

    public void setUser_pw(String user_pw) {
        this.user_pw = user_pw;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idToken", idToken);
        result.put("user_email", user_email);
        result.put("user_pw", user_pw);
        result.put("user_name", user_name);
        result.put("phone_num", phone_num);

        return result;
    }
}
