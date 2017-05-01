package com.example.kent.androidwebview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final private String TAG = "AndroidWebView";
    private List<String> mList = new ArrayList<>();
    private List<WebInfo> mWebInfos = new ArrayList<>();

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        parseJson();
        getConfig();
    }

    private void getConfig() {

        String t_Max = PreferenceManager.getDefaultSharedPreferences(this).getString("max_count", "10");
        String t_InterVal = PreferenceManager.getDefaultSharedPreferences(this).getString("sleep_interval", "30");

        try {
            mMax = Integer.parseInt(t_Max);
            SLEEP_INTERVAL = Integer.parseInt(t_InterVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onResume max:" + mMax + " interval:" + SLEEP_INTERVAL);
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
    final private int CHECK_MY_IP = 2;
    final private int EDIT_LIST = 3;
    final private int RESET_LIST = 4;
    final private String MENU_START = "start";
    final private String MENU_CONFIG = "config";
    final private String MENU_CHECKIP = "check ip";
    final private String MENU_EDIT_LIST = "Edit list";
    final private String MENU_RESET_LIST = "Reset list";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, START_AUTO, 0, MENU_START);
        menu.add(0, CONFIG, 1, MENU_CONFIG);
        menu.add(0, CHECK_MY_IP, 2, MENU_CHECKIP);
        menu.add(0, EDIT_LIST, 3, MENU_EDIT_LIST);
        menu.add(0, RESET_LIST, 4, MENU_RESET_LIST);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch (item.getItemId()) {
            case START_AUTO: {
                Log.d(TAG, "start to auto browse menu.");
                final String msg = "Start to auto browse ? max:" + mMax + " interval:" + SLEEP_INTERVAL;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Auto Browse")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Start the thread");
                                mThread = new Thread(new MyThread());
                                mThread.start();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

            }
            break;
            case CONFIG: {
                Log.d(TAG, "start to config page");
                Intent newintent = new Intent();
                newintent.setClass(MainActivity.this, SettingsActivity.class);
                startActivity(newintent);
            }
            break;
            case CHECK_MY_IP: {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Check ip");
                        if (mWebView != null) {
                            mWebView.loadUrl("https://whatismyip.li");
                        }
                    }
                });
            }
            break;

            case EDIT_LIST: {
                Log.d(TAG, "launch alert dialog");

                final int size = mWebInfos.size();
                String[] items = new String[size];
                final boolean[] enables = new boolean[size];

                //for (WebInfo webIntem : mWebInfos) {
                for (int i = 0; i < size; i++) {
                    WebInfo webIntem = mWebInfos.get(i);
                    String name = webIntem.getName();
                    boolean enable = webIntem.isEnable();
                    items[i] = name;
                    enables[i] = enable;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(" Edit Web List")
                        .setMultiChoiceItems(items, enables, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                mWebInfos.get(indexSelected).setEnable(isChecked);
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = 0; i < size; i++) {
                                    Log.d(TAG, "Check list:" + i + " " + mWebInfos.get(i).isEnable());
                                }
                                // create json data
                                String newConfig = createJson();
                                // save to file
                                saveToFile(newConfig);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
            }
            break;

            case RESET_LIST: {
                new AlertDialog.Builder(this)
                        .setTitle("Delete File")
                        .setMessage("Delete the file")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                removeFile();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();

            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // File related
    private final String fileName = "pixnet.json";

    private void saveToFile(String data) {
        // reparse json
        Log.d(TAG, " saveToFile ");
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, " saveToFile done.");
    }

    private void removeFile() {
        Log.d(TAG, " Reset list. ");
        File file = new File(this.getFilesDir(), fileName);
        if (file.exists()) {
            Log.d(TAG, " Delete file list. ");
            file.delete();
        }
        parseJson();
    }

    private String createJson() {
        try {

            Log.d(TAG, " create Json ");
            JSONObject obj = new JSONObject();
            JSONArray jlist = new JSONArray();
            for (int i = 0; i < mWebInfos.size(); i++) {
                WebInfo item = mWebInfos.get(i);
                JSONObject jitem = new JSONObject();
                jitem.put("name", item.getName());
                jitem.put("enable", item.isEnable());
                jitem.put("url", item.getURL());
                jlist.put(jitem);
            }
            obj.put("web", jlist);
            Log.d(TAG, "Create Json:" + obj.toString());
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void parseJson() {
        try {
            mList.clear();
            mWebInfos.clear();
            String config = loadConfig();
            //Log.d(TAG," parse Json "+config);
            JSONObject obj = new JSONObject(config);
            JSONArray jlist = obj.getJSONArray("web");
            for (int i = 0; i < jlist.length(); i++) {
                JSONObject item = jlist.getJSONObject(i);
                String name = item.getString("name");
                String url = item.getString("url");
                boolean enable = item.getBoolean("enable");
                // Web info list
                mWebInfos.add(new WebInfo(name, url, enable));
                if (enable) {
                    Log.d(TAG, "Enable name:" + name + " enable:" + enable);
                    mList.add(url);
                }
            }
            Log.d(TAG, "Total size:" + mList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadConfig() {
        Log.d(TAG, "loadConfig");
        String json;
        InputStream is = null;
        try {

            File file = new File(this.getFilesDir(), fileName);
            if (file.exists()) {
                Log.d(TAG, "Read file");
                is = new FileInputStream(file);
            } else {
                Log.d(TAG, "load default config.");
                is = getAssets().open(fileName);
            }
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
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading." + url);
                if (view != null) {
                    view.loadUrl(url);
                }
                return true;
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Bye Bye.");
                        finish();
                    }
                });
            }
        }
    }

    private Thread mThread = null;
    private Object mWait = new Object();

    private class WebInfo {
        private boolean mEnalbe;
        private String mURL;
        private String mName;

        public WebInfo(String name, String url, boolean enable) {
            mName = name;
            mURL = url;
            mEnalbe = enable;
        }

        boolean isEnable() {
            return mEnalbe;
        }

        String getName() {
            return mName;
        }

        String getURL() {
            return mURL;
        }

        void setEnable(boolean enable) {
            mEnalbe = enable;
        }
    }
}
