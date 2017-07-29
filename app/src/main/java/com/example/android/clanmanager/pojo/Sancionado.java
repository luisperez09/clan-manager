package com.example.android.clanmanager.pojo;

import java.util.Map;

/**
 * Gestiona usuarios que recibieron Strikes por haber cometido alguna falta
 */
public class Sancionado {

    private String name;
    private String key;
    private Map<String, Strike> strikes;

    /**
     * Crea nuevo Sancionado
     *
     * @param name Nombre del sancionado
     */
    public Sancionado(String name) {
        this.name = name;
    }

    /**
     * Constructor vacío para la base de datos de Firebase
     */
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
