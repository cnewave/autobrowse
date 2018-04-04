package com.example.kent.androidwebview;

import java.util.Random;

/**
 * Created by kent on 2018/2/18.
 */

public class Common {
    public static String TAG = "AutoBrowse";
    public static boolean DEBUG = true;

    /**
     * getRandom value between 0 to max value
     *
     * @param maxValue
     * @return
     */
    public static int getRandom(int maxValue) {
        return (int) (Math.random() * maxValue) + 1;
    }

    public static int getRandom(int maxValue, int minValue) {
        Random r = new Random();
        return r.nextInt(maxValue - minValue +1) + minValue;
    }
}
