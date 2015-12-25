package com.citsin.pedometer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Citsin on 2015/12/22.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private final static String DATABASE_NAME = "records";

    private final static int VERSION = 1;



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "+SportColumn.TABLE +"(" +
                SportColumn._ID +" integer primary key autoincrement,  "+
                SportColumn.TIME +" text,  "+
                SportColumn.DURATION +" text,  "+
                SportColumn.STEP +" text,  "+
                SportColumn.DISTANCE +" text,  "+
                SportColumn.SPEED +" text,  "+
                SportColumn.CALORIES +" text  "+
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
