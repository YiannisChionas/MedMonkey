package com.example.medmonkey.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.medmonkey.data.local.AppDatabase;
import com.example.medmonkey.data.local.dao.PrescriptionDrugDao;
import com.example.medmonkey.data.local.entitiy.PrescriptionDrug;

/**
 * ContentProvider implementation for accessing PrescriptionDrug data.
 * Enables external apps or components to perform CRUD operations using content URIs.
 */
public class PrescriptionDrugContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.medmonkey.provider";
    public static final String PATH_DRUGS = "drugs";

    // Base URI: content://com.example.medmonkey.provider/drugs
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_DRUGS);

    private static final int CODE_DRUGS = 1;
    private static final int CODE_DRUGS_ID = 2;

    // Matches URIs to operation codes
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, PATH_DRUGS, CODE_DRUGS);
        uriMatcher.addURI(AUTHORITY, PATH_DRUGS + "/#", CODE_DRUGS_ID);
    }

    private AppDatabase database;

    /**
     * Initializes the ContentProvider and the Room database instance.
     */
    @Override
    public boolean onCreate() {
        Log.d("ContentProvider", "onCreate called");
        database = AppDatabase.getInstance(getContext());
        return true;
    }

    /**
     * Handles query operations via URIs.
     * Supports both full list query and single item by ID.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final Cursor[] result = new Cursor[1];
        final PrescriptionDrugDao dao = database.prescriptionDrugDao();
        int match = uriMatcher.match(uri);

        Thread queryThread = new Thread(() -> {
            if (match == CODE_DRUGS) {
                result[0] = dao.getAllCursor();
            } else if (match == CODE_DRUGS_ID) {
                int id = Integer.parseInt(uri.getLastPathSegment());
                result[0] = dao.getDrugCursorById(id);
            }
        });

        queryThread.start();
        try {
            queryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    /**
     * Returns the MIME type for the given URI.
     * Used to identify directory or single item queries.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_DRUGS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".drugs";
            case CODE_DRUGS_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".drugs";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * Inserts a new PrescriptionDrug into the database.
     * Parses ContentValues into a PrescriptionDrug object.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != CODE_DRUGS) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }

        PrescriptionDrug drug = PrescriptionDrug.fromContentValues(values);
        final long[] insertedId = {-1};

        Thread insertThread = new Thread(() -> {
            insertedId[0] = database.prescriptionDrugDao().insert(drug);
        });

        insertThread.start();

        try {
            insertThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        return ContentUris.withAppendedId(CONTENT_URI, insertedId[0]);
    }

    /**
     * Deletes a PrescriptionDrug either by ID or custom selection.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        PrescriptionDrugDao dao = database.prescriptionDrugDao();
        int match = uriMatcher.match(uri);

        if (match == CODE_DRUGS_ID) {
            int id = Integer.parseInt(uri.getLastPathSegment());
            return dao.deleteById(id);
        } else if (match == CODE_DRUGS) {
            if (selection != null && selectionArgs != null && selectionArgs.length > 0) {
                return dao.deleteBySelection(selectionArgs[0]);
            } else {
                throw new IllegalArgumentException("Missing selection for delete.");
            }
        } else {
            throw new IllegalArgumentException("Invalid URI for delete: " + uri);
        }
    }

    /**
     * Updates a specific PrescriptionDrug's brief description.
     * Only supports update by ID and requires 'brief_description' key in ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        if (uriMatcher.match(uri) != CODE_DRUGS_ID) {
            throw new IllegalArgumentException("Invalid URI for update: " + uri);
        }

        int uid = Integer.parseInt(uri.getLastPathSegment());

        if (values == null || !values.containsKey("brief_description")) {
            Log.d("TestCP", "Missing brief_description in ContentValues");
            return 0;
        }

        String newDescription = values.getAsString("brief_description");

        final int[] rowsUpdated = {0};

        Thread updateThread = new Thread(() -> {
            Log.d("TestCP", "Attempting update on uid = " + uid);
            rowsUpdated[0] = database.prescriptionDrugDao().updateFromContentProvider(uid, newDescription);
            Log.d("TestCP", "Rows updated: " + rowsUpdated[0]);
        });

        updateThread.start();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rowsUpdated[0];
    }
}
