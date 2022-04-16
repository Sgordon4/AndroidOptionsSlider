package com.example.optionsslider;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;



/*
TODO

In order to fill most use cases for this api, we still need several things:

A setup callback when triggered in onFinishInflate
How to hook in animation and allow canceling downSwipe?
Might be able to pass drag event or pointer id or something to the callback

Potentially an option to set window w/h ahead of time, otherwise use main window w/h

Let options extend beyond window height, currently clips it at window height

Make a customizable lip height for the slider

Using ChrisBanes PhotoView:
When options is open, upon single/double tapping on media, close options and do nothing else.
Currently just allows media to be zoomed when options is open.
Also allows viewpager to be swiped using the options view when media is zoomed, which is fine
as it would never get to that point with the above fix

 */


public class SlideFragment extends FrameLayout {
    ViewDragHelper dragHelper;
    int globalAnchorPos;
    int sliderAnchorPos;

    int windowWidth = -1;
    int windowHeight = -1;
    int sliderTopHeight = -1;
    int mainContentHeight = -1;

    int baselineSliderHeight = 1750;           //Arbitrary-ish good looking height that works

    FrameLayout dragWrapper;
    FrameLayout mainViewWrapper, sliderViewWrapper;

    private DownSwipeListener downSwipeListener;

    private boolean isDragEnabled = true;





    public SlideFragment(Context context) {
        this(context, null);
    }
    public SlideFragment(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SlideFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClipChildren(false);
        setClipToOutline(false);
        setClipToPadding(false);

        dragWrapper = new FrameLayout(context);

        mainViewWrapper = new FrameLayout(context);
        sliderViewWrapper = new FrameLayout(context);

        mainViewWrapper.setOnClickListener(view -> {
            if(getSliderVisible())
                closeSlider();
        });


        addView(dragWrapper);
        dragWrapper.addView(mainViewWrapper);
        dragWrapper.addView(sliderViewWrapper);


        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (SlideFragment.this.getViewTreeObserver().isAlive())
                    SlideFragment.this.getViewTreeObserver().removeOnPreDrawListener(this);


                windowWidth = getWidth();
                windowHeight = getHeight();


                //Refresh the height of the main content wrapper ----------------------------------

                LayoutParams mainParams = (LayoutParams) mainViewWrapper.getLayoutParams();
                mainParams.width = windowWidth;
                mainParams.height = windowHeight;
                mainViewWrapper.setLayoutParams(mainParams);



                //Refresh the height of the options wrapper ---------------------------------------

                //Make sure the options slider height is tall enough to work well and look good
                int sliderChildHeight = baselineSliderHeight;
                if(sliderViewWrapper.getChildCount() == 1) {
                    int childHeight = sliderViewWrapper.getChildAt(0).getHeight();

                    if(childHeight > sliderChildHeight)
                        sliderChildHeight = childHeight;
                }

                LayoutParams sliderParams = (LayoutParams) sliderViewWrapper.getLayoutParams();
                sliderParams.width = windowWidth;
                sliderParams.height = sliderChildHeight;
                sliderParams.gravity = Gravity.BOTTOM;
                sliderViewWrapper.setLayoutParams(sliderParams);


                setSliderVisible(false);


                //Refresh the height of the dragWrapper -------------------------------------------

                //If the user never sent in the main content height...
                if(mainContentHeight == -1)
                    setMainContentHeight( mainViewWrapper.getChildAt(0).getHeight() );

                globalAnchorPos = (int) (windowHeight * 0.33);              //Position on the screen that the slider expands to
                sliderTopHeight = mainContentHeight - 40;                   //Position in dragWrapper where slider top will be placed
                sliderAnchorPos = globalAnchorPos - sliderTopHeight;        //Position to move top of dragWrapper to so that slider opens


                int totalDragwrapperHeight = sliderTopHeight + sliderChildHeight;

                LayoutParams wrapperParams = (LayoutParams) dragWrapper.getLayoutParams();
                wrapperParams.width = windowWidth;
                wrapperParams.height = totalDragwrapperHeight;
                dragWrapper.setLayoutParams(wrapperParams);


                invalidate();
                return true;
            }
        });


        dragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragCallback());
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Dragwrapper is in child index 1, look for more children defined in xml
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
            case 2:
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

    //TODO Refresh dragWrapper on call to setMainContentHeight
    //Send in the height of the main content so that slider knows where to attach
    public void setMainContentHeight(int height) {
        if(!(height > 0))
            throw new IllegalArgumentException("Main content height must be greater than 0!");
        this.mainContentHeight = height;
    }



    public boolean getSliderVisible() {
        return sliderViewWrapper.getVisibility() == VISIBLE;
    }
    private void setSliderVisible(boolean isVisible) {
        if(isVisible)
            sliderViewWrapper.setVisibility(VISIBLE);
        else
            sliderViewWrapper.setVisibility(INVISIBLE);

        sliderViewWrapper.setClickable(isVisible);
    }

    public void closeSlider() {
        //Move slider to closed position (animate?)
        moveWrapperBy(-dragWrapper.getTop());

        setSliderVisible(false);
    }
    public void openSlider() {
        System.out.println("Opening slider to "+(-dragWrapper.getTop() + sliderAnchorPos));
        //Move slider to open position (animate?)
        moveWrapperBy(-dragWrapper.getTop() + sliderAnchorPos);

        setSliderVisible(true);
    }

    //TODO Animate the move
    private void moveWrapperBy(int dy) {
        ViewCompat.offsetTopAndBottom(dragWrapper, dy);
    }



    //---------------------------------------------------------------------------------------------



    public boolean isDragEnabled() {
        return isDragEnabled;
    }
    public void setDragEnabled(boolean dragEnabled) {
        isDragEnabled = dragEnabled;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }


    //Spy on our children lmao (don't try this at home)
    boolean dragSlop;
    float startY;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                dragSlop = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if(!isDragEnabled())
                    break;

                int touchSlop = dragHelper.getTouchSlop();
                int touchDiff = (int) Math.abs(ev.getY()-startY);

                //If we've confirmed a relatively straight vertical drag
                if((touchDiff > touchSlop) && !dragSlop) {
                    dragSlop = true;

                    //Stop things like a viewpager from stealing input
                    requestDisallowInterceptTouchEvent(true);

                    //Reset the start coords of the motionEvent so the dragHelper doesn't jump
                    ev = MotionEvent.obtain(0, SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), ev.getMetaState());
                }

                if(dragSlop) {
                    dragHelper.processTouchEvent(ev);

                    //If drag was started on a child view like a button,
                    // that button will be activated if we don't tell it to fuck off
                    MotionEvent fuckOff = MotionEvent.obtain(0, SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_CANCEL, 0, 0, ev.getMetaState());
                    return super.dispatchTouchEvent(fuckOff);
                }
                break;
            default:
                if(dragSlop) {
                    dragHelper.processTouchEvent(ev);
                }
        }

        return super.dispatchTouchEvent(ev);
    }




    public class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == dragWrapper;
        }

        //We only care about stopping the bottom of the view from going above the bottom of the screen
        //Pulling the top below the top will be a gesture command later
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            int topBound, bottomBound;

            topBound = top;
            bottomBound = windowHeight - dragWrapper.getHeight();

            return Math.max(topBound, bottomBound);
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            //Speed has priority over position
            final double SPEED_LIMIT = 1000;            //Not actually a limiter, just a metric

            //Where top of slider is right now
            int currentSliderTopPos = releasedChild.getTop() + sliderTopHeight;


            //Note: Height is 0 at top of screen, so when something is visibly above something else,
            // it is technically at a smaller height value. That makes the math below somewhat confusing.

            //When seeking to understand, the if statements below are in a very particular order
            // based on sliderTop pos from top of screen to bottom, and the slider would not
            // behave correctly if the order was changed.


            //Move slider based on position -------------------------------------------------------

            //If top of slider is less than the anchor point, but greater than its max...
            if(currentSliderTopPos < globalAnchorPos){
                int bottomBound = windowHeight - releasedChild.getHeight();

                // Just fling the view, constrained between those two points
                dragHelper.flingCapturedView(0, bottomBound, 0, sliderAnchorPos);
                invalidate();
            }

            //If we're swiping down fast...
            else if(yvel > SPEED_LIMIT) {
                moveView(0);                    //Move to slider-closed
            }

            //If top of slider is less than window center...
            else if(currentSliderTopPos < windowHeight/2) {
                moveView(sliderAnchorPos);             //Move to slider-open
            }

            //If we're swiping up fast...
            else if(yvel < -SPEED_LIMIT) {
                moveView(sliderAnchorPos);             //Move to slider-open
            }

            //If top of slider is greater than window center...
            else if(currentSliderTopPos > windowHeight/2) {
                moveView(0);                    //Move to slider-closed
            }

            //If top of entire view is below window top, we've swiped down to close
            else if(releasedChild.getTop() > 0) {
                if(downSwipeListener != null)
                    downSwipeListener.onDownSwipe();
            }


            //TODO Check to trigger callback for fast swipe down when closed. AKA popBackStack()
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);

            //Show slider based on dragWrapper position
            if (top <= -50 && !getSliderVisible())
                setSliderVisible(true);
            else if(top > -50 && getSliderVisible())
                setSliderVisible(false);

        }

        public void moveView(int endPos){
            if(dragHelper.settleCapturedViewAt(0, endPos)) {
                ViewCompat.postInvalidateOnAnimation(SlideFragment.this);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    public interface DownSwipeListener {
        void onDownSwipe();
    }

    public void setDownSwipeListener(@NonNull DownSwipeListener listener) {
        downSwipeListener = listener;
    }
    public DownSwipeListener getDownSwipeListener() {
        return downSwipeListener;
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