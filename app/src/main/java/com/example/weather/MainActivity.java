package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weather.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    ActivityMainBinding binding;
    FusedLocationProviderClient fusedLocationProviderClient;
    String weather_url = "";
    LocationManager locationManager;
    String apiKey = "030314b750cc43e7b39e503dfe37150c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        binding.pb.setVisibility(View.GONE);

        binding.temp.setVisibility(View.GONE);
        binding.textView.setVisibility(View.GONE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }


            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.e("lat", weather_url);
        binding.btVar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.temp.setVisibility(View.VISIBLE);
                Log.e("lat", "onClick");
                accessLoc();
            }
        });
        binding.temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.pb.setVisibility(View.VISIBLE);
                accessLoc();
                accessLoc();
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void accessLoc() {

        Log.e("lat", "function");
        // get the last location
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                weather_url = "https://api.weatherbit.io/v2.0/current?" + "lat=" + (location != null ? location.getLatitude() : null) + "&lon=" + (location != null ? location.getLongitude() : null) + "&key=" + apiKey;
                Log.e("lat", weather_url.toString());
            }
        });

        getTemp();

    }

    private void getTemp() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = weather_url;
        Log.e("lat", url);

        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                (Response.Listener<String>) response -> {
                    Log.e("lat", response.toString());

                    try {
                        JSONObject obj = new JSONObject((String) response);
                        JSONArray arr = obj.getJSONArray("data");
                        Log.e("lat obj1", arr.toString());
                        JSONObject obj2 = arr.getJSONObject(0);
                        Log.e("lat obj2", obj2.toString());
                        binding.textView.setVisibility(View.VISIBLE);

                        binding.textView.setText(obj2.getString("temp") + " ° Celcius in \n" + obj2.getString("city_name"));
                        binding.pb.setVisibility(View.GONE);



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    binding.textView.setVisibility(View.VISIBLE);
                    binding.pb.setVisibility(View.GONE);




                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = false;
                    boolean network_enabled = false;

                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch(Exception ex) {}

                    try {
                        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch(Exception ex) {}
                    if(!gps_enabled && !network_enabled)
                    {
                        // notify user
                        new AlertDialog.Builder(this)
                                .setMessage("Turn On Location To get Weather Report")
                                .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                                    {
                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        binding.temp.setVisibility(View.GONE);
                                    }
                                })
                                .setNegativeButton("Cancel",null)
                                .show();
                    }

                });
        queue.add(stringRequest);
    }
}

