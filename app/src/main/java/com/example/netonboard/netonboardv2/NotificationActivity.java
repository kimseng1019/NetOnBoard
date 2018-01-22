package com.example.netonboard.netonboardv2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationActivity extends AppCompatActivity {

    Timer tm_sound;
    TimerTask tt_sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String strRingtonePreference = preferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        Uri notificationSound = Uri.parse(strRingtonePreference);
        if (notificationSound.toString().equals("DEFAULT_SOUND")) {
            notificationSound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        }
        final Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
        final Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        tm_sound = new Timer();
        tt_sound = new TimerTask() {
            @Override
            public void run() {
                ringtone.play();
                v.vibrate(500);
            }
        };
        tm_sound.schedule(tt_sound, 0, 2000);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tt_sound.cancel();
        System.out.println("Notification destroyed");
    }

    public void onHandle(View view) {
        System.out.println("mID value:" + BackgroundService.mID);

        Intent i = new Intent(this, PasscodeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        tt_sound.cancel();
        finish();
    }

    public void onSnooze(View view) {
        tt_sound.cancel();
        moveTaskToBack(true);
        finish();
    }
}
