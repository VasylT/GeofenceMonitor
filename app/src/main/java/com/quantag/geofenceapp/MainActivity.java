package com.quantag.geofenceapp;

import android.Manifest;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.quantag.geofenceapp.GeofenceEventsReceiver.IGeofenceEventsReceiver;

public class MainActivity extends AppCompatActivity implements IGeofenceEventsReceiver {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextInputLayout      latitudeLayout;
    private TextInputLayout      longitudeLayout;
    private TextInputLayout      radiusLayout;
    private TextInputEditText    latitudePrompt;
    private TextInputEditText    longitudePrompt;
    private TextInputEditText    radiusPrompt;
    private FloatingActionButton geofenceSetButton;
    private TextView             geofenceStatusView;

    private GeofenceController geofenceController;
    private GeofenceEventsReceiver geofenceEventsReceiver = new GeofenceEventsReceiver(this);

    private MapController mapController;

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
        MapView mapView = (MapView)  findViewById(R.id.map_view);

        latitudeLayout.setErrorEnabled(true);
        longitudeLayout.setErrorEnabled(true);
        radiusLayout.setErrorEnabled(true);

        latitudePrompt.setText(String.valueOf(Constants.DEFAULT_LAT));
        longitudePrompt.setText(String.valueOf(Constants.DEFAULT_LON));
        radiusPrompt.setText(String.valueOf(Constants.DEFAULT_RADIUS));

        geofenceSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentMapLocation();
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

        geofenceController = new GeofenceController(this);
        mapController = new MapController(this, mapView);
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean permissionGranted = checkPermissions();
        toggleGeofenceButton(permissionGranted);
        if (!permissionGranted) {
            requestPermissions();
        } else {
            mapController.initializeMap();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        geofenceEventsReceiver.register(this);
        mapController.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        geofenceEventsReceiver.unregister(this);
        mapController.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapController.onLowMemory();
    }

    @Override
    public void onDestroy() {
        mapController.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onEnterGeofence() {
        toggleGeofenceStatus(true);
    }

    @Override
    public void onExitGeofence() {
        toggleGeofenceStatus(false);
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

    private void getCurrentMapLocation() {
        LatLng latLng = mapController.getCurrentLocation();
        latitudePrompt.setText(String.valueOf(latLng.latitude));
        longitudePrompt.setText(String.valueOf(latLng.longitude));
    }

    private void validateEditFields() {
        Utils.hideKeyboard(MainActivity.this);

        boolean isValid = checkEditField(latitudePrompt, latitudeLayout)
                && checkEditField(longitudePrompt, longitudeLayout)
                && checkEditField(radiusPrompt, radiusLayout);
        if (isValid) {
            // Reset location status to 'outside'
            toggleGeofenceStatus(false);

            // Update geofence with new values.
            double latitude = Double.valueOf(latitudePrompt.getText().toString());
            double longitude = Double.valueOf(longitudePrompt.getText().toString());
            float radius = Float.valueOf(radiusPrompt.getText().toString());
            geofenceController.updateGeofence(latitude, longitude, radius);
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
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
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
