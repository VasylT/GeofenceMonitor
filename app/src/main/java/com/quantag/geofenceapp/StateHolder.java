package com.quantag.geofenceapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.quantag.geofenceapp.utilities.Constants;

public class StateHolder {

    private SharedPreferences sPrefs;

    public StateHolder(Context context) {
        sPrefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }

    public void setGeofenceWifiAdded(boolean added) {
        sPrefs.edit().putBoolean(Constants.ARG_WIFI_ADDED, added).apply();
    }

    public String getGeofenceWiFiName() {
        return sPrefs.getString(Constants.ARG_WIFI_NAME, "");
    }

    private boolean isGeofenceWiFiAdded() {
        return sPrefs.getBoolean(Constants.ARG_WIFI_ADDED, false);
    }

    private void setConnected(boolean connected) {
        sPrefs.edit().putBoolean(Constants.ARG_WIFI_CONNECTED, connected).apply();
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

    public void setInsideStatus(boolean inside) {
        sPrefs.edit().putBoolean(Constants.ARG_INSIDE_GEOFENCE, inside).apply();
    }

    public boolean isInside() {
        return sPrefs.getBoolean(Constants.ARG_INSIDE_GEOFENCE, false);
    }
}
