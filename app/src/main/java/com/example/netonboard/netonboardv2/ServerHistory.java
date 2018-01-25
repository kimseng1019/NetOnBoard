package com.example.netonboard.netonboardv2;

/**
 * Created by Netonboard on 9/1/2018.
 */

public class ServerHistory {
    private int sos_alert_id;
    private String server_name;
    private int user_id_handle;
    private String user_handle_name;
    private String dt_created;
    private String dt_start;
    private String dt_complete;

    public ServerHistory() {
    }

    public ServerHistory(int sos_alert_id, String server_name, int user_id_handle, String user_handle_name, String dt_created, String dt_start, String dt_complete) {
        this.sos_alert_id = sos_alert_id;
        this.server_name = server_name;
        this.user_id_handle = user_id_handle;
        this.user_handle_name = user_handle_name;
        this.dt_created = dt_created;
        this.dt_start = dt_start;
        this.dt_complete = dt_complete;
    }

    public int getSos_alert_id() {
        return sos_alert_id;
    }

    public void setSos_alert_id(int sos_alert_id) {
        this.sos_alert_id = sos_alert_id;
    }

    public String getServer_name() {
        return server_name;
    }

    public void setServer_name(String server_name) {
        this.server_name = server_name;
    }

    public int getUser_id_handle() {
        return user_id_handle;
    }

    public void setUser_id_handle(int user_id_handle) {
        this.user_id_handle = user_id_handle;
    }

    public String getUser_handle_name() {
        return user_handle_name;
    }

    public void setUser_handle_name(String user_handle_name) {
        this.user_handle_name = user_handle_name;
    }

    public String getDt_created() {
        return dt_created;
    }

    public void setDt_created(String dt_created) {
        this.dt_created = dt_created;
    }

    public String getDt_start() {
        return dt_start;
    }

    public void setDt_start(String dt_start) {
        this.dt_start = dt_start;
    }

    public String getDt_complete() {
        return dt_complete;
    }

    public void setDt_complete(String dt_complete) {
        this.dt_complete = dt_complete;
    }

    @Override
    public String toString() {
        return "ServerHistory{" +
                "sos_alert_id=" + sos_alert_id +
                ", server_name='" + server_name + '\'' +
                ", user_id_handle=" + user_id_handle +
                ", user_handle_name='" + user_handle_name + '\'' +
                ", dt_created='" + dt_created + '\'' +
                ", dt_start='" + dt_start + '\'' +
                ", dt_complete='" + dt_complete + '\'' +
                '}';
    }
}
