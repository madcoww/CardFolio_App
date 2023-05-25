package com.inhatc.cardfolio_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;         // 인증
    private DatabaseReference mDatabaseRef;     // DB
    private EditText uId, uPw;                  // 입력 필드
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");

        // 초기화
        uId = findViewById(R.id.edt_login_id);
        uPw = findViewById(R.id.edt_login_pw);

        // Intent 이동
        findViewById(R.id.et_to_register).setOnClickListener(onclick_Event);
        findViewById(R.id.btn_login).setOnClickListener(onclick_Event);

        // 비밀번호 재설정
        findViewById(R.id.et_send_pw).setOnClickListener(requestSendPwEmail);
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
    View.OnClickListener onclick_Event = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.et_to_register:
                    Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                    registerIntent.putExtra("intent_name", "registerIntent");
                    startActivity(registerIntent);
                    break;

                case R.id.btn_login:
                    requestLogin(); // 로그인 처리
                    break;

                default:
                    System.out.println("No Intent");
                    break;
            }
        }
    };
    // 비밀번호 재설정
    View.OnClickListener requestSendPwEmail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //로그인 성공
            Intent requestModifyPasswordIntent = new Intent(LoginActivity.this, RequestModifyPasswordActivity.class);
            startActivity(requestModifyPasswordIntent);
        }
    };

    public void requestLogin(){
        String strId = uId.getText().toString();
        String strPw = uPw.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(strId, strPw).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //로그인 성공
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(loginIntent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}