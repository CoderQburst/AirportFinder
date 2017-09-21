package com.finder.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.airport.finder.R;
import com.finder.network.NetworkApiCaller;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.finder.values.Constants.BUTTON_CLICK_DELAY;
import static com.finder.values.Constants.DATA;
import static com.finder.values.Constants.EMAIL;
import static com.finder.values.Constants.EXECUTION_EXCEPTION;
import static com.finder.values.Constants.FULL_NAME;
import static com.finder.values.Constants.GET;
import static com.finder.values.Constants.HOST_URL;
import static com.finder.values.Constants.INTERRUPTED_EXCEPTION;
import static com.finder.values.Constants.JSON_EXCEPTION;
import static com.finder.values.Constants.LOGIN_DETAILS_API_URL;
import static com.finder.values.Constants.PROFILE_PICTURE_URL;
import static com.finder.values.Constants.STORED_EMAILS;
import static com.finder.values.Constants.STORED_PASSWORDS;

public class LogInActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        init();
    }

    public void init() {
        final Handler buttonHandler = new Handler();
        final Button loginButton = (Button) findViewById(R.id.button_login);
        final Runnable setClickable = () -> loginButton.setClickable(true);
        loginButton.setOnClickListener((View view) -> {
            loginButton.setClickable(false);
            buttonHandler.postDelayed(setClickable, BUTTON_CLICK_DELAY);
            validateUser();
        });
    }

    private void validateUser() {
        final EditText emailTextView = (EditText) findViewById(R.id.edittext_login_email);
        final EditText passwordTextView = (EditText) findViewById(R.id.edittext_login_password);

        // Adds a delay of two seconds between each button click
        if (isValidEmail(emailTextView.getText().toString())) {
            for (int counter = 0; counter < STORED_EMAILS.length; counter++) {
                if (emailTextView.getText().toString().equals(STORED_EMAILS[counter]) &&
                        passwordTextView.getText().toString()
                                .equals(STORED_PASSWORDS[counter])) {
                    if (isOnline()) {
                        getUserDetails(HOST_URL + LOGIN_DETAILS_API_URL[counter]);
                        break;
                    } else {
                        createAlert(getResources().getString(R.string.netError));
                    }
                } else if (counter == STORED_EMAILS.length - 1) {
                    createAlert(getResources().getString(R.string.loginError));
                }
            }
        } else {
            createAlert(getResources().getString(R.string.emailError));
        }

    }

    private boolean isValidEmail(String target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Gets user details from api
    private void getUserDetails(String url) {
        ArrayList<String> userData;
        JSONObject responseData;
        String response;

        NetworkApiCaller mNetworkApiCaller = new NetworkApiCaller(this);
        try {
            response = mNetworkApiCaller.execute(url, GET).get();
            if (checkStatus(response)) {
                responseData = new JSONObject(response);
                JSONObject data = responseData.getJSONObject(DATA);
                userData = parseJsonObject(data, EMAIL, FULL_NAME, PROFILE_PICTURE_URL);
                // Throws jsonException if there is error in parsing
                if (userData.get(0) != null && userData.get(1) != null && userData.get(2) != null)
                    goToHomeActivity(userData.get(0), userData.get(1), userData.get(2));
                else throw new JSONException(null);
            }
        } catch (JSONException e) {
            displaySnackBar(findViewById(android.R.id.content)
                    , getResources().getString(R.string.dataError));
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e);
        } catch (InterruptedException e) {
            displaySnackBar(findViewById(android.R.id.content)
                    , getResources().getString(R.string.interruptedException));
            Log.e(e.getClass().getName(), INTERRUPTED_EXCEPTION, e);
        } catch (ExecutionException e) {
            displaySnackBar(findViewById(android.R.id.content)
                    , getResources().getString(R.string.executionException));
            Log.e(e.getClass().getName(), EXECUTION_EXCEPTION, e);
        }
    }
}