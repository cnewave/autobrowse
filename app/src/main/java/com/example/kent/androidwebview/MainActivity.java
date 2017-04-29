package com.example.kent.androidwebview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String TAG = "AndroidWebView";
    ArrayList<String> mList = new ArrayList<>();
    WebView mWebView = null;
    private int mMax = 10;
    private int SLEEP_INTERVAL = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "CreateAutoView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadWeb();
        parseJson();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "destroy and stop thread");
        synchronized (mWait) {
            mWait.notifyAll();
        }
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    private void parseJson() {
        try {
            mList.clear();
            String config = loadConfig();
            //Log.d(TAG," parse Json "+config);
            JSONObject obj = new JSONObject(config);
            JSONArray jlist = obj.getJSONArray("web");
            for (int i = 0; i < jlist.length(); i++) {
                String temp = jlist.getString(i);
                //Log.d(TAG, "web:" + temp);
                mList.add(temp);
            }
            Log.d(TAG, "total size:" + mList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadConfig() {
        Log.d(TAG, "loadConfig");
        String json = null;
        try {

            InputStream is = getAssets().open("pixnet.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void loadWeb() {
        Log.d(TAG, "loadWeb.");
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0");

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.d(TAG, "Start the thread");
                mThread = new Thread(new MyThread());
                mThread.start();
            }
        });
    }

    private int mPrevious = -1;

    private class MyThread implements Runnable {
        public void run() {
            int seed = mList.size();
            if (seed > 0) {
                try {
                    for (int i = 0; i < mMax && !Thread.interrupted(); i++) {
                        int index = -1;
                        do {
                            index = (int) (Math.random() * seed);// index of list
                        } while (index == mPrevious);
                        mPrevious = index;
                        // random sleep interval 1-10
                        int sleep_time = (int) (Math.random() * 10) + 1;
                        Log.d(TAG, "SleepTime:" + sleep_time);
                        sleep_time += SLEEP_INTERVAL;


                        final String t_cURL = mList.get(index);

                        Log.d(TAG, "Index:" + index);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d(TAG, "-->" + t_cURL);
                                if (mWebView != null) {
                                    mWebView.loadUrl(t_cURL);
                                }
                            }
                        });

                        synchronized (mWait) {
                            mWait.wait(sleep_time * 1000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Got exception");
                } finally {
                    Log.d(TAG, "Ok, exit the thread");
                }
            }
        }
    }

    private Thread mThread = null;
    private Object mWait = new Object();
}
