package com.example.android.clanmanager;

public class Sancionado {

    private String name;
    private String key;

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
}
