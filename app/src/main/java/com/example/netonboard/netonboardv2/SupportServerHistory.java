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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;

public class SupportServerHistory extends Fragment {
    private static final String TAG = "SupportServerHistory";
    ArrayList<ServerHistory> al_serverHistory;
    TextView tv_no_server_down_history;
    SortableTableView table_history;
    ServerHistoryAdapter serverHistoryAdapter;
    GlobalFileIO fileIO;
    Handler handlerServerHistory;
    Runnable runnableServerHistory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_serverhistory, container, false);

        fileIO = new GlobalFileIO(getContext());
        al_serverHistory = new ArrayList<>();
        tv_no_server_down_history = (TextView) view.findViewById(R.id.tv_error_history_no_server_down);
        table_history = (SortableTableView) view.findViewById(R.id.table_history);
        if (table_history != null) {
            table_history.setColumnCount(3);
            TableColumnWeightModel weightModel = new TableColumnWeightModel(3);
            weightModel.setColumnWeight(0, 2);
            weightModel.setColumnWeight(1, 3);
            weightModel.setColumnWeight(2, 3);
            table_history.setColumnModel(weightModel);
            String[] table_header = {"Server", "User", "Completed"};
            table_history.setHeaderAdapter(new SimpleTableHeaderAdapter(view.getContext(), table_header));
            serverHistoryAdapter = new ServerHistoryAdapter(view.getContext(), al_serverHistory);
            table_history.setDataAdapter(serverHistoryAdapter);
            table_history.setColumnComparator(0, new ServerHistoryNameComparator());
            table_history.setColumnComparator(1, new ServerHistoryHandlerNameComparator());
            table_history.setColumnComparator(2, new ServerHistoryDtCompleteComparator());
            table_history.setHeaderBackgroundColor(getResources().getColor(R.color.colorTextAreaBG));
            table_history.setElevation(10);
            int colorEvenRow = getResources().getColor(R.color.colorTableRowEven);
            int colorOddRow = getResources().getColor(R.color.colorTableRowOdd);
            table_history.setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(colorEvenRow, colorOddRow));
        }

        loadServerHistory();
        return view;
    }

    @Override
    public void onDestroy() {
        handlerServerHistory.removeCallbacks(runnableServerHistory);
        Log.i(TAG, "Destroyed");
        super.onDestroy();
    }

    public void loadServerHistory() {
        handlerServerHistory = new Handler();
        runnableServerHistory = new Runnable() {
            @Override
            public void run() {
                String body = fileIO.readFile(fileIO.FILENAMEHISTORY);
                JSONObject jObj;
                JSONArray jArray;
                if (body.equals("[]") || body.equals("")) {
                    Log.i(TAG, "No history");
                    table_history.setEmptyDataIndicatorView(tv_no_server_down_history);
                    al_serverHistory.clear();
                } else {
                    try {
                        al_serverHistory.clear();
                        jObj = new JSONObject(body);
                        jArray = jObj.getJSONArray("server_list");
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jServerListObj = jArray.getJSONObject(i);
                            ServerHistory serverObj = new ServerHistory(jServerListObj.getInt("sos_alert_id"), jServerListObj.getString("s_server_name"), jServerListObj.getInt("user_id_handle"),
                                    jServerListObj.getString("s_user_handle_name"), jServerListObj.getString("dt_create"), jServerListObj.getString("dt_start"), jServerListObj.getString("dt_complete"));
                            al_serverHistory.add(serverObj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    serverHistoryAdapter.notifyDataSetChanged();
                    Log.i(TAG, "History Table Updated");
                }
                handlerServerHistory.postDelayed(runnableServerHistory, 1000);
            }
        };
        handlerServerHistory.post(runnableServerHistory);
    }


    private static class ServerHistoryNameComparator implements Comparator<ServerHistory> {
        @Override
        public int compare(ServerHistory serverHistory, ServerHistory t1) {
            return serverHistory.getServer_name().compareTo(t1.getServer_name());
        }
    }

    private static class ServerHistoryHandlerNameComparator implements Comparator<ServerHistory> {
        @Override
        public int compare(ServerHistory serverHistory, ServerHistory t1) {
            return serverHistory.getUser_handle_name().compareTo(t1.getUser_handle_name());
        }
    }

    private static class ServerHistoryDtCompleteComparator implements Comparator<ServerHistory> {
        @Override
        public int compare(ServerHistory serverHistory, ServerHistory t1) {
            return serverHistory.getDt_complete().compareTo(t1.getDt_complete());
        }
    }
}
