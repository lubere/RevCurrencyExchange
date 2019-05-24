package com.revolut.kataykin.revolutcurrency.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.revolut.kataykin.revolutcurrency.model.Rate;

import java.util.ArrayList;
import java.util.List;

public class RateDBOp extends DBOp {

    private final String TABLE_NAME = "Rate";
    private final String ID = "id";
    private final String KEY_FROM = "key_from";
    private final String KEY_TO = "key_to";
    private final String RATE = "rate";

    public RateDBOp() {
        super();
    }

    String getCreateTableScript() {
        return "create table " + TABLE_NAME + " ( " + ID
                + " integer primary key autoincrement, " + KEY_FROM + " text, "
                + KEY_TO + " text, " + RATE + " integer )";
    }

    private final String[] columns = new String[]{KEY_FROM, KEY_TO, RATE};


    public List<Rate> getAll(Context context) {
        super.open(context);
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        List<Rate> lObj = getList(cursor);
        super.close();
        return lObj;
    }


    public List<Rate> getByKeyBase(Context context, String keyFrom) {
        super.open(context);
        Cursor cursor = db.query(TABLE_NAME, columns, KEY_FROM + "='" + keyFrom + "'", null, null, null, null);
        List<Rate> lObj = getList(cursor);
        super.close();
        return lObj;
    }

    private boolean existByKey(Context context, String keyFrom, String keyTo) {
        if (keyFrom == null || keyTo == null)
            return false;
        super.open(context);
        Cursor cursor = db.query(TABLE_NAME, columns,
                KEY_FROM + "='" + keyFrom + "' AND " + KEY_TO + "='" + keyTo + "'", null, null, null, null);
        int res = cursor != null ? cursor.getCount() : 0;
        if (cursor != null)
            cursor.close();
        super.close();
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
        cursor.close();
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

    public void update(Context context, List<Rate> lRate) {
        if (context == null || lRate != null)
            for (Rate rate : lRate) {
                update(context, rate.getKeyFrom(), rate.getKeyTo(), rate.getRate());
            }
    }

    private void update(Context context, String keyFrom, String keyTo, double rate) {
        if (existByKey(context, keyFrom, keyTo)) {
            edit(context, keyFrom, keyTo, rate);
        } else {
            add(context, keyFrom, keyTo, rate);
        }
    }


    private void add(Context context, String keyFrom, String keyTo, double rate) {
        if (keyFrom == null || keyTo == null)
            return;
        ContentValues cv = new ContentValues();
        cv.put(KEY_FROM, keyFrom);
        cv.put(KEY_TO, keyTo);
        cv.put(RATE, rate);
        super.open(context);
        db.insert(TABLE_NAME, null, cv);
        super.close();
    }

    private void edit(Context context, String keyFrom, String keyTo, double rate) {
        if (keyFrom == null || keyTo == null)
            return;
        String filter = KEY_FROM + "='" + keyFrom + "' AND " + KEY_TO + "='" + keyTo + "'";
        ContentValues cv = new ContentValues();
        cv.put(RATE, rate);
        super.open(context);
        db.update(TABLE_NAME, cv, filter, null);
        super.close();
    }
}
