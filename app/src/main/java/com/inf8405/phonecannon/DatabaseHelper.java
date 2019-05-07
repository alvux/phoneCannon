package com.inf8405.phonecannon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PhoneCannon.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryReaderContract.HistoryEntry.History_Table + "("
                    + HistoryReaderContract.HistoryEntry._ID + " INTEGER PRIMARY KEY,"
                    + HistoryReaderContract.HistoryEntry.Game_Date + " INTEGER,"
                    + HistoryReaderContract.HistoryEntry.Opponent + " TEXT,"
                    + HistoryReaderContract.HistoryEntry.Result + " TEXT" + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryReaderContract.HistoryEntry.History_Table;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database operations", "Database created");
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Table","Table Created");
       db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


}
