package vn.techres.line.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import vn.techres.line.R
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.chat.OnBackClick

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseBindingFragment<VB: ViewBinding>(private val inflate: Inflate<VB>) : Fragment(),
    OnBackClick {

    protected abstract fun onBackPress() : Boolean

    private var _binding: VB? = null
    val binding get() = _binding!!
    val cacheManager: CacheManager = CacheManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        binding.root.isClickable = true
        binding.root.isFocusable = true
        return binding.root
    }

    override fun onBack(): Boolean {
        activity?.window?.statusBarColor = view?.let { AlolineColorUtil(it.context).convertColor(R.color.main_bg) }!!
        return onBackPress()
    }

    fun closeKeyboard(edt: EditText) {
        edt.requestFocus()
        edt.isCursorVisible = false
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt.windowToken, 0)
    }

    fun showKeyboard(edt: EditText) {
        edt.requestFocus()
        edt.isCursorVisible = true
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }

    fun restaurant() : RestaurantCard {
        var restaurant = RestaurantCard()
        val sharedPreference = PreferenceHelper(requireActivity())
        val restaurantJson = sharedPreference.getValueString(TechresEnum.RESTAURANT_CARD.toString())

        if(!restaurantJson.isNullOrBlank()){
            restaurant = Gson().fromJson(restaurantJson, RestaurantCard::class.java)
        }
        return restaurant
    }

    /**
     * Store new Restaurant info into cache
     */
    fun saveRestaurantInfo(restaurant: RestaurantCard?) {
        restaurant?.let {
            val restaurantJson = Gson().toJson(it)
            val sharedPreference = PreferenceHelper(requireActivity())
            sharedPreference.save(TechresEnum.RESTAURANT_CARD.toString(), restaurantJson)
        }
    }
}