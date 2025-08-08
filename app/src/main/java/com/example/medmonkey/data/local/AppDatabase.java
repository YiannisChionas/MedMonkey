package com.example.medmonkey.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.dao.TimeTermDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;
import com.example.medmonkey.data.local.entitiy.TimeTermEntity;

/**
 * Main Room database definition for the application.
 * Holds two entities: PrescriptionDrug and TimeTermEntity.
 * Provides DAOs to access and manage data in the database.
 */
@Database(
        entities = {PrescriptionDrug.class, TimeTermEntity.class},
        version = 9
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    /**
     * Returns the DAO for accessing prescription drug records.
     */
    public abstract PrescriptionDrugDao prescriptionDrugDao();

    /**
     * Returns the DAO for accessing time term definitions.
     */
    public abstract TimeTermDao timeTermDao();

    /**
     * Returns a singleton instance of the AppDatabase.
     * Initializes the database if it hasn't been created already.
     *
     * @param context Application context
     * @return Singleton instance of AppDatabase
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "medmonkey_db"
                    )
                    // Recreates the database if migration is not provided
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
