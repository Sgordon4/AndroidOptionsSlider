package com.example.optionssliderexample.pages;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.optionssliderexample.R;
import com.sgordon4.slideview.SlideView;

import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;


public class GifPage extends Fragment {

    String mediaName = "sample_animated_gif.gif";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.gifpage, container, false);

        //If the height of a view is not directly set through XML (either through setting
        // an image source or setting a height explicitly), SlideView is unable to tell
        // where the slider should attach.
        //
        //Thus, we need to set the height manually:


        SlideView slideView = view.findViewById(R.id.slide_view);
        GifImageView gifImageView = view.findViewById(R.id.gif_view);

        //An example of manually loading the media we want.
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/"+mediaName))
                .into(gifImageView);



        //Set the desired media height as soon as we can
        slideView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                try {

                    //Get the width and height of the media we used
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream( getContext().getAssets().open(mediaName), null, options );

                    int mediaHeight = options.outHeight;
                    int mediaWidth = options.outWidth;

                    //This is where it gets a tad complicated.
                    //We need to scale the dimensions of the media based on the window size to get the correct height.
                    //To allow for more user freedom, SlideView does not do this automatically.
                    //However, it does provide a simple to use method to calculate the values needed:
                    Pair<Integer, Integer> mediaSizing = SlideView.calculateScaledContentDimens(slideView.getWidth(), slideView.getHeight(), mediaWidth, mediaHeight);

                    //Finally, set the correct height
                    slideView.setMainContentHeight(mediaSizing.second);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        return view;
    }
}