package vn.techres.line.helper

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Kolincodes on 10/05/2018.
 */

class PreferenceHelper(val context: Context) {
    private val PREFS_NAME = "SHAREDPREFERENCES_TECHRES"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(name: String, text: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putString(name, text)

        editor.apply()
    }

    fun save(name: String, value: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putInt(name, value)

        editor.apply()
    }

    fun save(name: String, status: Boolean) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.putBoolean(name, status)

        editor.apply()
    }

    fun getValueString(name: String): String? {

        return sharedPref.getString(name, null)


    }

    fun getValueInt(name: String): Int {

        return sharedPref.getInt(name, 0)
    }

    fun getValueBoolien(name: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(name, defaultValue)

    }

    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        editor.clear()
        editor.apply()
    }

    fun removeValue(name: String) {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(name)
        editor.apply()
    }
}