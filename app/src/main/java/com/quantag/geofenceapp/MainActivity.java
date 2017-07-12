package com.quantag.geofenceapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GeofencingClient    mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent       mGeofencePendingIntent;

    private TextInputLayout      latitudeLayout;
    private TextInputLayout      longitudeLayout;
    private TextInputLayout      radiusLayout;
    private TextInputEditText    latitudePrompt;
    private TextInputEditText    longitudePrompt;
    private TextInputEditText    radiusPrompt;
    private FloatingActionButton geofenceSetButton;
    private TextView             geofenceStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        latitudeLayout = (TextInputLayout) findViewById(R.id.latitude_layout);
        longitudeLayout = (TextInputLayout) findViewById(R.id.longitude_layout);
        radiusLayout = (TextInputLayout) findViewById(R.id.radius_layout);
        latitudePrompt = (TextInputEditText) findViewById(R.id.latitude_prompt);
        longitudePrompt = (TextInputEditText) findViewById(R.id.longitude_prompt);
        radiusPrompt = (TextInputEditText) findViewById(R.id.radius_prompt);
        geofenceSetButton = (FloatingActionButton) findViewById(R.id.set_geofence_button);
        geofenceStatusView = (TextView) findViewById(R.id.geofence_status_view);

        mGeofenceList = new ArrayList<>();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofencePendingIntent = null;

        geofenceSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEditFields();
            }
        });

        radiusPrompt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    validateEditFields();
                    return true;
                }
                return false;
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
        geofenceSetButton.setEnabled(enable);
    }

    private void toggleGeofenceStatus(boolean inside) {
        if (inside) {
            geofenceStatusView.setBackgroundColor(ContextCompat.getColor(this, R.color.inside));
            geofenceStatusView.setText(R.string.geofence_inside);
        } else {
            geofenceStatusView.setBackgroundColor(ContextCompat.getColor(this, R.color.outside));
            geofenceStatusView.setText(R.string.geofence_outside);
        }
    }

    /**
     * Add new circular area geofence based on the given parameters.
     * Also sets expiration time and monitored transitions.
     *
     * @param key       request ID of the geofence.
     * @param latitude  latitude of geographical coordinates of the geofence.
     * @param longitude longitude of geographical coordinates of the geofence.
     * @param radius    geofence radius in meters.
     */
    private void populateGeofenceList(String key, double latitude, double longitude, float radius) {
        Log.i(TAG, "add geofence area: " + latitude + ":" + longitude);
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(latitude, longitude, radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_TIME)
                .build());
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        Log.i(TAG, "number of geofences:" + mGeofenceList.size());
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeofenceList)
                .build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent == null) {
            mGeofencePendingIntent = PendingIntent.getService(
                    this,
                    0,
                    new Intent(this, GeofenceService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
    }

    /**
     * Adds geofences.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        mGeofencingClient.addGeofences(
                getGeofencingRequest(),
                getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String msg = getString(R.string.geofences_set);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String msg = GeofenceErrorMessages.getErrorString(MainActivity.this, e);
                        Log.w(TAG, msg);
                    }
                });
    }

    /**
     * Removes geofences.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (checkPermissions()) {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String msg = GeofenceErrorMessages.getErrorString(MainActivity.this, e);
                            Log.w(TAG, msg);
                        }
                    });
        }
    }

    private void updateGeofence(double latitude, double longitude, float radius) {
        removeGeofences();
        mGeofenceList.clear();
        populateGeofenceList(Constants.GEOFENCE_KEY, latitude, longitude, radius);
        addGeofences();
    }

    private void validateEditFields() {
        if (checkEditField(latitudePrompt, latitudeLayout)
                && checkEditField(longitudePrompt, longitudeLayout)
                && checkEditField(radiusPrompt, radiusLayout)) {
            double latitude = Double.valueOf(latitudePrompt.getText().toString());
            double longitude = Double.valueOf(longitudePrompt.getText().toString());
            float radius = Float.valueOf(radiusPrompt.getText().toString());
            updateGeofence(latitude, longitude, radius);
        }
    }

    private boolean checkEditField(TextInputEditText inputEditText, TextInputLayout inputLayout) {
        boolean valid = false;
        if (TextUtils.isEmpty(inputEditText.getText().toString())) {
            inputLayout.setError(getResources().getString(R.string.field_is_empty));
        } else {
            inputLayout.setError(null);
            valid = true;
        }
        return valid;
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
                                    Constants.LOCATION_PERMISSIONS_RC);
                        }
                    }).show();
        } else {
            // Requesting permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSIONS_RC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.LOCATION_PERMISSIONS_RC) {
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
