package com.example.medmonkey;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.medmonkey.background.PrescriptionDrugWorker;
import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.dao.TimeTermDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;
import com.example.medmonkey.data.local.entitiy.TimeTermEntity;
import com.example.medmonkey.ui.AddDrugFragment;
import com.example.medmonkey.ui.DrugListFragment;
import com.example.medmonkey.ui.ExportFragment;
import com.example.medmonkey.ui.TestContentProviderFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the top toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Initialize bottom navigation view
        bottomNav = findViewById(R.id.bottom_nav);

        // Perform database initialization and UI setup in a background thread
        new Thread(() -> {
            AppDatabase db = getDatabase();
            TimeTermDao timeTermDao = db.timeTermDao();
            PrescriptionDrugDao drugDao = db.prescriptionDrugDao();

            // Insert default time terms only if the table is empty
            if (timeTermDao.count() == 0) {
                List<TimeTermEntity> defaultTerms = Arrays.asList(
                        new TimeTermEntity(1, "before-breakfast"),
                        new TimeTermEntity(2, "at-breakfast"),
                        new TimeTermEntity(3, "after-breakfast"),
                        new TimeTermEntity(4, "before-lunch"),
                        new TimeTermEntity(5, "at-lunch"),
                        new TimeTermEntity(6, "after-lunch"),
                        new TimeTermEntity(7, "before-dinner"),
                        new TimeTermEntity(8, "at-dinner"),
                        new TimeTermEntity(9, "after-dinner")
                );
                for (TimeTermEntity term : defaultTerms) {
                    timeTermDao.insert(term);
                }
            }

            // Insert sample prescription drugs if no entries exist
            if (drugDao.getAll().isEmpty()) {
                drugDao.insertAll(
                        new PrescriptionDrug("Panadol", "Για πονοκέφαλο", "2025-08-01", 1, "2025-08-31", "Dr. Papadopoulos", "Athens", true, null, false),
                        new PrescriptionDrug("Amoxil", "Αντιβίωση", "2025-08-03", 2, "2025-08-15", "", "", true, null, false),
                        new PrescriptionDrug("Nurofen", "Αντιφλεγμονώδες", "2025-08-05", 3, "2025-08-15", "Dr. Kosta", "Thessaloniki", true, null, false),
                        new PrescriptionDrug("Depon", "Πυρετός", "2025-08-06", 4, "2025-08-20", "Dr. Maria", "Patra", true, null, false),
                        new PrescriptionDrug("Zantac", "Καούρα στομάχου", "2025-08-01", 5, "2025-08-10", "Dr. Giorgos", "Athens", true, null, false),
                        new PrescriptionDrug("Lipitor", "Χοληστερίνη", "2025-08-02", 6, "2025-09-02", "Dr. Eleni", "Larisa", true, null, false),
                        new PrescriptionDrug("Augmentin", "Αντιβίωση", "2025-08-04", 7, "2025-08-18", "Dr. Nikos", "Volos", true, null, false),
                        new PrescriptionDrug("Aspirin", "Αραίωση αίματος", "2025-08-01", 8, "2025-08-31", "Dr. Katerina", "Kavala", true, null, false),
                        new PrescriptionDrug("Xanax", "Άγχος", "2025-08-03", 9, "2025-08-30", "Dr. Dimitris", "Chania", true, null, false),
                        new PrescriptionDrug("Plavix", "Προστασία καρδιάς", "2025-08-01", 1, "2025-09-01", "Dr. Sofia", "Ioannina", true, null, false),
                        new PrescriptionDrug("Voltaren", "Μυοσκελετικός πόνος", "2025-08-06", 2, "2025-08-12", "Dr. Andreas", "Athens", true, null, false),
                        new PrescriptionDrug("Omeprazole", "Γαστροπροστασία", "2025-08-05", 3, "2025-08-25", "Dr. Giannis", "Heraklion", true, null, false)
                );
            }

            // Run UI-related setup on the main thread
            runOnUiThread(() -> {
                setupBottomNav(); // Initialize bottom navigation functionality
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new DrugListFragment()) // Set default fragment
                        .commit();
                bottomNav.setSelectedItemId(R.id.nav_list);
            });

        }).start();

        // Configure background worker to run every 1 hour for checking prescriptions
        PeriodicWorkRequest drugWorkRequest =
                new PeriodicWorkRequest.Builder(PrescriptionDrugWorker.class, 1, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "PrescriptionDrugChecker",
                ExistingPeriodicWorkPolicy.KEEP, // Ensures only one instance runs at a time
                drugWorkRequest
        );
    }

    /**
     * Initializes and returns a singleton instance of the Room database.
     * Uses destructive migration only for development purposes.
     */
    private AppDatabase getDatabase() {
        return Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "medmonkey_db"
                )
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * Sets up bottom navigation logic and fragment switching.
     * Each menu item loads the corresponding fragment and updates the toolbar title.
     */
    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            String title;

            int id = item.getItemId();

            if (id == R.id.nav_add) {
                selectedFragment = new AddDrugFragment();
                title = "Προσθήκη Φαρμάκου";
            } else if (id == R.id.nav_export) {
                selectedFragment = new ExportFragment();
                title = "Εξαγωγή Φαρμάκων";
            } else if (id == R.id.nav_test_cp) {
                selectedFragment = new TestContentProviderFragment();
                title = "Test ContentProvider";
            } else {
                selectedFragment = new DrugListFragment();
                title = "Λίστα Φαρμάκων";
            }

            getSupportActionBar().setTitle(title);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });
    }
}
