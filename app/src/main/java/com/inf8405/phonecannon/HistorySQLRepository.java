package com.inf8405.phonecannon;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

public class HistorySQLRepository {


    public DatabaseHelper dbHelper;
    public HistorySQLRepository(Activity activity){
       this.dbHelper  =  new DatabaseHelper(activity);;


    }

    public void addHistory(Date date, String opponent, String result){
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HistoryReaderContract.HistoryEntry.Game_Date, date.getTime());
        values.put(HistoryReaderContract.HistoryEntry.Opponent, opponent);
        values.put(HistoryReaderContract.HistoryEntry.Result, result);


// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(HistoryReaderContract.HistoryEntry.History_Table, null, values);
    }

    public ArrayList<History> getHistory(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                HistoryReaderContract.HistoryEntry.Game_Date + " DESC";

        Cursor cursor = db.query(
                HistoryReaderContract.HistoryEntry.History_Table,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<History> historyEntries = new ArrayList<>();

        while(cursor.moveToNext()) {
            long gameDate = cursor.getLong(
                    cursor.getColumnIndexOrThrow(HistoryReaderContract.HistoryEntry.Game_Date));
            String opponent = cursor.getString(
                    cursor.getColumnIndexOrThrow(HistoryReaderContract.HistoryEntry.Opponent));
            String result = cursor.getString(
                    cursor.getColumnIndexOrThrow(HistoryReaderContract.HistoryEntry.Result));
            History historyEntry= new History();
            historyEntry.gameDate = new Date(gameDate);
            historyEntry.opponent = opponent;
            historyEntry.result = result;

            historyEntries.add(historyEntry);
        }
        cursor.close();

        return historyEntries;
    }
}
