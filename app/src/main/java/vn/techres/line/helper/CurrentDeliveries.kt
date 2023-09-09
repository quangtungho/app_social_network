package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import vn.techres.line.data.model.deliveries.ConfigDeliveries
import vn.techres.line.helper.techresenum.TechresEnum

object CurrentDeliveries {
    private var configDeliveries : ConfigDeliveries? = ConfigDeliveries()

    fun saveCurrentConfigDeliveries(ct: Context, configDeliveries : ConfigDeliveries?) {
        try {
            val configJSon = Gson().toJson(configDeliveries, ConfigDeliveries::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CACHE_DELIVERIES.toString(), configJSon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentConfigDeliveries(ct: Context) : ConfigDeliveries {
        try {
            val sharedPreference = PreferenceHelper(ct)
            val configJson = sharedPreference.getValueString(TechresEnum.CACHE_DELIVERIES.toString())
            configDeliveries = Gson().fromJson<ConfigDeliveries>(configJson, ConfigDeliveries::class.java)
        } catch (e: Exception) {
            e.stackTrace
            return ConfigDeliveries()
        }
        if (configDeliveries == null) configDeliveries = ConfigDeliveries()
        return this.configDeliveries!!
    }
}