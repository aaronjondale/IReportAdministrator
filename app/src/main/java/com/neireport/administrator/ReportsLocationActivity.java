package com.neireport.administrator;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReportsLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap google_map;

    private double latitude, longitude;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        latitude = getIntent().getDoubleExtra("latitude", 15.5784);
        longitude = getIntent().getDoubleExtra("longitude", 121.1113);
        location = getIntent().getStringExtra("location");
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        google_map = googleMap;

        LatLng currentLocation = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation , 16);

        google_map.addMarker(new MarkerOptions().position(currentLocation).title(location));
        google_map.moveCamera(cameraUpdate);
        google_map.animateCamera(cameraUpdate);
    }
}
