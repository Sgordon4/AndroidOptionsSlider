package com.sgordon4.slideview;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

public class SlideViewDragCallback extends ViewDragHelper.Callback {

    private final SlideView slideView;
    private final SlideDragHandler slideDragHandler;

    public SlideViewDragCallback(SlideDragHandler slideDragHandler) {
        this.slideDragHandler = slideDragHandler;
        this.slideView = slideDragHandler.slideView;
    }

    @Override
    public boolean tryCaptureView(@NonNull View child, int pointerId) {
        return child == slideDragHandler.slideView.fullWrapper;
    }

    //We only care about stopping the bottom of the view from going above the bottom of the screen
    //Pulling the top below the top will be a gesture command later
    @Override
    public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
        int topBound, bottomBound;

        topBound = top;
        bottomBound = slideDragHandler.slideView.windowHeight - slideDragHandler.slideView.fullWrapper.getHeight();

        System.out.println("Top: "+top+", "+bottomBound);

        //Constrain between 0 and bottomBound
        if(top < bottomBound)
            return bottomBound;
        else if(top > 0)
            return 0;
        else return top;

        //return Math.max(topBound, bottomBound);
    }

    @Override
    public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
        super.onViewReleased(releasedChild, xvel, yvel);

        //Speed has priority over position
        final double SPEED_LIMIT = 1000;            //Not actually a limiter, just a metric

        //Where top of slider is right now
        int currentSliderTopPos = releasedChild.getTop() + releasedChild.getHeight() - slideDragHandler.slideView.sliderContentHeight;


        //Note: Height is 0 at top of screen, so when something is visibly above something else,
        // it is technically at a smaller height value. That makes the math below somewhat confusing.

        //When seeking to understand, the if statements below are in a very particular order
        // based on sliderTop pos from top of screen to bottom, and the slider would not
        // behave correctly if the order was changed.


        //Move slider based on position -------------------------------------------------------

        //If top of slider is less than the anchor point, but greater than its max (can still move farther)...
        if (currentSliderTopPos <= slideDragHandler.globalAnchorPos +50) {  // + a buffer for small sliders
            int bottomBound = slideView.windowHeight - releasedChild.getHeight();

            // Just fling the view, constrained between those two points
            slideDragHandler.dragHelper.flingCapturedView(0, bottomBound, 0, slideDragHandler.sliderAnchorPos);
            slideDragHandler.slideView.invalidate();
        }

        //If we're swiping down fast...
        else if (yvel > SPEED_LIMIT) {
            moveView(0);                                        //Move to slider-closed
            slideDragHandler.notifyListenersSliderClosed();
        }

        //(Unreachable if slider is too small)
        //If top of slider is less than window center...
        else if (currentSliderTopPos < slideView.windowHeight / 2) {
            moveView(slideDragHandler.sliderAnchorPos);                //Move to slider-open
            slideDragHandler.notifyListenersSliderOpened();
        }

        //If we're swiping up fast...
        else if (yvel < -SPEED_LIMIT) {
            moveView(slideDragHandler.sliderAnchorPos);                //Move to slider-open
            slideDragHandler.notifyListenersSliderOpened();
        }

        //If top of slider is greater than window center...
        else if (currentSliderTopPos > slideView.windowHeight / 2) {
            moveView(0);                                        //Move to slider-closed
            slideDragHandler.notifyListenersSliderClosed();
        }
    }

    @Override
    public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
        super.onViewPositionChanged(changedView, left, top, dx, dy);

        //Show slider based on dragWrapper position
        if (top <= -30 && !slideView.getSliderVisible())
            slideView.setSliderVisible(true);
        else if (top > -30 && slideView.getSliderVisible())
            slideView.setSliderVisible(false);

    }

    private void moveView(int endPos) {
        if (slideDragHandler.dragHelper.settleCapturedViewAt(0, endPos)) {
            ViewCompat.postInvalidateOnAnimation(slideView);
        }
    }
}
