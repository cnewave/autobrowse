package com.example.kent.androidwebview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String TAG = "AndroidWebView";
    ArrayList<String> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "CreateAutoView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parsetJson();

    }

    private void parsetJson(){
        try {
            String config = loadConfig();
            Log.d(TAG," parse Json "+config);
            JSONObject obj = new JSONObject(config);
        }catch (Exception e){
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
        final WebView myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("https://www.google.com.tw/");

        Button next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.d(TAG, "Start next");
                myWebView.loadUrl("https://www.whatismyip.com/");
            }
        });
    }
}
