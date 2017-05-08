package com.example.android.clanmanager;


/**
 * Maneja opciones a mostrar en MainActivity
 */
public class Option {
    private int mImageResourceId;
    private String mDescription;
    private String mSummary;

    public Option(String description, String summary) {
        mDescription = description;
        mSummary = summary;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getSummary() {
        return mSummary;
    }
}
