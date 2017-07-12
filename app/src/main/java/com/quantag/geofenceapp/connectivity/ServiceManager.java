package com.quantag.geofenceapp.connectivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private Context mContext;

    private boolean serviceStarted = false;
    private Intent  serviceIntent  = null;

    public ServiceManager(Context context) {
        mContext = context;
        serviceIntent = new Intent(context, ConnectivityMonitorService.class);
    }

    public void startService() {
        Log.d(TAG, "start connectivity monitor service");
        serviceStarted = true;
        mContext.startService(serviceIntent);
    }

    public void stopService() {
        Log.d(TAG, "stop connectivity monitor service");
        serviceStarted = false;
        mContext.stopService(serviceIntent);
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

}
