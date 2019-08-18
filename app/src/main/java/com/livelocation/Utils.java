package com.livelocation;

import android.content.Context;
import android.content.SharedPreferences;

public enum Utils {

    INSTANCE;

    public void setLoginPref(Context context, String name, String mobile, String session) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.INSTANCE.preApp, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(Constants.INSTANCE.UName, name);
        editor.putString(Constants.INSTANCE.UMobile, mobile);
        editor.putString(Constants.INSTANCE.SessionID, session);

        editor.apply();

    }

    public String getSession(Context context){
        SharedPreferences myPrefs = context.getSharedPreferences(Constants.INSTANCE.preApp, context.MODE_PRIVATE);
        return myPrefs.getString(Constants.INSTANCE.SessionID, null);
    }

    public String getName(Context context){
        SharedPreferences myPrefs = context.getSharedPreferences(Constants.INSTANCE.preApp, context.MODE_PRIVATE);
        return myPrefs.getString(Constants.INSTANCE.UName, null);
    }

    public String getNum(Context context){
        SharedPreferences myPrefs = context.getSharedPreferences(Constants.INSTANCE.preApp, context.MODE_PRIVATE);
        return myPrefs.getString(Constants.INSTANCE.UMobile, null);
    }

}
