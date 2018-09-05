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
// manage 2 gpio
// improve few functions to be more generic (require to manage many GPIO)


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private LinearLayout linearLayout;
    private Button apiStatusButton;
    private Button manageGpio17Button;
    private Button manageGpio21Button;
    private TextView resultsTextView;
    private Snackbar snackbar;
    private String TAG = "APIRest";
    private String credentials = "foo:bar";
    private boolean gpioStatus;
    private boolean gpio17Status;
    private boolean gpio21Status;

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
        getGpioStatus(17);
        getGpioStatus(21);

        // api status button
        apiStatusButton = new Button(this);
        apiStatusButton.setText("Get API status");
        apiStatusButton.setOnClickListener(this);
        apiStatusButton.setId(R.id.bt_get_api_status);

        // gpio 17 button
        manageGpio17Button = new Button(this);
        manageGpio17Button.setText("Get GPIO 17 status");
        manageGpio17Button.setOnClickListener(this);
        manageGpio17Button.setId(R.id.bt_manage_gpio17);

        // gpio 21 button
        manageGpio21Button = new Button(this);
        manageGpio21Button.setText("Get GPIO 21 status");
        manageGpio21Button.setOnClickListener(this);
        manageGpio21Button.setId(R.id.bt_manage_gpio21);

        resultsTextView = new TextView(this);

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(apiStatusButton);
        linearLayout.addView(manageGpio17Button);
        linearLayout.addView(manageGpio21Button);
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

            case R.id.bt_manage_gpio17:
                // manage possible actions on this button
                if (gpio17Status) {
                    // gpio status is true, so led is ON - user want to switch off
                    switchOff(17);
                } else {
                    // gpio status is false, so led is OFF - user want to switch on
                    switchOn(17);
                }
                break;

            case R.id.bt_manage_gpio21:
                // manage possible actions on this button
                if (gpio21Status) {
                    // gpio status is true, so led is ON - user want to switch off
                    switchOff(21);
                } else {
                    // gpio status is false, so led is OFF - user want to switch on
                    switchOn(21);
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

    public void getGpioStatus(final int gpioNb){
        // get the gpio <gpioNb> status - if true the led is ON, if false the led is OFF
        Log.i(TAG, "getGpioStatus has been called");
        url = "http://192.168.1.29:8088/admin/gpiostatus/"+gpioNb;
        // string request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // should return true or false
                        gpioStatus = Boolean.parseBoolean(response);
                        Log.i(TAG, "getGpioStatus - onResponse: " + response);
                        switch (gpioNb) {
                            case 17:
                                gpio17Status = gpioStatus;
                                if (gpio17Status) {
                                    manageGpio17Button.setBackgroundColor(Color.YELLOW);
                                    manageGpio17Button.setText("gpio17: put OFF");
                                } else {
                                    manageGpio17Button.setBackgroundColor(Color.BLUE);
                                    manageGpio17Button.setText("gpio17: put ON");
                                }
                                break;

                            case 21:
                                gpio21Status = gpioStatus;
                                if (gpio21Status) {
                                    manageGpio21Button.setBackgroundColor(Color.YELLOW);
                                    manageGpio21Button.setText("gpio21: put OFF");
                                } else {
                                    manageGpio21Button.setBackgroundColor(Color.BLUE);
                                    manageGpio21Button.setText("gpio21: put ON");
                                }
                                break;
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

    public void switchOn(final int gpioNb) {
        // send a request to the raspberry to switch on a led (on GPIO 17)
        Log.i(TAG, "Switch on GPIO button clicked");
        url = "http://192.168.1.29:8088/admin/switchongpio/"+gpioNb;
        // string request
        StringRequest switchonRequest = new StringRequest(Request.Method.PUT , url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // we consider that gpio (17) has been set to high level (led is ON)
                        Log.i(TAG, "switchOn - onResponse: " + response);
                        switch (gpioNb) {
                            case 17:
                                gpio17Status = true;
                                manageGpio17Button.setBackgroundColor(Color.YELLOW);
                                manageGpio17Button.setText("gpio17: put OFF");
                                resultsTextView.setText(response);
                                break;
                            case 21:
                                gpio21Status = true;
                                manageGpio21Button.setBackgroundColor(Color.YELLOW);
                                manageGpio21Button.setText("gpio21: put OFF");
                                resultsTextView.setText(response);
                                break;
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
        queue.add(switchonRequest);
    }

    public void switchOff(final int gpioNb){
        // send a request to the raspberry to switch off a led (on GPIO 17)
        Log.i(TAG, "Switch off GPIO button clicked");
        url = "http://192.168.1.29:8088/admin/switchoffgpio/"+gpioNb;
        // string request
        StringRequest switchoffRequest = new StringRequest(Request.Method.PUT , url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // we consider that gpio (17) has been set to low level (led is OFF)
                        Log.i(TAG, "switchOff - onResponse: " + response);
                        switch (gpioNb) {
                            case 17:
                                gpio17Status = false;
                                manageGpio17Button.setBackgroundColor(Color.BLUE);
                                manageGpio17Button.setText("gpio17: put ON");
                                resultsTextView.setText(response);
                                break;

                            case 21:
                                gpio21Status = false;
                                manageGpio21Button.setBackgroundColor(Color.BLUE);
                                manageGpio21Button.setText("gpio21: put ON");
                                resultsTextView.setText(response);
                                break;
                        }
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