package com.example.medmonkey.data.local.dao;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PrescriptionDrugDao {

    @Query("SELECT * FROM prescription_drug")
    List<PrescriptionDrug> getAll();

    @Query("SELECT * FROM prescription_drug WHERE uid ==(:id)")
    PrescriptionDrug getById(int id);

    @Query("SELECT * FROM prescription_drug WHERE uid IN (:uid)")
    List<PrescriptionDrug> getAll(int[] uid);

    @Insert
    void insertAll(PrescriptionDrug... prescriptionDrugs);

    @Insert
    long insert(PrescriptionDrug prescriptionDrug);

    @Delete
    void delete(PrescriptionDrug prescriptionDrug);

    @Query("DELETE FROM prescription_drug WHERE uid=(:uid)")
    int deleteById(int uid); //Returns number of rows affected

    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 ORDER BY time_term_id")
    List<PrescriptionDrug> getActiveOrderedByTime();

    @Update
    void update(PrescriptionDrug prescriptionDrug);

    @Query("SELECT * FROM prescription_drug")
    Cursor getAllCursor();

    @Query("DELETE FROM prescription_drug WHERE short_name = :name")
    int deleteBySelection(String name);

    @Query("SELECT * FROM prescription_drug WHERE uid = :id")
    Cursor getDrugCursorById(int id);

    @Query("SELECT * FROM prescription_drug ORDER BY is_active DESC, time_term_id ASC")
    List<PrescriptionDrug> getAllOrderedByActiveAndTimeTerm();

    @RawQuery
    int performUpdate(SupportSQLiteQuery query);

    @Query("UPDATE prescription_drug SET brief_description = :desc WHERE uid = :uid")
    int updateFromContentProvider(int uid, String desc);

}
