package com.finder.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

import static com.finder.data.Constants.VIEWPAGER_TRANSFORMER_MIN_ALPHA;
import static com.finder.data.Constants.VIEWPAGER_TRANSFORMER_MIN_SCALE;

public class ZoomOutPageTransformer implements ViewPager.PageTransformer {

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) {
            // This page is way off-screen to the left.
            view.setAlpha(0);
        } else if (position <= 1) {
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(VIEWPAGER_TRANSFORMER_MIN_SCALE, 1 - Math.abs(position));
            float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
            float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horizontalMargin - verticalMargin / 2);
            } else {
                view.setTranslationX(-horizontalMargin + verticalMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(VIEWPAGER_TRANSFORMER_MIN_ALPHA +
                    (scaleFactor - VIEWPAGER_TRANSFORMER_MIN_ALPHA) /
                            (1 - VIEWPAGER_TRANSFORMER_MIN_ALPHA) *
                            (1 - VIEWPAGER_TRANSFORMER_MIN_ALPHA));
        } else {
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}