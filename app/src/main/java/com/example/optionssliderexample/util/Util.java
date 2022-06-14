package com.example.optionssliderexample.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.optionssliderexample.R;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<Bitmap> getExampleMedia(Context context) {
        List<Bitmap> testData = new ArrayList<>();
        testData.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_jpg));
        testData.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_png));
        //testData.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_webp));
        //testData.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_animated_webp));
        //testData.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_animated_gif));

        return testData;
    }
}
