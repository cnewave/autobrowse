package com.example.kent.androidwebview;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

public class AutoService extends Service {
    private DataModel mDataModel = null;
    private boolean firstLaunch = false;

    public AutoService() {
    }

    @Override
    public void onCreate() {
        // The service is being created
        Log.d(Common.TAG, "Service is started...");
        mDataModel = DataModel.getInstance(this);
        startForeground();
    }

    @Override
    public void onDestroy() {
        Log.d(Common.TAG, "Service is stopped...");
        canelAlarm();
        DataModel.release();
        mDataModel = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Log.d(Common.TAG, "onStartCommand...");
        if (mDataModel != null) {
            int value = 2;
            if (firstLaunch) {
                value = mDataModel.getNextInterval();
            } else {
                firstLaunch = true;
            }

            startNextAlarm(value);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private int ONGOING_NOTIFICATION_ID = 998;

    private void startForeground() {
        Log.d(Common.TAG, "Start foreground service.");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
//                        .setSmallIcon(R.drawable.notify)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }


    private void startNextAlarm(int nextInterval) {
        if (mDataModel == null || mDataModel.isReachMaximum()) {
            Log.d(Common.TAG, "Reach Maximum count, stop the service");
            stopSelf();
            return;
        }

        Log.d(Common.TAG, "startAlarm:" + nextInterval);
        Calendar cal = Calendar.getInstance();

        // 設定於 10 seconds
        cal.add(Calendar.SECOND, nextInterval);

        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);


//        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
//                1000 * 15, pi);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

    }
    private void canelAlarm(){
        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }
}
