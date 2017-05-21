package com.example.android.clanmanager;


public class Coleader {
    private String name;
    private boolean responsible;
    private String key;

    public Coleader() {

    }

    public Coleader(String name, boolean responsible) {
        this.name = name;
        this.responsible = responsible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isResponsible() {
        return responsible;
    }

    public void setResponsible(boolean responsible) {
        this.responsible = responsible;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
