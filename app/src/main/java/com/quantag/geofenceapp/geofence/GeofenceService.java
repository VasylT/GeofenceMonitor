package com.quantag.geofenceapp.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.quantag.geofenceapp.StateHolder;
import com.quantag.geofenceapp.utilities.Constants;
import com.quantag.geofenceapp.R;

/**
 * Listener for geofence transition changes.
 */
public class GeofenceService extends IntentService {

    private static final String TAG = GeofenceService.class.getSimpleName();

    private StateHolder stateHolder;

    public GeofenceService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stateHolder = new StateHolder(this);
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                onEnterGeofence();
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                onExitGeofence();
            } else {
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            }
        } else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this, geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
        }
    }

    private void onEnterGeofence() {
        stateHolder.setInsideStatus(true);
        sendCallback(Constants.MSG_ENTER);
    }

    private void onExitGeofence() {
        stateHolder.setInsideStatus(false);
        sendCallback(Constants.MSG_EXIT);
    }

    private void sendCallback(int message) {
        Intent intent = new Intent(Constants.ACTION_GEOFENCE);
        intent.putExtra(Constants.ARG_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
