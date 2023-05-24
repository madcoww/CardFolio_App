package com.inhatc.cardfolio_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private String toolbarName;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB
    private FirebaseUser firebaseUser;
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private ArrayList<Card> mArrayList;
    private ArrayList<Card> otherArrayList;
    private ArrayList<Card> searchArrayList;
    private CustomAdapter mAdapter, otherAdapter, searchAdapter;

    static TabHost myTabHost = null;
    static TabHost.TabSpec myTabSpec;
    private EditText searchData;
    private Query queryLoadOtherCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (BottomNav.mn_idx == 1) {
            toolbarName = "명함집";
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            toolbarName = "CardFolio";
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        getSupportActionBar().setTitle("");

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");
        firebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인한 사용자 정보 읽기

        // 하단 네비게이션
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNav = new BottomNav(bottomNavigationView);
        bottomNav.setBottomNavigationListener(this);

        findViewById(R.id.btn_register_card).setOnClickListener(requestRegisterCard);

        // 나의 명함 로드
        loadMyCard();

        // 다른명함 로드
        queryLoadOtherCards = mDatabaseRef.child("OtherCards").orderByChild("u_id").equalTo(firebaseUser.getUid());
        loadOtherCard(queryLoadOtherCards);

        // 검색창
        searchData = findViewById(R.id.edt_search);
        searchData.addTextChangedListener(searchDataWatcher);

        // Tab 동작
        myTabHost = (TabHost) findViewById(R.id.tabhost);
        myTabHost.setup();
        myTabSpec = myTabHost.newTabSpec("MyCard").setIndicator("MyCard").setContent(R.id.tab1); // 나의명함
        myTabHost.addTab(myTabSpec);
        myTabSpec = myTabHost.newTabSpec("OtherCard").setIndicator("OtherCard").setContent(R.id.tab2); // 명함집
        myTabHost.addTab(myTabSpec);
        myTabSpec = myTabHost.newTabSpec("SearchCard").setIndicator("SearchCard").setContent(R.id.tab3); // 검색결과
        myTabHost.addTab(myTabSpec);

        if(BottomNav.mn_idx == 1) {
            // 명함집일때
            MainActivity.myTabHost.setCurrentTab(1);
        }else{
            // HOME일때
            MainActivity.myTabHost.setCurrentTab(0);
        }
    }

    // 명함 검색
    private TextWatcher searchDataWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 타이핑 전에 호출되는 메소드
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            searchOtherCard(queryLoadOtherCards);
        }

        @Override
        public void afterTextChanged(final Editable s) {
            // 타이핑 후에 호출되는 메소드
            runnable = new Runnable() {
                @Override
                public void run() {
                }
            };

            // 일정 시간이 지난 후에 검사 요청을 실행 (타이핑이 멈추면 실행)
            handler.postDelayed(runnable, 500); // 500ms 후에 실행하도록 설정 (원하는 시간으로 변경 가능)
        }
    };

    // 나의 명함 로드
    public void loadMyCard(){
        mArrayList = new ArrayList<>();

        Query query = mDatabaseRef.child("CardInfo").orderByChild("u_id").equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    //String userId = userSnapshot.getKey();
                    Card card = new Card();
                    card.setC_id(userSnapshot.child("c_id").getValue(String.class));
                    card.setCard_logo(userSnapshot.child("card_logo").getValue(String.class));
                    card.setU_id(userSnapshot.child("u_id").getValue(String.class));
                    card.setCard_uname(userSnapshot.child("card_uname").getValue(String.class));
                    card.setCard_cname(userSnapshot.child("card_cname").getValue(String.class));
                    card.setCard_team(userSnapshot.child("card_team").getValue(String.class));
                    card.setCard_rank(userSnapshot.child("card_rank").getValue(String.class));
                    card.setCard_pnum(userSnapshot.child("card_pnum").getValue(String.class));
                    card.setCard_email(userSnapshot.child("card_email").getValue(String.class));
                    card.setCard_caddr(userSnapshot.child("card_caddr").getValue(String.class));
                    card.setIs_default(userSnapshot.child("is_default").getValue(Integer.class));
                    mArrayList.add(card);
                }

                // 오름차순 정렬로 대표명함이 항상 위에 오도록
                Collections.sort(mArrayList, new Comparator<Card>() {
                    @Override
                    public int compare(Card card1, Card card2) {
                        // is_default 값을 기준으로 정렬 (1이면 우선순위가 높음)
                        return Integer.compare(card2.getIs_default(), card1.getIs_default());
                    }
                });
                mAdapter.notifyDataSetChanged(); // 데이터 변경 알림
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        // mRecyclerView adaptor
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_main_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new CustomAdapter(mArrayList, this, "my");
        mRecyclerView.setAdapter(mAdapter); // mAdapter 정보 연결
    }

    // 명함집 로드
    public void loadOtherCard(Query query){
        otherArrayList = new ArrayList<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String cId = userSnapshot.child("c_id").getValue(String.class);

                    // CardInfo에서 일치하는 c_id의 데이터를 찾아 읽어온다.
                    Query query3 = mDatabaseRef.child("CardInfo").orderByChild("c_id").equalTo(cId);
                    query3.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                Card card = new Card();
                                card.setC_id(userSnapshot.child("c_id").getValue(String.class));
                                card.setCard_logo(userSnapshot.child("card_logo").getValue(String.class));
                                card.setU_id(userSnapshot.child("u_id").getValue(String.class));
                                card.setCard_uname(userSnapshot.child("card_uname").getValue(String.class));
                                card.setCard_cname(userSnapshot.child("card_cname").getValue(String.class));
                                card.setCard_team(userSnapshot.child("card_team").getValue(String.class));
                                card.setCard_rank(userSnapshot.child("card_rank").getValue(String.class));
                                card.setCard_pnum(userSnapshot.child("card_pnum").getValue(String.class));
                                card.setCard_email(userSnapshot.child("card_email").getValue(String.class));
                                card.setCard_caddr(userSnapshot.child("card_caddr").getValue(String.class));
                                card.setIs_default(0); // 다른 사람 명함일 경우 대표명함 인보여야 함
                                otherArrayList.add(card);
                            }
                            otherAdapter.notifyDataSetChanged(); // 데이터 변경 알림
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 쿼리가 취소되었거나 실패한 경우에 대한 처리
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        // recycle adaptor
        RecyclerView otherRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_other_list);
        LinearLayoutManager otherLinearLayoutManager = new LinearLayoutManager(this);
        otherRecyclerView.setLayoutManager(otherLinearLayoutManager);
        otherAdapter = new CustomAdapter(otherArrayList, this,"other");
        otherRecyclerView.setAdapter(otherAdapter); // mAdapter 정보 연결
    }
    
    // 검색한 명함 로드
    public void searchOtherCard(Query query){
        searchArrayList = new ArrayList<>();
        toolbarName = "검색결과";
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            String keyWord = null;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MainActivity.myTabHost.setCurrentTab(2);
                keyWord = searchData.getText().toString();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String cId = userSnapshot.child("c_id").getValue(String.class);

                    // CardInfo에서 일치하는 c_id의 데이터를 찾아 읽어온다.
                    Query query3 = mDatabaseRef.child("CardInfo").orderByChild("c_id").startAt(cId);
                    query3.addListenerForSingleValueEvent(new ValueEventListener() {
                        String card_uname = null;
                        String card_cname = null;
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                Card card = new Card();

                                card_uname = userSnapshot.child("card_uname").getValue(String.class);
                                card_cname = userSnapshot.child("card_cname").getValue(String.class);

                                card.setC_id(userSnapshot.child("c_id").getValue(String.class));
                                card.setCard_logo(userSnapshot.child("card_logo").getValue(String.class));
                                card.setU_id(userSnapshot.child("u_id").getValue(String.class));
                                card.setCard_uname(card_uname);
                                card.setCard_cname(card_cname);
                                card.setCard_team(userSnapshot.child("card_team").getValue(String.class));
                                card.setCard_rank(userSnapshot.child("card_rank").getValue(String.class));
                                card.setCard_pnum(userSnapshot.child("card_pnum").getValue(String.class));
                                card.setCard_email(userSnapshot.child("card_email").getValue(String.class));
                                card.setCard_caddr(userSnapshot.child("card_caddr").getValue(String.class));
                                card.setIs_default(0); // 다른 사람 명함일 경우 대표명함 인보여야 함

                                keyWord = keyWord.toLowerCase();
                                card_uname = card_uname.toLowerCase();
                                card_cname = card_cname.toLowerCase();
                                if(card_uname.contains(keyWord) || card_cname.contains(keyWord)) {
                                    boolean isDuplicate = false;
                                    for (Card c : searchArrayList) {
                                        String c_id = c.getC_id();
                                        if (c_id.equals(userSnapshot.child("c_id").getValue(String.class))) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    if (!isDuplicate) {
                                        searchArrayList.add(card);
                                    }
                                }
                            }
                            searchAdapter.notifyDataSetChanged(); // 데이터 변경 알림
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 쿼리가 취소되었거나 실패한 경우에 대한 처리
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        });

        // recycle adaptor
        RecyclerView schRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_other_list2);
        LinearLayoutManager schLinearLayoutManager = new LinearLayoutManager(this);
        schRecyclerView.setLayoutManager(schLinearLayoutManager);
        searchAdapter = new CustomAdapter(searchArrayList, this,"other");
        schRecyclerView.setAdapter(searchAdapter); // mAdapter 정보 연결
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

    // 신귬명함 등록
    View.OnClickListener requestRegisterCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent registerCard = new Intent(MainActivity.this, RegisterCardActivity.class);
            registerCard.putExtra("intent_name", "RegisterCard");
            startActivity(registerCard);
        }
    };
}