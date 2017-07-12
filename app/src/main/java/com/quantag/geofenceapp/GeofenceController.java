package com.quantag.geofenceapp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class GeofenceController {

    private static final String TAG = GeofenceController.class.getSimpleName();

    private Context             mContext;
    private GeofencingClient    mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent       mGeofencePendingIntent;

    public GeofenceController(Context context) {
        mContext = context;
        mGeofenceList = new ArrayList<>();
        mGeofencingClient = LocationServices.getGeofencingClient(context);
        mGeofencePendingIntent = null;
    }

    /**
     * Template to update geofence area coordinates and radius.
     *
     * @param latitude  latitude of geographical coordinates of the geofence.
     * @param longitude longitude of geographical coordinates of the geofence.
     * @param radius    geofence radius in meters.
     */
    public void updateGeofence(double latitude, double longitude, float radius) {
        removeGeofences();
        mGeofenceList.clear();
        populateGeofenceList(Constants.GEOFENCE_KEY, latitude, longitude, radius);
        addGeofences();
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
                    mContext,
                    0,
                    new Intent(mContext, GeofenceService.class),
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String msg = mContext.getString(R.string.geofences_set);
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String msg = GeofenceErrorMessages.getErrorString(mContext, e);
                        Log.w(TAG, msg);
                    }
                });
    }

    /**
     * Removes geofences.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String msg = GeofenceErrorMessages.getErrorString(mContext, e);
                        Log.w(TAG, msg);
                    }
                });
    }
}
