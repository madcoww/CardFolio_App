package com.inhatc.cardfolio_app;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;


/*
 * 최초 작성자 : 정다운
 * 최초 작성일 : 2023-05-17
 * 목적 : 하단 네비게이션 메뉴 Action 관리
 * 개정 이력 : 정다운, 2023-05-17
 * */
public class BottomNav {
    private static BottomNavigationView bottomNav;
    public static int mn_idx; // 멤버 변수로 선언

    public BottomNav(BottomNavigationView bottomNavigationView){
        this.bottomNav = bottomNavigationView;
    }

    public void setBottomNavigationListener(final Activity currentActivity) {
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        currentActivity.finish();
                    case R.id.nav_cards:
                        if(item.getItemId() == R.id.nav_home) {
                            mn_idx = 0;
                        }
                        else if(item.getItemId() == R.id.nav_cards){
                            mn_idx = 1;
                        }
                        Intent intent = new Intent(currentActivity, MainActivity.class);
                        currentActivity.startActivity(intent);
                        return true;

                    case R.id.nav_scan:
                        // 카메라 스캔
                        Intent intent3 = new Intent(currentActivity, QrScannerActivity.class);
                        currentActivity.startActivity(intent3);
                        return true;

                    case R.id.nav_mypage:
                        // 마이페이지
                        Intent intent4 = new Intent(currentActivity, ViewMypage.class);
                        currentActivity.startActivity(intent4);
                        return true;
                }
                return false;
            }
        });
    }
}
