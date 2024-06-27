package com.example.projet_daoudi_halima;

import android.content.Intent;
import android.content.ContentValues;
import android.content.Context;
import java.util.Arrays;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.widget.Spinner;
import android.widget.Toast;
import android.graphics.BitmapFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.nio.file.*;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
public class ProfileActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;


    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText phoneEditText;
    private MaterialAutoCompleteTextView genderSpinner;
    private MaterialAutoCompleteTextView filiereSpinner;
    private ImageView profilePictureImageView;
    private long userId;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        usernameEditText = findViewById(R.id.Usernametext);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        phoneEditText = findViewById(R.id.phoneNumber);
        genderSpinner = (MaterialAutoCompleteTextView) findViewById(R.id.genderSpinner);
        filiereSpinner = (MaterialAutoCompleteTextView) findViewById(R.id.filiereSpinner);
        profilePictureImageView = findViewById(R.id.imageViewFace);
        profilePictureImageView.setImageResource(R.drawable.picture);
        // Load user data
        userId = SignInActivity.getSessionUserId(this);
        displayUserInfo(userId);

        // Set up spinners
        setupSpinners();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Demander la permission d'accès au stockage externe
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }





        //Navigation vers les pages :
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(R.id.navigation_profile);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_history:
                        Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.navigation_home:
                        Intent intent2 = new Intent(ProfileActivity.this, HomeActivity.class);
                        startActivity(intent2);
                        finish();
                        return true;
                }
                return false;
            }
        });

        // Set up save button
        ImageView saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        //

        // Set up profile picture button
        Button profilePictureButton = findViewById(R.id.buttonload);
        profilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage(GALLERY_REQUEST_CODE);
                // Load profile picture
                loadUserProfilePicture();
            }
        });


    }

    private void setupSpinners() {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genderOptions);
        if (genderSpinner != null) {
            genderSpinner.setAdapter(adapter);
            genderSpinner.setClickable(true);
            genderSpinner.setFocusable(true);
            genderSpinner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    genderSpinner.showDropDown();
                }
            });
        }
        String[] filiereOptions = getResources().getStringArray(R.array.filiere_options);
        ArrayAdapter<String> filiereAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, filiereOptions);
        if (filiereSpinner != null) {
            filiereSpinner.setAdapter(filiereAdapter);
            filiereSpinner.setClickable(true);
            filiereSpinner.setFocusable(true);
            filiereSpinner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filiereSpinner.showDropDown();
                }
            });
        }
    }

    private void displayUserInfo(long userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                MyContract.UserEntry.COLUMN_USERNAME,
                MyContract.UserEntry.COLUMN_EMAIL,
                MyContract.UserEntry.COLUMN_PASSWORD,
                MyContract.UserEntry.COLUMN_PHONE_NUMBER,
                MyContract.UserEntry.COLUMN_GENDER,
                MyContract.UserEntry.COLUMN_FILIERE,
                MyContract.UserEntry.COLUMN_IMAGE
        };

        String selection = MyContract.UserEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

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
            String username = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_EMAIL));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_PASSWORD));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_PHONE_NUMBER));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_GENDER));
            String filiere = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_FILIERE));
            String profilePicturePath = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_IMAGE));

            usernameEditText.setText(username);
            emailEditText.setText(email);
            passwordEditText.setText(password);
            phoneEditText.setText(phone);

            int genderPosition = Arrays.asList(getResources().getStringArray(R.array.gender_options)).indexOf(gender);
            if (genderPosition != -1) {
                genderSpinner.setText(getResources().getStringArray(R.array.gender_options)[genderPosition], false);

            }

            int filierePosition = Arrays.asList(getResources().getStringArray(R.array.filiere_options)).indexOf(filiere);
            if (filierePosition != -1) {
                filiereSpinner.setText(getResources().getStringArray(R.array.filiere_options)[filierePosition], false);
            }

            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                System.out.println("profile existe");
                loadUserProfilePicture();
            }
        }

        cursor.close();
        dbHelper.close();
    }

    private void saveUserData() {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        String gender = genderSpinner.getText().toString();
        String filiere = filiereSpinner.getText().toString();

        DatabaseHelper dbHelper = new DatabaseHelper(ProfileActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyContract.UserEntry.COLUMN_USERNAME, username);
        values.put(MyContract.UserEntry.COLUMN_EMAIL, email);
        values.put(MyContract.UserEntry.COLUMN_PASSWORD, password);
        values.put(MyContract.UserEntry.COLUMN_PHONE_NUMBER, phone);
        values.put(MyContract.UserEntry.COLUMN_GENDER, gender);
        values.put(MyContract.UserEntry.COLUMN_FILIERE, filiere);
        if (imageUri != null) {
            values.put(MyContract.UserEntry.COLUMN_IMAGE, getImagePathFromUri(imageUri));
        }

        String selection = MyContract.UserEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        int count = db.update(
                MyContract.UserEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        dbHelper.close();

        if (count > 0) {
            Toast.makeText(ProfileActivity.this, "Modifications enregistrées avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ProfileActivity.this, "Échec de l'enregistrement des modifications", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

             imageUri = data.getData();

            // Load the selected image into the ImageView using Glide
            Glide.with(this).load(imageUri).into(profilePictureImageView);

            // Save the image to the app's internal storage
            saveImageToInternalStorage(imageUri);

        }
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            OutputStream outputStream = openFileOutput("profile_picture.jpg", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getImagePathFromUri(Uri imageUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();
            return imagePath;
        } else {
            return null;
        }
    }


        private void loadUserProfilePicture() {
            String imagePath = getImagePathFromDatabase(userId);
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    profilePictureImageView.setImageBitmap(bitmap);
                }
            }
        }


    private String getImagePathFromDatabase(long userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                MyContract.UserEntry.COLUMN_IMAGE
        };

        String selection = MyContract.UserEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

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
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.UserEntry.COLUMN_IMAGE));
            cursor.close();
            return imagePath;
        } else {
            cursor.close();
            return null;
        }
    }
}