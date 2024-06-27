package com.example.projet_daoudi_halima;

import android.content.Intent;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;



public class SignUpActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText usernameEditText = findViewById(R.id.UserName);
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        EditText phoneNumberEditText = findViewById(R.id.phoneNumber);
        Spinner genderSpinner = findViewById(R.id.genderSpinner);
        Spinner fieldSpinner = findViewById(R.id.fieldSpinner);


        findViewById(R.id.signUpButton).setOnClickListener(view -> {
            // Lecture des valeurs saisies
            String username = usernameEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String phoneNumber = phoneNumberEditText.getText().toString();
            String gender = genderSpinner.getSelectedItem().toString();
            String field = fieldSpinner.getSelectedItem().toString();

            // Insertion des données dans la base de données SQLite
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(MyContract.UserEntry.COLUMN_USERNAME, username);
            values.put(MyContract.UserEntry.COLUMN_EMAIL, email);
            values.put(MyContract.UserEntry.COLUMN_PASSWORD, password);
            values.put(MyContract.UserEntry.COLUMN_PHONE_NUMBER, phoneNumber);
            values.put(MyContract.UserEntry.COLUMN_GENDER, gender);
            values.put(MyContract.UserEntry.COLUMN_FILIERE, field);

            // Insérez les valeurs dans la table
            long newRowId = db.insert(MyContract.UserEntry.TABLE_NAME, null, values);

            if (newRowId != -1) {
                // L'insertion a réussi
                Toast.makeText(this, "L'insertion a réussi", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            } else {
                // L'insertion a échoué
                Toast.makeText(this, "Vérifier les informations", Toast.LENGTH_SHORT).show();
            }
        });
        TextView signInTextView = findViewById(R.id.signInTextView);

        signInTextView.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

    }
}