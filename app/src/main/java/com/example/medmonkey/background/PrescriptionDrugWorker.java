package com.example.medmonkey.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionDrugWorker extends Worker {

    public PrescriptionDrugWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        PrescriptionDrugDao dao = db.prescriptionDrugDao();

        List<PrescriptionDrug> allDrugs = dao.getAll();

        LocalDate today = LocalDate.now();

        for (PrescriptionDrug drug : allDrugs) {
            LocalDate start = LocalDate.parse(drug.startDate);
            LocalDate end = LocalDate.parse(drug.endDate);

            boolean wasActive = drug.isActive;
            boolean shouldBeActive = !today.isBefore(start) && !today.isAfter(end);

            if (wasActive != shouldBeActive) {
                drug.isActive = shouldBeActive;
                dao.update(drug);
            }

            if (drug.lastDateReceived != null && !drug.lastDateReceived.equals(today.toString())) {
                drug.receivedToday = false;
                dao.update(drug);
            }
        }

        return Result.success();
    }
}
