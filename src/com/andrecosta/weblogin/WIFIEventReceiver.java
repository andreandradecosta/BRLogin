package com.andrecosta.weblogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class WIFIEventReceiver extends BroadcastReceiver {

    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(MainActivity.LOG_TAG, "WIFIEventReceiver.onReceive:" + intent.getAction());
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null) {
            Log.i(MainActivity.LOG_TAG, "   > No active net");
            stopService(context);
        } else {
            Log.i(MainActivity.LOG_TAG, "   > " + netInfo.getTypeName() + ", connected:" + netInfo.isConnected());
            ConnectionHelper connectionHelper = new ConnectionHelper(context);
            if (netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI && connectionHelper.isConnectedToKnownWifi()) {
                Log.d(MainActivity.LOG_TAG, "   > SSID:" + connectionHelper.getSSDIName());
                startService(context);
            } else {
                stopService(context);
            }
        }
    }

    private void startService(Context context) {
        context.startService(new Intent(context, ConnectionManagerService.class));
    }

    private void stopService(Context context) {
        context.stopService(new Intent(context, ConnectionManagerService.class));
    }
  
}
