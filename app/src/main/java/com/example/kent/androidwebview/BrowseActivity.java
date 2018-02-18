package com.example.kent.androidwebview;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class BrowseActivity extends AppCompatActivity {
    private int TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wakeLock();
        postDelay();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        releaseLock();
    }
    private void postDelay(){
        new Handler().postDelayed(new Runnable(){
            public void run(){
                //處理少量資訊或UI
                Log.d(Common.TAG,"Close the Activity");
                finish();
            }
        }, TIME_OUT);
    }

    PowerManager.WakeLock wakeLock;
    private void wakeLock(){
        Log.d(Common.TAG,"wake lock");
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), Common.TAG);
        wakeLock.acquire();


        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
    }

    private void releaseLock(){
        Log.d(Common.TAG,"release lock");
//        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if(wakeLock != null && wakeLock.isHeld()) {
            Log.d(Common.TAG,"release lock done");
            wakeLock.release();
        }else{
            Log.d(Common.TAG,"skip release lock");
        }


        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.reenableKeyguard();
    }

}
