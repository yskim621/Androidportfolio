package com.example.androidportfolio;

import android.widget.ListView;

public class Item {
    // 접근 지정자를 public으로 선언해서 사용자가 편리하게 사용할 수 있도록 만듦
    public int itemid;
    public String itemname;
    public int price;
    public String description;
    public String pictureurl;

    @Override
    //ListView는 객체를 데이터로 주입하면 toString의 결과를 셀에 출력하기 때문
    public String toString() {
        return itemname;
    }
}
