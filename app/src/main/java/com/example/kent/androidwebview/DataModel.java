package com.example.kent.androidwebview;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kent on 2018/2/18.
 */

public class DataModel {
    // android
    Context mContext = null;

    private int mMax = 0;
    private int SLEEP_INTERVAL = 10;// minimum should bigger than 10
    private String SERVER_URL = "https://duck2server.herokuapp.com";
    private String TOKEN = "";

    private int mCurrent = 0;
    // data model related
    private final String fileName = "pixnet.json";
    private List<String> mUserSelection = new ArrayList<>();
    private List<WebInfo> mWebInfos = new ArrayList<>();

    private DataModel(Context context) {
        mContext = context;
    }

    //Singleton model
    private static DataModel mMe = null;

    public static DataModel getInstance(Context context) {
        if (mMe == null) {
            mMe = new DataModel(context);
        }
        mMe.getConfig(context);
        mMe.parseJson(null);
        return mMe;
    }

    public static void release() {
        mMe = null;
    }

    private void getConfig(Context context) {

        String t_Max = PreferenceManager.getDefaultSharedPreferences(context).getString("max_count", "10");
        String t_InterVal = PreferenceManager.getDefaultSharedPreferences(context).getString("sleep_interval", "30");

        try {
            mMax = Integer.parseInt(t_Max);
            SLEEP_INTERVAL = Integer.parseInt(t_InterVal);
            SERVER_URL = PreferenceManager.getDefaultSharedPreferences(context).getString("server_url", SERVER_URL);
            TOKEN = PreferenceManager.getDefaultSharedPreferences(context).getString("token",context.getString(R.string.pref_default_token_name));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(Common.TAG, "DataModel max:" + mMax + " interval:" + SLEEP_INTERVAL+ " server url:"+SERVER_URL);
    }

    public boolean isReachMaximum() {
        if ((mCurrent++) >= mMax) {
            return true;
        }
        Log.d(Common.TAG, "Current:" + mCurrent + " Max:" + mMax);
        return false;
    }

    public int getMax() {
        return mMax;
    }

    public int getSleepInterval() {
        return SLEEP_INTERVAL;
    }

    public int getNextInterval() {
        int value = Common.getRandom(20, 10);
        value += SLEEP_INTERVAL;
        return value;
    }

    public String getServerURL(){
        return SERVER_URL;
    }
    public String getToken(){
        return TOKEN;
    }
    // get data model from xml or files

    public synchronized void parseJson(JSONArray refArray) {
        try {
            mUserSelection.clear();
            mWebInfos.clear();
            String config = loadConfig();
            //Log.d(TAG," parse Json "+config);
            JSONObject obj = new JSONObject(config);
            JSONArray jlist = (refArray == null)? obj.getJSONArray("web") : refArray;
            for (int i = 0; i < jlist.length(); i++) {
                JSONObject item = jlist.getJSONObject(i);
                String name = item.getString("name");
                String url = item.getString("url");
                boolean enable = item.getBoolean("enable");
                // Web info list
                mWebInfos.add(new WebInfo(name, url, enable));
                if (enable) {
                    if (Common.DEBUG) {
                        Log.d(Common.TAG, "Enable name:" + name + " enable:" + enable);
                    }
                    mUserSelection.add(url);
                }
            }
            Log.d(Common.TAG, "Total size:" + mUserSelection.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadConfig() {
        Log.d(Common.TAG, "loadConfig");
        String json;
        InputStream is;
        try {

            File file = new File(mContext.getFilesDir(), fileName);
            if (file.exists()) {
                Log.d(Common.TAG, "Read file");
                is = new FileInputStream(file);
            } else {
                Log.d(Common.TAG, "load default config.");
                is = mContext.getAssets().open(fileName);
            }
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public List<WebInfo> getInfos() {
        return mWebInfos;
    }

    private int mPrevious; // previous index for browse;

    public String getRandomURL() {
        int seed = mUserSelection.size();
        Log.d("AutoBrowse","Seed:"+seed);
        int index;
        do {
            index = (int) (Math.random() * seed);// index of list
        } while (index == mPrevious && seed > 1);// if seed is 1 , not do again.
        mPrevious = index;
        return mUserSelection.get(index);
    }
}
