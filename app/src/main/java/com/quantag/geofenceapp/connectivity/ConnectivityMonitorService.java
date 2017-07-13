package com.quantag.geofenceapp.connectivity;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quantag.geofenceapp.StateHolder;
import com.quantag.geofenceapp.utilities.Constants;

public class ConnectivityMonitorService extends Service {

    private static final String TAG = ConnectivityMonitorService.class.getSimpleName();

    private final IBinder mBinder = new Binder();

    private ConnectivityReceiver mConnectivityReceiver;
    private StateHolder          stateHolder;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        // Initialize user state controller.
        stateHolder = new StateHolder(this);

        // Create connectivity receiver.
        mConnectivityReceiver = new ConnectivityReceiver(this, new ConnectivityReceiver.IConnectivity() {
            @Override
            public void onConnected(NetworkInfo networkInfo) {
                Log.d(TAG, "on connected");
                String networkName = networkInfo.getExtraInfo();
                boolean isNewWiFi = stateHolder.onConnected(networkName);
                sendCallback(Constants.MSG_CONNECTED, networkName, isNewWiFi);
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "on disconnected");
                stateHolder.onDisconnected();
                sendCallback(Constants.MSG_DISCONNECTED);
            }
        });

        // Register receiver to connectivity change events.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mConnectivityReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_STICKY;
    }

    private void sendCallback(int message) {
        Intent intent = new Intent(Constants.ACTION_CONNECTIVITY);
        intent.putExtra(Constants.ARG_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendCallback(int message, String stringExtra, boolean booleanExtra) {
        Intent intent = new Intent(Constants.ACTION_CONNECTIVITY);
        intent.putExtra(Constants.ARG_MESSAGE, message);
        if (stringExtra != null) {
            intent.putExtra(Constants.ARG_STRING, stringExtra);
        }
        intent.putExtra(Constants.ARG_BOOLEAN, booleanExtra);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}