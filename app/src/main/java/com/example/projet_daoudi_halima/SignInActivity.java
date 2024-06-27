package com.example.projet_daoudi_halima;

import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    private static final String SESSION_ID_KEY = "user_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);

        findViewById(R.id.signInButton).setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String[] projection = {
                    MyContract.UserEntry.COLUMN_ID,
                    MyContract.UserEntry.COLUMN_USERNAME,
                    MyContract.UserEntry.COLUMN_EMAIL,
                    MyContract.UserEntry.COLUMN_PASSWORD
            };

            String selection = MyContract.UserEntry.COLUMN_EMAIL + " = ? AND " +
                    MyContract.UserEntry.COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {email, password};

            Cursor cursor = db.query(
                    MyContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                // Utilisateur existe
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_ID));
                saveSession(userId);

                // affiche message succes
                Toast.makeText(this, "Successfully signed in!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                // Utilisateur not exists
                Toast.makeText(this, "Sign in failed!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    // Méthode pour enregistrer l ID de l'utilisateur en session dans SharedPreferences
    public void saveSession(long userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SESSION_ID_KEY, userId);
        editor.apply();
    }

    // Méthode pour récupérer l'ID de l'utilisateur en session
    public static long getSessionUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(SESSION_ID_KEY, -1);
    }

}

