package com.example.optionssliderexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.optionssliderexample.ui.main.ViewPager;
import com.example.optionssliderexample.util.Util;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        List<Bitmap> mediaList = Util.getExampleMedia(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ViewPager.newInstance(mediaList, 0))
                    .commitNow();
        }
    }
}



