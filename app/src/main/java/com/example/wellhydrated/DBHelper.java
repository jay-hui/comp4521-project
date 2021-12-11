package com.example.wellhydrated;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WellHydratedHistory.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DBHelper", "constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + WellHydratedDBEntries.TABLE_NAME +
                                                "("+ WellHydratedDBEntries.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                WellHydratedDBEntries.COLUMN_NAME_DRINK_DATE + " TEXT," +
                                                WellHydratedDBEntries.COLUMN_NAME_DRINK_TIME + " TEXT," +
                                                WellHydratedDBEntries.COLUMN_NAME_AMOUNT + " INTEGER)";
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("DBHelper", "onCreate: " + SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }
}
