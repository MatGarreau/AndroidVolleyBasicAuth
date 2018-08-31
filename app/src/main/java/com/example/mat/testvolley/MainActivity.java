package com.example.mat.testvolley;

import android.content.Context;
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
        implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener {

    private LinearLayout linearLayout;
    private Button apiStatusButton;
    private Button gpioStatusButton;
    private Button switchonGpioButton;
    private Button switchoffGpioButton;
    private TextView resultsTextView;
    private Snackbar snackbar;
    private String TAG = "APIRest";

    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeView();
        setContentView(linearLayout);
        snackbar = Snackbar.make(linearLayout, "Request on going",
                Snackbar.LENGTH_INDEFINITE);
    }

    // this method generate the view : four buttons and a textView to display information (request response)
    private void makeView() {
        // api status button
        apiStatusButton = new Button(this);
        apiStatusButton.setText("Get API status");
        apiStatusButton.setOnClickListener(this);
        apiStatusButton.setId(R.id.bt_get_api_status);

        // gpio status button
        gpioStatusButton = new Button(this);
        gpioStatusButton.setText("Get GPIO status");
        gpioStatusButton.setOnClickListener(this);
        gpioStatusButton.setId(R.id.bt_get_gpio_status);

        // switch on gpio button
        switchonGpioButton = new Button(this);
        switchonGpioButton.setText("Switch ON");
        switchonGpioButton.setOnClickListener(this);
        switchonGpioButton.setId(R.id.bt_switchon_gpio);

        // switch off gpio button
        switchoffGpioButton = new Button(this);
        switchoffGpioButton.setText("Switch OFF");
        switchoffGpioButton.setOnClickListener(this);
        switchoffGpioButton.setId(R.id.bt_switchoff_gpio);

        resultsTextView = new TextView(this);

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(apiStatusButton);
        linearLayout.addView(gpioStatusButton);
        linearLayout.addView(switchonGpioButton);
        linearLayout.addView(switchoffGpioButton);
        linearLayout.addView(resultsTextView);
    }

    @Override
    public void onClick(View view) {
        if (!isConnected()) {
            Snackbar.make(view, "Internet access is not available!", Snackbar.LENGTH_LONG).show();
            return;
        }
        snackbar.show();

        String url;
        RequestQueue queue = Volley.newRequestQueue(this);

        switch (view.getId()) {
            case R.id.bt_get_api_status:
                // send a request to the Raspberry to know API Status (UP or DOWN)
                Log.i(TAG, "API get status button clicked");
                url = "http://192.168.1.29:8088/status";
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        this, this);
                queue.add(request);
                break;

            case R.id.bt_get_gpio_status:
                // get the gpio 17 status - if true the led is ON, if false the led is OFF
                Log.i(TAG, "GPIO get status button clicked");
                url = "http://192.168.1.29:8088/admin/gpiostatus/17";
                // string request
                StringRequest stringRequest = new StringRequest(Request.Method.GET , url, this, this)
                {
                    // this methods allows to set request headers for basic authentication
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String credentials = "foo:bar";
                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };
                queue.add(stringRequest);
                break;

            case R.id.bt_switchon_gpio:
                // send a request to the raspberry to switch on a led (on GPIO 17)
                Log.i(TAG, "Switch on GPIO button clicked");
                url = "http://192.168.1.29:8088/admin/switchongpio/17";
                // string request
                StringRequest switchonRequest = new StringRequest(Request.Method.PUT , url, this, this)
                {
                    // this methods allows to set request headers for basic authentication
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String credentials = "foo:bar";
                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };
                queue.add(switchonRequest);
                break;

            case R.id.bt_switchoff_gpio:
                // send a request to the raspberry to switch off a led (on GPIO 17)
                Log.i(TAG, "Switch off GPIO button clicked");
                url = "http://192.168.1.29:8088/admin/switchoffgpio/17";
                // string request
                StringRequest switchoffRequest = new StringRequest(Request.Method.PUT , url, this, this)
                {
                    // this methods allows to set request headers for basic authentication
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String credentials = "foo:bar";
                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };
                queue.add(switchoffRequest);
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

    @Override
    public void onResponse(String response) {
        resultsTextView.setText(response.toString());
        snackbar.dismiss();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        resultsTextView.setText(error.toString());
        snackbar.dismiss();
    }
}