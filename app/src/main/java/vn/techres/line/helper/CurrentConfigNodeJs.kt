package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.data.model.utils.ConfigNodeJs

object CurrentConfigNodeJs {
    private var configNodeJs = ConfigNodeJs()

    fun saveConfigNodeJs(ct: Context, configNodeJs: ConfigNodeJs?) {
        try {
            val userJson = Gson().toJson(configNodeJs, ConfigNodeJs::class.java)
            val sharedPreference =PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CACHE_CONFIG_NODE_JS.toString(), userJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getConfigNodeJs(ct: Context): ConfigNodeJs {
        try {
            val sharedPreference = PreferenceHelper(ct)
            val configNodeJsJson = sharedPreference.getValueString(TechresEnum.CACHE_CONFIG_NODE_JS.toString())
            configNodeJs = Gson().fromJson(configNodeJsJson, ConfigNodeJs::class.java)
        } catch (e: Exception) {
            e.stackTrace
            return ConfigNodeJs()
        }
        return configNodeJs
    }
}