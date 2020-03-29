package com.example.mat.testvolley;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
// improve layout management

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private String credentials = "foo:bar";
    private String TAG = "APIRest";
    private RelativeLayout relativeLayout;
    private LinearLayout linearLayout;
    private LinearLayout gpio17Layout;
    private LinearLayout gpio21Layout;
    private LinearLayout gpio27Layout;
    private Button apiStatusButton;
    private ImageButton gpio17Button;
    private ImageButton gpio21Button;
    private ImageButton gpio27Button;
    private TextView gpio17Label;
    private TextView gpio21Label;
    private TextView gpio27Label;
    private TextView resultsTextView;
    private boolean gpioStatus;
    private boolean gpio17Status;
    private boolean gpio21Status;
    private boolean gpio27Status;
    private Snackbar snackbar;

    private String url;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
       // gpio17Status = getGpioStatus(17);
        makeView();
        setContentView(relativeLayout);
    }

    // this method generate the view : four buttons and a textView to display information (request response)
    private void makeView() {
        // check gpio status to set background color of the manageGpioButton
        getGpioStatus(17);
        getGpioStatus(21);
        getGpioStatus(27);

        // api status button
        apiStatusButton = new Button(this);
        apiStatusButton.setText("Get API status");
        apiStatusButton.setOnClickListener(this);
        apiStatusButton.setId(R.id.bt_get_api_status);

        // gpio 17 button
        gpio17Button = new ImageButton(this);
        gpio17Button.setBackgroundResource(R.drawable.bulbonmicro);
        gpio17Button.setOnClickListener(this);
        gpio17Button.setId(R.id.bt_manage_gpio17);
        // gpio 17 label
        gpio17Label = new TextView(this);
        gpio17Label.setText("gpio 17");
        // linear layout gpio17
        gpio17Layout = new LinearLayout(this);
        gpio17Layout.setOrientation(LinearLayout.HORIZONTAL);
        gpio17Layout.addView(gpio17Label);
        gpio17Layout.addView(gpio17Button);
        gpio17Layout.setVerticalGravity(Gravity.CENTER);


        // gpio 21 button
        gpio21Button = new ImageButton(this);
        gpio21Button.setBackgroundResource(R.drawable.bulbonmicro);
        gpio21Button.setOnClickListener(this);
        gpio21Button.setId(R.id.bt_manage_gpio21);
        // gpio 21 label
        gpio21Label = new TextView(this);
        gpio21Label.setText("gpio 21");
        // linear layout gpio 21
        gpio21Layout = new LinearLayout(this);
        gpio21Layout.setOrientation(LinearLayout.HORIZONTAL);
        gpio21Layout.addView(gpio21Label);
        gpio21Layout.addView(gpio21Button);
        gpio21Layout.setVerticalGravity(Gravity.CENTER);


        // gpio 27 button
        gpio27Button = new ImageButton(this);
        gpio27Button.setBackgroundResource(R.drawable.bulbonmicro);
        gpio27Button.setOnClickListener(this);
        gpio27Button.setId(R.id.bt_manage_gpio27);
        // gpio 27 label
        gpio27Label = new TextView(this);
        gpio27Label.setText("gpio 27");
        // linear layout gpio 27
        gpio27Layout = new LinearLayout(this);
        gpio27Layout.setOrientation(LinearLayout.HORIZONTAL);
        gpio27Layout.addView(gpio27Label);
        gpio27Layout.addView(gpio27Button);
        gpio27Layout.setVerticalGravity(Gravity.CENTER);



        // text view to display server response
        resultsTextView = new TextView(this);
        resultsTextView.setId(R.id.tv_message);

        // this is the parent layout. It contains linearLayout and resultsTextView
        relativeLayout = new RelativeLayout(this);

        // this layout contains the GPIOs buttons
        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(apiStatusButton);
        linearLayout.addView(gpio17Layout);
        linearLayout.addView(gpio21Layout);
        linearLayout.addView(gpio27Layout);

        // add linearLayout (buttons) and text view to parent layout
        relativeLayout.addView(linearLayout);
        relativeLayout.addView(resultsTextView);

        RelativeLayout.LayoutParams tvParam = (RelativeLayout.LayoutParams) resultsTextView.getLayoutParams();
        tvParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        resultsTextView.setLayoutParams(tvParam);

        RelativeLayout.LayoutParams llParam = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        llParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearLayout.setLayoutParams(llParam);
    }

    @Override
    public void onClick(View view) {
        if (!isConnected()) {
            snackbar.make(view, "Internet access is not available!", snackbar.LENGTH_LONG).show();
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

            case R.id.bt_manage_gpio27:
                // manage possible actions on this button - orders are reversed because of use of relay
                if (gpio27Status) {
                    // gpio status is true, so led is OFF - user want to switch on
                    switchOff(27); // light on the light bulb
                } else {
                    // gpio status is false, so led is ON - user want to switch off
                    switchOn(27); // light off the light bulb
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
        // url must be updated each time your raspberry got a new @IP !
        url = "http://192.168.1.27:8088/status";
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
        url = "http://192.168.1.27:8088/admin/gpiostatus/"+gpioNb;
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
                                    gpio17Button.setBackgroundResource(R.drawable.bulbonmicro);
                                } else {
                                    gpio17Button.setBackgroundResource(R.drawable.bulboffmicro);
                                }
                                break;

                            case 21:
                                gpio21Status = gpioStatus;
                                if (gpio21Status) {
                                    gpio21Button.setBackgroundResource(R.drawable.bulbonmicro);
                                } else {
                                    gpio21Button.setBackgroundResource(R.drawable.bulboffmicro);
                                }
                                break;

                            case 27:
                                gpio27Status = gpioStatus;
                                if (gpio27Status) {
                                    gpio27Button.setBackgroundResource(R.drawable.bulboffmicro);
                                } else {
                                    gpio27Button.setBackgroundResource(R.drawable.bulbonmicro);
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
        url = "http://192.168.1.27:8088/admin/switchongpio/"+gpioNb;
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
                                gpio17Button.setBackgroundResource(R.drawable.bulbonmicro);
                                resultsTextView.setText(response);
                                break;

                            case 21:
                                gpio21Status = true;
                                gpio21Button.setBackgroundResource(R.drawable.bulbonmicro);
                                resultsTextView.setText(response);
                                break;

                            case 27:
                                gpio27Status = true;
                                gpio27Button.setBackgroundResource(R.drawable.bulboffmicro);
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
        url = "http://192.168.1.27:8088/admin/switchoffgpio/"+gpioNb;
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
                                gpio17Button.setBackgroundResource(R.drawable.bulboffmicro);
                                resultsTextView.setText(response);
                                break;

                            case 21:
                                gpio21Status = false;
                                gpio21Button.setBackgroundResource(R.drawable.bulboffmicro);
                                resultsTextView.setText(response);
                                break;

                            case 27:
                                gpio27Status = false;
                                gpio27Button.setBackgroundResource(R.drawable.bulbonmicro);
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