package com.revolut.kataykin.revolutcurrency.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBOp {


    private static DBOpen dbOpen;
    static SQLiteDatabase db;

    static synchronized void open(Context context) {
        if (context == null || isOpen())
            return;
        dbOpen = new DBOpen(context);
        db = dbOpen.getWritableDatabase();
    }

    private static boolean isOpen() {
        return db != null && dbOpen != null && db.isOpen();
    }

    public static synchronized void close() {
        if (db != null)
            db.close();
        db = null;
        if (dbOpen != null)
            dbOpen.close();
        dbOpen = null;
    }
}
