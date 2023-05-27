package com.inhatc.cardfolio_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class RegisterActivity extends AppCompatActivity {
    private String toolbarName;

    private FirebaseAuth mFirebaseAuth;                    // 인증
    private DatabaseReference mDatabaseRef;                // DB
    private FirebaseUser firebaseUser;                // DB
    private EditText uId, uPw, uPwConfirm, uName, uTel;    // 입력 필드
    private TextView warning_email, warning_pw, warning_pw2, warning_name, warning_tel, warning_chapcha;
    private InputPatternChecker inputPatternChecker;
    private ScrollView scrollView;
    private Button btnisDuplicateId, btn_register;
    private boolean isDuplicateId = false; // 중복검사여부
    private boolean isMachedPw = false; // 비밀번호 일치여부
    private boolean isVaileTel = false; // 전화번호 유효성
    private User loginUser = null;
    private boolean isNewRegister = true;
    boolean isVaildPw = false;
    private ImageView chapcha, btn_refresh;
    private EditText chapchaOutput;
    private Button chapchaOutput_chk;
    private String R_key, img_src;
    private LinearLayout chapchaArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 툴바 생성
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        // 파이어베이스
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef  = FirebaseDatabase.getInstance().getReference("cardFolio");

        // 초기화
        uId = findViewById(R.id.edt_register_id);
        uPw = findViewById(R.id.edt_register_pw);
        uPwConfirm = findViewById(R.id.edt_register_pw2);
        uName = findViewById(R.id.edt_register_name);
        uTel = findViewById(R.id.edt_register_pnum);
        warning_email = findViewById(R.id.warning_email);
        warning_pw = findViewById(R.id.warning_pw);
        warning_pw2 = findViewById(R.id.warning_pw2);
        warning_tel = findViewById(R.id.warning_tel);
        warning_name = findViewById(R.id.warning_name);
        inputPatternChecker = InputPatternChecker.getInputPatternChecker();
        scrollView = findViewById(R.id.pageScroll);
        btnisDuplicateId = findViewById(R.id.btn_register_id_chk);
        btn_register = findViewById(R.id.btn_register);
        chapcha = findViewById(R.id.chapcha);
        chapchaArea = (LinearLayout) findViewById(R.id.chapchaArea);

        btn_register.setOnClickListener(registerEvent);
        btnisDuplicateId.setOnClickListener(registerIdCheck);
        
        toolbarName = "회원가입";

        // 정보 수정시, 기본정보 load
        Intent thisIntent = getIntent();
        String callingIntent = thisIntent.getStringExtra("intent_name");
        if (callingIntent.equals("registerModifyIntent")) {
            toolbarName = "기본정보 수정";

            isNewRegister = false;
            loginUser = (User) thisIntent.getSerializableExtra("UserAccount");

            // 정보수정 form으로 변경
            uId.setText(loginUser.getUser_email());
            ViewGroup parentView = (ViewGroup) btnisDuplicateId.getParent();
            ViewGroup parentView2 = (ViewGroup) uPwConfirm.getParent();
            ViewGroup parentView3 = (ViewGroup) warning_pw2.getParent();
            parentView.removeView(btnisDuplicateId); // 중복확인 삭제
            parentView2.removeView(uPwConfirm); // 비밀번호 확인 삭제
            parentView3.removeView(warning_pw2);
            uId.setFocusable(false);
            uId.setClickable(false);
            uId.setTextColor(Color.parseColor("#FFAAAAAA"));
            uId.setBackgroundResource(R.drawable.edittext_background_unactive);

            uName.setText(loginUser.getUser_name());
            uTel.setText(loginUser.getPhone_num());
            uPwConfirm.setText(loginUser.getUser_pw());
            btn_register.setText("수정 완료");
        }

        // 회원가입, 정보 수정 구분
        String btnText = btn_register.getText().toString();
        if(btnText.equals("회원가입 완료")){
            isNewRegister = true; // 신규 가입
        }else if(btnText.equals("수정 완료")){
            isNewRegister = false; // 정보 수정
        }

        // 툴바 이름
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarName);


        // 검사
        uId.addTextChangedListener(uIdWatcher); // 이메일
        uPw.addTextChangedListener(uPwWatcher); // 패스워드
        uPwConfirm.addTextChangedListener(uPwConfirmWatcher);
        uName.addTextChangedListener(uNameWatcher); // 이름
        uTel.addTextChangedListener(uTelWatcher); // 휴대폰


        // 캡챠 인증
        if(isNewRegister)
        {
            chapcha = (ImageView) findViewById(R.id.chapcha);
            chapchaOutput = (EditText) findViewById(R.id.chapchaOutput);
            chapchaOutput_chk = (Button) findViewById(R.id.chapchaOutput_chk);
            warning_chapcha = (TextView) findViewById(R.id.warning_chapcha);
            btn_refresh = (ImageView) findViewById(R.id.btn_refresh);
            chapchaOutput_chk.setOnClickListener(chapchaCheck);
            btn_refresh.setOnClickListener(chapchaRefresh);
            new Thread(() -> {
                R_key = reCap_key(); // 키
                img_src = reCap_Image(R_key);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeFile(img_src);
                        chapcha.setImageBitmap(bitmap);
                        chapcha.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                });
            }).start();
        }else{
            ViewGroup parentView4 = (ViewGroup) chapchaArea.getParent();
            parentView4.removeView(chapchaArea); // 캡챠 삭제
        }
    }

    public String reCap_key(){
        String key;
        String clientId = "_9bGx8j0n2SxCgw3mdtv"; //애플리케이션 클라이언트 아이디값";
        String clientSecret = "m0t2n3lQb_"; //애플리케이션 클라이언트 시크릿값";

        String code = "0"; // 키 발급시 0,  캡차 이미지 비교시 1로 세팅
        String apiURL = "https://openapi.naver.com/v1/captcha/nkey?code=" + code;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        String responseBody = getR(apiURL, requestHeaders);
        System.out.println("responseBody" + responseBody);

        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            key = jsonObject.getString("key");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println("key"+key);

        return key;
    }
    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }
    public String reCap_Image(String R_key){
        String Ikey;
        String clientId = "_9bGx8j0n2SxCgw3mdtv"; //애플리케이션 클라이언트 아이디값";
        String clientSecret = "m0t2n3lQb_"; //애플리케이션 클라이언트 시크릿값";

        String apiURL = "https://openapi.naver.com/v1/captcha/ncaptcha.bin?key="+R_key;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        Log.d("apiURL", apiURL);
        Log.d("requestHeaders", requestHeaders+"");
        Context context = RegisterActivity.this;
        String responseBody = getI(apiURL,requestHeaders, context);

        Log.d("responseBody!!!", responseBody+"");

        return responseBody;
    }

    private static String getI(String apiUrlI, Map<String, String> requestHeaders, Context context){
        HttpURLConnection con = connectI(apiUrlI);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            Log.d("getI", "setRequestPropertysetRequestPropertysetRequestProperty");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                Log.d("getI", "HTTP_OKHTTP_OK");
                return getImageI(con.getInputStream(), context);
            } else { // 에러 발생
                Log.d("에러", "에러");
                return errorI(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }
    private static HttpURLConnection connectI(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String errorI(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

    private static String getImageI(InputStream is, Context context){
        int read;
        byte[] bytes = new byte[1024];
        // 랜덤한 이름으로  파일 생성
        String filename = Long.valueOf(new Date().getTime()).toString();
        File f = new File(context.getFilesDir(),filename + ".jpg");
        Log.d("File:", f + "");
        try {
            f.createNewFile();  // 파일 생성
            try (OutputStream outputStream = new FileOutputStream(f)) {
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                return f+"";
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 캡차 파일 생성에 실패 했습니다.", e);
        }
    }


    private static HttpURLConnection connectR(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBodyR(InputStream body) {
        try {
            InputStreamReader streamReader = new InputStreamReader(body, StandardCharsets.UTF_8);

            try (BufferedReader lineReader = new BufferedReader(streamReader)) {
                StringBuilder responseBody = new StringBuilder();

                String line;
                while ((line = lineReader.readLine()) != null) {
                    responseBody.append(line);
                }
                return responseBody.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 이메일 검사

    // 이메일 검사
    private TextWatcher uIdWatcher = new TextWatcher() {
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
        String strData = uId.getText().toString();
        if(strData.isEmpty()){
            // 이메일 주소 빈값일때
            moveFocus(uId);
            warning_email.setText("이메일 주소를 입력해주세요");
            warning_email.setTextColor(Color.RED);
            return false;
        }else if(!inputPatternChecker.isEmail(strData)){
            // 이메일 주소 형식 어긋날 때
            warning_email.setText("이메일 주소에 '@'를 포함해 주세요");
            warning_email.setTextColor(Color.RED);
            return false;
        }else if(!isDuplicateId && isNewRegister) {
            // 중복 확인 안했을 때
            warning_email.setText("이메일 주소 중복확인을 해주세요.");
            warning_email.setTextColor(Color.RED);
            return false;
        }else if(isDuplicateId){
            warning_email.setText("사용가능한 이메일 주소입니다.");
            warning_email.setTextColor(Color.parseColor("#16a54f"));
            return true;
        }else{
            warning_email.setText("");
            return true;
        }
    }
    // 중복확인 버튼
    View.OnClickListener registerIdCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String emailAddr = uId.getText().toString();
            if (!emailAddr.isEmpty() && inputPatternChecker.isEmail(emailAddr)) {
                mFirebaseAuth.fetchSignInMethodsForEmail(emailAddr)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();

                                    if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                                        // 비밀번호 재설정 이메일 보내기
                                        mFirebaseAuth.sendPasswordResetEmail(emailAddr)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // 중복된 이메일 주소
                                                            isDuplicateId = false; // 중복됨
                                                            warning_email.setText("중복된 이메일 주소입니다.");
                                                            warning_email.setTextColor(Color.RED);
                                                        }
                                                    }
                                                });
                                    } else {
                                        // 중복된 이메일 주소
                                        isDuplicateId = true; // 중복되지 않음
                                        warning_email.setText("사용가능한 이메일 주소입니다.");
                                        warning_email.setTextColor(Color.parseColor("#16a54f"));
                                    }
                                } else {
                                    // 사용 가능
                                    Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }else if(emailAddr.isEmpty()){
                // 이메일 주소 빈값일때
                warning_email.setText("이메일 주소를 입력해주세요");
                warning_email.setTextColor(Color.RED);
            }else if(!inputPatternChecker.isEmail(emailAddr)){
                // 이메일 주소 형식 어긋날 때
                warning_email.setText("이메일 주소에 '@'를 포함해 주세요");
                warning_email.setTextColor(Color.RED);
            }
        };
    };

    // 비밀번호 검사
    private TextWatcher uPwWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isChekPw;
            isChekPw = isChekPw();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekPw(){
        String strData = uPw.getText().toString();
        if (isNewRegister) {
            if(strData.length()<6 || strData.length()>16){
                warning_pw2.setText("6자~16자의 영문/숫자 조합으로 입력해주세요.");
                warning_pw2.setTextColor(Color.RED);
                return false;
            }else{
                warning_pw2.setText("");
                return true;
            }
        }else {
            // 정보수정
            firebaseUser = mFirebaseAuth.getCurrentUser(); // 로그인한 사용자 정보 읽기
            Log.d("getUid : ", "getUid" + firebaseUser.getUid());
            if(firebaseUser != null) {
                Query query = mDatabaseRef.child("UserAccount").orderByChild("idToken").equalTo(firebaseUser.getUid());
                query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String pw = null;
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            pw = userSnapshot.child("user_pw").getValue(String.class);
                            Log.d("getPw : ", "pw" + pw);
                        }

                        if(strData.isEmpty()){
                            moveFocus(uPw);
                            warning_pw.setText("비밀번호를 입력해주세요.");
                            warning_pw.setTextColor(Color.RED);
                            isVaildPw = false;
                        } else if (!pw.equals(uPw.getText().toString())) {
                            warning_pw.setText("비밀번호를 확인해주세요.");
                            warning_pw.setTextColor(Color.RED);
                            isVaildPw = false;
                        }else{
                            warning_pw.setText("");
                            isVaildPw = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RegisterActivity.this, "정보를 읽는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                isVaildPw = true;
            }
            return true;
        }
    }

    // 비밀번호 확인 검사
    private TextWatcher uPwConfirmWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isChekPwConfirm;
            isChekPwConfirm = isChekPwConfirm();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekPwConfirm(){
        String strData2 = uPw.getText().toString();
        String strData = uPwConfirm.getText().toString();
        if (isNewRegister) {
            if (strData.isEmpty()) {
                moveFocus(uPw);
            } else if (!strData.equals(strData2)) {
                warning_pw.setText("비밀번호가 일치하지 않습니다.");
                warning_pw.setTextColor(Color.RED);
                return false;
            } else {
                warning_pw.setText("비밀번호가 일치합니다.");
                warning_pw.setTextColor(Color.parseColor("#16a54f"));
                isMachedPw = true;
                return true;
            }
        }
        return true;
    }

    // 이름 검사
    private TextWatcher uNameWatcher = new TextWatcher() {
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
        String strData = uName.getText().toString();
        if(strData.isEmpty()){
            moveFocus(uName);
            warning_name.setText("이름을 입력해주세요.");
            warning_name.setTextColor(Color.RED);
            return false;
        }else{
            warning_name.setText("");
            return true;
        }
    }

    // 휴대폰 검사
    private TextWatcher uTelWatcher = new TextWatcher() {
        private Handler handler = new Handler();
        private Runnable runnable;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 타이핑 도중에 호출되는 메소드
            handler.removeCallbacks(runnable); // 기존의 검사 요청이 있다면 제거
            boolean isCheck;
            isCheck = isChekPnums();
        }
        @Override
        public void afterTextChanged(final Editable s) { }
    };
    public boolean isChekPnums(){
        String strData = uTel.getText().toString();

        if(strData.isEmpty()){
            moveFocus(uTel);
            warning_tel.setText("휴대폰 번호를 입력해주세요.");
            warning_tel.setTextColor(Color.RED);
            return false;
        }else if(!inputPatternChecker.isMob(strData)){
            warning_tel.setText("유효한 휴대폰 번호를 입력해주세요.");
            warning_tel.setTextColor(Color.RED);
            return false;
        }else{
            warning_tel.setText("");
            isVaileTel = true;
            return true;
        }
    }
    
    // 완료 버튼
    View.OnClickListener registerEvent = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String strId = uId.getText().toString(); // 이메일
            String strPw = uPw.getText().toString();
            String struPwConfirm = uPwConfirm.getText().toString();
            String strName = uName.getText().toString();
            String strTel = uTel.getText().toString();
            Boolean isChekChapcha = true;

            if(isNewRegister){
                isChekChapcha = isChekChapcha();
            }
            // 완료
            if(isChekEmail() && isChekPw() && isChekPwConfirm() && isChekName() && isChekPnums() && chapchaResult && isChekChapcha &&
                    isDuplicateId && inputPatternChecker.isEmail(strId) && isNewRegister ) {
                // 회원가입 일때만
                    // Firebase Auth 처리 : 계정 생성
                    mFirebaseAuth.createUserWithEmailAndPassword(strId, strPw).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                                // 이메일 인증 메일 발송 처리 추가
                                if (firebaseUser != null) {
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> emailTask) {
                                            if (emailTask.isSuccessful()) {
                                                // account 객체 생성 및 데이터 설정
                                                User account = new User();
                                                account.setIdToken(firebaseUser.getUid());
                                                account.setUser_email(firebaseUser.getEmail());
                                                account.setUser_pw(strPw);
                                                account.setUser_name(strName);
                                                account.setPhone_num(strTel);

                                                // 객체추가 완료
                                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account); //DB에 Insert

                                                // RegisterDoneActivity로 전환
                                                Intent intent = new Intent(RegisterActivity.this, RegisterDoneActivity.class);
                                                startActivity(intent);
                                                finish(); // 현재 액티비티 종료

                                                // 이메일 확인 이메일이 성공적으로 전송되었을 때의 처리
                                                Toast.makeText(RegisterActivity.this, firebaseUser.getEmail() + "로 이메일 인증 메일이 전송되었습니다. 이메일을 확인해주세요.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // 이메일 확인 이메일 전송이 실패했을 때의 처리
                                                Toast.makeText(RegisterActivity.this, "이메일 인증 메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }


            
            // 검사
            isChekEmail();
            if(isDuplicateId){
                warning_email.setText("사용가능한 이메일 주소입니다.");
                warning_email.setTextColor(Color.parseColor("#16a54f"));
            }
            isChekPw();
            isChekPwConfirm();
            isChekName();
            isChekPnums();
            if(isNewRegister){ isChekChapcha();}

            if(!isNewRegister && !strId.isEmpty() && !strPw.isEmpty() && !strName.isEmpty() && !strTel.isEmpty() && isVaildPw){
                // 기본정보 수정일 때 update처리
                String key = mDatabaseRef.child("posts").push().getKey();
                Map<String, Object> userValues = loginUser.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("user_name", uName.getText().toString());
                childUpdates.put("phone_num", uTel.getText().toString());
                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).updateChildren(childUpdates);

                Toast.makeText(RegisterActivity.this, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                // RegisterDoneActivity로 전환
                Intent intent = new Intent(RegisterActivity.this, ViewMypage.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        }
    };

    // 포커스 이동
    public void moveFocus(View view){
        view.requestFocus();
        scrollToFocusedTextView(view); // 화면 이동
    }

    // 포커스 받는 Textview로 이동
    private void scrollToFocusedTextView(View focusedTextView) {
        int[] location = new int[2];
        focusedTextView.getLocationOnScreen(location);

        int focusedTextViewTop = location[1];
        int scrollViewTop = scrollView.getTop();
        int scrollViewBottom = scrollView.getBottom();

        if (focusedTextViewTop < scrollViewTop || focusedTextViewTop > scrollViewBottom) {
            // 포커스를 받은 TextView가 스크롤 가능한 컨테이너의 가시 영역 밖에 있는 경우
            scrollView.smoothScrollTo(0, focusedTextViewTop);
        }
    }

    private static boolean chapchaResult = true;
    View.OnClickListener chapchaCheck = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(() -> {
                reCap_Result(R_key);
            }).start();
        }
    };
    View.OnClickListener chapchaRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new Thread(() -> {
                img_src = reCap_Image(R_key);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeFile(img_src);
                        chapcha.setImageBitmap(bitmap);
                        chapcha.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                });
            }).start();
        }
    };

    // 이름 검사
    public boolean isChekChapcha(){
        String strData = chapchaOutput.getText().toString();
        moveFocus(chapchaOutput);
        if(strData.isEmpty()){
            warning_chapcha.setText("인증해주세요.");
            warning_chapcha.setTextColor(Color.RED);
            return false;
        }
        return true;
    }
    public boolean reCap_Result(String R_key){
        String result;
        String clientId = "_9bGx8j0n2SxCgw3mdtv";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "m0t2n3lQb_";//애플리케이션 클라이언트 시크릿값";
        String code = "1"; // 키 발급시 0,  캡차 이미지 비교시 1로 세팅
        String key = R_key; // 캡차 키 발급시 받은 키값
        String value = chapchaOutput.getText().toString(); // 사용자가 입력한 캡차 이미지 글자값

        String apiURL = "https://openapi.naver.com/v1/captcha/nkey?code=" + code + "&key=" + key + "&value=" + value;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);
        String responseBody = getR(apiURL, requestHeaders);

        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            result = jsonObject.getString("result");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Log.d("chapchaResult", Boolean.toString(chapchaResult));
        chapchaResult = result.equals("true") ? true : false;
        if(!chapchaResult){
            warning_chapcha.setText("인증번호가 틀립니다.");
            warning_chapcha.setTextColor(Color.RED);
        }else{
            warning_chapcha.setText("인증되었습니다!");
            warning_chapcha.setTextColor(Color.parseColor("#16a54f"));
        }

        return true;
    }

    private static String getR(String apiUrlR, Map<String, String> requestHeaders) {
        HttpURLConnection con = connectR(apiUrlR);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBodyR(con.getInputStream());
            } else { // 에러 발생
                return readBodyR(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

}