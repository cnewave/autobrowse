package com.example.kent.androidwebview;

/**
 * Created by kent on 2018/2/19.
 */

public class WebInfo {
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

    void setEnable(boolean enable) {
        mEnalbe = enable;
    }

    String getName() {
        return mName;
    }

    String getURL() {
        return mURL;
    }
}
