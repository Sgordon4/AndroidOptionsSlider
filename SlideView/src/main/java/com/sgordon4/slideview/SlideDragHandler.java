package com.sgordon4.slideview;

import android.os.SystemClock;
import android.view.MotionEvent;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import java.util.ArrayList;
import java.util.List;

public class SlideDragHandler {
    SlideView slideView;

    ViewDragHelper dragHelper;
    int globalAnchorPos;
    int sliderAnchorPos;

    private boolean isDragEnabled = true;

    private final List<SlideViewDragEventListener> dragEventListeners = new ArrayList<>();


    public SlideDragHandler(SlideView slideView) {
        this.slideView = slideView;
        dragHelper = ViewDragHelper.create(slideView, 1.0f, new SlideViewDragCallback(this));

        recalculateDragPositions();
    }


    public void recalculateDragPositions() {

        //Position on the screen that the slider first expands to
        //Either the child expands to its full height or it stops expanding at 2/3 up the screen
        globalAnchorPos = Math.max((int) (slideView.windowHeight * 0.33),
                                          slideView.windowHeight - slideView.sliderContentHeight);

        int heightThatIsntSlider;

        //If the slider is big enough...
        if(slideView.sliderContentHeight >= slideView.minSliderHeight) {
            //It will be attached just at the bottom of the main content
            heightThatIsntSlider = slideView.windowHeight / 2 + slideView.mainContentHeight / 2;
            heightThatIsntSlider -= slideView.sliderLipHeight;
        }
        else {
            //Otherwise it will be attached at the bottom of the screen
            heightThatIsntSlider = slideView.windowHeight;
            heightThatIsntSlider -= slideView.sliderLipHeight;
        }


        //Position to move top of dragWrapper to so that slider is at globalAnchor
        sliderAnchorPos = globalAnchorPos - heightThatIsntSlider;
    }



    public boolean getDragEnabled() {
        return isDragEnabled;
    }
    public void setDragEnabled(boolean dragEnabled) {
        isDragEnabled = dragEnabled;
    }


    public void closeSlider() {
        //Move slider to closed position (animate?)
        moveWrapperBy(-slideView.fullWrapper.getTop());
        notifyListenersSliderClosed();
    }
    public void openSlider() {
        //Move slider to open position (animate?)
        moveWrapperBy(-slideView.fullWrapper.getTop() + sliderAnchorPos);
        notifyListenersSliderOpened();
    }

    public void hideSlider() {
        closeSlider();
        slideView.setSliderVisible(false);
    }
    public void showSlider() {
        openSlider();
        slideView.setSliderVisible(true);
    }

    //TODO Animate the move
    private void moveWrapperBy(int dy) {
        ViewCompat.offsetTopAndBottom(slideView.fullWrapper, dy);
    }


    //---------------------------------------------------------------------------------------------


    protected boolean onTouchEvent(MotionEvent event) {
        return true;
    }


    //Spy on our children lmao (don't try this at home)
    boolean dragSlop;
    float startY;
    protected MotionEvent dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                dragSlop = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if(!getDragEnabled())
                    break;

                int touchSlop = dragHelper.getTouchSlop();
                int touchDiff = (int) Math.abs(ev.getY()-startY);

                //If we've confirmed a relatively straight vertical drag
                if((touchDiff > touchSlop) && !dragSlop) {
                    dragSlop = true;

                    //Stop things like a viewpager from stealing input
                    slideView.requestDisallowInterceptTouchEvent(true);

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
                    return fuckOff;
                }
                break;
            default:
                if(dragSlop) {
                    dragHelper.processTouchEvent(ev);
                }
        }

        return ev;
    }


    protected void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(slideView);
        }
    }



    public void addDragListener(SlideViewDragEventListener listener) {
        dragEventListeners.add(listener);
    }
    public void removeDragListener(SlideViewDragEventListener listener) {
        dragEventListeners.remove(listener);
    }

    protected void notifyListenersSliderClosed() {
        for (SlideViewDragEventListener l : dragEventListeners) {
            l.onCloseSlider();
        }
    }
    protected void notifyListenersSliderOpened() {
        for (SlideViewDragEventListener l : dragEventListeners) {
            l.onOpenSlider();
        }
    }

    public interface SlideViewDragEventListener {
        //SlideView drag systems will auto-hide/show the slider based on certain drag inputs
        void onCloseSlider();
        void onOpenSlider();
    }
}
