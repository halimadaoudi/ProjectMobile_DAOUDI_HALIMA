package com.example.projet_daoudi_halima;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.content.Context;
import java.text.SimpleDateFormat;
import android.content.ContentValues;
import java.util.Locale;
import java.util.Date;
import androidx.annotation.NonNull;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.util.Log;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {
    private TextView confidenceDebout, confidenceAssis, confidenceMarcher, confidenceSauter, confidenceCourir;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private int[] confidences = new int[5];
    private Handler handler = new Handler();
    private static final int UPDATE_INTERVAL = 60000; // 60 secondes
    private String[] activites = {"Assis", "Debout", "Marcher", "Sauter", "Courir"};  // Noms des activités
    private long userId;
    private String currentActivity = "";
    private int currentActivityConfidence = 0;
    private long currentActivityId = -1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION = 1;
    private Location currentLocation;
    private boolean activityEnding = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Initialisation des vues
        confidenceDebout = findViewById(R.id.confidenceDebout);
        confidenceAssis = findViewById(R.id.confidenceAssis);
        confidenceMarcher = findViewById(R.id.confidenceMarcher);
        confidenceSauter = findViewById(R.id.confidenceSauter);
        confidenceCourir = findViewById(R.id.confidenceCourir);

        // Initialisation du gestionnaire de capteurs et de l'accéléromètre
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Load user id
        userId = SignInActivity.getSessionUserId(this);

        // Navigation vers les pages :
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(R.id.navigation_home);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_history:
                        activityEnding = true;
                        saveCurrentActivityEndInfo(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                                startActivity(intent);
                            }
                        });
                        return true;
                    case R.id.navigation_profile:
                        activityEnding = true;
                        saveCurrentActivityEndInfo(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent2 = new Intent(HomeActivity.this, ProfileActivity.class);
                                startActivity(intent2);
                            }
                        });
                        return true;
                }
                return false;
            }
        });

        ImageView logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // supprimer user_id depuis session et quitter
                activityEnding = true;
                saveCurrentActivityEndInfo(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        TextView currentDateTextView = findViewById(R.id.current_date);
        setCurrentDate(currentDateTextView);

        // Initialisation
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };
        // Demander les permissions de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void setCurrentDate(TextView textView) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.FRENCH); // Format de la date en français
        String currentDate = dateFormat.format(new Date());
        textView.setText(currentDate);
    }

    //après que l'utilisateur a répondu à une demande de permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                  // mise a jour de localisation
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            } else {
                // La permission a été refusée. Implémentez la gestion du cas où l'utilisateur refuse la permission.
            }
        }
    }

    private int trouverMax() {
        int maxIndex = 0;
        for (int i = 1; i < confidences.length; i++) {
            if (confidences[i] > confidences[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private int calculateTotalConfidence() {
        int sum = 0;
        for (int value : confidences) {
            sum += value;
        }
        return sum;
    }
    private void updateConfidenceDisplay() {
        int totalConfidence = calculateTotalConfidence();
        if (totalConfidence > 0) {
            confidenceAssis.setText(String.format("%d%%", (confidences[0] * 100 / totalConfidence)));
            confidenceDebout.setText(String.format("%d%%", (confidences[1] * 100 / totalConfidence)));
            confidenceMarcher.setText(String.format("%d%%", (confidences[2] * 100 / totalConfidence)));
            confidenceSauter.setText(String.format("%d%%", (confidences[3] * 100 / totalConfidence)));
            confidenceCourir.setText(String.format("%d%%", (confidences[4] * 100 / totalConfidence)));

            int maxIndex = trouverMax();
            EnvoieNotification(activites[maxIndex]);
        }
    }
    private void EnvoieNotification(String activiteType) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "activity_notifications";
        String channelName = "Activity Notifications";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Activité détectée")
                .setContentText("Vous êtes actuellement en train de " + activiteType)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        notificationManager.notify(1, notification);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        sensorManager.unregisterListener(this);
    }

    private int sensorEventCount = 0;
    private static final int MIN_EVENTS_BEFORE_SAVE = 20;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (activityEnding) return;  // Ignore sensor events if activity is ending

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            final float alpha = 0.8f; // Constante du filtre passe-bas
            // Filtre passe-bas pour isoler la gravité
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Soustraction de la gravité pour obtenir l'accélération linéaire
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            int activite = TypeActivite(linear_acceleration);
            confidences[activite]++;
            updateConfidenceDisplay();

            sensorEventCount++;
            if (sensorEventCount > MIN_EVENTS_BEFORE_SAVE) {
                // Vérifier si le pourcentage dépasse 50%
                int totalConfidence = calculateTotalConfidence();
                int confidencePourcentage = (confidences[activite] * 100 / totalConfidence);

                if (totalConfidence > 0 && confidencePourcentage > 50) {
                    boolean confidenceAugmente = confidencePourcentage >= currentActivityConfidence + 3;

                    // Vérifier si la nouvelle activité est différente de l'activité actuelle
                    if (!activites[activite].equals(currentActivity)) {
                        // Terminer l'activité courante si elle existe
                        if (!currentActivity.isEmpty()) {
                            updateActivityEndDate(currentActivity, new Runnable() {
                                @Override
                                public void run() {
                                    // Callback after activity end date is updated
                                }
                            });
                        }
                        // Enregistrer une nouvelle activité
                        saveActivity(activite, confidencePourcentage);
                        currentActivity = activites[activite];
                        currentActivityConfidence = confidencePourcentage;
                    } else {
                        // Mise à jour de la confiance si elle augmente
                        if (confidenceAugmente) {
                            currentActivityConfidence = confidencePourcentage;
                            updateConfidence();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method can be left empty
    }

    private int TypeActivite(float[] acceleration) {
        // Simple activity classifier based on the magnitude of acceleration
        float x = acceleration[0];
        float y = acceleration[1];
        float z = acceleration[2];
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        if (magnitude < 2.0f) return 0; // Assis
        else if (magnitude < 4.0f) return 1; // Debout
        else if (magnitude < 7.0f) return 2; // Marche
        else if (magnitude < 10.0f) return 3; // Sauter
        else return 4; // Courir
    }



    private String getCurrentDateTime() {
        // Obtenir la date et l'heure actuelles au format texte
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // save start activity
    private void saveActivity(int activite, int confidencePercentage) {
        DatabaseHelper dbHelper = new DatabaseHelper(HomeActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyContract.ActivityEntry.COLUMN_ACTIVITY, activites[activite]);
        values.put(MyContract.ActivityEntry.COLUMN_CONFIDENCE, confidencePercentage);  // Utiliser le pourcentage de confiance
        values.put(MyContract.ActivityEntry.COLUMN_START_DATE, getCurrentDateTime());
        values.put(MyContract.ActivityEntry.COLUMN_USER_ID, userId);
        if (currentLocation != null) {
            values.put(MyContract.ActivityEntry.COLUMN_LATITUDE_START, currentLocation.getLatitude());
            values.put(MyContract.ActivityEntry.COLUMN_LONGITUDE_START, currentLocation.getLongitude());
        } else {
            Log.e("Location", "Localisation non disponible, activité non enregistrée.");
            return;  // Ne pas enregistrer si la localisation n'est pas disponible
        }

        long newRowId = db.insert(MyContract.ActivityEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            Log.e("Database", "Erreur lors de l'insertion de l'activité dans la base de données");
        } else {
            currentActivityId = newRowId;
            Log.d("Database", "Activité enregistrée avec succès: " + activites[activite] + " avec un pourcentage de confiance de " + confidencePercentage + "%");
        }
    }

    // modifier le pourcentage d'activité actuelle s'il augmente
    private void updateConfidence() {
        DatabaseHelper dbHelper = new DatabaseHelper(HomeActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MyContract.ActivityEntry.COLUMN_CONFIDENCE, currentActivityConfidence);

        String selection = MyContract.ActivityEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(currentActivityId)};

        int count = db.update(MyContract.ActivityEntry.TABLE_NAME, values, selection, selectionArgs);
        if (count == 0) {
            Log.e("Database", "Erreur lors de la mise à jour de l'activité");
        }
    }

    private void saveCurrentActivityEndInfo(final Runnable callback) {
        if (!currentActivity.isEmpty()) {
            updateActivityEndDate(currentActivity, new Runnable() {
                @Override
                public void run() {
                    currentActivity = "";
                    currentActivityConfidence = 0;
                    currentActivityId = -1;
                    callback.run();
                }
            });
        } else {
            callback.run();
        }
    }

    private void updateActivityEndDate(String activity, final Runnable callback) {
        if (currentActivityId != -1) {
            // Initialisation de la base de données
            DatabaseHelper dbHelper = new DatabaseHelper(HomeActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Mettre à jour la date de fin de l'activité dans la base de données
            ContentValues values = new ContentValues();
            values.put(MyContract.ActivityEntry.COLUMN_END_DATE, getCurrentDateTime());
            if (currentLocation != null) {
                values.put(MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH, currentLocation.getLatitude());
                values.put(MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH, currentLocation.getLongitude());
            } else {
                // Gérer l'absence de localisation
                values.put(MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH, 0);
                values.put(MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH, 0);
            }

            // Mise à jour de l'entrée d'activité avec currentActivityId
            String whereClause = MyContract.ActivityEntry.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(currentActivityId)};

            int count = db.update(MyContract.ActivityEntry.TABLE_NAME, values, whereClause, whereArgs);
            if (count == 0) {
                Log.e("Database", "Aucune activité à mettre à jour");
            } else {
                Log.d("Database", "Activité mise à jour avec succès: " + activity);
            }
            callback.run();
        } else {
            Log.e("Database", "currentActivityId est invalide");
            callback.run();
        }
    }
}
