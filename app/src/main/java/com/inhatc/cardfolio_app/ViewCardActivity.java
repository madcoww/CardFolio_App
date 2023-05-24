package com.inhatc.cardfolio_app;
/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-19
 * 목적 : 카드 상세 보기
 * 개정 이력 : 김원준, 2023-05-22
 * MainActivity putExtra(card_id) - > ViewCardActivity getExtra(card_id), putExtra(card_id) -> share
 * */
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import org.w3c.dom.Text;

import java.util.ArrayList;


public class ViewCardActivity extends AppCompatActivity {
    private final String toolbarName = "명함 상세보기";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB

    FirebaseStorage storage = FirebaseStorage.getInstance(); //스토리지 (정보 삭제 시 필요)

    private String card_id; // getExtra() 가져올 현재 카드 id 값
    private ImageView c_logo;
    private TextView c_uname, c_cname, c_team_rank, c_pnum, c_email, c_addr;

    private String  card_uname, card_cname, card_team, card_rank, card_pnum, card_email, card_caddr;
    private LinearLayout ll_call, ll_share, ll_update, ll_delete;

    private String strImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);

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
        card_id = intent.getStringExtra("c_id");

        c_logo = (ImageView) findViewById(R.id.img_logo);
        c_uname = (TextView) findViewById(R.id.tv_view_uname);
        c_cname = (TextView) findViewById(R.id.tv_view_cname);
        c_team_rank = (TextView) findViewById(R.id.tv_view_team_rank);
        c_pnum = (TextView) findViewById(R.id.tv_view_pnum);
        c_email = (TextView) findViewById(R.id.tv_view_email);
        c_addr = (TextView) findViewById(R.id.tv_view_addr);

        //load_logo();
        load_data(card_id);

        ll_call = (LinearLayout) findViewById(R.id.btn_call);
        ll_share = (LinearLayout) findViewById(R.id.btn_share);
        ll_update = (LinearLayout) findViewById(R.id.btn_update);
        ll_delete = (LinearLayout) findViewById(R.id.btn_delete);

        ll_call.setOnClickListener(btn_event);
        ll_share.setOnClickListener(btn_event);
        ll_update.setOnClickListener(btn_event);
        ll_delete.setOnClickListener(btn_event);
    }
    View.OnClickListener btn_event = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                // 전화 OnClickListener
                case R.id.btn_call:
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+c_pnum.getText()));
                    dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialIntent);
                    break;
                case R.id.btn_share:
                    Intent shareIntent = new Intent(ViewCardActivity.this, ShareCardActivity.class);
                    shareIntent.putExtra("c_id", card_id); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                    startActivity(shareIntent);
                    break;
                case R.id.btn_update:


                    break;
                case R.id.btn_delete:
                    showDialog();
                    break;
            }
        }
    };
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    void load_data(String card_id){
        mDatabaseRef.child("CardInfo").child(card_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card card_data = snapshot.getValue(Card.class);

                strImgUri = card_data.getCard_logo();
                load_logo();

                card_uname = card_data.getCard_uname();
                card_cname = card_data.getCard_cname();
                card_team = card_data.getCard_team();
                card_rank = card_data.getCard_rank();
                card_pnum = card_data.getCard_pnum();
                card_email = card_data.getCard_email();
                card_caddr = card_data.getCard_caddr();

                c_uname.setText(card_uname);
                c_cname.setText(card_cname);
                c_team_rank.setText(card_team+" | "+card_rank);
                c_pnum.setText(card_pnum);
                c_email.setText(card_email);
                c_addr.setText(card_caddr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewCardActivity.this, "정보를 불러 오지 못했습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }
    void showDialog(){
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(ViewCardActivity.this)
                .setTitle("해당 명함을 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete_data(card_id);
                        Toast.makeText(ViewCardActivity.this, "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ViewCardActivity.this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }
    //firebase 데이터 삭제
    void delete_data(String c_id){
        mDatabaseRef.child("CardInfo").child(c_id).setValue(null);

        Intent intent = new Intent(ViewCardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //logo 이미지 불러오기
    void load_logo(){
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("logo").child(strImgUri);

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ViewCardActivity.this).load(uri).into(c_logo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ViewCardActivity.this, "이미지 불러 오기 실패했습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }
}