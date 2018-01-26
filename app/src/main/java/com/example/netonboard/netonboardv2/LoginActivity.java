package com.example.netonboard.netonboardv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.TextUtils;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "UserData";
    public static SharedPreferences sp_name;
    public static SharedPreferences.Editor sp_editor;
    public static boolean is_login = false;
    EditText tf_passcode;
    Spinner spn_username;
    String s_all_name, s_user_id;
    TextView tv_name;
    String[] arr_user_name, arr_uid;
    Button btn_login;

    public static Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        spn_username = (Spinner) findViewById(R.id.spinner);
        tv_name = (TextView) findViewById(R.id.tv_login_info);
        tf_passcode = (EditText) findViewById(R.id.getPasscode);

        checkLogin();

        btn_login = (Button) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_click();
            }
        });
    }

    public void checkLogin() {
        sp_name = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String s_username = sp_name.getString("key", "");
        if (!s_username.equals("")) {
            is_login = true;
            Toast.makeText(getApplicationContext(), "Welcome " + s_username, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(LoginActivity.this, PasscodeActivity.class);
            startService(new Intent(LoginActivity.this, BackgroundService.class));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else {
            loadNameList();
        }
    }

    public void loadNameList() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://cloudsub04.trio-mobile.com/curl/mobile/user/get_user.php"
                , new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        s_all_name = new String(responseBody);
                        JSONArray jobject = null;
                        try {
                            jobject = new JSONArray(s_all_name);
                            arr_user_name = new String[jobject.length()];
                            arr_uid = new String[jobject.length()];
                            for (int x = 0; x < jobject.length(); x++) {
                                arr_user_name[x] = jobject.getJSONObject(x).getString("s_first_name");
                                arr_uid[x] = jobject.getJSONObject(x).getString("user_id");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this, R.layout.spinner_item, arr_user_name);
                        spn_username.setAdapter(adapter);
                        spn_username.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                                s_user_id = arr_uid[i];
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
    }

    public void login_click() {

        String userPassCode = tf_passcode.getText().toString();
        final String userName = spn_username.getSelectedItem().toString();
        if (TextUtils.isEmpty(userPassCode)) {
            Toast.makeText(getApplicationContext(), "Please Key In Your Passcode!!", Toast.LENGTH_SHORT).show();
        } else {
            AsyncHttpClient client = new AsyncHttpClient();
            client.get("http://cloudsub04.trio-mobile.com/curl/mobile/user/login.php?id=" + s_user_id + "&p=" + userPassCode
                    , new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (new String(responseBody).equals("success")) {
                                Toast.makeText(getApplicationContext(), "Successful login!", Toast.LENGTH_SHORT).show();
                                sp_name = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                sp_editor = sp_name.edit();
                                sp_editor.putString("key", userName);
                                sp_editor.putString("userID", s_user_id);
                                sp_editor.commit();
                                is_login = true;
                                startService(new Intent(LoginActivity.this, BackgroundService.class));
                                Intent i = new Intent(LoginActivity.this, PasscodeActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong Passcode", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "Fail To Connect!!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
