package com.example.hartrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static  final String DATABASE_NAME = "hartrainer.db";
    public static final int DB_VERSION = 1;

    public static  final String TABLE1 = "user";
    public static  final String TABLE1_COL1 = "ID";
    public static  final String TABLE1_COL2 = "email";
    public static  final String TABLE1_COL3 = "name";
    public static  final String TABLE1_COL4 = "phone";
    public static  final String TABLE1_COL5 = "password";
    public static  final String TABLE1_COL6 = "height";
    public static  final String TABLE1_COL7 = "weight";



    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT,Email TEXT,Phone TEXT,Password TEXT,Height TEXT,Weight TEXT) ");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE1 ); //Drop older table if exists
        onCreate(db);
    }

}
