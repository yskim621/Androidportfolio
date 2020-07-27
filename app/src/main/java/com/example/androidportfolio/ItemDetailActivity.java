package com.example.androidportfolio;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {
    //화면에 사용할 뷰
    TextView lblitemname, lbldescription, lblprice;
    ImageView imgpictureurl;

    //텍스트 데이터를 웹에서 다운로드 받아서 출력
    //다운로드 -> 파싱 -> 출력

    //텍스트 데이터를 출력할 핸들러
    Handler textHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            //넘어온 데이터 찾아오기
            Map<String,Object> map = (Map<String,Object>)message.obj;

            //데이터 출력하기
            lblitemname.setText((String)map.get("itemname"));
            lblprice.setText((Integer)map.get("price") +"");
            lbldescription.setText((String)map.get("description"));

            //이미지 파일명을 ImageThread에게 넘겨서 출력
            new ImageThread((String)map.get("pictureurl")).start();
        }
    };

    //텍스트 데이터를 가져올 스레드 클래스
    class TextThread extends Thread{
        StringBuilder sb = new StringBuilder();
        @Override
        public void run(){
            //텍스트 데이터 다운로드
            try{
                //URL 만들기
                URL url = new URL("http://192.168.0.109:8080/yskim62100/detail?itemid=" + 1);

                //Connection 객체 만들기
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                //옵션 설정
                con.setUseCaches(false);
                con.setConnectTimeout(30000);

                //스트림 객체 생성
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                //문자열 읽기
                while(true){
                    String line = br.readLine();
                    if(line == null){
                        break;
                    }
                    sb.append(line + "\n");
                }

                br.close();
                con.disconnect();

            }catch(Exception e){
                //이 메시지가 보이면 서버가 구동 중인지 확인하고
                //URL은 제대로 입력했는지 확인
                Log.e("다운로드 에러", e.getMessage());
            }
            Log.e("다운로드 받은 문자열", sb.toString());

            try{
                //다운로드 받은 문자열에서 필요한 데이터 추출
                JSONObject object = new JSONObject(sb.toString());
                JSONObject item = object.getJSONObject("item");

                String itemname = item.getString("itemname");
                int price = item.getInt("price");
                String descritpion = item.getString("description");
                String pictureurl = item.getString("pictureurl");

                //4개의 데이터를 하나로 묶기
                Map<String, Object> map = new HashMap<>();
                map.put("itemname", itemname);
                map.put("price", price);
                map.put("description", descritpion);
                map.put("pictureurl", pictureurl);

                //핸들러에게 데이터를 전송하고 호출
                Message message = new Message();
                message.obj = map;
                textHandler.sendMessage(message);


            }catch(Exception e){
                Log.e("파싱 에러", e.getMessage());
            }
        }
    }

    //이미지 출력을 위한 핸들러
    Handler imageHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            //스레드가 전달해준 데이터를 이미지 뷰에 출력
            Bitmap bitmap = (Bitmap)message.obj;
            imgpictureurl.setImageBitmap(bitmap);
        }
    };

    //이미지 다운로드를 위한 스레드
    class ImageThread extends Thread{
        //다운로드 받을 파일명
        String pictureurl;
        public ImageThread(String pictureurl){
            this.pictureurl = pictureurl;
        }

        @Override
        public void run(){
            try{
                URL url = new URL("http://192.168.0.109:8080/yskim62100/img/" + pictureurl);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(30000);
                con.setUseCaches(false);

                //바로 출력
                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //Message에 저장
                Message message = new Message();
                message.obj = bitmap;
                imageHandler.sendMessage(message);

            }catch(Exception e){
                Log.e("이미지 다운로드 실패", e.getMessage());
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        new TextThread().start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        //디자인 한 뷰 찾아오기
        lblitemname = (TextView)findViewById(R.id.lblitemname);
        lbldescription = (TextView)findViewById(R.id.lbldescription);
        lblprice = (TextView)findViewById(R.id.lblprice);

        imgpictureurl = (ImageView)findViewById(R.id.imgpictureurl);
    }
}