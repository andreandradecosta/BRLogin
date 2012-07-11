package com.andrecosta.weblogin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

public class WIFIEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(MainActivity.LOG_TAG, "WIFIEventReceiver.onReceive"); 
        if (SettingsUtil.isSettingsDefined(context)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get(WifiManager.EXTRA_NETWORK_INFO);
                    Log.d(MainActivity.LOG_TAG, "\tState:" + netInfo.getState());
                    MyConnectionManager connMan = new MyConnectionManager(context);
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED && connMan.isConnectedToKnownWifi()) {
                        if (!connMan.isNetConnectionPossible()) {
                            Log.d(MainActivity.LOG_TAG, "\t going to authenticate");
                            connMan.authenticate();
                            SettingsUtil.incStat(context);
                        }
                    }
                    return null;
                }
            }.execute((Void)null);
            
        }
    }

  
}
