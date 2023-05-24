package com.inhatc.cardfolio_app;
/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-24
 * 목적 : 카드 등록
 * 개정 이력 : 김원준, 2023-05-24
 * */
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ModifyCardActivity extends AppCompatActivity {

    private final String toolbarName = "명함 수정";
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB

    FirebaseStorage storage = FirebaseStorage.getInstance(); //스토리지


    private String c_user; //현재 접속한 유저

    private String card_id;

    private EditText mLogo, mName, mCname, mDept, mPos, mPnum, mEmail, mAddr;

    private String modify_strImgUri, modify_uname, modify_cname, modify_team, modify_rank, modify_pnum, modify_email, modify_addr;

    private RadioButton r_btn1, r_btn2;
    private RadioGroup rdg1;

    private String basic_Uri;

    private int is_default; // 1 : 대표 명함

    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_card);

        //툴바 생성
//        Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar1);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("");

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");

        //현재 접속한 사용자 정보 가져오기
        c_user = mFirebaseAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        card_id = intent.getStringExtra("c_id");

        load_data(card_id);

        mLogo = (EditText) findViewById(R.id.edt_register_logo);
        mName = (EditText) findViewById(R.id.edt_register_name);
        mCname = (EditText) findViewById(R.id.edt_register_cname);
        mDept = (EditText) findViewById(R.id.edt_register_team);
        mPos = (EditText) findViewById(R.id.edt_register_rank);
        mPnum = (EditText) findViewById(R.id.edt_register_pnum);
        mEmail = (EditText) findViewById(R.id.edt_register_email);
        mAddr = (EditText) findViewById(R.id.edt_register_addr);

        r_btn1 = (RadioButton)findViewById(R.id.radioButton);
        r_btn2 = (RadioButton)findViewById(R.id.radioButton2);
        rdg1 = (RadioGroup)findViewById(R.id.rdg1);

        findViewById(R.id.btn_register_logo_upload).setOnClickListener(logo_Upload);
        findViewById(R.id.btn_register).setOnClickListener(card_Modify);

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
    View.OnClickListener card_Modify = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //스토리지 업로드
            uploadToStorage();
            //RDB 업로드
            uploadToRDB();
            //MainActivity 이동
            Intent intent = new Intent(ModifyCardActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        }
    };

    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    {
                        if(result.getResultCode() == RESULT_OK && result.getData() != null){
                            imageUri = result.getData().getData();

                            String strData = imageUri.toString();

                            mLogo.setText(strData);
                        }
                    }
                }
            }
    );
    //firebase storage logo image 업로드 168-172 24일 수정
    private void uploadToStorage(){
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child("logo").child(imageUri.toString());

        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ModifyCardActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ModifyCardActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 카드 정보 수정
    void load_data(String card_id){
        mDatabaseRef.child("CardInfo").child(card_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Card modify_data = snapshot.getValue(Card.class);

                modify_strImgUri = modify_data.getCard_logo();

                modify_uname = modify_data.getCard_uname();
                modify_cname = modify_data.getCard_cname();
                modify_team = modify_data.getCard_team();
                modify_rank = modify_data.getCard_rank();
                modify_pnum = modify_data.getCard_pnum();
                modify_email = modify_data.getCard_email();
                modify_addr = modify_data.getCard_caddr();

                mLogo.setText(modify_strImgUri);
                mName.setText(modify_uname);
                mCname.setText(modify_cname);
                mDept.setText(modify_team);
                mPos.setText(modify_rank);
                mPnum.setText(modify_pnum);
                mEmail.setText(modify_email);
                mAddr.setText(modify_addr);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ModifyCardActivity.this, "정보를 불러 오지 못했습니다..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //수정 정보 업로드
    void uploadToRDB(){

        String i_Logo = mLogo.getText().toString();
        String i_Name = mName.getText().toString();
        String i_Cname = mCname.getText().toString();
        String i_Team = mDept.getText().toString();
        String i_Rank = mPos.getText().toString();
        String i_Pnum = mPnum.getText().toString();
        String i_Email = mEmail.getText().toString();
        String i_Addr = mAddr.getText().toString();
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

        mDatabaseRef.child("CardInfo").child(card_id).setValue(cData); //수정으로 교체
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}