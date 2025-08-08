package com.example.medmonkey.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.medmonkey.R;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportFragment extends Fragment {

    public ExportFragment() {
        super(R.layout.fragment_export);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up button click to trigger HTML export
        view.findViewById(R.id.button_export).setOnClickListener(v -> {
            exportActiveDrugsToHTML();
        });
    }

    // Asynchronously exports active prescription drugs to an HTML file and opens it in a browser
    private void exportActiveDrugsToHTML() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<PrescriptionDrug> activeDrugs = db.prescriptionDrugDao().getActiveOrderedByTime();

            // Build HTML content with drug information
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset=\"UTF-8\">\n</head><body><h2>Ενεργά Φάρμακα</h2><ul>");

            for (PrescriptionDrug drug : activeDrugs) {
                html.append("<li><b>").append(drug.name).append("</b><br/>")
                        .append("Περιγραφή: ").append(drug.description).append("<br/>")
                        .append("Από: ").append(drug.startDate).append(" έως ").append(drug.endDate).append("<br/>")
                        .append("Όνομα Ιατρού: ").append(drug.doctorName).append("<br/>")
                        .append("Τοποθεσία Ιατρού: ").append(drug.doctorLocation).append("<br/>")
                        .append("Ελήφθη σήμερα: ").append(drug.receivedToday ? "Ναι" : "Όχι").append("<br/>")
                        .append("Τελευταία λήψη: ").append(drug.lastDateReceived != null ? drug.lastDateReceived : "Ποτέ")
                        .append("</li><br/>");
            }

            html.append("</ul></body></html>");

            try {
                // Create or access the app-specific downloads directory
                File downloadsDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
                if (!downloadsDir.exists()) downloadsDir.mkdirs();

                // Create the HTML file
                File file = new File(downloadsDir, "ActiveDrugsExport.html");

                // Write the content to the file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(html.toString().getBytes());
                fos.close();

                // Notify the user that the file was saved successfully
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Το αρχείο αποθηκεύτηκε στο: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
                );

                // Get URI for the file using FileProvider
                Uri fileUri = FileProvider.getUriForFile(getContext(),
                        "com.example.medmonkey.fileprovider", file);

                // Create an intent to open the HTML file in a browser or other capable app
                Intent openIntent = new Intent(Intent.ACTION_VIEW);
                openIntent.setDataAndType(fileUri, "text/html");
                openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Start the activity to open the file
                requireActivity().startActivity(openIntent);

            } catch (IOException e) {
                e.printStackTrace();

                // Show error toast if file creation or writing fails
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Σφάλμα κατά την αποθήκευση.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

}
