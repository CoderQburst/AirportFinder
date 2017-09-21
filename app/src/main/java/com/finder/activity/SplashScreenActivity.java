package com.finder.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.airport.finder.R;

import static com.finder.data.Constants.GOOGLE_ACCOUNT;
import static com.finder.data.Constants.MY_PERMISSIONS_REQUEST_CONTACT_INFO;

public class SplashScreenActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getAccountPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CONTACT_INFO: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAccountDetails();
                } else {
                    finish();
                    startActivity(new Intent(this, LogInActivity.class));
                }
                break;
            }
        }
    }

    // Checks whether permission is granted to access contact information
    public void getAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    MY_PERMISSIONS_REQUEST_CONTACT_INFO);
        } else {
            getAccountDetails();
        }
    }

    // Checks whether user has a google login. If not move to Login activity, else move to Homepage
    public void getAccountDetails() {
        String email, userName;

        Account[] accounts = AccountManager.get(this).getAccountsByType(GOOGLE_ACCOUNT);
        // The first account saved in device is taken as login account
        if (accounts.length > 0) {
            email = accounts[0].name;
            userName = email.substring(0, email.indexOf("@"));
            goToHomeActivity(email, userName, null);
        } else {
            finish();
            startActivity(new Intent(this, LogInActivity.class));
        }
    }
}