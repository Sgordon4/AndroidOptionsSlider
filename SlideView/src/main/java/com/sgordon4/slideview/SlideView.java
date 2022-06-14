package com.example.slideview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;



/*
TODO

In order to fill some use cases for this api, we still need several things:

Vertical ScrollViews (either inside or encapsulating this view) + drag = problemo.
This disallows the user adding down-swipe detection (a-la Google photos) or whatever else is wanted.
Plan to re-work this in the future, using ScrollView and PhotoView as inspiration.
A previous note with photoView: https://github.com/Baseflow/PhotoView/issues/142

If someone wants to position things oddly, attaching slider to windowHeight/2 + mainContentHeight/2
may not cut it. Change slider attachment system to attach based on the actual locations of items.

Ensure Viewpager swiping and swiping while libraries like ChrisBanes PhotoView are zoomed works.
 */


public class SlideView extends FrameLayout {
    int windowWidth = -1;
    int windowHeight = -1;

    int mainContentHeight = -1;
    int sliderContentHeight = -1;
    int sliderLipHeight = 40;

    //If slider height is <= this value, we will attach it to the bottom of the screen
    // rather than to the bottom of the main content
    protected int minSliderHeight;
    private boolean sliderAlwaysVisible = false;

    protected final FrameLayout fullWrapper;
    private final FrameLayout mainViewWrapper, sliderViewWrapper;


    private final SlideDragHandler dragHandler;




    public SlideView(Context context) {
        this(context, null);
    }
    public SlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClipChildren(false);
        setClipToOutline(false);
        setClipToPadding(false);

        fullWrapper = new FrameLayout(context);

        mainViewWrapper = new FrameLayout(context);
        sliderViewWrapper = new FrameLayout(context);


        addView(fullWrapper);
        fullWrapper.addView(mainViewWrapper);
        fullWrapper.addView(sliderViewWrapper);

        dragHandler = new SlideDragHandler(SlideView.this);


        //Every draw time, change the dimensions of our views
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (SlideView.this.getViewTreeObserver().isAlive())
                    SlideView.this.getViewTreeObserver().removeOnPreDrawListener(this);


                windowWidth = getWidth();
                windowHeight = getHeight();

                minSliderHeight = windowHeight/2;


                //Refresh the height of the main content wrapper ----------------------------------

                //While the content might be smaller than the window, the 'main content wrapper'
                // actually fills all available window space for better touch events
                LayoutParams mainParams = (LayoutParams) mainViewWrapper.getLayoutParams();
                mainParams.width = windowWidth;
                mainParams.height = windowHeight;
                mainViewWrapper.setLayoutParams(mainParams);

                //If the user never sent in the main content height...
                if(mainContentHeight == -1)
                    setMainContentHeight( mainViewWrapper.getChildAt(0).getHeight() );



                //Refresh the height of the options wrapper ---------------------------------------

                //TODO does this need a check for number of children?
                sliderContentHeight = sliderViewWrapper.getChildAt(0).getHeight();

                LayoutParams sliderParams = (LayoutParams) sliderViewWrapper.getLayoutParams();
                sliderParams.width = windowWidth;
                sliderParams.height = sliderContentHeight;
                sliderParams.gravity = Gravity.BOTTOM;
                sliderViewWrapper.setLayoutParams(sliderParams);


                if(sliderLipHeight > sliderContentHeight)
                    throw new IllegalArgumentException("Lip height must be <= to the slider's height!");

                setSliderVisible(getSliderAlwaysVisible());


                //Refresh the height of the fullWrapper --------------------------------------

                //If slider is tall enough, attach it to the bottom of the main content, otherwise place
                // it just past the bottom of the screen to avoid whitespace underneath it when opened
                int mainContentBottom = windowHeight/2 + mainContentHeight/2;
                int extendPastWindowBottom;

                if(sliderContentHeight <= minSliderHeight) {
                    extendPastWindowBottom = sliderContentHeight - sliderLipHeight;
                }
                else {
                    extendPastWindowBottom = sliderContentHeight - sliderLipHeight - (windowHeight - mainContentBottom);
                }

                int totalWrapperHeight = windowHeight + extendPastWindowBottom;

                LayoutParams wrapperParams = (LayoutParams) fullWrapper.getLayoutParams();
                wrapperParams.width = windowWidth;
                wrapperParams.height = totalWrapperHeight;
                fullWrapper.setLayoutParams(wrapperParams);

                //----------------------------------------------------------------------------


                getDragHandler().recalculateDragPositions();

                return true;
            }
        });
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //FullWrapper is in child index 1, look for more children defined in xml
        //If user added child count is not 0 or 2, throw a tantrum

        addPlaceholderViews(getContext());

        switch (getChildCount()) {
            case 3:
                View sliderChild = getChildAt(2);
                removeView(sliderChild);
                setSliderView(sliderChild);

                View mainChild = getChildAt(1);
                removeView(mainChild);
                setMainView(mainChild);

                mainContentHeight = -1;
                break;
            case 1:
                break;
            default:throw new IllegalArgumentException("Child count of "+getChildCount()+" not valid, SlideTest must have 0 or 2 children upon initialization!");
        }
    }

    public void setMainView(@NonNull View mainChild) {
        mainViewWrapper.removeAllViews();
        mainViewWrapper.addView(mainChild);
    }
    public void setSliderView(@NonNull View sliderChild) {
        sliderViewWrapper.removeAllViews();
        sliderViewWrapper.addView(sliderChild);
    }
    public void setViews(@NonNull View mainChild, @NonNull View sliderChild) {
        setMainView(mainChild);
        setSliderView(sliderChild);
    }


    public FrameLayout getMainViewWrapper() {
        return mainViewWrapper;
    }
    public FrameLayout getSliderViewWrapper() {
        return sliderViewWrapper;
    }

    //Send in the height of the main content so that slider knows where to attach
    //This height must be scaled to the screen or there will be problems
    //The method calculateScaledContentHeight() can help with that
    public void setMainContentHeight(int height) {
        if(height <= 0)
            throw new IllegalArgumentException("Main content height must be greater than 0!");
        this.mainContentHeight = height;
        invalidate();
    }

    //TODO does this break if we turn the phone sideways and the width is wacky?
    public static Pair<Integer, Integer> calculateScaledContentHeight(int parentWidth, int parentHeight, int mediaWidth, int mediaHeight) {
        //Calculate the height for the mediaView
        int newViewHeight = Math.round(((float)parentWidth/mediaWidth) * mediaHeight);

        //If our calculated height fits inside the parent height...
        if(newViewHeight <= parentHeight)
            return new Pair<>(parentWidth, newViewHeight);
        else
            return new Pair<>(parentWidth, parentHeight);
    }

    //User is allowed to set a -lipHeight. No idea what you would use it for, but why not.
    //Lip height is not allowed to be > than slider height however. The check for that comes later.
    public void setSliderLipHeight(int lipHeight) {
        this.sliderLipHeight = lipHeight;
        invalidate();
    }



    public boolean getSliderVisible() {
        return sliderViewWrapper.getVisibility() == View.VISIBLE;
    }
    protected void setSliderVisible(boolean isVisible) {
        if(isVisible) {
            sliderViewWrapper.setVisibility(View.VISIBLE);
            sliderViewWrapper.setClickable(true);
        }
        else {
            if(!getSliderAlwaysVisible()) {
                sliderViewWrapper.setVisibility(View.INVISIBLE);
                sliderViewWrapper.setClickable(false);
            }
        }
    }


    public boolean getSliderAlwaysVisible() {
        return sliderAlwaysVisible;
    }
    public void setSliderAlwaysVisible(boolean sliderAlwaysVisible) {
        this.sliderAlwaysVisible = sliderAlwaysVisible;
        invalidate();
    }



    //---------------------------------------------------------------------------------------------


    public SlideDragHandler getDragHandler() {
        return dragHandler;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return getDragHandler().onTouchEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent( getDragHandler().dispatchTouchEvent(ev) );
    }
    @Override
    public void computeScroll() {
        getDragHandler().computeScroll();
    }





    private void addPlaceholderViews(Context context) {
        String placeholder1Text = "\nBillboard space here!\n\n" +
                "For best results, it is recommended to set\n" +
                "your main view's height to MATCH_PARENT,\n" +
                "then to center its content vertically\n\n\n\n" +
                "Swipe up for options!";
        TextView placeholder1 = new TextView(context);
        placeholder1.setSingleLine(false);
        placeholder1.setMaxLines(10);
        placeholder1.setText(placeholder1Text);
        placeholder1.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        placeholder1.setTextColor(Color.WHITE);
        placeholder1.setBackgroundColor(Color.GRAY);

        placeholder1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        String placeholder2Text = "\nOptions go here!\n\n" +
                "Feel free to use setMainContentHeight()\n" +
                "and shouldUseLip() to settle this slider right\n" +
                "where you want it!";
        TextView placeholder2 = new TextView(context);
        placeholder2.setSingleLine(false);
        placeholder2.setMaxLines(6);
        placeholder2.setText(placeholder2Text);
        placeholder2.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        placeholder2.setTextColor(Color.WHITE);
        placeholder2.setBackgroundColor(Color.DKGRAY);

        placeholder2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1750));

        setViews(placeholder1, placeholder2);
    }
}