package com.andrecosta.weblogin;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsUtil {

    static final String PREFS_FILE_NAME = "settings";
    private static final String KEY_PASS = "pass";
    private static final String KEY_USER = "user";
    private static final String KEY_STAT = "stat";

    
    public static boolean isSettingsDefined(Context context) {
        SharedPreferences prefs = getPreferences(context);
        return prefs.contains(KEY_USER) && prefs.contains(KEY_PASS);
    }


    public static SharedPreferences getPreferences(Context context) {
        SharedPreferences preferences =  context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        if (preferences instanceof ObscuredSharedPreferences) {
            return preferences;
        }
        return new ObscuredSharedPreferences(context, preferences);
    }


    public static String getUser(Context context) {
        return getPreferences(context).getString(KEY_USER, "");
    }

    public static void setUser(Context context, String username) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_USER, username);
        editor.commit();
    }
    
    public static String getPassword(Context context) {
        return getPreferences(context).getString(KEY_PASS, "");
    }

    public static void setPassword(Context context, String password) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_PASS, password);
        editor.commit();
    }
    
    public static int getStat(Context context){
      return getPreferences(context).getInt(KEY_STAT, 0);
    }
    

    public static void incStat(Context context){
      SharedPreferences.Editor editor = getPreferences(context).edit();
      editor.putInt("stat", 1 + getStat(context));
      editor.commit();
    }

    
}
