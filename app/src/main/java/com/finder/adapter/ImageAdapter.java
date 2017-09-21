package com.finder.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.airport.finder.R;
import com.finder.view.CustomNavigationHeaderImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> mImageUrlArray;

    public ImageAdapter(Context context, final ArrayList<String> imageUrls) {
        this.mContext = context;
        this.mImageUrlArray = imageUrls;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int imagePosition = getImagePosition(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout = inflater
                .inflate(R.layout.layout_airport_details_viewpager, container, false);
        final ProgressBar progressBar = layout.findViewById(R.id.progressbar_airportdetails_viewpager);
        CustomNavigationHeaderImageView imageView =
                layout.findViewById(R.id.imageview_airportdetails_viewpager);
        Picasso.with(mContext)
                .load(mImageUrlArray.get(imagePosition))
                .error(R.drawable.plane_icon)
                .centerCrop()
                .fit()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
        container.addView(layout);
        return layout;
    }

    private int getImagePosition(int currentPosition) {
        return currentPosition % mImageUrlArray.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }
}