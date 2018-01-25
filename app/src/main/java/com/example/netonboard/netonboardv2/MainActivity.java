package com.example.netonboard.netonboardv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    File fileDown, fileHistory, fileSupport;
    static final String FILENAMEDOWN = "serverDown";
    static final String FILENAMEHISTORY = "serverHistory";
    static final String FILENAMESUPPORT = "support";
    static final String TAG = "MainActivity";

    //Support Standby section
    TextView tv_standby, tv_standby_support;

    //Server Down section
    ArrayList<ServerDown> al_serverDown;
    ServerDownAdapter server_down_adapter;
    RecyclerView rv_server_down;
    LinearLayoutManager layout_manager_server;


    //Server Down History section
    ArrayList<ServerHistory> al_serverHistory;
    TextView tv_no_server_down_history;
    TableLayout tableLayout;

    Handler handlerSupport, handlerServerDown, handlerServerHistory;
    Runnable runnableSupport, runnableServerDown, runnableServerHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialization
        //1st section, standby support
        tv_standby = (TextView) findViewById(R.id.tv_standby_data);
        tv_standby_support = (TextView) findViewById(R.id.tv_standby_descrip);

        //2nd section, server down
        al_serverDown = new ArrayList<>();
        //RecyclerView
        rv_server_down = (RecyclerView) findViewById(R.id.rv_server_down);

        layout_manager_server = new LinearLayoutManager(this);
        rv_server_down.setLayoutManager(layout_manager_server);
        //Item Decoration for recyclerView item
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv_server_down.getContext(), layout_manager_server.getOrientation());
        rv_server_down.addItemDecoration(dividerItemDecoration);
        ViewGroup.LayoutParams params = rv_server_down.getLayoutParams();
        server_down_adapter = new ServerDownAdapter(al_serverDown);
        rv_server_down.setAdapter(server_down_adapter);

        //3rd section, server down history
        al_serverHistory = new ArrayList<>();
        tv_no_server_down_history = (TextView) findViewById(R.id.tv_error_history_no_server_down);

        loadStandBy();
        loadServerDown();
        loadServerHistory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                logout();
                break;
        }
        return false;
    }

    private static final int TIME_DELAY_BACK = 2000;
    private static long back_pressed;

    @Override
    public void onBackPressed() {//TODO DECIDE IF STOP BACKGROUND
        if (back_pressed + TIME_DELAY_BACK > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        handlerSupport.removeCallbacks(runnableSupport);
        handlerServerDown.removeCallbacks(runnableServerDown);
        handlerServerHistory.removeCallbacks(runnableServerHistory);
        super.onDestroy();
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

    public void loadStandBy() {
        handlerSupport = new Handler();
        runnableSupport = new Runnable() {
            @Override
            public void run() {
                String body = readFile(FILENAMESUPPORT);
                tv_standby_support.setText("Primary Support: " + "\n" + "Secondary Support: ");
                try {
                    JSONObject jobject = new JSONObject(body);
                    tv_standby.setText(jobject.getString("s_user_id_standby") + "\n"
                            + jobject.getString("s_user_id_standby_backup"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "Support update");
                handlerSupport.postDelayed(this, 30000);
            }
        };

        handlerSupport.post(runnableSupport);
    }

    public void loadServerDown() {
        handlerServerDown = new Handler();
        runnableServerDown = new Runnable() {
            @Override
            public void run() {
                String body = readFile(FILENAMEDOWN);
                try {
                    JSONArray jsonArray = new JSONArray(body);
                    al_serverDown.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ServerDown serverDown = new ServerDown(jsonObject.getString("s_type"), jsonObject.getString("id"), jsonObject.getString("s_server_name"),
                                jsonObject.getString("s_desc"), jsonObject.getString("sos_alert_id"), jsonObject.getString("user_id_handle"), jsonObject.getString("s_user_handle_name"),
                                jsonObject.getString("dt_create"), jsonObject.getString("dt_start"), jsonObject.getString("light"), jsonObject.getString("dt_next_snooze"), jsonObject.getString("dt_complete"),
                                jsonObject.getString("s_remark"));
                        al_serverDown.add(serverDown);
                    }
                    server_down_adapter.notifyDataSetChanged();
                    Log.i(TAG, "Down updated");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handlerServerDown.postDelayed(this, 1000);
            }
        };
        handlerServerDown.post(runnableServerDown);
    }

    public void loadServerHistory() {
        handlerServerHistory = new Handler();

        final TableRow tr_title = new TableRow(MainActivity.this);
        tr_title.setLayoutParams(getLayoutParams());
        tr_title.addView(getTextView(0, "Server Down", ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark), Typeface.BOLD));
        tr_title.addView(getTextView(0, "Handler", ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark), Typeface.BOLD));
        tr_title.addView(getTextView(0, "Complete Time", ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark), Typeface.BOLD));
        final TableRow tr_content = new TableRow(getApplication());
        tr_content.setLayoutParams(getLayoutParams());
        runnableServerHistory = new Runnable() {
            @Override
            public void run() {
                String body = readFile(FILENAMEHISTORY);
                JSONObject jObj;
                JSONArray jArray;

                if (body.equals("[]")) {
                    tv_no_server_down_history.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.INVISIBLE);
                    tableLayout.removeAllViews();
                    Log.i(TAG, "No history");
                } else {
                    tableLayout.setVisibility(View.VISIBLE);
                    tv_no_server_down_history.setVisibility(View.INVISIBLE);
                    tableLayout.removeAllViews();
                    tableLayout.addView(tr_title, getTblLayoutParams());
                    try {
                        jObj = new JSONObject(body);
                        jArray = jObj.getJSONArray("server_list");
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jServerListObj = jArray.getJSONObject(i);
                            ServerHistory serverObj = new ServerHistory(jServerListObj.getInt("sos_alert_id"), jServerListObj.getString("s_server_name"), jServerListObj.getInt("user_id_handle"),
                                    jServerListObj.getString("s_user_handle_name"), jServerListObj.getString("dt_create"), jServerListObj.getString("dt_start"), jServerListObj.getString("dt_complete"));

                            addRecordToTable(tr_content, i, serverObj.getServer_name(), serverObj.getUser_handle_name(), serverObj.getDt_complete());
                            tableLayout.addView(tr_content, getTblLayoutParams());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "History Table Updated");
                }
                handlerServerHistory.postDelayed(runnableServerHistory, 1000);
            }
        };
        handlerServerHistory.post(runnableServerHistory);
    }

    private void addRecordToTable(TableRow trContent, int num, String serverName, String handlerName, String dtComplete){
        if (num % 2 == 0) {
            trContent.addView(getTextView(num + 1, serverName, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowOdd))), Typeface.NORMAL));
            trContent.addView(getTextView(num , handlerName, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowOdd))), Typeface.NORMAL));
            trContent.addView(getTextView(num + 1, dtComplete, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowOdd))), Typeface.NORMAL));
        } else {
            trContent.addView(getTextView(num + 1, serverName, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowEven))), Typeface.NORMAL));
            trContent.addView(getTextView(num , handlerName, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowEven))), Typeface.NORMAL));
            trContent.addView(getTextView(num + 1, dtComplete, Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(getApplication(), R.color.colorTableRowEven))), Typeface.NORMAL));
        }
    }

    private TextView getTextView(int id, String title, int color, int typeface) {
        TextView tv_history_content = new TextView(this);
        tv_history_content.setId(id);
        tv_history_content.setText(title);
        tv_history_content.setBackgroundColor(color);
        tv_history_content.setPadding(30, 5, 30, 5);
        tv_history_content.setTypeface(Typeface.DEFAULT, typeface);
        tv_history_content.setLayoutParams(getLayoutParams());
        if (id == 0) {
            tv_history_content.setTextColor(Color.WHITE);
        } else {
            tv_history_content.setTextColor(Color.BLACK);
        }
        return tv_history_content;
    }

    //Table Row/Content
    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(2, 0, 0, 2);
        return params;
    }

    //Table Layout
    @NonNull
    private TableLayout.LayoutParams getTblLayoutParams() {
        return new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 3f);
    }

    public void logout() {
        LoginActivity.is_login = false;
        stopService(new Intent(this, BackgroundService.class));
        LoginActivity.sp_name = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        LoginActivity.sp_editor = LoginActivity.sp_name.edit();
        LoginActivity.sp_editor.clear();
        LoginActivity.sp_editor.commit();

        PasscodeActivity.sharedPreferences = getSharedPreferences(PasscodeActivity.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = PasscodeActivity.sharedPreferences.edit();
        editor.clear();
        editor.commit();
        stopService(new Intent(MainActivity.this, BackgroundService.class));
        finish();
    }
}
