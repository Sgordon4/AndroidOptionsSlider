package com.example.optionssliderexample.ui.main;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.optionssliderexample.R;
import com.sgordon4.slideview.SlideView;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;


public class Page extends Fragment {
    Bitmap media;

    public static Page newInstance(Bitmap media) {
        Page page = new Page();
        page.media = media;
        return page;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.page, container, false);
        //ViewGroup view = (ViewGroup) inflater.inflate(R.layout.scrollpage, container, false);

        SlideView slideTest = view.findViewById(R.id.slide_test);

        PhotoView photoView = view.findViewById(R.id.photo_view);
        photoView.setImageBitmap(media);

        photoView.setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                float currScale = photoView.getScale();
                boolean isCurrentlyMinScale = currScale <= 1.0001;

                float newScale = scaleFactor*photoView.getScale();
                boolean willBeMinScale = Math.abs(newScale - photoView.getMinimumScale()) < 0.0001;

                System.out.println("Scale changing: ");
                System.out.println("Curr: "+isCurrentlyMinScale+" : "+photoView.getScale());
                System.out.println("Next: "+willBeMinScale+" : "+Math.abs(newScale - photoView.getMinimumScale()));

                //If we are currently <= 1.0 scale, and are not moving to 1.0 from below 1.0
                if(isCurrentlyMinScale && !willBeMinScale) {
                    slideTest.getDragHandler().setDragEnabled(false);
                    photoView.setAllowParentInterceptOnEdge(false);
                }
                //Else if we are moving to 1.0 scale from above 1.0
                else if(willBeMinScale) {
                    slideTest.getDragHandler().setDragEnabled(true);
                    photoView.setAllowParentInterceptOnEdge(true);
                }
            }
        });
        photoView.getAttacher().setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {

            }
        });

        slideTest.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                SlideView slideTest = view.findViewById(R.id.slide_test);

                int windowWidth = slideTest.getWidth();
                int windowHeight = slideTest.getHeight();

                //Calculate the actual height of the media so the slider attaches where we want it
                Pair<Integer, Integer> mediaSizing = SlideView.calculateScaledContentHeight(windowWidth, windowHeight, media.getWidth(), media.getHeight());
                slideTest.setMainContentHeight(mediaSizing.second);
                //slideTest.setSliderLipHeight(825);
                //slideTest.setSliderAlwaysVisible(true);
            }
        });


        return view;
    }
}
