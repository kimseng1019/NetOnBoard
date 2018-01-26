package com.example.netonboard.netonboardv2;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SupportServerDown extends Fragment {
    private static final String TAG = "SupportServerDown";
    ArrayList<ServerDown> al_serverDown;
    ServerDownAdapter server_down_adapter;
    RecyclerView rv_server_down;
    LinearLayoutManager layout_manager_server;
    TextView tv_server_down_empty;
    Handler handlerServerDown;
    Runnable runnableServerDown;
    GlobalFileIO fileIO;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_serverdown, container, false);

        fileIO = new GlobalFileIO(getContext());
        al_serverDown = new ArrayList<>();
        tv_server_down_empty = (TextView)view.findViewById(R.id.tv_server_down_empty);
        //RecyclerView
        rv_server_down = (RecyclerView) view.findViewById(R.id.rv_server_down);
        layout_manager_server = new LinearLayoutManager(getContext());
        rv_server_down.setLayoutManager(layout_manager_server);
        //Item Decoration for recyclerView item
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv_server_down.getContext(), layout_manager_server.getOrientation());
        rv_server_down.addItemDecoration(dividerItemDecoration);
        ViewGroup.LayoutParams params = rv_server_down.getLayoutParams();
        server_down_adapter = new ServerDownAdapter(al_serverDown);
        rv_server_down.setAdapter(server_down_adapter);

        loadServerDown();
        return view;
    }

    @Override
    public void onDestroy() {
        handlerServerDown.removeCallbacks(runnableServerDown);
        Log.i(TAG, "Destroyed");
        super.onDestroy();
    }

    public void loadServerDown() {
        handlerServerDown = new Handler();
        runnableServerDown = new Runnable() {
            @Override
            public void run() {
                String body = fileIO.readFile(fileIO.FILENAMEDOWN);
                try {
                    if(!body.equals("")) {
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
                        tv_server_down_empty.setVisibility(View.INVISIBLE);
                        server_down_adapter.notifyDataSetChanged();
                        Log.i(TAG, "Down updated");
                    }else{
                        tv_server_down_empty.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handlerServerDown.postDelayed(this, 1000);
            }
        };
        handlerServerDown.post(runnableServerDown);
    }
}
