package com.inhatc.cardfolio_app;

/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-18
 * 목적 : 카드 등록
 * 개정 이력 : 김원준, 2023-05-22
 * */

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.Random;


public class RegisterCardActivity extends AppCompatActivity {
    private final String toolbarName = "신규 명함등록";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB

    FirebaseStorage storage = FirebaseStorage.getInstance(); //스토리지


    private String c_user; //현재 접속한 유저

    private EditText cLogo, cName, cCname, cDept, cPos, cPnum, cEmail, cCaddr;

    private RadioButton r_btn1, r_btn2;
    private RadioGroup rdg1;

    private int is_default; // 1 : 대표 명함

    private Uri imageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_card);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");

        //현재 접속한 사용자 정보 가져오기
        c_user = mFirebaseAuth.getCurrentUser().getUid();

        cLogo = (EditText) findViewById(R.id.edt_register_logo);
        cName = (EditText) findViewById(R.id.edt_register_name);
        cCname = (EditText) findViewById(R.id.edt_register_company);
        cDept = (EditText) findViewById(R.id.edt_register_team);
        cPos = (EditText) findViewById(R.id.edt_register_rank);
        cPnum = (EditText) findViewById(R.id.edt_register_tel);
        cEmail = (EditText) findViewById(R.id.edt_register_email);
        cCaddr = (EditText) findViewById(R.id.edt_register_addr);

        r_btn1 = (RadioButton)findViewById(R.id.radioButton);
        r_btn2 = (RadioButton)findViewById(R.id.radioButton2);
        rdg1 = (RadioGroup)findViewById(R.id.rdg1);

        findViewById(R.id.btn_register_logo_upload).setOnClickListener(logo_Upload);
        findViewById(R.id.btn_register).setOnClickListener(card_Register);


        //라디오 버튼을 통해 대표 명함 설정 여부 받기
        rdg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radioButton:
                        is_default = 1; //대표 명함 설정
                        break;
                    case R.id.radioButton2:
                        is_default = 0;
                        break;
                }
            }
        });
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
    //이미지 업로드 버튼
    View.OnClickListener logo_Upload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            activityResult.launch(galleryIntent);
        }
    };

    //명함 등록
    View.OnClickListener card_Register = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //스토리지 업로드
            uploadToStorage();
            //RDB 업로드
            uploadToRDB();
            //MainActivity 이동
            Intent intent = new Intent(RegisterCardActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        }
    };

    //랜덤 코드 생성
    private static String generateRandomCode() {
        int codeLength = 6; // 회원 코드 길이
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();

        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            codeBuilder.append(randomChar);
        }

        return codeBuilder.toString();
    }

    //logo image URI
    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    {
                        if(result.getResultCode() == RESULT_OK && result.getData() != null){
                            imageUri = result.getData().getData();

                            String strData = imageUri.toString();

                            cLogo.setText(strData);
                        }
                    }
                }
            }
    );
    //firebase storage logo image 업로드
    private void uploadToStorage(){

        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("logo").child(imageUri.toString());

        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(RegisterCardActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterCardActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //firebase RDB upload
    void uploadToRDB(){
        String card_id = generateRandomCode();
        String i_Logo = cLogo.getText().toString();
        String i_Name = cName.getText().toString();
        String i_Cname = cCname.getText().toString();
        String i_Team = cDept.getText().toString();
        String i_Rank = cPos.getText().toString();
        String i_Pnum = cPnum.getText().toString();
        String i_Email = cEmail.getText().toString();
        String i_Addr = cCaddr.getText().toString();
        int i_is_default = is_default;

        Card cData = new Card();
        cData.setC_id(card_id);
        cData.setU_id(c_user);
        cData.setCard_logo(i_Logo);
        cData.setCard_uname(i_Name);
        cData.setCard_cname(i_Cname);
        cData.setCard_team(i_Team);
        cData.setCard_rank(i_Rank);
        cData.setCard_email(i_Email);
        cData.setCard_pnum(i_Pnum);
        cData.setCard_caddr(i_Addr);
        cData.setIs_default(i_is_default);

        mDatabaseRef.child("CardInfo").child(card_id).setValue(cData);
    }
}