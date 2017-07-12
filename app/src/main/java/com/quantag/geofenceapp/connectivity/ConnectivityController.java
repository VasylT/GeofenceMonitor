package com.quantag.geofenceapp.connectivity;

import android.content.Context;

public class ConnectivityController {

    private ServiceManager serviceManager;

    public ConnectivityController(Context context) {
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
}
