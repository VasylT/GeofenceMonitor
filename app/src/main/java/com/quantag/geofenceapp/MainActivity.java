package com.quantag.geofenceapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG                      = MainActivity.class.getSimpleName();
    private static final int    LOCATION_PERMISSIONS_RC  = 101;

    private TextInputEditText latitudePrompt;
    private TextInputEditText longitudePrompt;
    private TextInputEditText radiusPrompt;
    private Button            setGeofenceButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        latitudePrompt = (TextInputEditText) findViewById(R.id.latitude_prompt);
        longitudePrompt = (TextInputEditText) findViewById(R.id.longitude_prompt);
        radiusPrompt = (TextInputEditText) findViewById(R.id.radius_prompt);
        setGeofenceButton = (Button) findViewById(R.id.set_geofence_button);

        setGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude = Double.valueOf(latitudePrompt.getText().toString());
                double longitude = Double.valueOf(longitudePrompt.getText().toString());
                float radius = Float.valueOf(radiusPrompt.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean permissionGranted = checkPermissions();
        toggleGeofenceButton(permissionGranted);
        if (!permissionGranted) {
            requestPermissions();
        }
    }

    private void toggleGeofenceButton(boolean enable) {
        setGeofenceButton.setEnabled(enable);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            // Show rationale
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.permission_rationale),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSIONS_RC);
                        }
                    }).show();
        } else {
            // Requesting permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_RC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_RC) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    toggleGeofenceButton(true);
                } else {
                    // Show more rationale
                    Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.permission_denied_explanation),
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.settings), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            }).show();
                }
            }
        }
    }
}
