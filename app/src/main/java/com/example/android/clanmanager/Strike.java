package com.example.android.clanmanager;

public class Strike {
    private String date;
    private String reason;

    public Strike(String date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    public Strike() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
