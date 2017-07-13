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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.quantag.geofenceapp.connectivity.ConnectivityController;
import com.quantag.geofenceapp.connectivity.ConnectivityEventsReceiver;
import com.quantag.geofenceapp.connectivity.ConnectivityEventsReceiver.IConnectivityReceiver;
import com.quantag.geofenceapp.geofence.GeofenceController;
import com.quantag.geofenceapp.geofence.GeofenceEventsReceiver;
import com.quantag.geofenceapp.geofence.GeofenceEventsReceiver.IGeofenceEventsReceiver;
import com.quantag.geofenceapp.utilities.Constants;
import com.quantag.geofenceapp.utilities.Utils;

public class MainActivity extends AppCompatActivity implements IGeofenceEventsReceiver, IConnectivityReceiver {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextInputLayout      latitudeLayout;
    private TextInputLayout      longitudeLayout;
    private TextInputLayout      radiusLayout;
    private TextInputEditText    latitudePrompt;
    private TextInputEditText    longitudePrompt;
    private TextInputEditText    radiusPrompt;
    private TextView             wifiView;
    private TextView             geofenceStatusView;
    private FloatingActionButton geofenceSetButton;
    private FloatingActionButton locationGetButton;
    private FloatingActionButton wifiSetButton;
    private TextView             geofenceSetHintView;
    private TextView             locationGetHintView;
    private TextView             wifiSetHintView;

    private boolean isMenuOpened  = false;
    private boolean isMenuOpening = false;

    private GeofenceController     geofenceController;
    private ConnectivityController connectivityController;
    private StateHolder            stateHolder;

    private GeofenceEventsReceiver     geoEventsReceiver  = new GeofenceEventsReceiver(this);
    private ConnectivityEventsReceiver connEventsReceiver = new ConnectivityEventsReceiver(this);

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
        wifiView = (TextView) findViewById(R.id.wifi_name_view);
        geofenceStatusView = (TextView) findViewById(R.id.geofence_status_view);
        geofenceSetButton = (FloatingActionButton) findViewById(R.id.set_geofence_button);
        locationGetButton = (FloatingActionButton) findViewById(R.id.get_location_button);
        wifiSetButton = (FloatingActionButton) findViewById(R.id.set_wifi_button);
        geofenceSetHintView = (TextView) findViewById(R.id.set_geofence_hint_view);
        locationGetHintView = (TextView) findViewById(R.id.get_location_hint_view);
        wifiSetHintView = (TextView) findViewById(R.id.set_wifi_hint_view);
        FloatingActionButton menuButton = (FloatingActionButton) findViewById(R.id.menu_button);
        MapView mapView = (MapView) findViewById(R.id.map_view);

        // Make edit fields be prepared for errors.
        latitudeLayout.setErrorEnabled(true);
        longitudeLayout.setErrorEnabled(true);
        radiusLayout.setErrorEnabled(true);

        // Display default geofence values.
        radiusPrompt.setText(String.valueOf(Constants.DEFAULT_RADIUS));

        geofenceSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateEditFields();
            }
        });

        locationGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentMapLocation();
            }
        });

        wifiSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectivityController.startWiFiPicker();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuOpening) {
                    isMenuOpening = true;
                    if (!isMenuOpened) {
                        openMenu();
                    } else {
                        closeMenu();
                    }
                    isMenuOpening = false;
                }
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

        // Init controllers.
        stateHolder = new StateHolder(this);
        mapController = new MapController(this, mapView);
        geofenceController = new GeofenceController(this);
        connectivityController = new ConnectivityController(this);

        // Display wifi name.
        String wifiName = stateHolder.getGeofenceWiFiName();
        displayWiFiName(wifiName);
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
        Log.i(TAG, "onResume");
        mapController.onResume();
        geoEventsReceiver.register(this);
        connEventsReceiver.register(this);
        connectivityController.startServiceIfNeeded();
        considerGeofenceStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        geoEventsReceiver.unregister(this);
        connEventsReceiver.unregister(this);
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
        Log.i(TAG, "enter geofence");
        considerGeofenceStatus();
    }

    @Override
    public void onExitGeofence() {
        Log.i(TAG, "exit geofence");
        considerGeofenceStatus();
    }

    @Override
    public void onConnected(String extraInfo, boolean isNewWiFi) {
        Log.i(TAG, "network connected to: " + extraInfo);
        if (isNewWiFi) {
            displayWiFiName(extraInfo);
        }
        considerGeofenceStatus();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "network disconnected");
        considerGeofenceStatus();
    }

    private void considerGeofenceStatus() {
        Log.i(TAG, "consider connected: " + stateHolder.isConnected()
                + " inside: " + stateHolder.isInside());
        boolean isInside = stateHolder.isConnected() || stateHolder.isInside();
        toggleGeofenceStatus(isInside);
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

    private void displayWiFiName(String name) {
        String title = String.format(getResources().getString(R.string.wifi_name), name);
        wifiView.setText(title);
    }

    private void getCurrentMapLocation() {
        LatLng latLng = mapController.getCurrentLocation();
        latitudePrompt.setText(String.valueOf(latLng.latitude));
        longitudePrompt.setText(String.valueOf(latLng.longitude));
    }

    private void openMenu() {
        isMenuOpened = true;
        float geofenceRange = -getResources().getDimension(R.dimen.geofence_fab_transition_height);
        float locationRange = -getResources().getDimension(R.dimen.location_fab_transition_height);
        float wifiRange = -getResources().getDimension(R.dimen.wifi_fab_transition_height);

        // Animate FABs
        geofenceSetButton.animate().translationY(geofenceRange);
        locationGetButton.animate().translationY(locationRange);
        wifiSetButton.animate().translationY(wifiRange);

        // Animate hint views
        geofenceSetHintView.animate().translationY(geofenceRange);
        locationGetHintView.animate().translationY(locationRange);
        wifiSetHintView.animate().translationY(wifiRange);
        geofenceSetHintView.animate().alpha(1.0f);
        locationGetHintView.animate().alpha(1.0f);
        wifiSetHintView.animate().alpha(1.0f);
        geofenceSetHintView.setVisibility(View.VISIBLE);
        locationGetHintView.setVisibility(View.VISIBLE);
        wifiSetHintView.setVisibility(View.VISIBLE);
    }

    private void closeMenu() {
        isMenuOpened = false;

        // Animate FABs
        geofenceSetButton.animate().translationY(0);
        locationGetButton.animate().translationY(0);
        wifiSetButton.animate().translationY(0);

        // Animate hint views
        geofenceSetHintView.animate().translationY(0);
        locationGetHintView.animate().translationY(0);
        wifiSetHintView.animate().translationY(0);
        geofenceSetHintView.animate().alpha(0.0f);
        locationGetHintView.animate().alpha(0.0f);
        wifiSetHintView.animate().alpha(0.0f);
        geofenceSetHintView.setVisibility(View.GONE);
        locationGetHintView.setVisibility(View.GONE);
        wifiSetHintView.setVisibility(View.GONE);
    }

    private void validateEditFields() {
        Utils.hideKeyboard(MainActivity.this);

        boolean isValid = checkEditField(latitudePrompt, latitudeLayout)
                && checkEditField(longitudePrompt, longitudeLayout)
                && checkEditField(radiusPrompt, radiusLayout);
        if (isValid) {
            // Reset location status to 'outside'
            stateHolder.setInsideStatus(false);
            considerGeofenceStatus();

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