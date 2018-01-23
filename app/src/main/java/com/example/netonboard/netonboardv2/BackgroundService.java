package com.example.netonboard.netonboardv2;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class BackgroundService extends Service {
    File fileDown, fileHistory, fileSupport, fileRemark, fileNotifyTime;
    static final String FILENAMEDOWN = "serverDown";
    static final String FILENAMEHISTORY = "serverHistory";
    static final String FILENAMESUPPORT = "support";
    static final String FILENAMENOTIFYTIME = "notifyTime";
    public static final String FILENAMEREMARK = "remark";
    static final int REFRESHSUPPORT = 1000 * 60 * 30;
    static final int REFRESHSERVER = 1000;
    static final int REFRESHNOTIFICATION = 1000 * 60;
    final static String TAG = "BackgroundService";

    final static int fiveMinuteMillis = 1000 * 60 * 5;
    String tester = "ABCDEFGHIJK";
    OkHttpClient client = new OkHttpClient();
    private Timer tm_notification;
    private Timer tm_standbySupport;
    private Timer tm_serverDown;
    private Timer tm_serverHistory;
    private Timer tm_network;
    public static int mID = 1;

    TimerTask ttSupport, ttServer, ttNotification;

    private ConnectivityManager connectivityManager;
    public static NotificationManager notificationManager;

    Timestamp lastPromptTime;
    int curServerDown = 0;

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
        fileRemark = new File(getApplicationContext().getFilesDir(), FILENAMEREMARK);
        fileNotifyTime = new File(getApplicationContext().getFilesDir(), FILENAMENOTIFYTIME);
        lastPromptTime = new Timestamp(System.currentTimeMillis());

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {

                    loadServerDownList();
                    LoadErrorHistory();
                    postRemark();
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
        tm_notification.schedule(timerTaskNotification, 5000, REFRESHNOTIFICATION);

    }

    public void loadSupport() {
        Request request = new Request.Builder().url("http://cloudsub04.trio-mobile.com/curl/mobile/sos/standby_support.php")
                .build();
        try {
            String body = client.newCall(request).execute().body().string();
            writeToFile(FILENAMESUPPORT, body);
//            Log.i(TAG, "Written sP");
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
            try {
                JSONArray jArray = new JSONArray(body);
                if (jArray.length() > curServerDown) //if newComing serverDown is more than curServerDown
                    notificationBuilder(jArray.length());

                curServerDown = jArray.length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Written sDown");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at loadServerDownList");
        }
    }

    public void LoadErrorHistory() {
        final Request request = new Request.Builder()
                .url("http://cloudsub04.trio-mobile.com/curl/mobile/sos/get_sos_history.php")
                .build();
        try {
            String body = client.newCall(request).execute().body().string();
            writeToFile(FILENAMEHISTORY, body);
            Log.i(TAG, "Written sH");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at LoadErrorHistory");
        }
    }

    public void postRemark() {
        if (fileRemark.exists()) {
            SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
            final String storedUserID = pref.getString("userID", "-1");
            String body = readFile(FILENAMEREMARK);
            String delims = ";";
            String[] token = body.split(delims);
            final String sosID = token[0];
            final String taskStatus = token[1];
            String timeToComplete = null;
            final String remark;
            if (taskStatus.equals("snooze")) {
                timeToComplete = token[2];
                remark = token[3];
            } else
                remark = token[2];

            final Handler handler = new Handler(Looper.getMainLooper());
            final String finalTimeToComplete = timeToComplete;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    String url = "http://cloudsub04.trio-mobile.com/curl/mobile/sos/update_progress_sos.php?id=" + sosID + "&uid=" + storedUserID;
                    RequestParams params = new RequestParams();
                    params.put("s_type", taskStatus);
                    params.put("s_remark", remark);
                    if (taskStatus.equals("snooze"))
                        params.put("i_snooze", finalTimeToComplete);
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.i(TAG, "Posted remark");
                            fileRemark.delete();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e(TAG, "Failed to post remark");
                        }
                    });
                }
            };
            handler.post(runnable);
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
            while ((str = bufferedReader.readLine()) != null) {
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
    //TODO MOVE NOTIFICATIONPROMPT TO THE SERVERUPDATE TIMER AND DELETE THE NOTIFICATION TIMER
    public void notificationPrompt() {
        long lastPromptLong = lastPromptTime.getTime();
        long curTimeLong = System.currentTimeMillis();
        if (curServerDown > 0 && curTimeLong > lastPromptLong + 1000*60);
            notificationBuilder(curServerDown);
    }

    public void notificationBuilder(int numServerDown) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "my_channel_01";
        CharSequence name = "Server Down ALERT";
        String description = numServerDown + " is down";
        int importance = NotificationManager.IMPORTANCE_HIGH;

//        Log.e("Notification context", getApplication().toString());
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String strRingtonePreference = preferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
//        Uri notificationSound = Uri.parse(strRingtonePreference);
//
//        if (notificationSound.toString().equals("DEFAULT_SOUND")) {
//            notificationSound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
//        }

//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_cloud_off_black_24dp)
//                .setContentTitle("NetOnBoard")
//                .setContentText(numServerDown + "server is currently down")
//                .setOngoing(false)
//                .setLights(Color.RED, 2000, 1000)
//                .setVibrate(new long[]{0, 1000})
//                .setSound(notificationSound);
//
//        notificationBuilder.setAutoCancel(true);
//        Intent intent = new Intent(this, PasscodeActivity.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addNextIntent(intent);
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        notificationBuilder.setContentIntent(pendingIntent);
//        notificationManager.notify(mID, notificationBuilder.build());
//        Log.v("VALUE NOTIFICATION: ", String.valueOf(mID));
        if (isAppIsInBackground(getApplicationContext())) {
            Intent alert = new Intent(this, NotificationActivity.class);
            alert.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            alert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lastPromptTime.setTime(System.currentTimeMillis());
            startActivity(alert);
        }
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

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }

}
