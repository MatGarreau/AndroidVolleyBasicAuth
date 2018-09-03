package com.example.mat.testvolley;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/*
* @Author : Mathieu GARREAU
* @mail   : mat.garreau<at>gmail.com
* this code is based on : https://www.supinfo.com/articles/single/2592-android-faire-requetes-http-simplement
 */


// TODO :
// create a function getStatus
// manage only 1 button to switch on/off the led and change button color when led is on/off


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private LinearLayout linearLayout;
    private Button apiStatusButton;
    private Button manageGpioButton;
    private TextView resultsTextView;
    private Snackbar snackbar;
    private String TAG = "APIRest";
    private String credentials = "foo:bar";
    private boolean gpioStatus;
    private String url;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        makeView();
        setContentView(linearLayout);
    }

    // this method generate the view : four buttons and a textView to display information (request response)
    private void makeView() {
        // check gpio status to set background color of the manageGpioButton
        getGpioStatus();

        // api status button
        apiStatusButton = new Button(this);
        apiStatusButton.setText("Get API status");
        apiStatusButton.setOnClickListener(this);
        apiStatusButton.setId(R.id.bt_get_api_status);

        // gpio status button
        manageGpioButton = new Button(this);
        manageGpioButton.setText("Get GPIO status");
        manageGpioButton.setOnClickListener(this);
        manageGpioButton.setId(R.id.bt_manage_gpio);

        resultsTextView = new TextView(this);

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(apiStatusButton);
        linearLayout.addView(manageGpioButton);
        linearLayout.addView(resultsTextView);
    }

    @Override
    public void onClick(View view) {
        if (!isConnected()) {
            Snackbar.make(view, "Internet access is not available!", Snackbar.LENGTH_LONG).show();
            return;
        }

        switch (view.getId()) {
            case R.id.bt_get_api_status:
                // send a request to the Raspberry to know API Status (UP or DOWN)
                getApiStatus();
                break;

            case R.id.bt_manage_gpio:
                // manage possible actions on this button
                if (gpioStatus) {
                    // gpio status is true, so led is ON - user want to switch off
                    switchOff();
                } else {
                    // gpio status is false, so led is OFF - user want to switch on
                    switchOn();
                }
                break;

            default:
                break;
        }

    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void getApiStatus(){
        // send a request to the Raspberry to know API Status (UP or DOWN)
        Log.i(TAG, "API get status button clicked");
        url = "http://192.168.1.29:8088/status";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        resultsTextView.setText(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        resultsTextView.setText(error.toString());
                    }
                }
        );
        queue.add(request);
    }

    public void getGpioStatus(){
        // get the gpio 17 status - if true the led is ON, if false the led is OFF
        Log.i(TAG, "getGpioStatus has been called");
        url = "http://192.168.1.29:8088/admin/gpiostatus/17";
        // string request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // should return true or false
                        gpioStatus = Boolean.parseBoolean(response);
                        Log.i(TAG, "getGpioStatus - onResponse: " + response);
                        if (gpioStatus) {
                            manageGpioButton.setBackgroundColor(Color.YELLOW);
                            manageGpioButton.setText("put OFF");
                        } else {
                            manageGpioButton.setBackgroundColor(Color.BLUE);
                            manageGpioButton.setText("put ON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // do something...
                        Log.i(TAG, "getGpioStatus - onErrorResponse: " + error);
                    }
                })
        {
            // this methods allows to set request headers for basic authentication
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(stringRequest);
    }

    public void switchOn() {
        // send a request to the raspberry to switch on a led (on GPIO 17)
        Log.i(TAG, "Switch on GPIO button clicked");
        url = "http://192.168.1.29:8088/admin/switchongpio/17";
        // string request
        StringRequest switchonRequest = new StringRequest(Request.Method.PUT , url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // we consider that gpio (17) has been set to high level (led is ON)
                        gpioStatus = true;
                        manageGpioButton.setBackgroundColor(Color.YELLOW);
                        manageGpioButton.setText("put OFF");
                        resultsTextView.setText(response);
                        Log.i(TAG, "switchOn - onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // do something...
                        Log.i(TAG, "getGpioStatus - onErrorResponse: " + error);
                    }
                })
        {
            // this methods allows to set request headers for basic authentication
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(switchonRequest);
    }

    public void switchOff(){
        // send a request to the raspberry to switch off a led (on GPIO 17)
        Log.i(TAG, "Switch off GPIO button clicked");
        url = "http://192.168.1.29:8088/admin/switchoffgpio/17";
        // string request
        StringRequest switchoffRequest = new StringRequest(Request.Method.PUT , url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // we consider that gpio (17) has been set to low level (led is OFF)
                        gpioStatus = false;
                        manageGpioButton.setBackgroundColor(Color.BLUE);
                        manageGpioButton.setText("put ON");
                        resultsTextView.setText(response);
                        Log.i(TAG, "switchOff - onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // do something...
                        Log.i(TAG, "switchOff - onErrorResponse: " + error);
                    }
                })
        {
            // this methods allows to set request headers for basic authentication
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(switchoffRequest);
    }
}