package com.inhatc.cardfolio_app;
/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-22
 * 목적 : 카드 상세 보기
 * 개정 이력 : 김원준, 2023-05-22
 * */
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;


public class ShareCardActivity extends AppCompatActivity {
    private final String toolbarName = "명함 공유하기";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;
    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB
    private FirebaseUser firebaseUser;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private String s_card_id;

    private ImageView s_qr;
    private ImageView s_logo;
    private TextView  s_uname, s_cname, s_team_rank, s_pnum, s_email;
    private Button btn_store;
    private String share_strImgUri;
    private String  share_uname, share_cname, share_team, share_rank, share_pnum, share_email;
    private LinearLayout areaQr;
    private boolean isForStore = false; // 저장 액티비티인지 아닌지 구분자
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_card);

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
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");
        firebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인한 사용자 정보 읽기

        s_logo = (ImageView) findViewById(R.id.img_share_logo);
        s_uname = (TextView) findViewById(R.id.tv_share_uname);
        s_cname = (TextView) findViewById(R.id.tv_share_cname);
        s_team_rank = (TextView) findViewById(R.id.tv_share_team_rank);
        s_pnum = (TextView) findViewById(R.id.tv_share_pnum);
        s_email = (TextView) findViewById(R.id.tv_share_email);
        s_qr = (ImageView) findViewById(R.id.s_qr) ;

        areaQr = (LinearLayout) findViewById(R.id.areaQr);
        btn_store = (Button) findViewById(R.id.btn_store);

        Intent thisIntent = getIntent();
        String callingIntent = thisIntent.getStringExtra("intent_name");
        s_card_id = thisIntent.getStringExtra("c_id");
        load_data(s_card_id); // data load
        isForStore = callingIntent.equals("qrScanStoreIntent") ? true : false;
        if (isForStore) {
            // 스캔한 명함 저장
            Log.d("s_card_id 정보 스캔", "스캔시작");
            Log.d("s_card_id 정보", s_card_id);
            ViewGroup parentView = (ViewGroup) areaQr.getParent();
            parentView.removeView(areaQr); // Qr 영역 삭제
            btn_store.setOnClickListener(onClickListenerForStore);
        }else{
            // 명함공유
            ViewGroup parentView = (ViewGroup) btn_store.getParent();
            parentView.removeView(btn_store); // 저장 버튼 삭제
            makeQR(s_card_id); // QR코드 생성
        }
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
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            mFirebaseAuth.signInAnonymously();
        }

    }
    void load_data(String card_id){
        mDatabaseRef.child("CardInfo").child(card_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card share_data = snapshot.getValue(Card.class);

                share_strImgUri = share_data.getCard_logo().toString();
                load_logo();

                share_uname = share_data.getCard_uname();
                share_cname = share_data.getCard_cname();
                share_team = share_data.getCard_team();
                share_rank = share_data.getCard_rank();
                share_pnum = share_data.getCard_pnum();
                share_email = share_data.getCard_email();

                s_uname.setText(share_uname);
                s_cname.setText(share_cname);
                s_team_rank.setText(share_team+" | "+share_rank);
                s_pnum.setText(share_pnum);
                s_email.setText(share_email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShareCardActivity.this, "정보를 불러 오지 못했습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //logo 이미지 불러오기
    void load_logo(){
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("logo").child(share_strImgUri);

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ShareCardActivity.this).load(uri).into(s_logo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShareCardActivity.this, "이미지 불러 오기 실패했습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void makeQR(String card_id){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        String text = card_id;
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,150,150);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            s_qr.setImageBitmap(bitmap);
        }catch (Exception e){}
    }

    View.OnClickListener onClickListenerForStore = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String uid = firebaseUser.getUid();
            uploadToRDB_rel(uid, s_card_id);

            // 액티비티 종료
            Intent intent = new Intent(ShareCardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    //OtherCards 등록 다른 사람 명함을 등록(스캔 후) 동작이 좋을 것으로 사료
    void uploadToRDB_rel(String user, String idForStore){
        OtherCards rData = new OtherCards();
        rData.setU_id(user);
        rData.setC_id(idForStore);

        RandomCodeMaker randomCodeMaker = RandomCodeMaker.getRandomCodeMaker();
        String code = randomCodeMaker.getCode();

        // 중복 여부 체크하여 저장
        if(!isDuplicateCard(user, idForStore)) {
            Log.d("명함 저장 여부 : ", "완료");
            // 명함 저장
            mDatabaseRef.child("OtherCards").child(code).setValue(rData);
            Toast.makeText(ShareCardActivity.this, "명함 공유가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        }else {
            // 이미 존재
            Log.d("명함 저장 여부 : ", "이미 존재");
            Toast.makeText(ShareCardActivity.this, "이미 저장된 명함입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<OtherCards> otherArrayList;
    public boolean isDuplicateCard(String user, String idForStore){
        otherArrayList = new ArrayList<>();
        // 로그인한 사용자 명함집
        Query query = mDatabaseRef.child("OtherCards").orderByChild("u_id").equalTo(user);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        OtherCards otherCards = new OtherCards();
                        otherCards = userSnapshot.getValue(OtherCards.class);
                        otherArrayList.add(otherCards);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 쿼리가 취소되었거나 실패한 경우에 대한 처리
            }
        });

        String stdCidData = null;
        for(OtherCards c : otherArrayList){
            stdCidData = c.getU_id();
            if(stdCidData.equals(idForStore)){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.myTabHost.setCurrentTab(1);
    }
}