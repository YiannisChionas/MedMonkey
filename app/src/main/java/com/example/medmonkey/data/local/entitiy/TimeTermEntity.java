package com.example.medmonkey.data.local.entitiy;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_terms")
public class TimeTermEntity {

    @PrimaryKey
    public int id;

    @NonNull
    public String label;

    public TimeTermEntity(int id,String label) {
        this.id = id;
        this.label = label;
    }
}

