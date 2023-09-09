package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.helper.techresenum.TechresEnum

object CurrentCartFoodTakeAway {
    private var cartFoodTakeAway : CartFoodTakeAway? = CartFoodTakeAway()

    fun saveCurrentCartFoodTakeaway(ct: Context, cartFoodTakeAway : CartFoodTakeAway?) {
        try {
            val configJSon = Gson().toJson(cartFoodTakeAway, CartFoodTakeAway::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CACHE_CARD_ORDER_TAKEAWAY.toString(), configJSon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentCartFoodTakeaway(ct: Context) : CartFoodTakeAway {
        try {
            val sharedPreference = PreferenceHelper(ct)
            val configJson = sharedPreference.getValueString(TechresEnum.CACHE_CARD_ORDER_TAKEAWAY.toString())
            cartFoodTakeAway = Gson().fromJson<CartFoodTakeAway>(configJson, CartFoodTakeAway::class.java)
        } catch (e: Exception) {
            e.stackTrace
            return CartFoodTakeAway()
        }
        if (cartFoodTakeAway == null) cartFoodTakeAway = CartFoodTakeAway()
        return this.cartFoodTakeAway!!
    }
}
