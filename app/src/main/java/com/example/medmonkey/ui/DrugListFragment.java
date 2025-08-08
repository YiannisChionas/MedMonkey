package com.example.medmonkey.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.medmonkey.R;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;
import com.example.medmonkey.ui.adapter.PrescriptionDrugAdapter;

import java.util.ArrayList;
import java.util.List;

public class DrugListFragment extends Fragment {

    private PrescriptionDrugAdapter adapter;

    public DrugListFragment() {
        super(R.layout.fragment_drug_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView and adapter
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_drugs);
        adapter = new PrescriptionDrugAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Handle item click to navigate to detail view
        adapter.setOnItemClickListener(drug -> {
            Bundle args = new Bundle();
            args.putInt("drugId", drug.uid);

            DrugDetailFragment detailFragment = new DrugDetailFragment();
            detailFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Load drug data asynchronously
        loadDrugsFromDatabase();
    }

    // Loads the drug list from the database and updates the adapter on the UI thread
    private void loadDrugsFromDatabase() {
        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(
                    requireContext().getApplicationContext(),
                    AppDatabase.class,
                    "medmonkey_db"
            ).build();

            PrescriptionDrugDao dao = db.prescriptionDrugDao();
            List<PrescriptionDrug> allDrugs = dao.getAllOrderedByActiveAndTimeTerm();

            requireActivity().runOnUiThread(() -> adapter.updateData(allDrugs));
        }).start();
    }
}
