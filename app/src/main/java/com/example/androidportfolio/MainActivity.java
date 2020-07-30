package com.example.androidportfolio;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    //콤보 박스 역할을 하는 위젯
    private Spinner searchtype;
    private EditText value;

    private Button btnsearch, btnnext;

    //private TextView list;

    private ListView listView;
    private List<Item> list;
    private ArrayAdapter<Item> itemAdapter;

    //Spinner에 데이터를 연결할 Adapter
    private ArrayAdapter<CharSequence> adapter;

    //페이지 번호와 페이지 당 데이터 개수를 저장할 변수
    int pageNo = 1;
    int size = 3;

    //조건에 맞는 데이터 개수를 저장할 변수
    int cnt;

    //출력할 내용
    String result = "";

    //스레드가 다운로드 받아서 파싱한 결과를 출력할 핸들러
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            //list.setText(result);

            // adapter를 이용해서 Listview에 데이터가 수정되면 다시 출력하도록 [신호를 보냄 - notification]
            itemAdapter.notifyDataSetChanged();
        }
    };

    //데이터를 다운로드 받아서 파싱하는 스레드
    class ThreadEx extends Thread{
        //다운로드 받은 문자열을 저장할 객체
        StringBuilder sb = new StringBuilder();

        public void run(){
            try{
                URL url = null;
                //콤보 박스 선택한 항목 번호를 idx에 저장
                int idx = searchtype.getSelectedItemPosition();
                if(idx == 0){
                    url = new URL(
                            "http://192.168.0.109:8080/yskim62100/list?" +
                                    "pageno=" + pageNo);
                }else if(idx == 1){
                    url = new URL(
                            "http://192.168.0.109:8080/yskim62100/list?"
                                    + "searchtype=itemname&" + "value=" +
                                    value.getText().toString() + "&pageno="
                                    +pageNo
                    );
                }else if(idx == 2){
                    url = new URL(
                            "http://192.168.0.109:8080/yskim62100/list?"
                                    + "searchtype=description&" + "value=" +
                                    value.getText().toString() + "&pageno="
                                    +pageNo
                    );
                }else{
                    url = new URL(
                            "http://192.168.0.109:8080/yskim62100/list?"
                                    + "searchtype=both&" + "value=" +
                                    value.getText().toString() + "&pageno="
                                    +pageNo
                    );
                }

                HttpURLConnection con = (
                        HttpURLConnection)url.openConnection();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
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
                Log.e("다운로드 예외", e.getMessage());
            }

            try{
                //객체로 변환
                JSONObject object = new JSONObject(sb.toString());
                //데이터 개수는 count에 숫자로 저장
                cnt = object.getInt("count");
                //list 키의 데이터를 배열로 가져오기
                JSONArray ar = object.getJSONArray("list");
                for(int i=0; i<ar.length(); i=i+1){
                    JSONArray temp = ar.getJSONArray(i);
                    // result = result + temp.getString(1) + "\n";
                    Item item = new Item();
                    item.itemid = temp.getInt(0);
                    item.itemname = temp.getString(1);
                    item.price = temp.getInt(2);
                    item.description = temp.getString(3);
                    item.pictureurl = temp.getString(4);

                    list.add(item);
                }
                //핸들러에게 출력을 요청
                handler.sendEmptyMessage(0);

            }catch(Exception e){
                Log.e("파싱에러", e.getMessage());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchtype = (Spinner)findViewById(R.id.searchtype);
        adapter = ArrayAdapter.createFromResource(
                this,R.array.searchtype_array,
                android.R.layout.simple_spinner_dropdown_item
        );
        searchtype.setAdapter(adapter);

        value = (EditText)findViewById(R.id.value);
        btnnext = (Button)findViewById(R.id.btnnext);
        btnsearch = (Button)findViewById(R.id.btnsearch);
        // list = (TextView)findViewById(R.id.list);
        listView = (ListView) findViewById(R.id.listview);
        list = new Stack<Item>();
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        listView.setAdapter(itemAdapter);


        btnnext.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                pageNo = pageNo + 1;
                new ThreadEx().start();
            }
        });

        btnsearch.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                pageNo = 1;
                result = "";
                new ThreadEx().start();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        new ThreadEx().start();
    }
}