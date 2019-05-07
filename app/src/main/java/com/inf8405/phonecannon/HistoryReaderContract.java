package com.inf8405.phonecannon;

import android.provider.BaseColumns;

public final class HistoryReaderContract {
    private HistoryReaderContract(){}

    public static class HistoryEntry implements BaseColumns {
        public static final String History_Table = "history_table";
        public static final String Game_Date = "game_date";
        public static final String Opponent= "opponent";
        public static final String Result= "result";

    }
}
