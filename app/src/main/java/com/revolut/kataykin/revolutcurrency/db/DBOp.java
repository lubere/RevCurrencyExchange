package com.revolut.kataykin.revolutcurrency.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

class DBOp {

    private DBOpen dbOpen;
    SQLiteDatabase db;

    synchronized void open(Context context) {
        if (context == null || isOpen())
            return;
        dbOpen = new DBOpen(context);
        db = dbOpen.getWritableDatabase();
    }

    private boolean isOpen() {
        return db != null && dbOpen != null && db.isOpen();
    }

    synchronized void close() {
        if (db != null)
            db.close();
        db = null;
        if (dbOpen != null)
            dbOpen.close();
        dbOpen = null;
    }
}
