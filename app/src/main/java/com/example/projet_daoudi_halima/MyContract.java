package com.example.projet_daoudi_halima;

import android.net.Uri;
public final class MyContract {
    private MyContract() {}

    public static class UserEntry {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_FILIERE = "filiere";
        public static final String COLUMN_IMAGE = "image";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_USERNAME + " TEXT UNIQUE," +
                        COLUMN_EMAIL + " TEXT UNIQUE," +
                        COLUMN_PASSWORD + " TEXT," +
                        COLUMN_PHONE_NUMBER + " TEXT," +
                        COLUMN_GENDER + " TEXT," +
                        COLUMN_FILIERE + " TEXT," +
                        COLUMN_IMAGE + " TEXT)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
    public static class ActivityEntry {
        public static final String TABLE_NAME = "activities";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ACTIVITY = "activity";
        public static final String COLUMN_CONFIDENCE = "confidence";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_LATITUDE_START = "latitude_start";
        public static final String COLUMN_LONGITUDE_START = "longitude_start";
        public static final String COLUMN_LATITUDE_FINISH = "latitude_finish";
        public static final String COLUMN_LONGITUDE_FINISH = "longitude_finish";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_ACTIVITY + " TEXT," +
                        COLUMN_CONFIDENCE + " INTEGER," +
                        COLUMN_START_DATE + " TEXT," +
                        COLUMN_END_DATE + " TEXT NULL," +
                        COLUMN_USER_ID + " INTEGER," +
                        COLUMN_LATITUDE_START + " REAL," +
                        COLUMN_LONGITUDE_START + " REAL," +
                        COLUMN_LATITUDE_FINISH + " REAL NULL," +
                        COLUMN_LONGITUDE_FINISH + " REAL NULL)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}

