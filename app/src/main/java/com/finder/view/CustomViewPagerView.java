package com.finder.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import java.lang.reflect.Field;

import static com.finder.data.Constants.ILLEGAL_ACCESS_EXCEPTION;
import static com.finder.data.Constants.NO_SUCH_FIELD_EXCEPTION;
import static com.finder.data.Constants.SCROLLER;

public class CustomViewPagerView extends ViewPager {

    public CustomViewPagerView(Context context) {
        super(context);
        postInitViewpager();
    }

    public CustomViewPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewpager();
    }

    private void postInitViewpager() {
        CustomScroller customScroller;
        try {
            Field scroller = ViewPager.class.getDeclaredField(SCROLLER);
            scroller.setAccessible(true);
            customScroller = new CustomScroller(getContext(),
                    new DecelerateInterpolator(2));
            scroller.set(this, customScroller);
        } catch (IllegalAccessException e) {
            Log.e(e.getClass().getName(), ILLEGAL_ACCESS_EXCEPTION, e);
        } catch (NoSuchFieldException e) {
            Log.e(e.getClass().getName(), NO_SUCH_FIELD_EXCEPTION, e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.AT_MOST);
        setMeasuredDimension(getMeasuredWidth(), (int) (heightMeasureSpec * 0.5));
    }
}