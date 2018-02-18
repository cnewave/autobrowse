package com.example.kent.androidwebview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        throw new UnsupportedOperationException("Not yet implemented");
        Log.d("AutoBrowse","Receive time out");
        Toast.makeText(context," time pu",Toast.LENGTH_LONG).show();
        // start activity
        Intent browseIntent = new Intent(context,BrowseActivity.class);
        browseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browseIntent);
    }
}
