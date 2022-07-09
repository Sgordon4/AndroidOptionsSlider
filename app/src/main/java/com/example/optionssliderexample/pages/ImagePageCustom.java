package com.example.optionssliderexample.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.optionssliderexample.R;
import com.sgordon4.slideview.SlideView;


public class ImagePageCustom extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.imagepagecustom, container, false);

        //What if we wanted the slider to always peek up from the bottom?
        SlideView slideView = view.findViewById(R.id.slide_view);

        slideView.setSliderAutoAssessHeight(false);
        slideView.setSliderLipHeight(200);
        slideView.setSliderAlwaysVisible(true);


        return view;
    }
}