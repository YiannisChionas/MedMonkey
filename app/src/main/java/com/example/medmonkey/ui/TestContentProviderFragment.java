package com.example.medmonkey.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medmonkey.R;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.entitiy.TimeTermEntity;
import com.example.medmonkey.data.provider.PrescriptionDrugContentProvider;

public class TestContentProviderFragment extends Fragment {

    // Content URI pointing to the PrescriptionDrug content provider
    private static final Uri CONTENT_URI = PrescriptionDrugContentProvider.CONTENT_URI;

    private TextView resultText;

    public TestContentProviderFragment() {
        super(R.layout.fragment_test_content_provider);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button insertBtn = view.findViewById(R.id.btn_insert);
        Button queryBtn = view.findViewById(R.id.btn_query);
        Button updateBtn = view.findViewById(R.id.btn_update);
        Button deleteBtn = view.findViewById(R.id.btn_delete);
        resultText = view.findViewById(R.id.text_result);

        insertBtn.setOnClickListener(v -> insertSampleDrug());
        queryBtn.setOnClickListener(v -> queryDrugs());
        updateBtn.setOnClickListener(v -> updateFirstDrug());
        deleteBtn.setOnClickListener(v -> deleteFirstDrug());
    }

    // Inserts a sample drug into the database using the ContentProvider
    private void insertSampleDrug() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Populate time terms if the table is empty (for foreign key integrity)
            if (db.timeTermDao().count() == 0) {
                db.timeTermDao().insert(new TimeTermEntity(1, "before-breakfast"));
                db.timeTermDao().insert(new TimeTermEntity(2, "at-breakfast"));
                db.timeTermDao().insert(new TimeTermEntity(3, "after-breakfast"));
                db.timeTermDao().insert(new TimeTermEntity(4, "before-lunch"));
                db.timeTermDao().insert(new TimeTermEntity(5, "at-lunch"));
                db.timeTermDao().insert(new TimeTermEntity(6, "after-lunch"));
                db.timeTermDao().insert(new TimeTermEntity(7, "before-dinner"));
                db.timeTermDao().insert(new TimeTermEntity(8, "at-dinner"));
                db.timeTermDao().insert(new TimeTermEntity(9, "after-dinner"));
            }

            // Prepare data for insertion
            ContentValues values = new ContentValues();
            values.put("short_name", "TestDrug");
            values.put("brief_description", "Inserted via ContentProvider");
            values.put("start_date", "2025-08-01");
            values.put("time_term_id", 1);
            values.put("end_date", "2025-08-10");
            values.put("doctor_name", "Dr. CP");
            values.put("doctor_location", "Athens");
            values.put("is_active", true);
            values.put("received_today", false);

            // Perform the insert operation
            Uri resultUri = requireContext().getContentResolver().insert(CONTENT_URI, values);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Insert completed: " + resultUri, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // Queries all drugs via ContentProvider and displays them in a TextView
    private void queryDrugs() {
        Cursor cursor = requireContext().getContentResolver().query(CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            resultText.setText("Query returned null.");
            return;
        }

        StringBuilder builder = new StringBuilder();

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("short_name"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("brief_description"));
            builder.append("• ").append(name).append(" - ").append(desc).append("\n");
        }

        resultText.setText(builder.toString());
        cursor.close();
    }

    // Updates the first drug found by changing its brief description
    private void updateFirstDrug() {
        Cursor cursor = requireContext().getContentResolver().query(CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("uid"));
            Log.d("TestCP", "Attempting update on uid = " + id);

            ContentValues values = new ContentValues();
            values.put("brief_description", "Updated via ContentProvider");

            int rows = requireContext().getContentResolver().update(
                    Uri.withAppendedPath(CONTENT_URI, String.valueOf(id)),
                    values,
                    null,
                    null
            );

            Log.d("TestCP", "Rows updated: " + rows);
            Toast.makeText(getContext(), "Updated rows: " + rows, Toast.LENGTH_SHORT).show();
            cursor.close();
        } else {
            Log.e("TestCP", "Cursor empty or null during update.");
        }
    }

    // Deletes the first drug record using the ContentProvider
    private void deleteFirstDrug() {
        new Thread(() -> {
            Cursor cursor = requireContext().getContentResolver().query(CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("uid"));

                int rows = requireContext().getContentResolver().delete(
                        Uri.withAppendedPath(CONTENT_URI, String.valueOf(id)),
                        null,
                        null
                );

                cursor.close();

                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Deleted rows: " + rows, Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

}
