package com.example.projet_daoudi_halima;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table des utilisateurs
        db.execSQL(MyContract.UserEntry.SQL_CREATE_TABLE);

        // Création de la table des activités
        db.execSQL(MyContract.ActivityEntry.SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression des tables existantes
        db.execSQL(MyContract.UserEntry.SQL_DELETE_TABLE);
        db.execSQL(MyContract.ActivityEntry.SQL_DELETE_TABLE);
        onCreate(db);
    }

}
