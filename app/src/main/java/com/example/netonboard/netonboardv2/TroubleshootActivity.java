package com.example.netonboard.netonboardv2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class TroubleshootActivity extends AppCompatActivity {

    final static String TAG = "TroubleshootActivity";
    static final String FILENAMEREMARK = "remark";
    Button btn_take, btn_update;
    String sos_id, userID;
    SharedPreferences sp_pref;
    ServerDown serverDown;
    String taskStatus;
    String timeToComplete;
    String url;

    Spinner spinner;
    EditText tf_remark;
    AlertDialog alertDialog;
    ServerDown obj;

    TextView tv_server_name, tv_server_desc, tv_server_type;
    TextView tv_dt_create, tv_dt_start, tv_dt_estimate, tv_user_handling_name;
    TextView tv_dt_create_v, tv_dt_start_v, tv_dt_estimate_v, tv_user_handling_name_v;
    TextView troubleshoot_remark_desc;
    ImageView iv_server_image_light;

    DateFormat dtServerFormat;
    DateFormat dtDisplayFormat;

    boolean isFirstUpdate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_troubleshoot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //initialization
        btn_take = (Button) findViewById(R.id.btn_troubleshoot_take);
        btn_update = (Button) findViewById(R.id.btn_troubleshoot_update);
        tv_server_name = (TextView) findViewById(R.id.troubleshoot_server_name);
        iv_server_image_light = (ImageView) findViewById(R.id.troubleshoot_server_image_light);
        tv_server_type = (TextView) findViewById(R.id.troubleshoot_server_type);
        tv_server_desc = (TextView) findViewById(R.id.troubleshoot_server_desc);
        tv_dt_create = (TextView) findViewById(R.id.troubleshoot_server_dt_create);
        tv_dt_create_v = (TextView) findViewById(R.id.troubleshoot_server_dt_create_v);
        tv_dt_start = (TextView) findViewById(R.id.troubleshoot_server_dt_start);
        tv_dt_start_v = (TextView) findViewById(R.id.troubleshoot_server_dt_start_v);
        tv_dt_estimate = (TextView) findViewById(R.id.troubleshoot_server_dt_estimate);
        tv_dt_estimate_v = (TextView) findViewById(R.id.troubleshoot_server_dt_estimate_v);
        tv_user_handling_name = (TextView) findViewById(R.id.troubleshoot_user_handle_name);
        troubleshoot_remark_desc = (TextView) findViewById(R.id.troubleshoot_remark_desc);
        tv_user_handling_name_v = (TextView) findViewById(R.id.troubleshoot_user_handle_name_v);
        tf_remark = (EditText) findViewById(R.id.troubleshoot_remark);
        dtServerFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        dtDisplayFormat = new SimpleDateFormat("E hh:mm a");

        //getting the ServerDown obj from MainPage
        obj = (ServerDown) getIntent().getSerializableExtra("server");

        getUserData();
        initUI();
    }

    public void initUI() {
        //When no one is handling the server, dont display data such as user handling name, update button, irrelevant data

        tv_server_name.setText(obj.getServer_name());
        tv_server_type.setText(obj.getType());
        tv_server_desc.setText(obj.getDesc());

        if (obj.getLight().equals("red"))
            iv_server_image_light.setColorFilter(Color.RED);
        else if (obj.getLight().equals("green"))
            iv_server_image_light.setColorFilter(Color.GREEN);
        else if (obj.getLight().equals("yellow"))
            iv_server_image_light.setColorFilter(Color.YELLOW);

        try {
            Date createDate = dtServerFormat.parse(obj.getDt_create());
            tv_dt_create_v.setText(dtDisplayFormat.format(createDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (obj.getUser_id_handle().equals("0") || obj.getUser_id_handle().equals("")) {
            btn_update.setVisibility(View.GONE);
            tv_dt_create.setVisibility(View.GONE);
            tv_dt_start.setVisibility(View.GONE);
            tv_dt_estimate.setVisibility(View.GONE);
            tv_user_handling_name.setVisibility(View.GONE);
            tv_dt_create_v.setVisibility(View.GONE);
            tv_dt_start_v.setVisibility(View.GONE);
            tv_dt_estimate_v.setVisibility(View.GONE);
            tv_user_handling_name_v.setVisibility(View.GONE);
            tf_remark.setVisibility(View.GONE);
            troubleshoot_remark_desc.setVisibility(View.GONE);

            btn_take.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isFirstUpdate = true;
                    taskStatus = "snooze";
                    showTimeDialog();
                }
            });
        } else {
            btn_take.setVisibility(View.GONE);
            tv_user_handling_name_v.setText(obj.getUser_handle_name());
            try {
                Date startDate = dtServerFormat.parse(obj.getDt_start());
                tv_dt_start_v.setText(dtDisplayFormat.format(startDate));

                if (!(obj.getDt_complete().equals(""))) {
                    tv_dt_estimate.setText("Completed since:");
                    tv_dt_estimate_v.setText(dtDisplayFormat.format(dtServerFormat.parse(obj.getDt_complete())));
                } else {
                    Date estimateDate = dtServerFormat.parse(obj.getDt_next_snooze());
                    tv_dt_estimate_v.setText(dtDisplayFormat.format(estimateDate));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tf_remark.setText(obj.getRemark());

            SharedPreferences pref = getSharedPreferences("UserData", MODE_PRIVATE);
            String storedUserID = pref.getString("userID", "-1");
            //check if the user if the one in-charge of this server, if no set textArea uneditable
            if (obj.getUser_id_handle().equals(userID)) {
//                tf_remark.setFocusable(true);
            } else {
                //TODO other user overtake job
//                tf_remark.setFocusable(false);
            }

            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!(tf_remark.getText().toString().equals(""))) {
                        isFirstUpdate = false;
                        showUpdateTypeDialog();
                    } else
                        Toast.makeText(getApplicationContext(), "Remark can't be empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getUserData() {
        sp_pref = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userID = sp_pref.getString("userID", "-1");
        Intent intent = getIntent();
        serverDown = (ServerDown) intent.getSerializableExtra("server");
        sos_id = serverDown.getSos_alert_id();
    }

    public void showTimeDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(TroubleshootActivity.this);
        dialog.setTitle("Estimated time");
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.troubleshoot_dialog, null);
        dialog.setView(dialogView);

        final EditText tf_inputTime = (EditText) dialogView.findViewById(R.id.tf_inputTime);

        dialog.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                timeToComplete = tf_inputTime.getText().toString();
                if (isFirstUpdate)
                    firstPostData();
                else
                    updatePostData();

                finish();
                Intent intent = new Intent(TroubleshootActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public void showUpdateTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TroubleshootActivity.this);
        builder.setTitle("Select Your Choice");
        CharSequence[] list_type = {"Update", "Completed"};
        builder.setSingleChoiceItems(list_type, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        taskStatus = "snooze";
                        break;
                    case 1:
                        taskStatus = "complete";
                        break;
                    default:
                        taskStatus = null;
                }
            }
        });

        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (taskStatus == null) {
                    Toast.makeText(getApplicationContext(), "Invalid, select an update or complete", Toast.LENGTH_SHORT).show();
                } else if (taskStatus.equals("snooze")) {
                    showTimeDialog();
                } else if (taskStatus.equals("complete")) {
                    updatePostData();
                    finish();
                    Toast.makeText(getApplicationContext(), "Job Completed", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TroubleshootActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                alertDialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void firstPostData() {
        //post snooze(ongoing) or completed(complete job)
        url = "http://cloudsub04.trio-mobile.com/curl/mobile/sos/update_first_sos.php?id=" + sos_id + "&uid=" + userID;
        RequestParams params = new RequestParams();
        params.put("s_type", taskStatus);
        if (taskStatus.equals("snooze")) {
            params.put("i_snooze", timeToComplete);
        }

        AsyncHttpClient client = new AsyncHttpClient();

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(getApplicationContext(), "Successfully taken the job", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "Failed to update, can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updatePostData() {
        //TODO DECIDE IF NEED IMMEDIATE POST DATA OR NOT, also how to give feedback to user on data sucessfully send
//        url = "http://cloudsub04.trio-mobile.com/curl/mobile/sos/update_progress_sos.php?id=" + sos_id + "&uid=" + userID;
//        RequestParams params = new RequestParams();
//        params.put("s_type", taskStatus);
//        params.put("s_remark", tf_remark.getText().toString());
//        if (taskStatus.equals("snooze"))
//            params.put("i_snooze", timeToComplete);
//
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.post(url, params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Toast.makeText(getApplicationContext(), "Job Updated", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_SHORT).show();
//
//            }
//        });
        try{
            FileOutputStream output = openFileOutput(FILENAMEREMARK, Context.MODE_PRIVATE);
            BufferedWriter bufferedWriter = new BufferedWriter(new PrintWriter(output));
            bufferedWriter.write(sos_id);
            bufferedWriter.write(";" + taskStatus);
            if(taskStatus.equals("snooze"))
                bufferedWriter.write(";"+timeToComplete);
            bufferedWriter.write(";" + tf_remark.getText().toString());
            bufferedWriter.flush();
            output.close();
            Log.i(TAG, "Written to file");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
