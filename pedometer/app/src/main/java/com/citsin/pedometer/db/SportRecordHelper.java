package com.citsin.pedometer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.citsin.pedometer.model.Sport;

import java.util.ArrayList;

/**
 * Created by Citsin on 2015/12/22.
 */
public class SportRecordHelper {

    public static void insert(Context context,int step,long duration,float distance,float speed,float calories){
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SportColumn.STEP,step);
        values.put(SportColumn.DURATION,duration);
        values.put(SportColumn.DISTANCE,distance);
        values.put(SportColumn.SPEED,speed);
        values.put(SportColumn.CALORIES,calories);
        values.put(SportColumn.TIME,System.currentTimeMillis());
        db.insert(SportColumn.TABLE, null, values);
        db.close();
    }

    public static ArrayList<Sport> findAll(Context context){
        ArrayList<Sport> sports = new ArrayList<>();
        SQLiteDatabase db = new DatabaseHelper(context).getReadableDatabase();
        String[] columns = new String[]{SportColumn._ID,SportColumn.STEP,SportColumn.DURATION,SportColumn.SPEED,
                SportColumn.CALORIES,SportColumn.TIME,SportColumn.DISTANCE};
        Cursor cursor = db.query(SportColumn.TABLE,columns,null,null,null,null,SportColumn.TIME +" DESC");
        while (cursor.moveToNext()){
            Sport sport = new Sport();
            sport.setId(cursor.getInt(0));
            sport.setStep(cursor.getLong(1));
            sport.setDuration(cursor.getLong(2));
            sport.setSpeed(cursor.getFloat(3));
            sport.setCalories(cursor.getFloat(4));
            sport.setTime(cursor.getLong(5));
            sport.setDistance(cursor.getFloat(6));
            sports.add(sport);
        }
        if (cursor != null)cursor.close();
        if (db != null)db.close();
        return sports;
    }
}
