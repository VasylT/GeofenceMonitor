package com.quantag.geofenceapp.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.quantag.geofenceapp.utilities.Constants;

public class GeofenceEventsReceiver extends BroadcastReceiver {
    private boolean isRegistered = false;

    public interface IGeofenceEventsReceiver {
        void onEnterGeofence();
        void onExitGeofence();
    }

    private IGeofenceEventsReceiver mListener;

    public GeofenceEventsReceiver(IGeofenceEventsReceiver listener) {
        mListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int message = intent.getIntExtra(Constants.ARG_MESSAGE, 0);
        switch (message) {
            case Constants.MSG_ENTER:
                mListener.onEnterGeofence();
                break;
            case Constants.MSG_EXIT:
                mListener.onExitGeofence();
                break;
            default:
        }
    }

    public void register(Context context) {
        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(Constants.ACTION_GEOFENCE);
            LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
            isRegistered = true;
        }
    }

    public void unregister(Context context) {
        if (isRegistered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            isRegistered = false;
        }
    }
}
