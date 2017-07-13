package com.quantag.geofenceapp.connectivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.quantag.geofenceapp.StateHolder;

public class ConnectivityController {

    private Context           mContext;
    private ServiceManager    serviceManager;

    public ConnectivityController(Context context) {
        mContext = context;
        if (serviceManager == null) {
            serviceManager = new ServiceManager(context);
        }

    }

    public void startServiceIfNeeded() {
        if (serviceManager != null) {
            if (!serviceManager.isServiceStarted()) {
                serviceManager.startService();
            }
        }
    }

    public void stopService() {
        if (serviceManager != null) {
            serviceManager.stopService();
        }
    }

    public void startWiFiPicker() {
        new StateHolder(mContext).setGeofenceWifiAdded(false);
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        mContext.startActivity(intent);
    }
}
