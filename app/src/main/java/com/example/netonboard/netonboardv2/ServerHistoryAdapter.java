package com.example.netonboard.netonboardv2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by Netonboard on 25/1/2018.
 */

public class ServerHistoryAdapter extends TableDataAdapter<ServerHistory> {
    final static int TEXT_SIZE = 14;

    public ServerHistoryAdapter(Context context, List<ServerHistory> data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        ServerHistory serverHistory = getRowData(rowIndex);
        View renderview = null;

        switch (columnIndex) {
            case 0:
                renderview = renderString(serverHistory.getServer_name());
                break;
            case 1:
                renderview = renderString(serverHistory.getUser_handle_name());
                break;
            case 2:
                renderview = renderString(serverHistory.getDt_complete());
        }
        return renderview;
    }

    private View renderString(final String value) {
        final TextView textView = new TextView(getContext());
        textView.setText(value);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }

}
