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
import android.view.MenuItem;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class ShareCardActivity extends AppCompatActivity {
    private final String toolbarName = "명함 공유하기";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증

    private DatabaseReference mDatabaseRef;                // DB

    FirebaseStorage storage = FirebaseStorage.getInstance();

    private String s_card_id;

    private ImageView s_qr;
    private ImageView s_logo;
    private TextView  s_uname, s_cname, s_team_rank, s_pnum, s_email;

    private String share_strImgUri;
    private String  share_uname, share_cname, share_team, share_rank, share_pnum, share_email;
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

        // 하단 네비게이션
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNav = new BottomNav(bottomNavigationView);
        bottomNav.setBottomNavigationListener(this);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");

        Intent intent = getIntent();
        s_card_id = intent.getStringExtra("c_id");
        //Toast.makeText(this, s_card_id, Toast.LENGTH_SHORT).show();



        s_logo = (ImageView) findViewById(R.id.img_share_logo);
        s_uname = (TextView) findViewById(R.id.tv_share_uname);
        s_cname = (TextView) findViewById(R.id.tv_share_cname);
        s_team_rank = (TextView) findViewById(R.id.tv_share_team_rank);
        s_pnum = (TextView) findViewById(R.id.tv_share_pnum);
        s_email = (TextView) findViewById(R.id.tv_share_email);
        s_qr = (ImageView) findViewById(R.id.s_qr) ;

        load_data(s_card_id);
        makeQR(s_card_id);
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
}