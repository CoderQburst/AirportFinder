package com.finder.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class CustomNavigationHeaderImageView extends AppCompatImageView {
    public CustomNavigationHeaderImageView(Context context) {
        super(context);
    }

    public CustomNavigationHeaderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNavigationHeaderImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.AT_MOST);
        setMeasuredDimension(getMeasuredWidth(), (int) (heightMeasureSpec * 0.5));
    }
}