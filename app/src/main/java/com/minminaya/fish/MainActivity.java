package com.minminaya.fish;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        ImageView imageView = (ImageView) findViewById(R.id.img);
//        imageView.setImageDrawable(new FishDrawable(this));
    }
}
