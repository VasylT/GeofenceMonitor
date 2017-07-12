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

import com.quantag.geofenceapp.utilities.Constants;

public class ConnectivityMonitorService extends Service {

    private static final String TAG = ConnectivityMonitorService.class.getSimpleName();

    private final IBinder mBinder = new Binder();

    ConnectivityReceiver mConnectivityReceiver;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        // Create connectivity receiver.
        mConnectivityReceiver = new ConnectivityReceiver(this, new ConnectivityReceiver.IConnectivity() {
            @Override
            public void onConnected(NetworkInfo networkInfo) {
                Log.d(TAG, "on connected");
                sendCallback(Constants.MSG_CONNECTED, networkInfo.getExtraInfo());
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "on disconnected");
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
        sendCallback(message, null);
    }

    private void sendCallback(int message, String stringExtra) {
        Intent intent = new Intent(Constants.ACTION_CONNECTIVITY);
        intent.putExtra(Constants.ARG_MESSAGE, message);
        if (stringExtra != null) {
            intent.putExtra(Constants.ARG_STRING, stringExtra);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}