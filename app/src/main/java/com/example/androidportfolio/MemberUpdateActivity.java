package com.example.androidportfolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;

public class MemberUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_update);

        try{
            // 로그인 성공한 경우 처리
            FileInputStream fis = openFileInput("login.txt");
            byte [] b = new byte[fis.available()];
            int length = fis.read(b);
            String str = new String(b,0, length);
            String [] ar = str.split(":");
            Toast.makeText(this, ar[0], Toast.LENGTH_LONG).show();
        } catch (Exception e){
            // 로그인이 안된 경우 처리
            Log.e("파일 읽기 예외", e.getMessage());
            // 로그인 페이지로 이동시키면 로그인이 안 돼있을 때 로그인 페이지로 이동함
        }
    }
}