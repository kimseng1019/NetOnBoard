package com.example.netonboard.netonboardv2;

import java.io.Serializable;

/**
 * Created by Netonboard on 8/1/2018.
 */

public class ServerDown implements Serializable {
    private String type;
    private String id;
    private String server_name;
    private String desc;
    private String sos_alert_id;
    private String user_id_handle;
    private String user_handle_name;
    private String dt_create;
    private String dt_start;
    private String dt_next_snooze;
    private String dt_complete;
    private String remark;
    private String light;

    public ServerDown() {
    }

    public ServerDown(String type, String id, String server_name, String desc, String sos_alert_id, String user_id_handle, String user_handle_name, String dt_create, String dt_start, String light, String dt_next_snooze, String dt_complete, String remark) {
        this.type = type;
        this.id = id;
        this.server_name = server_name;
        this.desc = desc;
        this.sos_alert_id = sos_alert_id;
        this.user_id_handle = user_id_handle;
        this.user_handle_name = user_handle_name;
        this.dt_create = dt_create;
        this.dt_start = dt_start;
        this.dt_next_snooze = dt_next_snooze;
        this.dt_complete = dt_complete;
        this.remark = remark;
        this.light = light;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServer_name() {
        return server_name;
    }

    public void setServer_name(String server_name) {
        this.server_name = server_name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSos_alert_id() {
        return sos_alert_id;
    }

    public void setSos_alert_id(String sos_alert_id) {
        this.sos_alert_id = sos_alert_id;
    }

    public String getUser_id_handle() {
        return user_id_handle;
    }

    public void setUser_id_handle(String user_id_handle) {
        this.user_id_handle = user_id_handle;
    }

    public String getUser_handle_name() {
        return user_handle_name;
    }

    public void setUser_handle_name(String user_handle_name) {
        this.user_handle_name = user_handle_name;
    }

    public String getDt_create() {
        return dt_create;
    }

    public void setDt_create(String dt_create) {
        this.dt_create = dt_create;
    }

    public String getDt_start() {
        return dt_start;
    }

    public void setDt_start(String dt_start) {
        this.dt_start = dt_start;
    }

    public String getDt_next_snooze() {
        return dt_next_snooze;
    }

    public void setDt_next_snooze(String dt_next_snooze) {
        this.dt_next_snooze = dt_next_snooze;
    }

    public String getDt_complete() {
        return dt_complete;
    }

    public void setDt_complete(String dt_complete) {
        this.dt_complete = dt_complete;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    @Override
    public String toString() {
        return "ServerDown{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", server_name='" + server_name + '\'' +
                ", desc='" + desc + '\'' +
                ", sos_alert_id='" + sos_alert_id + '\'' +
                ", user_id_handle='" + user_id_handle + '\'' +
                ", user_handle_name='" + user_handle_name + '\'' +
                ", dt_create='" + dt_create + '\'' +
                ", dt_start='" + dt_start + '\'' +
                ", dt_next_snooze='" + dt_next_snooze + '\'' +
                ", dt_complete='" + dt_complete + '\'' +
                ", remark='" + remark + '\'' +
                ", light='" + light + '\'' +
                '}';
    }
}
