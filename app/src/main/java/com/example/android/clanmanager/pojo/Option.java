package com.example.android.clanmanager.pojo;


import android.content.Context;
import android.content.Intent;

/**
 * Maneja opciones a mostrar en MainActivity
 */
public class Option {
    private String mDescription;
    private String mSummary;
    private Class mClass;
    private Context mContext;

    /**
     * Crea Option para mostrar en el menú principal de MainActivity, la cual muestras una
     * descripción y un resumen de la opción y construye Intent necesario para navegar hacia las
     * diferentes Activities según la clase
     * pasada en este constructor
     *
     * @param description Descripción principal de la opción
     * @param summary     Resumen de la opción
     * @param context     El contexto de la aplicación
     * @param aClass      Clase de la Activity de destino
     */
    public Option(String description, String summary, Context context, Class aClass) {
        mDescription = description;
        mSummary = summary;
        mContext = context;
        mClass = aClass;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getSummary() {
        return mSummary;
    }

    /**
     * Devuelve Intent explícito para navegar a la Activity representada por la clase recibida en el
     * constructor
     *
     * @return Intent explicito de navegación. <code>null</code> en caso de no existir clase
     */
    public Intent getOptionIntent() {
        if (mClass != null) {
            return new Intent(mContext, mClass);
        }
        return null;
    }
}
