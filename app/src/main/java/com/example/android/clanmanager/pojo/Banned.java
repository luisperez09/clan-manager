package com.example.android.clanmanager.pojo;

/**
 * Gestiona usuarios baneados
 */
public class Banned {
    private String banned;
    private String reason;

    /**
     * Crea instancia del usuario baneado del clan
     *
     * @param banned Nombre del baneado
     * @param reason Motivo del baneo
     */
    public Banned(String banned, String reason) {
        this.banned = banned;
        this.reason = reason;
    }

    /**
     * Constructor vacío para la base de datos de Firebase
     */
    public Banned() {

    }

    public String getBanned() {
        return banned;
    }

    public String getReason() {
        return reason;
    }
}
