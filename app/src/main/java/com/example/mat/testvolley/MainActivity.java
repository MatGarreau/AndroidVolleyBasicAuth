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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
* this code is based on : https://www.supinfo.com/articles/single/2592-android-faire-requetes-http-simplement
 */

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, Response.Listener<String>, Response.ErrorListener {

    private LinearLayout linearLayout;
    private Button apiStatusButton;
    private Button gpioStatusButton;
    private Button switchonGpioButton;
    private Button switchoffGpioButton;
    private TextView resultsTextView;
    private Snackbar snackbar;

    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeView();
        setContentView(linearLayout);
        snackbar = Snackbar.make(linearLayout, "Requête en cours d'exécution",
                Snackbar.LENGTH_INDEFINITE);
    }

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
        switchonGpioButton.setText("Get GPIO status");
        switchonGpioButton.setOnClickListener(this);
        switchonGpioButton.setId(R.id.bt_switchon_gpio);

        // switch off gpio button
        switchoffGpioButton = new Button(this);
        switchoffGpioButton.setText("Get GPIO status");
        switchoffGpioButton.setOnClickListener(this);
        switchoffGpioButton.setId(R.id.bt_switchoff_gpio);

        resultsTextView = new TextView(this);

        linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(apiStatusButton);
        linearLayout.addView(gpioStatusButton);
        linearLayout.addView(resultsTextView);
    }

    @Override
    public void onClick(View view) {
        if (!isConnected()) {
            Snackbar.make(view, "Aucune connexion à internet.", Snackbar.LENGTH_LONG).show();
            return;
        }
        snackbar.show();

        String url;
        // String url = "http://httpbin.org/ip";
        RequestQueue queue = Volley.newRequestQueue(this);

        switch (view.getId()) {
            case R.id.bt_get_api_status:
                // do some stuff on getApiStatus button clicked
                Log.i("API", "API get status button clicked");
                url = "http://192.168.1.29:8088/status";
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        this, this);
                queue.add(request);
                break;

            case R.id.bt_get_gpio_status:
                // do some stuff on getGpioStatus button clicked
                Log.i("API", "GPIO get status button clicked");
                url = "http://192.168.1.29:8088/admin/gpiostatus/17";
                // string request
                StringRequest stringRequest = new StringRequest(Request.Method.GET , url, this, this
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                resultsTextView.setText(response.toString());
//                                snackbar.dismiss();
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                resultsTextView.setText("Erreur:"+error);
//                                Log.i("GPIO", "onErrorResponse: "+ error);
//                                snackbar.dismiss();
//                            }
//                        }
                )
                {
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