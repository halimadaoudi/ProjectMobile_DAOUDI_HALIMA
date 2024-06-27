package com.example.projet_daoudi_halima;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import android.widget.ImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class HistoryActivity extends AppCompatActivity {

    private ImageView clear_history;
    private long userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        //Navigation vers les pages :
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(R.id.navigation_history);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.navigation_profile:
                        Intent intent2 = new Intent(HistoryActivity.this, ProfileActivity.class);
                        startActivity(intent2);
                        return true;
                }
                return false;
            }
        });

        // Load user id
        userId = SignInActivity.getSessionUserId(this);



        clear_history= findViewById(R.id.clear_history);
        clear_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the custom layout for the dialog
                View dialogView = getLayoutInflater().inflate(R.layout.popup_confirmation, null);

                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                // Show the dialog
                dialog.show();
                // Find views inside the dialog layout
                Button confirmButton = dialogView.findViewById(R.id.confirm_button);
                Button cancelButton = dialogView.findViewById(R.id.cancel_button);

                // Set click listener for the confirm button
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        clearHistory(userId);
                        dialog.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });

        displayActivities(userId);

    }

    public void displayActivities(long userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String[] projection = {
                    MyContract.ActivityEntry.COLUMN_ID,
                    MyContract.ActivityEntry.COLUMN_ACTIVITY,
                    MyContract.ActivityEntry.COLUMN_CONFIDENCE,
                    MyContract.ActivityEntry.COLUMN_START_DATE,
                    MyContract.ActivityEntry.COLUMN_END_DATE,
                    MyContract.ActivityEntry.COLUMN_LATITUDE_START,
                    MyContract.ActivityEntry.COLUMN_LONGITUDE_START,
                    MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH,
                    MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH,
            };

            String selection = MyContract.ActivityEntry.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            cursor = db.query(
                    MyContract.ActivityEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            LinearLayout activityContainer = findViewById(R.id.activityContainer);

            if (cursor.moveToFirst()) {
                do {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);


                    String activity = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_ACTIVITY));
                    String confidence = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_CONFIDENCE));
                    String startDate = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_START_DATE));
                    String endDate = cursor.getString(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_END_DATE));
                    double startLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_LATITUDE_START));
                    double startLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_LONGITUDE_START));
                    double finishLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH));
                    double finishLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH));


                    // Créer une vue d'activité en utilisant le layout XML
                    View activityView = getLayoutInflater().inflate(R.layout.list_view, activityContainer, false);
                    activityView.setTag(cursor.getLong(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_ID)));  // Supposons que vous avez une colonne _ID

                    ImageView deleteIcon = activityView.findViewById(R.id.deleteIcon);
                    deleteIcon.setTag(cursor.getLong(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_ID)));
                    deleteIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            long itemId = (long) v.getTag();
                            onDeleteClicked(itemId);
                        }
                    });

                    ImageView viewIcon = activityView.findViewById(R.id.viewIcon);
                    viewIcon.setTag(cursor.getLong(cursor.getColumnIndexOrThrow(MyContract.ActivityEntry.COLUMN_ID)));
                    viewIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            long itemId = (long) v.getTag();
                            Intent mapIntent = new Intent(HistoryActivity.this, MapActivity.class);
                            mapIntent.putExtra("ITEM_ID", itemId);
                            startActivity(mapIntent);
                        }
                    });

                    // Trouver les vues à l'intérieur de l'activitéView
                    TextView activityDescription = activityView.findViewById(R.id.activityDescription);
                    TextView confidenceTextView = activityView.findViewById(R.id.confidence);
                    TextView startDateTextView = activityView.findViewById(R.id.startDate);
                    TextView endDateTextView = activityView.findViewById(R.id.endDate);
                    CircularProgressIndicator circularProgressBar = activityView.findViewById(R.id.circularProgressBar);

                    // Formatez la date pour l'affichage
                    String formattedStartDate = startDate != null ? outputFormat.format(inputFormat.parse(startDate)) : "";
                    String formattedEndDate = endDate != null ? outputFormat.format(inputFormat.parse(endDate)) : "";

                    // Remplir les données dans les vues
                    activityDescription.setText(activity);
                    confidenceTextView.setText(confidence + "%");
                    circularProgressBar.setProgress(Integer.parseInt(confidence));
                    startDateTextView.setText(formattedStartDate);
                    endDateTextView.setText(formattedEndDate);

                    // Ajouter l'activitéView au conteneur LinearLayout avec les paramètres de layout
                    activityContainer.addView(activityView);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
        }
    }
    public void onDeleteClicked(long itemId) {
        // Création de la boîte de dialogue de confirmation
        new AlertDialog.Builder(this)
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet élément ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Appel de la méthode pour supprimer l'élément de la base de données
                        deleteActivityFromDb(itemId);

                        // Rafraîchir l'affichage
                        refreshActivityList();
                    }
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteActivityFromDb(long itemId) {
        // Obtenir l'accès à la base de données
        SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
        // Définir la condition de suppression
        String selection = MyContract.ActivityEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };
        // Exécuter la commande de suppression
        db.delete(MyContract.ActivityEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    private void refreshActivityList() {
        // Supprimer toutes les vues de l'activité dans le conteneur
        LinearLayout activityContainer = findViewById(R.id.activityContainer);
        activityContainer.removeAllViews();
        // Recharger les activités
        displayActivities(userId);
    }

    public void clearHistory(long userId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            String selection = MyContract.ActivityEntry.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = { String.valueOf(userId) };

            db.delete(MyContract.ActivityEntry.TABLE_NAME, selection, selectionArgs);

            LinearLayout activityContainer = findViewById(R.id.activityContainer);
            activityContainer.removeAllViews();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
        }
    }


}