package com.example.medmonkey;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Activity that allows the user to pick a location from the map.
 * Once a location is selected, the address and coordinates are returned via Intent.
 */
public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Initialize and load the Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Called when the map is fully loaded and ready to use.
     * Sets up a click listener and default camera position.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set default camera to Athens, Greece
        LatLng athens = new LatLng(37.9838, 23.7275);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(athens, 10));

        // Handle map click: get location and reverse geocode to address
        mMap.setOnMapClickListener(latLng -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String addressText = latLng.latitude + ", " + latLng.longitude;

            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressText = address.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace(); // Optionally show an error message to the user
            }

            // Package coordinates and address into result Intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("lat", latLng.latitude);
            resultIntent.putExtra("lng", latLng.longitude);
            resultIntent.putExtra("address", addressText);

            // Send result back and finish the activity
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
