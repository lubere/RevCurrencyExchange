package com.revolut.kataykin.revolutcurrency.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.revolut.kataykin.revolutcurrency.model.Rate;

import java.util.ArrayList;
import java.util.List;

public class RateDBOp {

    private final String TABLE_NAME = "Rate";
    private final String ID = "id";
    private final String KEY_FROM = "key_from";
    private final String KEY_TO = "key_to";
    private final String RATE = "rate";

    public RateDBOp(Context context) {
        DBOp.open(context);
    }

    String getCreateTableScript() {
        return "create table " + TABLE_NAME + " ( " + ID
                + " integer primary key autoincrement, " + KEY_FROM + " text, "
                + KEY_TO + " text, " + RATE + " integer )";
    }

    private final String[] columns = new String[]{KEY_FROM, KEY_TO, RATE};


    public List<Rate> getAll() {
        Cursor cursor = DBOp.db.query(TABLE_NAME, columns, null, null, null, null, null);
        if (cursor == null)
            return null;
        List<Rate> lObj = getList(cursor);
        cursor.close();
        return lObj;
    }


    public List<Rate> getByKeyBase(String keyFrom) {
        Cursor cursor = DBOp.db.query(TABLE_NAME, columns, KEY_FROM + "='" + keyFrom + "'", null, null, null, null);
        if (cursor == null)
            return null;
        List<Rate> lObj = getList(cursor);
        cursor.close();
        return lObj;
    }

    private boolean existByKey(String keyFrom, String keyTo) {
        if (keyFrom == null || keyTo == null)
            return false;
        Cursor cursor = DBOp.db.query(TABLE_NAME, columns,
                KEY_FROM + "='" + keyFrom + "' AND " + KEY_TO + "='" + keyTo + "'", null, null, null, null);
        if (cursor == null)
            return false;
        int res = cursor.getCount();
        cursor.close();
        return res > 0;
    }

    /**
     * PARSING
     */
    private List<Rate> getList(Cursor cursor) {
        if (cursor == null)
            return null;
        List<Rate> lObj = new ArrayList<>();
        while (cursor.moveToNext()) {
            Rate obj = parsingFromDB(cursor);
            if (obj != null)
                lObj.add(obj);
        }
        return lObj;
    }

    private Rate parsingFromDB(Cursor cursor) {
        if (cursor == null)
            return null;
        try {
            String keyFrom = cursor.isNull(0) ? null : cursor.getString(0);
            String keyTo = cursor.isNull(1) ? null : cursor.getString(1);
            Double rate = cursor.isNull(2) ? null : cursor.getDouble(2);
            if (keyFrom == null || keyTo == null || rate == null)
                return null;
            return new Rate(keyFrom, keyTo, rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(List<Rate> lRate) {
        if (lRate != null)
            for (Rate rate : lRate) {
                update(rate.getKeyFrom(), rate.getKeyTo(), rate.getRate());
            }
    }

    private void update(String keyFrom, String keyTo, double rate) {
        if (existByKey(keyFrom, keyTo)) {
            edit(keyFrom, keyTo, rate);
        } else {
            add(keyFrom, keyTo, rate);
        }
    }


    private void add(String keyFrom, String keyTo, double rate) {
        if (keyFrom == null || keyTo == null)
            return;
        ContentValues cv = new ContentValues();
        cv.put(KEY_FROM, keyFrom);
        cv.put(KEY_TO, keyTo);
        cv.put(RATE, rate);
        DBOp.db.insert(TABLE_NAME, null, cv);
    }

    private void edit(String keyFrom, String keyTo, double rate) {
        if (keyFrom == null || keyTo == null)
            return;
        String filter = KEY_FROM + "='" + keyFrom + "' AND " + KEY_TO + "='" + keyTo + "'";
        ContentValues cv = new ContentValues();
        cv.put(RATE, rate);
        DBOp.db.update(TABLE_NAME, cv, filter, null);
    }
}
