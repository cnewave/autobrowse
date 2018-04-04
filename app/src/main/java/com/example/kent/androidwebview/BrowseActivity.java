package com.example.kent.androidwebview;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowseActivity extends AppCompatActivity {
    private int TIME_OUT = 15000;// 15 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Common.TAG, "Create ==============\n\n");
        setContentView(R.layout.activity_browse);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wakeLock();
        postDelay();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // load data model and get url
        String url = DataModel.getInstance(this).getRandomURL();
        Log.d(Common.TAG, "go to :" + url);
        loadWeb(url);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLock();
    }

    private void postDelay() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //處理少量資訊或UI
                loadWeb("about:blank");
                Log.d(Common.TAG, "Close the Activity");
                finish();
            }
        }, TIME_OUT);
    }

    PowerManager.WakeLock wakeLock;

    private void wakeLock() {
        Log.d(Common.TAG, "wake lock");
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), Common.TAG);
        wakeLock.acquire();

    }

    private void releaseLock() {
        Log.d(Common.TAG, "release lock");
        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d(Common.TAG, "release lock done");
            wakeLock.release();
        } else {
            Log.d(Common.TAG, "skip release lock");
        }
    }

    private void loadWeb(String t_cURL) {
        Log.d(Common.TAG, "loadWeb:" + t_cURL);
        final WebView mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(Common.TAG, "shouldOverrideUrlLoading." + url);
                if (view != null) {
                    view.loadUrl(url);
                }
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                Log.d(Common.TAG, "Finish the page load." + url);
            }
        });

        mWebView.loadUrl(t_cURL);
    }
}
