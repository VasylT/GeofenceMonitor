package com.quantag.geofenceapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quantag.geofenceapp.utilities.Constants;

/**
 * Controller used for displaying google map and managing marker position.
 */
class MapController {

    private static final String TAG = MapController.class.getSimpleName();

    private boolean isLocationSettled = false;

    private Context           context;
    private SharedPreferences sPrefs;
    private MapView           mapView;
    private GoogleMap         googleMap;
    private Marker            marker;
    private Location          markerLocation;

    private double  latitude;
    private double  longitude;
    private boolean isMapInitialized;
    private boolean isResumeCalled;

    MapController(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        sPrefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }

    void onResume() {
        if (mapView != null && isMapInitialized) {
            mapView.onResume();
        }
        isResumeCalled = true;
    }

    void onPause() {
        if (mapView != null && isMapInitialized) {
            mapView.onPause();
        }
        isResumeCalled = false;
    }

    void onLowMemory() {
        if (mapView != null && isMapInitialized) {
            mapView.onLowMemory();
        }
    }

    void onDestroy() {
        if (mapView != null && isMapInitialized) {
            mapView.onDestroy();
        }
    }

    /**
     * Asynchronously initialize google maps and load it to {@link mapView}.
     */
    void initializeMap() {
        if (!isMapInitialized) {
            restoreLastLocation();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mapView.onCreate(null);
                    } catch (Exception e) {
                        // Though exception occurred, maps were pre-loaded.
                    }
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mapView != null) {
                                try {
                                    // Load maps completely.
                                    mapView.onCreate(null);
                                    // Initialize map and set map ready observer.
                                    MapsInitializer.initialize(context);
                                    mapView.getMapAsync(new OnMapReadyCallback() {
                                        @Override
                                        public void onMapReady(GoogleMap googleMap) {
                                            initGoogleMap(googleMap);
                                        }
                                    });
                                    isMapInitialized = true;
                                    // Initialization may took time — call onResume manually.
                                    if (isResumeCalled) {
                                        mapView.onResume();
                                    }
                                } catch (Exception e) {
                                    mapView.setBackgroundColor(ContextCompat.getColor(context,
                                            android.R.color.darker_gray));
                                }
                            }
                        }
                    });
                }
            }).start();
        } else {
            Log.i(TAG, "Map is already initialized");
        }
    }

    /**
     * Get current location of the marker, save it to shared preferences.
     *
     * @return latitude plus longitude
     */
    LatLng getCurrentLocation() {
        if (isMapInitialized) {
            latitude = markerLocation.getLatitude();
            longitude = markerLocation.getLongitude();
            sPrefs.edit()
                    .putFloat(Constants.ARG_LAT, (float) latitude)
                    .putFloat(Constants.ARG_LON, (float) longitude)
                    .apply();
            return new LatLng(latitude, longitude);
        } else {
            return new LatLng(Constants.DEFAULT_LAT, Constants.DEFAULT_LON);
        }
    }

    /**
     * Initialize map ui components. Set draggable marker and checks for the last location, picked
     * either from network state or application preferences.
     *
     * @param googleMap google map ready to be used.
     */
    @SuppressWarnings("MissingPermission")
    private void initGoogleMap(GoogleMap googleMap) {
        Log.i(TAG, "initGoogleMap()");
        if (googleMap != null) {
            this.googleMap = googleMap;

            // Set last location.
            googleMap.setMyLocationEnabled(true);

            // Set map settings.
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(false);

            // Initialize marker.
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48px))
                    .draggable(true));

            // Set marker location.
            markerLocation = new Location("network");
            markerLocation.setLatitude(latitude);
            markerLocation.setLongitude(longitude);

            // Update map.
            setPosition(markerLocation);

            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    isLocationSettled = false;
                    return false;
                }
            });

            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    setPosition(location);
                    isLocationSettled = true;
                }
            });

            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    isLocationSettled = false;
                    Location location = new Location("network");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    setPosition(location);
                    isLocationSettled = true;
                }
            });

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    isLocationSettled = true;
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng latLng = marker.getPosition();
                    markerLocation.setLatitude(latLng.latitude);
                    markerLocation.setLongitude(latLng.longitude);
                }
            });
        } else {
            Toast.makeText(context, R.string.google_play_services_error, Toast.LENGTH_SHORT).show();
            mapView.setBackgroundColor(ContextCompat.getColor(context,
                    android.R.color.darker_gray));
        }
    }

    /**
     * Change map camera focus and marker position.
     *
     * @param location new position
     */
    private void setPosition(Location location) {
        if (!isLocationSettled) {
            // Move marker to given location.
            markerLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            marker.setPosition(latLng);
            // Animate camera focus to new position.
            CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            googleMap.animateCamera(position);
        }
    }

    /**
     * Get the last pointed location from shared preferences and saves its coordinates.
     */
    private void restoreLastLocation() {
        latitude = sPrefs.getFloat(Constants.ARG_LAT, (float) Constants.DEFAULT_LAT);
        longitude = sPrefs.getFloat(Constants.ARG_LON, (float) Constants.DEFAULT_LON);
    }
}
