package com.example.android.clanmanager.pojo;

import java.util.Map;

public class Sancionado {

    private String name;
    private String key;
    private Map<String, Strike> strikes;

    public Sancionado(String name) {
        this.name = name;
    }

    public Sancionado() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, Strike> getStrikes() {
        return strikes;
    }

    public void setStrikes(Map<String, Strike> strikes) {
        this.strikes = strikes;
    }
}
