package com.example.android.clanmanager;


import android.content.Context;
import android.content.Intent;

/**
 * Maneja opciones a mostrar en MainActivity
 */
public class Option {
    private int mImageResourceId;
    private String mDescription;
    private String mSummary;
    private Class mClass;
    private Context mContext;

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

    public Intent getOptionIntent() {
        if (mClass != null) {
            return new Intent(mContext, mClass);
        }
        return null;
    }
}
