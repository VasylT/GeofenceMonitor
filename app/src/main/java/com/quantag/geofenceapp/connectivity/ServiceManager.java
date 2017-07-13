package com.quantag.geofenceapp.connectivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private Context mContext;

    private boolean serviceStarted = false;
    private Intent  serviceIntent  = null;

    ServiceManager(Context context) {
        mContext = context;
        serviceIntent = new Intent(context, ConnectivityMonitorService.class);
    }

    void startService() {
        Log.d(TAG, "start connectivity monitor service");
        serviceStarted = true;
        mContext.startService(serviceIntent);
    }

    void stopService() {
        Log.d(TAG, "stop connectivity monitor service");
        serviceStarted = false;
        mContext.stopService(serviceIntent);
    }

    boolean isServiceStarted() {
        return serviceStarted;
    }

}
