package com.finder.network;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.finder.activity.BaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.finder.data.Constants.ERROR;
import static com.finder.data.Constants.GET;
import static com.finder.data.Constants.IO_EXCEPTION;
import static com.finder.data.Constants.JSON_EXCEPTION;
import static com.finder.data.Constants.NETWORK_ERROR;
import static com.finder.data.Constants.POST;
import static com.finder.data.Constants.REQUEST_HEADING;
import static com.finder.data.Constants.REQUEST_HEADING_TYPE;
import static com.finder.data.Constants.STATUS;
import static com.finder.data.Constants.UTF_CHARSET;
import static java.net.HttpURLConnection.HTTP_OK;

public class NetworkApiCaller extends AsyncTask<String, Void, String> {

    private BaseActivity activity;
    private ProgressDialog mProgressDialog;

    public NetworkApiCaller(BaseActivity currActivity) {
        this.activity = currActivity;
        setProgressDialogue();
    }

    protected void onPreExecute() {
        mProgressDialog.show();
    }

    protected String doInBackground(String... params) {
        HttpURLConnection mUrlConnection;
        String response;
        URL mUrl;

        try {
            mUrl = new URL(params[0]);
            mUrlConnection = (HttpURLConnection) mUrl.openConnection();

            setRequestMethod(mUrlConnection, params);
            response = getResponse(mUrlConnection);
            return response;

        } catch (IOException e) {
            Log.e(e.getClass().getName(), IO_EXCEPTION, e);
        }
        return null;
    }

    protected void onPostExecute(String response) {
        mProgressDialog.dismiss();
        activity.loadActivityComponents(response);
    }

    private void setRequestMethod(HttpURLConnection mUrlConnection, String... params) {
        BufferedWriter mBufferedWriter;
        OutputStream mUrlConnectionOutputStream;

        try {
            mUrlConnection.setRequestProperty(REQUEST_HEADING, REQUEST_HEADING_TYPE);
            if (params[1].equals(POST)) {
                mUrlConnection.setRequestMethod(POST);
                mUrlConnection.setDoInput(true);
                mUrlConnectionOutputStream = mUrlConnection.getOutputStream();
                mBufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(mUrlConnectionOutputStream, UTF_CHARSET));
                mBufferedWriter.write(params[2]);
                mBufferedWriter.flush();
                mBufferedWriter.close();
                mUrlConnectionOutputStream.close();
            } else if (params[1].equals(GET)) {
                mUrlConnection.setRequestMethod(GET);
            }
        } catch (IOException e) {
            Log.e(e.getClass().getName(), IO_EXCEPTION, e);
        }
    }

    private String getResponse(HttpURLConnection mUrlConnection) {
        BufferedReader mBufferedReader;
        int statusCode;
        String line;
        StringBuilder mStringBuilder;
        try {
            statusCode = mUrlConnection.getResponseCode();
            try {
                if (statusCode == HTTP_OK) {
                    mBufferedReader
                            = new BufferedReader(new InputStreamReader(mUrlConnection.getInputStream()));
                    mStringBuilder = new StringBuilder();
                    while ((line = mBufferedReader.readLine()) != null) {
                        mStringBuilder.append(line).append("\n");
                    }
                    mBufferedReader.close();
                    return mStringBuilder.toString();
                } else {
                    JSONObject response = new JSONObject();
                    response.put(STATUS, String.valueOf(statusCode));
                    response.put(ERROR, NETWORK_ERROR);
                    return response.toString();
                }
            } finally {
                mUrlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e(e.getClass().getName(), IO_EXCEPTION, e);
        } catch (JSONException e) {
            Log.e(e.getClass().getName(), JSON_EXCEPTION, e);
        }
        return null;
    }

    private void setProgressDialogue() {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setProgress(0);
    }
}