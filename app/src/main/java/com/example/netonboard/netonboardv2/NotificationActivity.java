package com.example.netonboard.netonboardv2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationActivity extends AppCompatActivity {

    Timer tm_sound;
    TimerTask tt_sound;
    final static String TAG = "NotificationActivity";
    final static long PROMPTDELAY = 1000 * 60 / 2;//Set the delay to next prompt when snooze is pressed, set based on
    final static long DURATIONNOTIFICATION = 1000 * 15;
    Ringtone ringtone;
    Vibrator v;

    Button btn_mute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        btn_mute = (Button)  findViewById(R.id.btn_mute);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String strRingtonePreference = preferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        Uri notificationSound = Uri.parse(strRingtonePreference);
        if (notificationSound.toString().equals("DEFAULT_SOUND")) {
            notificationSound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
        } else if (notificationSound.toString().equals(""))
            ringtone = null;
        else
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);

        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        tm_sound = new Timer();
        tt_sound = new TimerTask() {
            @Override
            public void run() {
                if (ringtone != null)
                    ringtone.play();
                if(v != null)
                    v.vibrate(500);
                if (System.currentTimeMillis() - BackgroundService.lastPromptTime.getTime() > DURATIONNOTIFICATION) {
                    moveTaskToBack(true);
                    finish();
                }
            }
        };
        tm_sound.schedule(tt_sound, 0, 1000);

        btn_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ringtone = null;
                v = null;
            }
        });
    }

    @Override
    protected void onDestroy() {
        BackgroundService.lastPromptTime.setTime(System.currentTimeMillis());
        tm_sound.cancel();
        super.onDestroy();
        System.out.println("Notification destroyed");
    }

    public void onHandle(View view) {
        System.out.println("mID value:" + BackgroundService.mID);

        Intent i = new Intent(this, PasscodeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        tm_sound.cancel();
        finish();
    }

    public void onSnooze(View view) {
        tm_sound.cancel();
//        BackgroundService.lastPromptTime.setTime(System.currentTimeMillis()); //Delay the timeToPrompt next notification by PROMPTDELAY
        moveTaskToBack(true);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        tm_sound.purge();
        Log.i(TAG, "Notification paused");
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAttachedToWindow() {
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Log.i(TAG, "Window attached run timer");
    }
}
