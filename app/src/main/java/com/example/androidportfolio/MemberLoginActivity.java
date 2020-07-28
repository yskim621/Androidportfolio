package com.example.androidportfolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MemberLoginActivity extends AppCompatActivity {
    EditText nicknameInput, pwInput;
    Button btnLogin, btnJoin, btnMain;

    class ThreadEx extends Thread{
        // 다운로드 받을 문자열을 저장할 변수
        StringBuilder sb = new StringBuilder();

        @Override
        public void run(){
            try{
                URL url = new URL("http://192.168.0.109:8080/yskim62100/login");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setConnectTimeout(30000);

                // 파라미터 만들기
                String parameter = URLEncoder.encode("nickname", "UTF-8") + "=" + URLEncoder.encode(nicknameInput.getText().toString().trim(), "UTF-8")
                        + "&" + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(pwInput.getText().toString().trim(), "UTF-8");

                // 파라미터 전송
                OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
                os.write(parameter);
                os.flush();

                // 결과 가져오기
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while (true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();
                Log.e("다운로드 받은 문자열", sb.toString());


            }catch (Exception e){
                Log.e("서버 연동 예외", e.getMessage());
            }

            try{
                Map<String, Object> map = new HashMap<>();
                JSONObject object = new JSONObject(sb.toString());

                map.put("result", (Boolean)map.get("result"));
                map.put("nickname", (String)map.get("nickname"));
                map.put("profile", (String)map.get("profile"));
                map.put("email", (String)map.get("email"));

                Message message = new Message();
                message.obj = map;
                handler.sendMessage(message);



            }catch (Exception e){
                Log.e("데이터 파싱 예외", e.getMessage());
            }
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
      @Override
      public void handleMessage(Message message) {


      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_login);

        nicknameInput = (EditText) findViewById(R.id.nicknameinput);
        pwInput = (EditText) findViewById(R.id.pwinput);

        btnJoin = (Button) findViewById(R.id.btnjoin);
        btnLogin = (Button) findViewById(R.id.btnlogin);
        btnMain = (Button) findViewById(R.id.btnmain);

        btnLogin.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                // 유효성 검사 수행

                // 서버에 요청
                new ThreadEx().start();
            }
        });
    }
}