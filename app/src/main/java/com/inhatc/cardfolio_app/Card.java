package com.inhatc.cardfolio_app;

/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-18
 * 목적 : DataBase 카드 정보 관리
 * 개정 이력 : 김원준, 2023-05-19
 * */

import android.net.Uri;

public class Card {

    private String c_id;

    private Uri imageUri;

    private String card_logo;
    private String u_id;
    private String card_uname;
    private String card_cname;
    private String card_team;
    private String card_rank;
    private String card_pnum;
    private String card_email;
    private String card_caddr;
    private int is_default;

    public Card() {
    }

    public Card(String c_id, Uri imageUri, String card_logo, String u_id, String card_uname, String card_cname, String card_team, String card_rank, String card_pnum, String card_email, String card_caddr, int is_default) {
        this.c_id = c_id;
        this.imageUri = imageUri;
        this.card_logo = card_logo;
        this.u_id = u_id;
        this.card_uname = card_uname;
        this.card_cname = card_cname;
        this.card_team = card_team;
        this.card_rank = card_rank;
        this.card_pnum = card_pnum;
        this.card_email = card_email;
        this.card_caddr = card_caddr;
        this.is_default = is_default;
    }

    public String getCard_logo() {
        return card_logo;
    }

    public void setCard_logo(String card_logo) {
        this.card_logo = card_logo;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    public String getCard_uname() {
        return card_uname;
    }

    public void setCard_uname(String card_uname) {
        this.card_uname = card_uname;
    }

    public String getCard_cname() {
        return card_cname;
    }

    public void setCard_cname(String card_cname) {
        this.card_cname = card_cname;
    }

    public String getCard_team() {
        return card_team;
    }

    public void setCard_team(String card_team) {
        this.card_team = card_team;
    }

    public String getCard_rank() {
        return card_rank;
    }

    public void setCard_rank(String card_rank) {
        this.card_rank = card_rank;
    }

    public String getCard_pnum() {
        return card_pnum;
    }

    public void setCard_pnum(String card_pnum) {
        this.card_pnum = card_pnum;
    }

    public String getCard_email() {
        return card_email;
    }

    public void setCard_email(String card_email) {
        this.card_email = card_email;
    }

    public String getCard_caddr() {
        return card_caddr;
    }

    public void setCard_caddr(String card_caddr) {
        this.card_caddr = card_caddr;
    }

    public int getIs_default() {
        return is_default;
    }

    public void setIs_default(int is_default) {
        this.is_default = is_default;
    }
}
