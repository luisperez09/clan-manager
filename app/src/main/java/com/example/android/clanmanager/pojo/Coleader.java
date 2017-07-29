package com.example.android.clanmanager.pojo;

/**
 * Gestiona colíderes del clan y su responsabilidad en el lanzamiento de guerras
 */
public class Coleader {
    private String name;
    private boolean responsible;
    private String key;

    /**
     * Constructor vacío para la base de datos de Firebase
     */
    public Coleader() {

    }

    /**
     * Crea nuevo colíder
     *
     * @param name        Nombre del colíder
     * @param responsible Responsabilidad de lanzamiento de guerra
     */
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
