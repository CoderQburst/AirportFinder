package com.finder.view;

import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import static com.finder.values.Constants.SCROLL_FACTOR;

@SuppressWarnings("WeakerAccess")
public class CustomScroller extends Scroller {

    CustomScroller(Context context, DecelerateInterpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, (int) (duration * SCROLL_FACTOR));
    }
}