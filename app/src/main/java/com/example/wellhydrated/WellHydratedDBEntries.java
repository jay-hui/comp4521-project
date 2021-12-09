package com.example.wellhydrated;

import android.provider.BaseColumns;

public class WellHydratedDBEntries implements BaseColumns {
    // Just define all constants, such as table names, column names
    public static final String TABLE_NAME = "wellhydrated_records";
    public static final String COLUMN_NAME_DRINK_DATE = "drink_date";
    public static final String COLUMN_NAME_DRINK_TIME = "drink_time";
    public static final String COLUMN_NAME_AMOUNT = "amount";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private WellHydratedDBEntries() {}
}
