package com.finder.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.airport.finder.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.finder.values.Constants.EMAIL;
import static com.finder.values.Constants.JSON_EXCEPTION;
import static com.finder.values.Constants.MY_PERMISSIONS_REQUEST_CALL_PHONE;
import static com.finder.values.Constants.NAME;
import static com.finder.values.Constants.PROFILE_PICTURE;
import static com.finder.values.Constants.STATUS;
import static com.finder.values.Constants.STATUS_OK;
import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;

public class BaseActivity extends AppCompatActivity {

    public boolean mGoingToExitApp = false;
    protected boolean mNeedToCheckNetStatus = false;
    protected AlertDialog mAlert;

    protected void init() {
    }

    public void loadActivityComponents(String dummyString) {
    }

    protected boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnected() && netInfo.isAvailable());
    }

    protected void goToHomeActivity(String email, String userName, String profilePicUrl) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(EMAIL, email);
        intent.putExtra(NAME, userName);
        intent.putExtra(PROFILE_PICTURE, profilePicUrl);
        finish();
        startActivity(intent);
    }

    protected ArrayList<String> parseJsonObject(JSONObject object, String... params) {
        ArrayList<String> response = new ArrayList<>();
        for (String param : params) {
            try {
                response.add(object.getString(param));
            } catch (JSONException e) {
                Log.e(e.getClass().getName(), JSON_EXCEPTION, e.getCause());
                response.add(null);
            }
        }
        return response;
    }

    protected void parseJsonArray(JSONArray menu, ArrayList<String> menuData) {
        try {
            for (int counter = 0; counter < menu.length(); counter++) {
                menuData.add(menu.getString(counter));
            }
        } catch (JSONException e) {
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e.getCause());
        }
    }

    // Creates an alert message.
    public void createAlert(String message) {
        final AlertDialog.Builder errorAlert = new AlertDialog.Builder(this);
        setAlertButton(errorAlert);
        errorAlert.setMessage(message);
        errorAlert.setCancelable(false);
        if (mAlert == null) displayAlert(errorAlert);
        else if (!mAlert.isShowing() || mNeedToCheckNetStatus) {
            displayAlert(errorAlert);
        }
    }

    // Displays the alert
    private void displayAlert(AlertDialog.Builder errorAlert) {
        mAlert = errorAlert.create();
        mAlert.show();
    }

    private void setAlertButton(AlertDialog.Builder errorAlert) {
        if (mNeedToCheckNetStatus) {
            errorAlert.setPositiveButton(getResources().getString(R.string.ok)
                    , (DialogInterface dialogInterface, int which) ->
                            checkNetConnection(BaseActivity.this));
            errorAlert.setNegativeButton(getResources().getString(R.string.exit)
                    , (DialogInterface dialogInterface, int which) ->
                            BaseActivity.this.finish());
        } else if (mGoingToExitApp) {
            errorAlert.setPositiveButton(getResources().getString(R.string.exit),
                    (DialogInterface dialogInterface, int which) ->
                            BaseActivity.this.finish());
            errorAlert.setNegativeButton(getResources().getString(R.string.cancel)
                    , (DialogInterface dialogInterface, int which) ->
                            mGoingToExitApp = false);
        } else {
            errorAlert.setPositiveButton(getResources().getString(R.string.ok), null);
        }
    }

    public void displaySnackBar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        setSnackBarActionButton(snackbar);
        snackbar.show();
    }

    private void setSnackBarActionButton(Snackbar snackbar) {
        if (mGoingToExitApp) {
            snackbar.setAction(getResources().getString(R.string.exit), (View view) ->
                    BaseActivity.this.finish());
        }
    }

    // Check status of response from api.
    protected boolean checkStatus(String apiResponseString) {
        int status;
        JSONObject jsonObject;

        if (apiResponseString != null) {
            try {
                jsonObject = new JSONObject(apiResponseString);
                status = Integer.parseInt(jsonObject.getString(STATUS));
                if (status == STATUS_OK) {
                    return true;
                } else if (status == HTTP_CLIENT_TIMEOUT) {
                    createAlert(getResources().getString(R.string.requestTimeOut));
                }
            } catch (JSONException e) {
                displaySnackBar(findViewById(android.R.id.content)
                        , getResources().getString(R.string.dataError));
                Log.e(e.getClass().getName(), JSON_EXCEPTION, e);
            }
        } else {
            mNeedToCheckNetStatus = true;
            createAlert(getResources().getString(R.string.netError));
        }
        return false;
    }

    protected void checkNetConnection(BaseActivity currentActivity) {
        if (isOnline()) {
            mNeedToCheckNetStatus = false;
            currentActivity.init();
        } else {
            createAlert(getResources().getString(R.string.netError));
        }
    }

    // Makes a call with the available phone number.
    public void callAirport(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phoneNumber));
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {
                createAlert(getResources().getString(R.string.requestCallPermission));
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        } else {
            startActivity(callIntent);
        }
    }
}