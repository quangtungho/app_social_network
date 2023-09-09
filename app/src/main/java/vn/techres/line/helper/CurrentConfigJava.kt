package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.data.model.utils.ConfigJava

object CurrentConfigJava {
    private var configJava : ConfigJava? = ConfigJava()

    fun saveConfigJava(ct: Context, configJava : ConfigJava?) {
        try {
            val configJSon = Gson().toJson(configJava, ConfigJava::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CONFIG_JAVA.toString(), configJSon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getConfigJava(ct: Context) : ConfigJava {
        try {
            val sharedPreference = PreferenceHelper(ct)
            val configJson = sharedPreference.getValueString(TechresEnum.CONFIG_JAVA.toString())
            configJava = Gson().fromJson(configJson, ConfigJava::class.java)
        } catch (e: Exception) {
            e.stackTrace
            return ConfigJava()
        }
        if (configJava == null) configJava = ConfigJava()
        return this.configJava!!
    }
}