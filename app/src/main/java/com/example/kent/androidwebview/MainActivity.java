package com.example.kent.androidwebview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    final private String TAG = "AutoBrowse";
    final private int START_AUTO = 0;
    final private int CONFIG = 1;
    final private int CHECK_MY_IP = 2;
    final private int EDIT_LIST = 3;
    final private int RESET_LIST = 4;
    final private int STOP_AUTO = 5;
    final private int UPDATE_LIST = 6;

    final private String MENU_START = "start";
    final private String MENU_STOP = "stop";
    final private String MENU_CONFIG = "config";
    final private String MENU_CHECKIP = "check ip";
    final private String MENU_UPDATE = "Update List";
    final private String MENU_EDIT_LIST = "Edit list";
    final private String MENU_RESET_LIST = "Reset list";
    // File related
    private final String fileName = "pixnet.json";
    private List<WebInfo> mWebInfos = new ArrayList<>();
    private WebView mWebView = null;

    // data model for web info
    DataModel mData = null;

    // okhttp
    private String BASE_URL = "http://192.168.1.4:8000/getViewList";
    final OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "CreateAutoView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mData = DataModel.getInstance(MainActivity.this);
//        new Thread(){
//            @Override
//            public void run() {
//                test();
//            }
//        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //參數1:群組id, 參數2:itemId, 參數3:item順序, 參數4:item名稱
        menu.add(0, START_AUTO, 0, MENU_START);
        menu.add(0, STOP_AUTO, 0, MENU_STOP);
        menu.add(0, CONFIG, 1, MENU_CONFIG);
        menu.add(0, UPDATE_LIST, 1, MENU_UPDATE);

        menu.add(0, CHECK_MY_IP, 2, MENU_CHECKIP);
        menu.add(0, EDIT_LIST, 3, MENU_EDIT_LIST);
        menu.add(0, RESET_LIST, 4, MENU_RESET_LIST);
        return super.onCreateOptionsMenu(menu);
    }


    private void startService() {
        Intent intent = new Intent(this, AutoService.class);
        startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(this, AutoService.class);
        stopService(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //依據itemId來判斷使用者點選哪一個item
        switch (item.getItemId()) {
            case START_AUTO: {
                Log.d(TAG, "start to auto browse menu.");
                int max = DataModel.getInstance(MainActivity.this).getMax();
                int interval = DataModel.getInstance(MainActivity.this).getSleepInterval();
                final String msg = "Start to auto browse ? max:" + max + " interval:" + interval;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Auto Browse")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Start the service");
                                startService();
                                finish();
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

            case STOP_AUTO: {
                stopService();
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

            case UPDATE_LIST: {
                new Thread() {
                    @Override
                    public void run() {
                        updateList();
                    }
                }.start();
            }
            break;

            case EDIT_LIST: {
                if (mData == null) {
                    Log.e(TAG, "Fail to edit list");
                    return true;
                }
                Log.d(TAG, "launch alert dialog");
                mWebInfos = mData.getInfos();

                final int size = mWebInfos.size();
                final String[] items = new String[size];
                final boolean[] enables = new boolean[size];

                //for (WebInfo webIntem : mWebInfos) {
                for (int i = 0; i < size; i++) {
                    WebInfo webIntem = mWebInfos.get(i);
                    String name = webIntem.getName();
                    boolean enable = webIntem.isEnable();
                    items[i] = name;
                    enables[i] = enable;
//                    Log.d(TAG, "name:" + name + " enable:" + enable);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                                mData.parseJson(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).setNeutralButton("Deselect all", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "De-select all");
                        for (int i = 0; i < size; i++) {
                            enables[i] = false;
                        }
                        //
                        for (WebInfo item : mWebInfos) {
                            item.setEnable(false);
                        }
                        builder.setMultiChoiceItems(items, enables, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                mWebInfos.get(indexSelected).setEnable(isChecked);
                            }
                        });
                    }
                })
                        .show();
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
        mData.parseJson(null);
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


    private void updateList() {
        Log.d(TAG, "updateList");
        try {

            RequestBody body = new FormBody.Builder()
                    .add("uuid", "217bd632-3577-11e8-b467-0ed5f89f718b")

                    .build();

            Request request = new Request.Builder()
                    .url("http://34.209.91.26:8000/getViewList/")
                    .post(body)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) {
                    Log.d(TAG, "onResponse ok." + response);
                    // updateLocation(latitude, longitude);

                    Log.d(TAG, "onResponse ok." + response.message());
                    Log.d(TAG, "onResponse ok." + response.code());
                    try {
                        final String jsonData = response.body().string();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                try {


                                    Log.d(TAG, "jsonData " + jsonData);
                                    JSONArray jsonArray = new JSONArray(jsonData);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject item = jsonArray.getJSONObject(i);
                                        Log.d(TAG, "json array:" + item);
                                    }
                                    mData.parseJson(jsonArray);
                                    // save to file
                                    JSONObject obj = new JSONObject();
                                    JSONArray jlist = jsonArray;

                                    obj.put("web", jlist);
                                    Log.d(TAG, "Create Json:" + obj.toString());
                                    saveToFile(obj.toString());
                                    Toast.makeText(MainActivity.this, "Update done", Toast.LENGTH_LONG).show();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "fail to connect");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
