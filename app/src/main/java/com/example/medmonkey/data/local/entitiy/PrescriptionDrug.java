package com.example.medmonkey.data.local.entitiy;

import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "prescription_drug",
        foreignKeys = @ForeignKey(
                entity = TimeTermEntity.class,
                parentColumns = "id",
                childColumns = "time_term_id",
                onDelete = ForeignKey.NO_ACTION
        ),indices = @Index("time_term_id")
)
public class PrescriptionDrug {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name="short_name")
    public String name;
    @ColumnInfo(name="brief_description")
    public String description;

    @ColumnInfo(name="start_date")
    public String startDate;

    @ColumnInfo(name="time_term_id")
    public int timeTermId;

    @ColumnInfo(name="end_date")
    public String endDate;

    @ColumnInfo(name="doctor_name")
    public String doctorName;

    @ColumnInfo(name="doctor_location")
    public String doctorLocation;

    @ColumnInfo(name="is_active")
    public boolean isActive;

    @ColumnInfo(name="last_date_received")
    public String lastDateReceived;

    @ColumnInfo(name="received_today")
    public boolean receivedToday;

    public PrescriptionDrug() {
        // Required by Room
    }
    @Ignore
    public PrescriptionDrug( String name, String description, String startDate, int timeTermId, String endDate, String doctorName, String doctorLocation, boolean isActive, String lastDateReceived, boolean receivedToday) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.timeTermId = timeTermId;
        this.endDate = endDate;
        this.doctorName = doctorName;
        this.doctorLocation = doctorLocation;
        this.isActive = isActive;
        this.lastDateReceived = lastDateReceived;
        this.receivedToday = receivedToday;
    }
    public static PrescriptionDrug fromContentValues(ContentValues values) {
        PrescriptionDrug drug = new PrescriptionDrug();

        if (values.containsKey("short_name")) {
            drug.name = values.getAsString("short_name");
        }
        if (values.containsKey("description")) {
            drug.description = values.getAsString("brief_description");
        }
        if (values.containsKey("start_date")) {
            drug.startDate = values.getAsString("start_date");
        }
        if (values.containsKey("end_date")) {
            drug.endDate = values.getAsString("end_date");
        }
        if (values.containsKey("time_term_id")) {
            drug.timeTermId = values.getAsInteger("time_term_id");
        }
        if (values.containsKey("doctor_name")) {
            drug.doctorName = values.getAsString("doctor_name");
        }
        if (values.containsKey("doctor_location")) {
            drug.doctorLocation = values.getAsString("doctor_location");
        }
        if (values.containsKey("is_active")) {
            drug.isActive = values.getAsBoolean("is_active");
        }
        if (values.containsKey("last_date_received")) {
            drug.lastDateReceived = values.getAsString("last_date_received");
        }
        if (values.containsKey("received_today")) {
            drug.receivedToday = values.getAsBoolean("received_today");
        }

        return drug;
    }

}
