package com.example.medmonkey.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medmonkey.MapPickerActivity;
import com.example.medmonkey.R;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.dao.TimeTermDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;
import com.example.medmonkey.data.local.entitiy.TimeTermEntity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddDrugFragment extends Fragment {

    private EditText doctorLocationEditText, startDateEditText, endDateEditText;
    private Spinner timeTermSpinner;
    private EditText nameEditText, descriptionEditText, doctorNameEditText;
    private Button submitButton;

    public AddDrugFragment() {
        super(R.layout.fragment_add_drug);
    }

    // Handles the result from the MapPickerActivity and sets the selected address
    private final ActivityResultLauncher<Intent> mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    String address = result.getData().getStringExtra("address");
                    if (doctorLocationEditText != null && address != null) {
                        doctorLocationEditText.setText(address);
                    }
                }
            }
    );

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        doctorLocationEditText = view.findViewById(R.id.textViewInputDoctorLocation);
        startDateEditText = view.findViewById(R.id.textViewInputStartDate);
        endDateEditText = view.findViewById(R.id.textViewInputEndDate);
        timeTermSpinner = view.findViewById(R.id.spinnerTimeTerm);
        nameEditText = view.findViewById(R.id.textViewLabelName);
        descriptionEditText = view.findViewById(R.id.textViewInputDescription);
        doctorNameEditText = view.findViewById(R.id.textViewInputDoctorName);
        submitButton = view.findViewById(R.id.button);

        // Open map picker when doctor location is clicked
        doctorLocationEditText.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });

        // Initialize date pickers
        setupDatePicker(startDateEditText);
        setupDatePicker(endDateEditText);

        // Load data and setup logic
        loadTimeTerms();
        setupSubmitLogic();
    }

    // Sets up a DatePickerDialog for the given EditText
    private void setupDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener dateSetListener = (view1, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editText.setText(sdf.format(calendar.getTime()));
        };

        editText.setOnClickListener(v -> {
            new DatePickerDialog(
                    getContext(),
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    // Loads all available time terms from the database and binds them to the spinner
    private void loadTimeTerms() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            TimeTermDao dao = db.timeTermDao();
            List<TimeTermEntity> terms = dao.getAll();

            List<String> termLabels = new ArrayList<>();
            for (TimeTermEntity term : terms) {
                termLabels.add(term.label);
            }

            requireActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        termLabels
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeTermSpinner.setAdapter(adapter);
            });
        }).start();
    }

    // Handles the submission of the form and inserts a new PrescriptionDrug into the database
    private void setupSubmitLogic() {
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();
            String doctorName = doctorNameEditText.getText().toString().trim();
            String doctorLocation = doctorLocationEditText.getText().toString().trim();
            String selectedTerm = (String) timeTermSpinner.getSelectedItem();

            if (name.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || selectedTerm == null) {
                Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validates that the start date is before or equal to the end date
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                if (start.isAfter(end)) {
                    Toast.makeText(getContext(), "Start date must be before or equal to end date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                TimeTermDao timeTermDao = db.timeTermDao();
                PrescriptionDrugDao drugDao = db.prescriptionDrugDao();

                int timeTermId = timeTermDao.getIdByLabel(selectedTerm);
                boolean isActive = isActive(startDate, endDate);

                PrescriptionDrug drug = new PrescriptionDrug(
                        name,
                        description,
                        startDate,
                        timeTermId,
                        endDate,
                        doctorName,
                        doctorLocation,
                        isActive,
                        null,
                        false
                );

                drugDao.insert(drug);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Drug saved successfully", Toast.LENGTH_SHORT).show();
                    clearInputs();
                });
            });
        });
    }

    // Determines if the drug is currently active based on start and end dates
    private boolean isActive(String startDate, String endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return (!today.isBefore(start) && !today.isAfter(end));
    }

    // Resets all input fields to their initial state
    private void clearInputs() {
        nameEditText.setText("");
        descriptionEditText.setText("");
        startDateEditText.setText("");
        endDateEditText.setText("");
        doctorNameEditText.setText("");
        doctorLocationEditText.setText("");
        if (timeTermSpinner.getAdapter().getCount() > 0) {
            timeTermSpinner.setSelection(0);
        }
    }
}
