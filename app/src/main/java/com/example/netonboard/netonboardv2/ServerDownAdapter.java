package com.example.netonboard.netonboardv2;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Netonboard on 8/1/2018.
 */

public class ServerDownAdapter extends RecyclerView.Adapter<ServerDownAdapter.ViewHolder> {
    private ArrayList<ServerDown> al_serverDown;

    public ServerDownAdapter(ArrayList<ServerDown> al_serverDown) {
        this.al_serverDown = al_serverDown;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_down_list, parent, false);
        return new ServerDownAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ServerDownAdapter.ViewHolder holder, int position) {
        ServerDown serverDown = al_serverDown.get(position);

        holder.tv_server_desc.setText(serverDown.getDesc());

        DateFormat dtServerFormat;
        DateFormat dtDisplayFormat;
        Date dateStarted, dateCompleteBy;
        dtServerFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        dtDisplayFormat = new SimpleDateFormat("E hh:mm a");

        try {
            holder.tv_dt_created.setText(dtDisplayFormat.format(dtServerFormat.parse(serverDown.getDt_create())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (serverDown.getUser_handle_name().equals("")) {
            holder.tv_handling_name.setText("Empty");
            holder.tv_dt_started.setText("");
            holder.tv_dt_complete_by.setText("");
            holder.tv_time_divider.setText("");
        } else {
            holder.tv_handling_name.setText(serverDown.getUser_handle_name());
            if (serverDown.getDt_complete().equals("0000-00-00 00:00:00")) {//if not complete
                try {
                    dateStarted = dtServerFormat.parse(serverDown.getDt_start());
                    dateCompleteBy = dtServerFormat.parse(serverDown.getDt_next_snooze());
                    holder.tv_dt_started.setText(dtDisplayFormat.format(dateStarted));
                    holder.tv_dt_complete_by.setText(dtDisplayFormat.format(dateCompleteBy));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                holder.tv_dt_complete_by.setText("Completed");
                holder.tv_dt_complete_by.setTextColor(Color.GREEN);
                holder.tv_time_divider.setVisibility(View.INVISIBLE);
                holder.tv_dt_started.setVisibility(View.INVISIBLE);
            }
        }

        if (serverDown.getLight().equals("red"))
            holder.iv_server_down_light.setColorFilter(Color.RED);
        else if (serverDown.getLight().equals("green"))
            holder.iv_server_down_light.setColorFilter(Color.GREEN);
        else if (serverDown.getLight().equals("yellow"))
            holder.iv_server_down_light.setColorFilter(Color.YELLOW);
    }


    @Override
    public int getItemCount() {
        return al_serverDown.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_server_desc;
        public TextView tv_dt_created;
        public TextView tv_handling_name;
        public TextView tv_dt_started;
        public TextView tv_dt_complete_by;
        public TextView tv_time_divider;
        ImageView iv_server_down_light;


        public ViewHolder(final View itemView) {
            super(itemView);
            tv_server_desc = (TextView) itemView.findViewById(R.id.server_down_desc);
            tv_dt_created = (TextView) itemView.findViewById(R.id.server_down_dt_created);
            tv_handling_name = (TextView) itemView.findViewById(R.id.server_down_handling_name);
            tv_dt_started = (TextView) itemView.findViewById(R.id.server_down_dt_started);
            tv_dt_complete_by = (TextView) itemView.findViewById(R.id.server_down_dt_complete_by);
            tv_time_divider = (TextView) itemView.findViewById(R.id.time_divider);
            iv_server_down_light = (ImageView) itemView.findViewById(R.id.server_down_light);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(), TroubleshootActivity.class);
                    intent.putExtra("server", al_serverDown.get(getLayoutPosition()));
                    itemView.getContext().startActivity(intent);
                }
            });
        }

    }
}
