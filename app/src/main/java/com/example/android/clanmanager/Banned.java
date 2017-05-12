package com.example.android.clanmanager;

public class Banned {
    private String banned;
    private String reason;

    public Banned(String banned, String reason) {
        this.banned = banned;
        this.reason = reason;
    }

    public Banned(){

    }

    public String getBanned() {
        return banned;
    }

    public String getReason() {
        return reason;
    }
}
