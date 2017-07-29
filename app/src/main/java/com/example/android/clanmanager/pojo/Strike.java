package com.example.android.clanmanager.pojo;

/**
 * Gestiona Strikes adjudicados al Sancionado
 */
public class Strike {
    private String date;
    private String reason;
    private String key;

    /**
     * Crea nuevo Strike, tomando la fecha actual al momento de instanciar el objeto
     *
     * @param date   Fecha actual al momento de la creación del objeto
     * @param reason Motivo del Strike
     */
    public Strike(String date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    /**
     * Constructor vacío para la base de datos de Firebase
     */
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
