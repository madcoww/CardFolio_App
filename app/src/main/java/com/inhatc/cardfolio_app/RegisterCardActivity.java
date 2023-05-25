package com.inhatc.cardfolio_app;

/*
 * 최초 작성자 : 김원준
 * 최초 작성일 : 2023-05-18
 * 목적 : 카드 등록
 * 개정 이력 : 김원준, 2023-05-24
 * 개정 이력 : 정다운, 2023-05-25
 * */

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;


public class RegisterCardActivity extends AppCompatActivity {

    private static String toolbarName;
    private BottomNavigationView bottomNavigationView;
    BottomNav bottomNav;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB
    private FirebaseUser firebaseUser;                // DB

    FirebaseStorage storage = FirebaseStorage.getInstance(); //스토리지


    private String c_user; //현재 접속한 유저

    private String card_id;

    private EditText cLogo, cName, cCname, cDept, cPos, cPnum, cEmail, cCaddr;
    private TextView warning_logo, warning_name, warning_cname, warning_cinfo, warning_pnum, warning_email, warning_addr;
    private InputPatternChecker inputPatternChecker;
    private Button btn_register;

    private RadioButton r_btn1, r_btn2;
    private RadioGroup rdg1;

    private String basic_Uri;
    private User loginUser = null;
    private int is_default; // 1 : 대표 명함
    private Uri imageUri;
    private boolean isVaileTel = false; // 전화번호 유효성
    private boolean isNewRegister = true; // 신규 여부
    private boolean isChangeUri = false; //로고 등록 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_card);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");
        firebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인한 사용자 정보 읽기

        //현재 접속한 사용자 정보 가져오기
        c_user = mFirebaseAuth.getCurrentUser().getUid();

        cLogo = (EditText) findViewById(R.id.edt_register_logo);
        cName = (EditText) findViewById(R.id.edt_register_name);
        cCname = (EditText) findViewById(R.id.edt_register_cname);
        cDept = (EditText) findViewById(R.id.edt_register_team);
        cPos = (EditText) findViewById(R.id.edt_register_rank);
        cPnum = (EditText) findViewById(R.id.edt_register_pnum);
        cEmail = (EditText) findViewById(R.id.edt_register_email);
        cCaddr = (EditText) findViewById(R.id.edt_register_addr);

        warning_logo =  (TextView) findViewById(R.id.warning_logo);
        warning_name =  (TextView) findViewById(R.id.warning_name);
        warning_cname =  (TextView) findViewById(R.id.warning_cname);
        warning_cinfo =  (TextView) findViewById(R.id.warning_cinfo);
        warning_pnum =  (TextView) findViewById(R.id.warning_pnum);
        warning_email =  (TextView) findViewById(R.id.warning_email);
        warning_addr =  (TextView) findViewById(R.id.warning_addr);
        inputPatternChecker = InputPatternChecker.getInputPatternChecker();

        r_btn1 = (RadioButton)findViewById(R.id.radioButton);
        r_btn2 = (RadioButton)findViewById(R.id.radioButton2);
        rdg1 = (RadioGroup)findViewById(R.id.rdg1);
        btn_register = (Button) findViewById(R.id.btn_register);

        findViewById(R.id.btn_register_logo_upload).setOnClickListener(logo_Upload);
        btn_register.setOnClickListener(card_Register);


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

        toolbarName = "신규명함 등록";
        // 정보수정 form으로 변경
        cLogo.setFocusable(false);
        cLogo.setClickable(false);
        cLogo.setTextColor(Color.parseColor("#FFAAAAAA"));
        cLogo.setBackgroundResource(R.drawable.edittext_background_unactive);

        // 정보 수정시, 기본정보 load
        Intent thisIntent = getIntent();
        String callingIntent = thisIntent.getStringExtra("intent_name");
        if (callingIntent.equals("ModifyCard")) {
            toolbarName = "명함 수정";
            isNewRegister = false;
            btn_register.setText("수정 완료");

            // 수정할 명함 정보 Load
            Intent intent = getIntent();
            Card card = (Card) intent.getSerializableExtra("CardInfo");

            // 명함 업로드된 파일 정보 load
            String card_file = card.getCard_logo().toString();
            makeLogoName(card_file);

            card_id = card.getC_id();
            cName.setText(card.getCard_uname());
            cCname.setText(card.getCard_cname());
            cDept.setText(card.getCard_team());
            cPos.setText(card.getCard_rank());
            cPnum.setText(card.getCard_pnum());
            cEmail.setText(card.getCard_email());
            cCaddr.setText(card.getCard_caddr());

            // 대표명함 여부 지정
            is_default = card.getIs_default();
            if(is_default == 0){
                r_btn2.setChecked(true);
                r_btn1.setChecked(false);
            }else if(is_default == 1){
                r_btn1.setChecked(true);
                r_btn2.setChecked(false);
            }

            // 기존 파일 Uri
            imageUri = Uri.parse(card.getCard_logo());
        }else{
            //기본 정보 불러오기
            getMyInfo();
        }

        // 신규등록, 정보 수정 구분
        String btnText = btn_register.getText().toString();
        if(btnText.equals("신규명함등록")){
            isNewRegister = true; // 신규 가입
        }else if(btnText.equals("수정 완료")){
            isNewRegister = false; // 정보 수정
        }

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);
        
        // 검사
        cName.addTextChangedListener(cNameWatcher); // 이름
        cCname.addTextChangedListener(cCnameWatcher); // 회사 이름
        cDept.addTextChangedListener(cDeptWatcher); // 소속팀
        cPos.addTextChangedListener(cPosWatcher); // 직급
        cPnum.addTextChangedListener(cPnumWatcher); // 휴대폰
        cEmail.addTextChangedListener(cEmailWatcher); // 이메일 주소
        cCaddr.addTextChangedListener(cCaddrWatcher); // 주소
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
                cName.setText(user.getUser_name());
                cPnum.setText(user.getPhone_num());
                cEmail.setText(user.getUser_email());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterCardActivity.this, "정보를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    };
    public boolean isChekLogo(){
        String strData = cLogo.getText().toString();
        if(strData.isEmpty()){
            warning_logo.setText("로고를 등록해주세요.");
            warning_logo.setTextColor(Color.RED);
            return false;
        }else{
            warning_logo.setText("");
            return true;
        }
    }

    // 로고 이름
    public void makeLogoName(String name){
        String[] parts = name.split("/");
        String imageName = parts[parts.length - 1];
        cLogo.setText(imageName);
    }

    // 이름 검사
    private TextWatcher cNameWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekName();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekName(){
        String strData = cName.getText().toString();
        if(strData.isEmpty()){
            warning_name.setText("이름을 입력해주세요.");
            warning_name.setTextColor(Color.RED);
            return false;
        }else{
            warning_name.setText("");
            return true;
        }
    }


    // 회사 이름 검사
    private TextWatcher cCnameWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            String strData = cCname.getText().toString();
            boolean isCheck;
            isCheck = isChekCname();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekCname(){
        String strData = cCname.getText().toString();
        if(strData.isEmpty()){
            warning_cname.setText("회사 이름을 입력해주세요.");
            warning_cname.setTextColor(Color.RED);
            return false;
        }else{
            warning_cname.setText("");
            return true;
        }
    }


    // 소속팀 검사
    private TextWatcher cDeptWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            String strData = cCname.getText().toString();
            boolean isCheck;
            isCheck = isChekCdept();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekCdept(){
        String strData = cDept.getText().toString();
        if(strData.isEmpty()){
            warning_cinfo.setText("소속팀/직급을 입력해주세요.");
            warning_cinfo.setTextColor(Color.RED);
            return false;
        }else{
            warning_cinfo.setText("");
            return true;
        }
    }

    // 직급 검사
    private TextWatcher cPosWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekCpos();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekCpos(){
        String strData = cPos.getText().toString();
        if(strData.isEmpty()){
            warning_cinfo.setText("소속팀/직급을 입력해주세요.");
            warning_cinfo.setTextColor(Color.RED);
            return false;
        }else{
            warning_cinfo.setText("");
            return true;
        }
    }

    // 휴대폰 검사
    private TextWatcher cPnumWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekPnums();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekPnums(){
        String strData = cPnum.getText().toString();
        if(strData.isEmpty()){
            warning_pnum.setText("휴대폰 번호를 입력해주세요.");
            warning_pnum.setTextColor(Color.RED);
            return false;
        }else{
            warning_pnum.setText("");
            return true;
        }
    }

    // 이메일 검사
    private TextWatcher cEmailWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 타이핑 도중에 호출되는 메소드
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekEmail();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekEmail(){
        String strData = cEmail.getText().toString();
        if(strData.isEmpty()){
            warning_email.setText("이메일 주소를 입력해주세요.");
            warning_email.setTextColor(Color.RED);
            return false;
        }else{
            warning_email.setText("");
            return true;
        }
    }

    // 주소 검사
    private TextWatcher cCaddrWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekCaddr();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekCaddr(){
        String strData = cCaddr.getText().toString();
        if(strData.isEmpty()){
            warning_addr.setText("주소를 입력해주세요.");
            warning_addr.setTextColor(Color.RED);
            return false;
        }else{
            warning_addr.setText("");
            return true;
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
            if (isChekLogo() && isChekName() && isChekCname() && isChekCdept() && isChekCpos() && isChekPnums() && isChekEmail() && isChekCaddr()) {
                if(isChangeUri) {
                    //스토리지 업로드
                    uploadToStorage();
                }
                //RDB 업로드
                uploadToRDB();
                //MainActivity 이동
                Intent intent = new Intent(RegisterCardActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
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
                            makeLogoName(strData);
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
                isChangeUri = true;
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
        if(isNewRegister) {
            card_id = generateRandomCode();
        }
        String i_Name = cName.getText().toString();
        String i_Cname = cCname.getText().toString();
        String i_Team = cDept.getText().toString();
        String i_Rank = cPos.getText().toString();
        String i_Pnum = cPnum.getText().toString();
        String i_Email = cEmail.getText().toString();
        String i_Addr = cCaddr.getText().toString();
        
        // 대표명함 여부 지정
        if(r_btn1.isChecked()){
            is_default = 1;
        }else{
            is_default = 0;
        }

        Card cData = new Card();
        cData.setC_id(card_id);
        cData.setU_id(c_user);
        cData.setCard_logo(imageUri.toString());
        cData.setCard_uname(i_Name);
        cData.setCard_cname(i_Cname);
        cData.setCard_team(i_Team);
        cData.setCard_rank(i_Rank);
        cData.setCard_email(i_Email);
        cData.setCard_pnum(i_Pnum);
        cData.setCard_caddr(i_Addr);
        cData.setIs_default(is_default);

        // 대표명함 설정
        if(is_default == 1) {
            is_default(card_id);
        }

        // DB업데이트
        mDatabaseRef.child("CardInfo").child(card_id).setValue(cData);
    }

    private ArrayList<Card> otherArrayList;
    public void is_default(String c_id){
        //u_id가 일치하는 모든 데이터를 읽어와서, c_id를 읽어와서
        otherArrayList = new ArrayList<>();
        // 로그인한 사용자 명함집 읽기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        String u_id = firebaseUser.getUid();
        Query query = mDatabaseRef.child("OtherCards").orderByChild("u_id").equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String cId = userSnapshot.child("c_id").getValue(String.class);

                        // CardInfo에서 일치하는 c_id의 데이터를 찾아 읽어온다.
                        Query query3 = mDatabaseRef.child("CardInfo").orderByChild("u_id").equalTo(u_id);
                        query3.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    if(userSnapshot.child("c_id").getValue(String.class).equals(c_id)) {
                                        mDatabaseRef.child("CardInfo").child(userSnapshot.child("c_id").getValue(String.class)).child("is_default").setValue(1);
                                        Log.d("명항설정O", "1" + u_id + "+" + userSnapshot.child("c_id").getValue(String.class));
                                    }else{
                                        mDatabaseRef.child("CardInfo").child(userSnapshot.child("c_id").getValue(String.class)).child("is_default").setValue(0);
                                        Log.d("명항설정X", "0"  + u_id + "+" + userSnapshot.child("c_id").getValue(String.class));
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 쿼리가 취소되었거나 실패한 경우에 대한 처리
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //is_default를 0으로 바꿈, 읽어온 c_id와 파라미터 c_id가 일치할 경우 1로 바꿈
    }
    //OtherCards 등록 다른 사람 명함을 등록(스캔 후) 동작이 좋을 것으로 사료
//    void uploadToRDB_rel(String user, String id){
//        CardRel rData = new CardRel();
//        rData.setU_id(user);
//        rData.setC_id(id);
//        mDatabaseRef.child("OtherCards").child(generateRandomCode()).setValue(rData);
//    }
}