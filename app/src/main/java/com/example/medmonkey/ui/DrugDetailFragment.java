package com.example.medmonkey.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.medmonkey.R;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.dao.TimeTermDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;
import com.example.medmonkey.data.local.entitiy.TimeTermEntity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DrugDetailFragment extends Fragment {

    private MapView mapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    public DrugDetailFragment() {
        super(R.layout.fragment_drug_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int drugId = getArguments().getInt("drugId");

        AppDatabase db = AppDatabase.getInstance(requireContext());
        PrescriptionDrugDao drugDao = db.prescriptionDrugDao();
        TimeTermDao timeTermDao = db.timeTermDao();

        mapView = view.findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.onResume();

        Button receivedButton = view.findViewById(R.id.button_mark_received);
        Button deleteButton = view.findViewById(R.id.button_delete);

        final PrescriptionDrug[] drugHolder = new PrescriptionDrug[1];

        // Load drug and related time term in background
        new Thread(() -> {
            PrescriptionDrug drug = drugDao.getById(drugId);
            drugHolder[0] = drug;

            TimeTermEntity term = timeTermDao.getById(drug.timeTermId);

            requireActivity().runOnUiThread(() -> {
                ((TextView) view.findViewById(R.id.text_detail_name)).setText(drug.name);
                ((TextView) view.findViewById(R.id.text_detail_description)).setText(drug.description);
                ((TextView) view.findViewById(R.id.text_detail_dates)).setText("Από: " + drug.startDate + "\nΈως: " + drug.endDate);
                ((TextView) view.findViewById(R.id.text_detail_term)).setText(term != null ? term.label : "Άγνωστο");
                ((TextView) view.findViewById(R.id.text_detail_doctor_name)).setText(drug.doctorName.isEmpty() ? "—" : drug.doctorName);
                ((TextView) view.findViewById(R.id.text_detail_doctor_location)).setText(drug.doctorLocation.isEmpty() ? "—" : drug.doctorLocation);
                ((TextView) view.findViewById(R.id.text_detail_status)).setText(drug.isActive ? "Ενεργό" : "Ανενεργό");

                // Enable or disable button based on active state
                receivedButton.setEnabled(drug.isActive);
                receivedButton.setAlpha(drug.isActive ? 1f : 0.5f);

                ((TextView) view.findViewById(R.id.text_detail_received_today)).setText(drug.receivedToday ? "Ναι" : "Όχι");
                ((TextView) view.findViewById(R.id.text_detail_last_received)).setText(drug.lastDateReceived != null ? drug.lastDateReceived : "Ποτέ");

                // Display doctor location on map if available
                if (!drug.doctorLocation.isEmpty()) {
                    mapView.getMapAsync(googleMap -> {
                        LatLng latLng = getLocationFromAddress(drug.doctorLocation);
                        if (latLng != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                            googleMap.addMarker(new MarkerOptions().position(latLng).title("Ιατρός"));
                        }
                    });
                } else {
                    mapView.setVisibility(View.GONE);
                }
            });
        }).start();

        // Handle delete button click with confirmation dialog
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Επιβεβαίωση")
                    .setMessage("Είστε σίγουρος ότι θέλετε να διαγράψετε αυτό το φάρμακο;")
                    .setPositiveButton("Ναι", (dialog, which) -> {
                        new Thread(() -> {
                            PrescriptionDrug selectedDrug = drugHolder[0];
                            if (selectedDrug != null) {
                                int rowsDeleted = drugDao.deleteById(selectedDrug.uid);
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(
                                            requireContext(),
                                            "Διαγράφηκαν " + rowsDeleted + " εγγραφές.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container, new DrugListFragment())
                                            .commit();
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("Όχι", null)
                    .show();
        });

        // Handle marking the drug as received today
        receivedButton.setOnClickListener(receiveView -> {
            new Thread(() -> {
                PrescriptionDrug drug = drugDao.getById(drugId);
                drug.receivedToday = true;
                drug.lastDateReceived = java.time.LocalDate.now().toString();
                drugDao.update(drug);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Καταγράφηκε η λήψη του φαρμάκου για σήμερα.", Toast.LENGTH_SHORT).show();

                    DrugDetailFragment newFragment = new DrugDetailFragment();
                    Bundle args = new Bundle();
                    args.putInt("drugId", drug.uid);
                    newFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, newFragment)
                            .commit();
                });
            }).start();
        });
    }

    // Converts a textual address to LatLng using geocoding
    private LatLng getLocationFromAddress(String addressStr) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delegate MapView lifecycle methods to the MapView instance
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }

    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
