package com.finder.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.airport.finder.R;
import com.finder.fragment.ContactsFragment;
import com.finder.fragment.MapViewFragment;
import com.finder.network.NetworkApiCaller;
import com.finder.data.Airports;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.finder.data.Constants.AIRPORT;
import static com.finder.data.Constants.DATA;
import static com.finder.data.Constants.DRAWABLE;
import static com.finder.data.Constants.EMAIL;
import static com.finder.data.Constants.GET;
import static com.finder.data.Constants.HOST_URL;
import static com.finder.data.Constants.ID;
import static com.finder.data.Constants.JSON_EXCEPTION;
import static com.finder.data.Constants.LATITUDE;
import static com.finder.data.Constants.LONGITUDE;
import static com.finder.data.Constants.MENU;
import static com.finder.data.Constants.MENU_DATA;
import static com.finder.data.Constants.NAME;
import static com.finder.data.Constants.PHONE_NUMBER;
import static com.finder.data.Constants.PROFILE_PICTURE;
import static com.finder.data.Constants.TELEPHONE_PREFIX;
import static com.finder.data.Constants.UI_DETAILS_API_URL;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapViewFragment.MapFragmentInteractionListener,
        ContactsFragment.ContactsFragmentInteractionListener {

    private ArrayList<Airports> mData = new ArrayList<>();
    private ArrayList<String> mMenuData = new ArrayList<>();
    private ArrayList<String> mMenuResource = new ArrayList<>();
    private String mEmail, mUserName, mProfilePic;

    // Need for future use
    public void onFragmentInteraction(Uri map) {
    }

    public void makeCallFromFragment(String phoneNo) {
        callAirport(TELEPHONE_PREFIX + phoneNo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    @Override
    public void init() {
        mEmail = getIntent().getStringExtra(EMAIL);
        mUserName = getIntent().getStringExtra(NAME);
        mProfilePic = getIntent().getStringExtra(PROFILE_PICTURE);

        if (isOnline()) {
            NetworkApiCaller mNetworkApiCaller = new NetworkApiCaller(this);
            mNetworkApiCaller.execute(HOST_URL + UI_DETAILS_API_URL, GET);
        } else {
            mNeedToCheckNetStatus = true;
            createAlert(getResources().getString(R.string.netError));
        }
    }

    @Override
    public void loadActivityComponents(String apiResponse) {
        if (checkStatus(apiResponse)) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_home);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout_home);
            NavigationView mNavigationView = (NavigationView)
                    findViewById(R.id.activity_home_navigationview);
            final View headerView = mNavigationView.getHeaderView(0);

            if (getData(apiResponse)) {
                setSupportActionBar(toolbar);
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        HomeActivity.this, drawer, toolbar, R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();

                setNavigationDrawerHeader(this, headerView);
                setNavigationDrawerMenu(mNavigationView);
                mNavigationView.setNavigationItemSelectedListener(HomeActivity.this);
                // Loads the first screen with map fragment
                displaySelectedScreen(0);
            } else {
                mGoingToExitApp = true;
                createAlert(getResources().getString(R.string.dataError));
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout_home);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            mGoingToExitApp = true;
            createAlert(getResources().getString(R.string.exitConfirm));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    // Gets data from the api response and stores them in the fields.
    private boolean getData(String apiResponse) {
        ArrayList<String> uiData;
        JSONArray menu;

        try {
            JSONObject mJsonResponse = new JSONObject(apiResponse);
            JSONObject jsonData = mJsonResponse.getJSONObject(DATA);
            if (jsonData == null) {
                return false;
            }
            parseJsonArray(jsonData.getJSONArray(MENU), mMenuData);
            parseJsonArray(jsonData.getJSONArray(MENU_DATA), mMenuResource);
            menu = jsonData.getJSONArray(AIRPORT);
            for (int counter = 0; counter < menu.length(); counter++) {
                Airports dataObject = new Airports();
                JSONObject airportData = menu.getJSONObject(counter);
                uiData = parseJsonObject(airportData, ID, NAME, PHONE_NUMBER, LATITUDE, LONGITUDE);
                if (uiData != null) {
                    dataObject.putData(uiData);
                    mData.add(dataObject);
                }
            }
            return true;
        } catch (JSONException e) {
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e.getCause());
            return false;
        }
    }

    private void setNavigationDrawerHeader(Context context, View headerView) {
        TextView userEmailField = headerView.findViewById(R.id.textview_navheader_useremail);
        TextView userNameField = headerView.findViewById(R.id.textview_navheader_username);
        final CircleImageView profileImage = headerView.findViewById(R.id.imageview_navheader);

        if (mEmail != null) userEmailField.setText(mEmail);
        if (mUserName != null) userNameField.setText(mUserName);
        setSize(headerView, profileImage);
        Picasso.with(context).load(mProfilePic).error(R.drawable.circle)
                .placeholder(R.drawable.circle).fit().into(profileImage);

    }

    // Calculates the size of the profile image
    public void setSize(final View view, final CircleImageView image) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    image.getLayoutParams().height = (int) (view.getHeight() * (0.5));
                }
            });
        }
    }

    // Adds items to the Navigation Drawer menu from api response.
    private void setNavigationDrawerMenu(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        if (mMenuData != null && mMenuResource != null) {
            for (int counter = 0; counter < mMenuData.size(); counter++) {
                menu.add(R.id.menu_navdrawer, counter, Menu.NONE, mMenuData.get(counter))
                        .setIcon(ResourcesCompat
                                .getDrawable(getResources(), getResources()
                                        .getIdentifier(mMenuResource.get(counter), DRAWABLE,
                                                getPackageName()), null));
            }
        }
    }

    // Inflates a fragment based on selected Navigation Item
    private void displaySelectedScreen(int id) {
        Fragment fragment;

        switch (id) {
            case 0:
                if (getSupportActionBar() != null) getSupportActionBar()
                        .setTitle(getResources().getString(R.string.home));
                fragment = searchFragment(MapViewFragment.sTag);
                if (fragment == null) {
                    if (mData != null) fragment = MapViewFragment.newInstance(mData, null);
                    else fragment = new MapViewFragment();
                }
                startFragmentTransaction(fragment, MapViewFragment.sTag);
                break;
            case 1:
                if (getSupportActionBar() != null) getSupportActionBar()
                        .setTitle(getResources().getString(R.string.contacts));
                fragment = searchFragment(ContactsFragment.sTag);
                if (fragment == null) {
                    if (mData != null) fragment = ContactsFragment.newInstance(mData);
                    else fragment = new ContactsFragment();
                }
                startFragmentTransaction(fragment, ContactsFragment.sTag);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout_home);
        drawer.closeDrawer(GravityCompat.START);
    }

    private Fragment searchFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    private void startFragmentTransaction(Fragment fragment, String tag) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.framelayout_home_content, fragment, tag);
            transaction.commit();
        }
    }

    // Interface to move camera to marker position.
    public void moveCameraToMarker(String markerTitle) {
        MapViewFragment fragment = (MapViewFragment) searchFragment(MapViewFragment.sTag);
        if (fragment != null) {
            fragment.searchForMarker(markerTitle);
        } else {
            if (getSupportActionBar() != null) getSupportActionBar()
                    .setTitle(getResources().getString(R.string.home));
            Fragment newFragment = MapViewFragment.newInstance(mData, markerTitle);
            startFragmentTransaction(newFragment, MapViewFragment.sTag);
        }
    }
}