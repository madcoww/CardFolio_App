package com.inhatc.cardfolio_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;


public class RequestModifyPasswordActivity extends AppCompatActivity {
    private final String toolbarName = "비밀번호 재설정";
    private FirebaseAuth mFirebaseAuth;         // 인증
    private TextView edt_login_id, warning_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_modify_password);

        //툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        //툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        edt_login_id = findViewById(R.id.edt_login_id);
        
        // 초기화
        warning_email = findViewById(R.id.warning_email);

        findViewById(R.id.btn_sed).setOnClickListener(sendModifyPassword);
    }

    //포커스 해제
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener sendModifyPassword = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String emailAddr = edt_login_id.getText().toString();

            if(emailAddr.isEmpty()){
                // 이메일 빈 값
                warning_email.setText("이메일 주소를 입력해주세요.");
                warning_email.setTextColor(Color.RED);
            }
            else {
                mFirebaseAuth.fetchSignInMethodsForEmail(emailAddr)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();
                                    if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                                        // 비밀번호 재설정 이메일 보내기
                                        mFirebaseAuth.sendPasswordResetEmail(emailAddr)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // 비밀번호 재설정 이메일 전송 성공
                                                            Toast.makeText(getApplicationContext(), "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // 비밀번호 재설정 이메일 전송 실패
                                                            Toast.makeText(getApplicationContext(), "비밀번호 재설정 이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        warning_email.setText("");
                                                    }
                                                });
                                    } else {
                                        // 이메일이 등록된 사용자가 아님
                                        warning_email.setText("가입되지 않은 이메일 주소입니다.");
                                        warning_email.setTextColor(Color.RED);
                                    }
                                } else {
                                    // 이메일 유효성 확인 실패
                                    warning_email.setText("올바른 형식의 이메일 주소를 입력해주세요.");
                                    warning_email.setTextColor(Color.RED);
                                }
                            }
                        });
            }
        }
    };
}