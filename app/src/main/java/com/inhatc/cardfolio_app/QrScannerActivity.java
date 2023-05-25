package com.inhatc.cardfolio_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QrScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // QR 코드 스캐너 실행
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0); // 후면 카메라 사용
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // QR 코드 스캔 결과 처리
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // QR 코드를 스캔하지 못한 경우
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            } else {
                // QR 코드 스캔 결과 가져오기
                String scannedData = result.getContents();
                Intent intent = new Intent(QrScannerActivity.this, ShareCardActivity.class);
                intent.putExtra("intent_name", "qrScanStoreIntent");
                intent.putExtra("c_id", scannedData); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                startActivity(intent);
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}