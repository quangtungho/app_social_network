package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import vn.techres.line.data.model.address.Address
import vn.techres.line.helper.techresenum.TechresEnum

object CurrentAddress {
    fun saveAddress(ct: Context, address : Address?) {
        try {
            val userJson = Gson().toJson(address, Address::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CACHE_ADDRESS.toString(), userJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAddress(ct: Context): Address? {
        return try {
            val sharedPreference = PreferenceHelper(ct)
            val addressJson = sharedPreference.getValueString(TechresEnum.CACHE_ADDRESS.toString())
            if(addressJson != null){
                Gson().fromJson(addressJson, Address::class.java)
            }else{
                null
            }
        } catch (e: Exception) {
            e.stackTrace
            null
        }
    }
}