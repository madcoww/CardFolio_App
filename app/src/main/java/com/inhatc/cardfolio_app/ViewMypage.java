package com.inhatc.cardfolio_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ViewMypage extends AppCompatActivity {
    private final String toolbarName = "마이페이지";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB
    private FirebaseUser firebaseUser;                // DB
    private TextView uname, utel, uemail, totMy, totOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        // 하단 네비게이션
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNav = new BottomNav(bottomNavigationView);
        bottomNav.setBottomNavigationListener(this);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");
        firebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인한 사용자 정보 읽기

        // 초기화
        uname = (TextView) findViewById(R.id.uname);
        utel = (TextView) findViewById(R.id.utel);
        uemail = (TextView) findViewById(R.id.uemail);
        totMy = (TextView) findViewById(R.id.totMy); // 등록된 나의 명함
        totOther = (TextView) findViewById(R.id.totOther); // 등록된 명함집

        findViewById(R.id.btn_modifiy_user).setOnClickListener(requestModify);
        findViewById(R.id.btn_modifiy_pw).setOnClickListener(requestModifyPw);
        findViewById(R.id.btn_logout).setOnClickListener(requestLogout);
        findViewById(R.id.btn_del_User).setOnClickListener(requestDeleteUser);

        // 정보 불러오기
        getMyInfo();

        // 등록된 나의 명함
        getTotMyCards();

        // 등록된 명함집
        getTotOtherCards();
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

    // 정보 불러오기
    public void getMyInfo(){
        Query query = mDatabaseRef.child("UserAccount").orderByChild("idToken").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = new User();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                {
                    user.setIdToken(userSnapshot.child("idToken").getValue(String.class));
                    user.setPhone_num(userSnapshot.child("phone_num").getValue(String.class));
                    user.setUser_email(userSnapshot.child("user_email").getValue(String.class));
                    user.setUser_name(userSnapshot.child("user_name").getValue(String.class));
                    user.setUser_pw(userSnapshot.child("user_pw").getValue(String.class));
                }
                uname.setText(user.getUser_name() + "님 기본정보");
                utel.setText("T." + user.getPhone_num());
                uemail.setText("E-mail." + user.getUser_email());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewMypage.this, "정보를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    };

    // 등록된 나의 명함
    public void getTotMyCards(){
        Query query = mDatabaseRef.child("CardInfo").orderByChild("u_id").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                {
                    count++;
                }
                totMy.setText(Integer.toString(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewMypage.this, "등록된 나의 명함 수를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    };

    // 등록된 명함집
    public void getTotOtherCards(){
        Query query = mDatabaseRef.child("OtherCards").orderByChild("u_id").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                {
                    count++;
                }
                totOther.setText(Integer.toString(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewMypage.this, "등록된 명함집 수를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    };

    // 기본정보 수정
    View.OnClickListener requestModify = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Query query = mDatabaseRef.child("UserAccount").orderByChild("idToken").equalTo(firebaseUser.getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = new User();
                    for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                    {
                        user.setIdToken(userSnapshot.child("idToken").getValue(String.class));
                        user.setPhone_num(userSnapshot.child("phone_num").getValue(String.class));
                        user.setUser_email(userSnapshot.child("user_email").getValue(String.class));
                        user.setUser_name(userSnapshot.child("user_name").getValue(String.class));
                        user.setUser_pw(userSnapshot.child("user_pw").getValue(String.class));
                    }

                    Intent registerModifyIntent = new Intent(ViewMypage.this, RegisterActivity.class);
                    registerModifyIntent.putExtra("intent_name", "registerModifyIntent");
                    registerModifyIntent.putExtra("UserAccount", user); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                    startActivity(registerModifyIntent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewMypage.this, "정보를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    View.OnClickListener requestModifyPw = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = firebaseUser.getEmail();
            mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // 비밀번호 재설정 이메일이 성공적으로 전송
                        Toast.makeText(ViewMypage.this, email+"로 비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 비밀번호 재설정 이메일 전송이 실패
                        Toast.makeText(ViewMypage.this, "비밀번호 재설정 이메일이 전송 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    // 로그아웃
    View.OnClickListener requestLogout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFirebaseAuth.signOut();
            Intent loginIntent = new Intent(ViewMypage.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    };
    
    // 회원탈퇴
    View.OnClickListener requestDeleteUser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFirebaseAuth.getCurrentUser().delete();
            Intent loginIntent = new Intent(ViewMypage.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    };
}