package com.example.kent.androidwebview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("AutoBrowse","Receive time out");
        // start activity
        Intent browseIntent = new Intent(context,BrowseActivity.class);
        browseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browseIntent);
        // start service with command
        Intent serverIntent = new Intent(context, AutoService.class);
        context.startService(serverIntent);
    }
}
