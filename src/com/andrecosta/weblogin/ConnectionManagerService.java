package com.andrecosta.weblogin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

public class ConnectionManagerService extends Service {
    
    private Looper mServiceLooper;
    private Handler mHandler;
    
    private Runnable mHandlerRunnable  = new Runnable() {
        @Override
        public void run() {
            Log.i(MainActivity.LOG_TAG, "Service.run");
            if (maintainConnection()) {
              mHandler.postDelayed(this, 1000 * 60 * 60);
            }
        }
    };
    
    
    private boolean maintainConnection() {
        ConnectionHelper connectionHelper = new ConnectionHelper(this);
        if (connectionHelper.isConnectedToKnownWifi()) {
            if (!connectionHelper.isNetConnectionPossible()) {
                Log.i(MainActivity.LOG_TAG, "   > Going to authenticate ...");
                connectionHelper.authenticate();
                if (connectionHelper.isNetConnectionPossible()) {
                    SettingsUtil.incStat(this);
                } else {
                    fireNotificaionAlert();
                }
            }
            return true;
        } else {
            return false;
        }
    }
        
    protected void fireNotificaionAlert() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence text = getText(R.string.alertstatus_text);
        Notification notification = new Notification(R.drawable.ic_stat_alert, text, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL; 
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(this, "BR Login", text, pendingIntent);
        notificationManager.notify(1, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(MainActivity.LOG_TAG, "Service.Create");
        HandlerThread thread = new HandlerThread("ConnectionManagerThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mHandler = new Handler(mServiceLooper);
        mHandler.post(mHandlerRunnable);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(MainActivity.LOG_TAG, "Service.onStartCommand:" + flags + "," + startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MainActivity.LOG_TAG, "Service.onDestroy");
        mHandler.removeCallbacks(mHandlerRunnable);
        mServiceLooper.quit();
    }
}
