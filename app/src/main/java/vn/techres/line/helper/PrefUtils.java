package vn.techres.line.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import vn.techres.line.activity.TechResAppGame;
import vn.techres.line.activity.TechResApplication;


public class PrefUtils {
    private static PrefUtils prefUtils;
    private SharedPreferences preferences;


    public static void savePreferences(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TechResApplication.Companion.getInstance());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public static String readPreferences(String key, String defaultValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TechResApplication.Companion.getInstance());
        return sp.getString(key, defaultValue);
    }

    private PrefUtils() {
    }
    public static PrefUtils getInstance() {
        if (prefUtils == null) {
            prefUtils = new PrefUtils();
            prefUtils.preferences = PreferenceManager.getDefaultSharedPreferences(TechResApplication.Companion.getInstance());
        }
        return prefUtils;
    }




    /**
     * Get String value from SharedPreferences at 'key'. If key not found, return ""
     *
     * @param key SharedPreferences key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    public String getString(String key) {
        if (preferences == null) return "";
        return preferences.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        if (preferences == null) return "";
        return preferences.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        if (preferences == null) return 0;
        return preferences.getInt(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        if (preferences == null) return false;
        return preferences.getBoolean(key, false);
    }

    public boolean getKeyEmpty(String key) {
        return preferences.contains(key);
    }

    public void removeKey(String key){
        preferences.edit().remove(key).apply();
    }

    /**
     * Put String value into SharedPreferences with 'key' and save
     *
     * @param key   SharedPreferences key
     * @param value String value to be added
     */
    public void putString(String key, String value) {
        checkForNullKey(key);
        checkForNullValue(value);
        preferences.edit().putString(key, value).apply();
    }

    public void putInt(String key, int value) {
        checkForNullKey(key);
        preferences.edit().putInt(key,value).apply();
    }
    public void putBoolean(String key, Boolean value) {
        checkForNullKey(key);
        preferences.edit().putBoolean(key, value).apply();
    }
    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param key pref key
     */
    public void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     *
     * @param value pref key
     */
    public void checkForNullValue(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }

}
