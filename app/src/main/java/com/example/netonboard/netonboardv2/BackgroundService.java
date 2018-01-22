package com.example.netonboard.netonboardv2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class BackgroundService extends Service {
    File fileDown, fileHistory, fileSupport;
    static final String FILENAMEDOWN = "serverDown";
    static final String FILENAMEHISTORY = "serverHistory";
    static final String FILENAMESUPPORT = "support";
    static final int REFRESHSUPPORT = 1000*60*30;
    static final int REFRESHSERVER = 1000;
    static final int REFRESHNOTIFICATION = 1000*30;
    final static String TAG = "BackgroundService";

    String tester = "ABCDEFGHIJK";
    OkHttpClient client = new OkHttpClient();
    private Timer tm_notification;
    private Timer tm_standbySupport;
    private Timer tm_serverDown;
    private Timer tm_serverHistory;
    private Timer tm_network;
    int mID = 1;

    TimerTask ttSupport, ttServer, ttNotification;


    int curServerDown = 0;
    private ConnectivityManager connectivityManager;


    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG, "BOUNDED SERVICE");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean stopService(Intent name) {
        Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT);
        tm_network.cancel();
        tm_notification.cancel();
        tm_standbySupport.cancel();
        return super.stopService(name);
    }

    @Override
    public void onCreate() {
        Toast.makeText(getApplicationContext(), "Service created", Toast.LENGTH_SHORT);

        fileDown = new File(getApplicationContext().getFilesDir(), FILENAMEDOWN);
        fileHistory = new File(getApplicationContext().getFilesDir(), FILENAMEHISTORY);
        fileSupport = new File(getApplicationContext().getFilesDir(), FILENAMESUPPORT);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {

                    loadServerDownList();
                    LoadErrorHistory();
                } else {
                    Log.e(TAG, "Network is not available");
                }
            }
        };
        tm_network = new Timer();
        tm_network.schedule(timerTask, 0, REFRESHSERVER);

        TimerTask timerTaskSupport = new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    loadSupport();
                } else {
                    Log.e(TAG, "Network is not available");
                }
            }
        };
        tm_standbySupport = new Timer();
        tm_standbySupport.schedule(timerTaskSupport, 0, REFRESHSUPPORT);

        TimerTask timerTaskNotification = new TimerTask() {
            @Override
            public void run() {
                notificationPrompt();
            }
        };
        tm_notification = new Timer();
        tm_notification.schedule(timerTaskNotification, 0, REFRESHNOTIFICATION);

    }

    public void loadSupport(){
        Request request = new Request.Builder().url("http://cloudsub04.trio-mobile.com/curl/mobile/sos/standby_support.php")
                .build();
        try {
            String body = client.newCall(request).execute().body().string();
            writeToFile(FILENAMESUPPORT, body);
            Log.i(TAG, "Written sP");
            readFile(FILENAMESUPPORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadServerDownList() {
        final Request request = new Request.Builder()
                .url("http://cloudsub04.trio-mobile.com/curl/mobile/sos/get_alert.php")
                .build();
        try {
            String body = client.newCall(request).execute().body().string();
            writeToFile(FILENAMEDOWN, body);
            Log.i(TAG, "Written sDown");
            readFile(FILENAMEDOWN);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at loadServerDownList");
        }
    }

    public void LoadErrorHistory() {
        final Request request = new Request.Builder()
                .url("http://cloudsub04.trio-mobile.com/curl/mobile/sos/get_sos_history.php")
                .build();
        try{
            String body = client.newCall(request).execute().body().string();
            writeToFile(FILENAMEHISTORY, body);
            Log.i(TAG, "Written sH");
            readFile(FILENAMEHISTORY);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at LoadErrorHistory");
        }
    }


    public void writeToFile(String fileName, String msg) {
        try {
            FileOutputStream output = openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new PrintWriter(output));
            bufferedWriter.write(msg);
            bufferedWriter.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile(String fileName) {
        try {
            FileInputStream fileInputStream = openFileInput(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String str;
            StringBuilder stringBuilder = new StringBuilder();
            while((str = bufferedReader.readLine()) != null){
                stringBuilder.append(str);
            }
            fileInputStream.close();
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void notificationPrompt(){
        try{
            String str = readFile(FILENAMEDOWN);
            JSONArray jArray = new JSONArray(str);
            curServerDown = jArray.length();
            if(curServerDown > 0)
                notificationBuilder(curServerDown);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notificationBuilder(int numServerDown) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "my_channel_01";
        CharSequence name = "Server Down ALERT";
        String description = numServerDown + " is down";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Log.e("Notification context", getApplication().toString());
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String strRingtonePreference = preferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        Uri notificationSound = Uri.parse(strRingtonePreference);

        if (notificationSound.toString().equals("DEFAULT_SOUND")) {
            notificationSound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cloud_off_black_24dp)
                .setContentTitle("NetOnBoard")
                .setContentText(numServerDown + "server is currently down")
                .setOngoing(false)
                .setLights(Color.RED, 2000, 1000)
                .setVibrate(new long[]{0, 1000})
                .setSound(notificationSound);

        notificationBuilder.setAutoCancel(true);
        Intent intent = new Intent(this, PasscodeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(mID, notificationBuilder.build());
        Log.v("VALUE NOTIFICATION: ", String.valueOf(mID));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tm_standbySupport.cancel();
        tm_notification.cancel();
        tm_network.cancel();
        System.out.println("SERVICE IS DESTROYED");
    }
}
