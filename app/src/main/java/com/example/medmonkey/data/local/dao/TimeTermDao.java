package com.example.medmonkey.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.medmonkey.data.local.entitiy.TimeTermEntity;

import java.util.List;

@Dao
public interface TimeTermDao {

    @Query("SELECT * FROM time_terms")
    List<TimeTermEntity> getAll();

    @Query("SELECT id FROM time_terms WHERE label = :label LIMIT 1")
    int getIdByLabel(String label);

    @Query("SELECT * FROM time_terms WHERE id ==(:id)")
    TimeTermEntity getById(int id);

    @Insert
    void insertAll(TimeTermEntity ... timeTermEntities);

    @Insert
    void insert(TimeTermEntity timeTerm);

    @Query("SELECT COUNT(*) FROM time_terms")
    int count();

    @Delete
    void delete(TimeTermEntity timeTermEntity);
}
