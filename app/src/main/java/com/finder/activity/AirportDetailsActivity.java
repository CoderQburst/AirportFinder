package com.finder.activity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airport.finder.R;
import com.finder.adapter.ImageAdapter;
import com.finder.network.NetworkApiCaller;
import com.finder.transformer.ZoomOutPageTransformer;
import com.finder.data.AirportDetails;
import com.finder.view.CustomViewPagerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;
import static com.finder.data.Constants.ADDRESS;
import static com.finder.data.Constants.AIRPORT_DETAILS_URL_ARRAY;
import static com.finder.data.Constants.DATA;
import static com.finder.data.Constants.DETAIL;
import static com.finder.data.Constants.GET;
import static com.finder.data.Constants.HOST_URL;
import static com.finder.data.Constants.ID;
import static com.finder.data.Constants.ILLEGAL_ACCESS_EXCEPTION;
import static com.finder.data.Constants.IMAGES;
import static com.finder.data.Constants.JSON_EXCEPTION;
import static com.finder.data.Constants.MY_PERMISSIONS_REQUEST_CALL_PHONE;
import static com.finder.data.Constants.NAME;
import static com.finder.data.Constants.NO_SUCH_FIELD_EXCEPTION;
import static com.finder.data.Constants.ON_CLICK_DELAY;
import static com.finder.data.Constants.PHONE_NUMBER;
import static com.finder.data.Constants.TELEPHONE_PREFIX;
import static com.finder.data.Constants.VIEWPAGER_CURRENT_ITEM;
import static com.finder.data.Constants.VIEWPAGER_DELAY;

public class AirportDetailsActivity extends BaseActivity {

    private ArrayList<String> mImageUrlArray = new ArrayList<>();
    private CustomViewPagerView mViewPager;
    // Initializes a runnable to update the viewpager
    private final Runnable mUpdate = () ->
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);

    private Handler mViewPagerHandler;
    private AirportDetails mAirportDetails = new AirportDetails();
    private long mLastClickTime;
    private RadioGroup mViewpagerIndicatorRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_details);

        init();
    }

    // Call api to get data if device is online.
    protected void init() {
        int id;

        id = Integer.parseInt(getIntent().getStringExtra(ID));
        if (isOnline()) {
            NetworkApiCaller mNetworkApiCaller = new NetworkApiCaller(this);
            mNetworkApiCaller.execute(HOST_URL +
                    AIRPORT_DETAILS_URL_ARRAY[id], GET);
        } else {
            mNeedToCheckNetStatus = true;
            createAlert(getResources().getString(R.string.netError));
        }
    }

    @Override
    public void loadActivityComponents(String apiResponse) {
        if (checkStatus(apiResponse)) {
            FloatingActionButton callButton =
                    (FloatingActionButton) findViewById(R.id.fab_airportdetails_call);
            ScrollView detailsScrollView = (ScrollView)
                    findViewById(R.id.scrollView_airportdetails);
            TextView airportNameTextView = (TextView)
                    findViewById(R.id.textView_airport_details_airportname);
            TextView airportDetailsTextView = (TextView)
                    findViewById(R.id.textView_airportdetails_airportdata);
            TextView airportAddressHeadingTextView = (TextView)
                    findViewById(R.id.textView_airportdetails_addressheading);
            TextView airportAddressTextView = (TextView)
                    findViewById(R.id.textView_airportdetails_address);
            TextView airportPhoneHeadingTextView = (TextView)
                    findViewById(R.id.textView_airportdetails_phonenumberheading);
            TextView airportPhoneNumberTextView = (TextView)
                    findViewById(R.id.textView_airportdetails_phonenumber);
            Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar_airportdetails);

            actionBar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(actionBar);
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Loads data into various views in activity.
            if (getData(apiResponse)) {
                checkDataAvailable(airportNameTextView, mAirportDetails.getName());
                checkDataAvailable(airportDetailsTextView, mAirportDetails.getDetail());
                setVisibility(airportAddressHeadingTextView, mAirportDetails.getAddress());
                checkDataAvailable(airportAddressTextView, mAirportDetails.getAddress());
                setVisibility(airportPhoneHeadingTextView, mAirportDetails.getPhoneNo());
                checkDataAvailable(airportPhoneNumberTextView, mAirportDetails.getPhoneNo());
                toggleFabVisibility(detailsScrollView, callButton);
                setViewPager();
            } else {
                mGoingToExitApp = true;
                createAlert(getResources().getString(R.string.dataError));
            }
        }
    }

    // Sets components as invisible if no data is present
    private void checkDataAvailable(TextView textView, String string) {
        if (string != null) textView.setText(string);
        else {
            textView.setVisibility(View.GONE);
            createAlert(getResources().getString(R.string.dataMissing));
        }
    }

    private void setVisibility(TextView textView, String string) {
        if (string == null) textView.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                if (grantResults.length > 0 ||
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callAirport(TELEPHONE_PREFIX + mAirportDetails.getPhoneNo());
                }
            }
        }
    }

    // Parses the API response to obtain data and returns true if there is no error.
    private boolean getData(String apiResponse) {
        ArrayList<String> details;
        try {
            JSONObject jsonObject = new JSONObject(apiResponse);
            JSONObject data = jsonObject.getJSONObject(DATA);
            details = parseJsonObject(data, NAME, DETAIL, ADDRESS, PHONE_NUMBER);
            if (details != null) mAirportDetails.putData(details);
            getImageUrl(data);
            return true;
        } catch (JSONException e) {
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e);
            return false;
        }
    }

    // Parses data and stores the url of images in ViewPager into an arrayList.
    private void getImageUrl(JSONObject data) {
        try {
            JSONArray menu = data.getJSONArray(IMAGES);
            parseJsonArray(menu, mImageUrlArray);
        } catch (JSONException e) {
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e);
        }
    }

    // Toggles floating button visibility on touching Scrollview.
    private void toggleFabVisibility(ScrollView view, final FloatingActionButton fab) {
        view.setOnTouchListener((View v, MotionEvent motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        fab.setVisibility(View.INVISIBLE);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        fab.setVisibility(View.VISIBLE);
                    }
                    return false;
                }
        );
    }

    // Uses reflection to set the initial page of view pager at mid-way mark.
    private void setViewPager() {
        mViewPager = (CustomViewPagerView) findViewById(R.id.viewPager_airportdetails);
        mViewPagerHandler = new Handler();
        mViewpagerIndicatorRadioGroup = (RadioGroup)
                findViewById(R.id.radioGroup_airportdetails);
        // Displays viewpager if there is data in mImageUrlArray.
        if (mImageUrlArray.size() != 0) {
            try {
                Field field = ViewPager.class.getDeclaredField(VIEWPAGER_CURRENT_ITEM);
                field.setAccessible(true);
                field.set(mViewPager, setInitialPosition());
            } catch (IllegalAccessException e) {
                Log.e(e.getClass().getName(), ILLEGAL_ACCESS_EXCEPTION, e);
            } catch (NoSuchFieldException e) {
                Log.e(e.getClass().getName(), NO_SUCH_FIELD_EXCEPTION, e);
            }
            mViewPager.setAdapter(new ImageAdapter(this, mImageUrlArray));
            mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
            setViewPagerIndicator();
            autoScrollViewpager();
            toggleAutoScroll();
            startViewPagerScroll();
        } else mViewPager.setVisibility(View.GONE);
    }

    // Calculates the initial position of viewpager
    private int setInitialPosition() {
        return (Integer.MAX_VALUE / (2 * mImageUrlArray.size())) * mImageUrlArray.size();
    }

    // Starts the first slide.
    private void startViewPagerScroll() {
        mViewPagerHandler.postDelayed(mUpdate, VIEWPAGER_DELAY);
    }

    // Overrides ViewPagerPageChangeListener to enable auto-scroll
    // and set the correct ViewPagerIndicator.
    private void autoScrollViewpager() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int buttonPosition = position % mImageUrlArray.size();
                ((RadioButton) mViewpagerIndicatorRadioGroup
                        .getChildAt(buttonPosition)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == SCROLL_STATE_IDLE) {
                    mViewPagerHandler.postDelayed(mUpdate, VIEWPAGER_DELAY);
                } else if (state == SCROLL_STATE_SETTLING) {
                    mViewPagerHandler.removeCallbacksAndMessages(null);
                }
            }
        });
    }

    // Toggles auto-scroll on touching Viewpager.
    private void toggleAutoScroll() {
        mViewPager.setOnTouchListener((View view, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mViewPagerHandler.removeCallbacksAndMessages(null);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mViewPagerHandler.postDelayed(mUpdate, VIEWPAGER_DELAY);
            }
            return false;
        });
    }

    // Adds  radio buttons to act as viewpager indicator.
    private void setViewPagerIndicator() {
        for (int counter = 0; counter < mImageUrlArray.size(); counter++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setClickable(false);
            mViewpagerIndicatorRadioGroup.addView(radioButton);
        }
        // Sets first radio button as checked initially.
        ((RadioButton) mViewpagerIndicatorRadioGroup
                .getChildAt(0)).setChecked(true);
    }

    // Places a call if phone number is stored in AirportDetails object.
    public void call(View view) {
        // Adds a delay of two seconds between each item click
        if (SystemClock.elapsedRealtime() - mLastClickTime > ON_CLICK_DELAY) {
            mLastClickTime = SystemClock.elapsedRealtime();
            if (mAirportDetails.getPhoneNo() != null) {
                callAirport(TELEPHONE_PREFIX + mAirportDetails.getPhoneNo());
            } else {
                createAlert(getResources().getString(R.string.netError));
            }
        }
    }
}