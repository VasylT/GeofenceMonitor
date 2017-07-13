package com.quantag.geofenceapp.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.quantag.geofenceapp.utilities.Constants;

public class ConnectivityEventsReceiver extends BroadcastReceiver {

    private boolean isRegistered = false;

    public interface IConnectivityReceiver {
        void onConnected(String extraInfo, boolean isNewWiFi);

        void onDisconnected();
    }

    private IConnectivityReceiver mListener;

    public ConnectivityEventsReceiver(IConnectivityReceiver listener) {
        mListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int message = intent.getIntExtra(Constants.ARG_MESSAGE, 0);
        switch (message) {
            case Constants.MSG_CONNECTED:
                String extraInfo = intent.getStringExtra(Constants.ARG_STRING);
                boolean isNewWiFi = intent.getBooleanExtra(Constants.ARG_BOOLEAN, false);
                mListener.onConnected(extraInfo, isNewWiFi);
                break;
            case Constants.MSG_DISCONNECTED:
                mListener.onDisconnected();
                break;
            default:
        }
    }

    public void register(Context context) {
        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(Constants.ACTION_CONNECTIVITY);
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
