package com.inhatc.cardfolio_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterDoneActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;                    // 인증
    private FirebaseUser firebaseUser;
    private TextView txtEmail;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_done);

        txtEmail = findViewById(R.id.txtEmail);
        btn_start = findViewById(R.id.btn_start);
        btn_start.setBackgroundColor(Color.parseColor("#AAAAAA"));

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        
        // 텍스트 변경
        String strEmail = firebaseUser.getEmail() + "로 보내드린\n인증 메일을 확인해주세요.";
        txtEmail.setText(strEmail);
        btn_start.setText("회원가입 취소");

        // Intent 이동
        btn_start.setOnClickListener(onclick_Event);
        findViewById(R.id.btn_confrim).setOnClickListener(onclick_confirm);
        findViewById(R.id.send_email).setOnClickListener(onclick_EmailVerified);
    }
    View.OnClickListener onclick_confirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firebaseUser.reload();
            if(mFirebaseAuth.getCurrentUser().isEmailVerified()){
                // 메일 재전송 텍스트 삭제
                LinearLayout bt_txt = findViewById(R.id.bt_txt);
                ViewGroup parentView = (ViewGroup) bt_txt.getParent();
                if (parentView != null) {
                    parentView.removeView(bt_txt); // LinearLayout을 부모 뷰에서 제거
                }

                // 시작하기 버튼 활성화
                btn_start.setBackgroundColor(Color.parseColor("#2962F6"));
                btn_start.setText("시작하기");
                
                Toast.makeText(RegisterDoneActivity.this, "이메일 인증 성공", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(RegisterDoneActivity.this, "이메일 인증이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener onclick_EmailVerified = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (firebaseUser != null) {
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // 이메일 확인 이메일이 성공적으로 전송되었을 때의 처리
                            Toast.makeText(RegisterDoneActivity.this, "이메일 인증 메일이 재전송되었습니다. 이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 이메일 확인 이메일 전송이 실패했을 때의 처리
                            Toast.makeText(RegisterDoneActivity.this, "이메일 인증 메일 재전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    };
    View.OnClickListener onclick_Event = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firebaseUser.reload();
            if(!mFirebaseAuth.getCurrentUser().isEmailVerified()){
                // 이메일 인증 안되어 있을경우 회원가입 취소(계정삭제)
                mFirebaseAuth.getCurrentUser().delete();
            }
            // 시작하기
            Intent loginIntent = new Intent(RegisterDoneActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();  // 현재 액티비티 종료
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseUser.reload();
        if(!mFirebaseAuth.getCurrentUser().isEmailVerified()){
            // 이메일 인증 안되어 있을경우 회원가입 취소(계정삭제)
            mFirebaseAuth.getCurrentUser().delete();
        }
    }
}
