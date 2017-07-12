package com.quantag.geofenceapp.connectivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import com.quantag.geofenceapp.utilities.Constants;

public class ConnectivityController {

    private Context           mContext;
    private SharedPreferences sPrefs;
    private ServiceManager    serviceManager;

    public ConnectivityController(Context context) {
        mContext = context;
        if (serviceManager == null) {
            serviceManager = new ServiceManager(context);
        }
        sPrefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
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
        setGeofenceWifiAdded(false);
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        mContext.startActivity(intent);
    }

    private void setGeofenceWifiAdded(boolean added) {
        sPrefs.edit().putBoolean(Constants.ARG_WIFI_ADDED, added).apply();
    }

    private boolean isGeofenceWiFiAdded() {
        return sPrefs.getBoolean(Constants.ARG_WIFI_ADDED, false);
    }

    private void setConnected(boolean connected) {
        sPrefs.edit().putBoolean(Constants.ARG_WIFI_CONNECTED, connected).apply();
    }

    public String getGeofenceWiFiName() {
        return sPrefs.getString(Constants.ARG_WIFI_NAME, "");
    }

    public boolean onConnected(String name) {
        if (!isGeofenceWiFiAdded()) {
            sPrefs.edit().putString(Constants.ARG_WIFI_NAME, name).apply();
            setGeofenceWifiAdded(true);
            setConnected(true);
            return true;
        } else {
            boolean connectedToRightWifi = name.equals(getGeofenceWiFiName());
            setConnected(connectedToRightWifi);
            return false;
        }
    }

    public void onDisconnected() {
        if (isGeofenceWiFiAdded()) {
            sPrefs.edit().putBoolean(Constants.ARG_WIFI_CONNECTED, false).apply();
        }
    }

    public boolean isConnected() {
        return sPrefs.getBoolean(Constants.ARG_WIFI_CONNECTED, false);
    }
}
