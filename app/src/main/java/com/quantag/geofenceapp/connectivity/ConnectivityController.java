package com.quantag.geofenceapp.connectivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.quantag.geofenceapp.StateHolder;

/**
 * Controller used for managing connectivity service and choosing wifi spot.
 */
public class ConnectivityController {

    private Context        mContext;
    private ServiceManager mServiceManager;

    public ConnectivityController(Context context) {
        mContext = context;
        if (mServiceManager == null) {
            mServiceManager = new ServiceManager(context);
        }
    }

    public void startServiceIfNeeded() {
        if (mServiceManager != null) {
            if (!mServiceManager.isServiceStarted()) {
                mServiceManager.startService();
            }
        }
    }

    public void stopService() {
        if (mServiceManager != null) {
            mServiceManager.stopService();
        }
    }

    public void startWiFiPicker() {
        new StateHolder(mContext).setGeofenceWifiAdded(false);
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        mContext.startActivity(intent);
    }
}
