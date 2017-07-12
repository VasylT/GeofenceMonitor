package com.quantag.geofenceapp.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.quantag.geofenceapp.utilities.Utils;

public class ConnectivityReceiver extends BroadcastReceiver {

    Context             mContext;
    ConnectivityManager mConnectivityManager;
    IConnectivity       mCallback;

    public interface IConnectivity {
        void onConnected(NetworkInfo networkInfo);

        void onDisconnected();
    }

    public ConnectivityReceiver(Context context, IConnectivity callback) {
        this.mContext = context;
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (networkInfo == null) {
                onDisconnected();
            } else {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    onConnected(networkInfo);
                } else {
                    if (Utils.isNetworkActive(mContext)) {
                        onConnected(networkInfo);
                    } else {
                        onDisconnected();
                    }
                }
            }
        }
    }

    void onConnected(NetworkInfo networkInfo) {
        mCallback.onConnected(networkInfo);
    }

    void onDisconnected() {
        mCallback.onDisconnected();
    }
}
