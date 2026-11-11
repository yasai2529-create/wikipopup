package com.example.wikipopup;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad * 2, pad, pad);
        tv.setTextSize(16);
        tv.setText("使い方\n\n"
                + "1. 他のアプリで文字を長押しして選択します。\n"
                + "2. 選択メニューの「Wikipedia」（見当たらなければ「⋮」の中）をタップします。\n"
                + "3. Wikipediaの記事が小窓で開きます。\n\n"
                + "閉じるには、右上の×か、小窓の外側をタップします。");
        setContentView(tv);
    }
}
