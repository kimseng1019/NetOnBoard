package com.example.netonboard.netonboardv2;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class SupportMain extends Fragment {
    private static final String TAG = "SupportMain";
    TextView tv_standby_data, tv_standby_descrip;
    Handler handlerSupport;
    Runnable runnableSupport;
    GlobalFileIO fileIO;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_main, container,false);
        fileIO = new GlobalFileIO(getContext());
        tv_standby_descrip = (TextView) view.findViewById(R.id.tv_standby_descrip);
        tv_standby_data = (TextView) view.findViewById(R.id.tv_standby_data);
        loadStandBy();
        return view;
    }

    @Override
    public void onDestroy() {
        handlerSupport.removeCallbacks(runnableSupport);
        Log.i(TAG, "Destroyed");
        super.onDestroy();
    }

    public void loadStandBy() {
        handlerSupport = new Handler();
        runnableSupport = new Runnable() {
            @Override
            public void run() {
                String body = fileIO.readFile(fileIO.FILENAMESUPPORT);
                tv_standby_descrip.setText("Primary Support: " + "\n" + "Secondary Support: ");
                try {
                    JSONObject jobject = new JSONObject(body);
                    tv_standby_data.setText(jobject.getString("s_user_id_standby") + "\n"
                            + jobject.getString("s_user_id_standby_backup"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    tv_standby_data.setText("Failed to parse JSON data");
                }
                Log.i(TAG, "Support update");
                handlerSupport.postDelayed(this, 30000);
            }
        };

        handlerSupport.post(runnableSupport);
    }



}
