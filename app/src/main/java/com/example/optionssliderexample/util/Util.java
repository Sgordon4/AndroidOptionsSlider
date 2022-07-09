package com.example.optionssliderexample.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Pair;

import androidx.fragment.app.Fragment;

import com.example.optionssliderexample.pages.GifPage;
import com.example.optionssliderexample.pages.ImagePage;
import com.example.optionssliderexample.pages.ImagePageCustom;
import com.example.optionssliderexample.pages.TextPage;
import com.example.optionssliderexample.pages.VideoPage;
import com.sgordon4.slideview.SlideView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<Fragment> getPageList(Context context) {
        List<Fragment> pageList = new ArrayList<>();


        pageList.add(new TextPage());
        pageList.add(new ImagePage());
        pageList.add(new ImagePageCustom());
        pageList.add(new GifPage());
        pageList.add(new VideoPage());

        return pageList;
    }


    public static Pair<Integer, Integer> getMediaDimens(int windowWidth, int windowHeight, Context context, String mediaName) {
        try {
            AssetManager assetManager = context.getAssets();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream( assetManager.open(mediaName), null, options );

            int mediaHeight = options.outHeight;
            int mediaWidth = options.outWidth;

            //Calculate the actual height of the media so the slider attaches where we want it
            Pair<Integer, Integer> mediaSizing = SlideView.calculateScaledContentDimens(windowWidth, windowHeight, mediaWidth, mediaHeight);
            return mediaSizing;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Pair<>(0, 0);
    }
}
