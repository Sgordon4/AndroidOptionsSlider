package com.example.optionssliderexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.optionssliderexample.ui.main.ViewPager;
import com.example.optionssliderexample.util.Util;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        List<Fragment> pageList = Util.getPageList(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, ViewPager.newInstance(pageList, 0))
                    .commitNow();
        }
    }
}



