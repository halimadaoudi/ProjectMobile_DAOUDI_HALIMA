package com.example.projet_daoudi_halima;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.util.Log;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        long itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId != -1) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    MyContract.ActivityEntry.TABLE_NAME,
                    new String[]{
                            MyContract.ActivityEntry.COLUMN_LATITUDE_START,
                            MyContract.ActivityEntry.COLUMN_LONGITUDE_START,
                            MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH,
                            MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH
                    },
                    MyContract.ActivityEntry.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(itemId)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                double startLat = cursor.getDouble(cursor.getColumnIndex(MyContract.ActivityEntry.COLUMN_LATITUDE_START));
                double startLng = cursor.getDouble(cursor.getColumnIndex(MyContract.ActivityEntry.COLUMN_LONGITUDE_START));
                double endLat = cursor.getDouble(cursor.getColumnIndex(MyContract.ActivityEntry.COLUMN_LATITUDE_FINISH));
                double endLng = cursor.getDouble(cursor.getColumnIndex(MyContract.ActivityEntry.COLUMN_LONGITUDE_FINISH));

                if (startLat != 0 && startLng != 0 && endLat != 0 && endLng != 0) {
                    LatLng startPoint = new LatLng(startLat, startLng);
                    LatLng endPoint = new LatLng(endLat, endLng);

                    map.addMarker(new MarkerOptions().position(startPoint).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Départ"));
                    map.addMarker(new MarkerOptions().position(endPoint).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("Arrivée"));

                    int markerColor = Color.parseColor("#0F706E");
                    map.addPolyline(new PolylineOptions().add(startPoint, endPoint).width(6).color(markerColor));

                    // Ensure the map is laid out before setting the bounds.
                    map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            LatLngBounds bounds = new LatLngBounds.Builder().include(startPoint).include(endPoint).build();
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        }
                    });
                } else {
                    Log.e("MapActivity", "Coordonnées de localisation invalides.");
                }
            }
            cursor.close();
            db.close();
        }
    }

}
