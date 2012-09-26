package com.andrecosta.weblogin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

public class WIFIEventReceiver extends BroadcastReceiver {

    private enum ConnectionAction {
        CONNECTED, NOT_CONNECTED, NONE;
    }
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(MainActivity.LOG_TAG, "WIFIEventReceiver.onReceive"); 
        if (SettingsUtil.isSettingsDefined(context)) {
            new AsyncTask<Void, Void, ConnectionAction>() {
                @Override
                protected ConnectionAction doInBackground(Void... params) {
                    NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get(WifiManager.EXTRA_NETWORK_INFO);
                    Log.d(MainActivity.LOG_TAG, "\tState:" + netInfo.getState());
                    ConnectionHelper connectionHelper = new ConnectionHelper(context);
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED && connectionHelper.isConnectedToKnownWifi()) {
                        if (!connectionHelper.isNetConnectionPossible()) {
                            Log.d(MainActivity.LOG_TAG, "\t going to authenticate");
                            connectionHelper.authenticate();
                            return connectionHelper.isNetConnectionPossible()? ConnectionAction.CONNECTED: ConnectionAction.NOT_CONNECTED;
                        }
                    }
                    return ConnectionAction.NONE;
                }
                
                @Override
                protected void onPostExecute(ConnectionAction result) {
                    if (result.equals(ConnectionAction.CONNECTED)) {
                        SettingsUtil.incStat(context);
                    } else if (result.equals(ConnectionAction.NOT_CONNECTED)) {
                        fireNotificaionAlert(context);
                    }
                }
                
            }.execute((Void)null);
            
        }
    }

    protected void fireNotificaionAlert(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence text = context.getText(R.string.alertstatus_text);
        Notification notification = new Notification(R.drawable.ic_stat_alert, text, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL; 
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, "BR Login", text, pendingIntent);
        notificationManager.notify(1, notification);
    }

  
}
