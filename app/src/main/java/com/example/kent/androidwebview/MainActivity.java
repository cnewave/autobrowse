package com.example.kent.androidwebview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final private String TAG = "AndroidWebView";
    private ArrayList<String> mList = new ArrayList<>();
    private WebView mWebView = null;
    private int mMax = 10;
    private int SLEEP_INTERVAL = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "CreateAutoView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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


    final private int START_AUTO = 0;
    final private int CONFIG = 1;
    final private String MENU_START = "start";
    final private String MENU_CONFIG = "config";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, START_AUTO, 0, MENU_START);
        menu.add(0, CONFIG, 1, MENU_CONFIG);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch (item.getItemId()) {
            case START_AUTO: {
                Log.d(TAG, "start to auto browse.");
                Log.d(TAG, "Start the thread");
                mThread = new Thread(new MyThread());
                mThread.start();
            }
            break;
            case CONFIG: {
                Log.d(TAG, "start to config page");
            }
            break;
            default:
        }
        return super.onOptionsItemSelected(item);
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
