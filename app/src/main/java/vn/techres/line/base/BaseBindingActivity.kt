package vn.techres.line.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import me.yokeyword.swipebackfragment.SwipeBackActivity
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.chat.OnBackClick

abstract class BaseBindingActivity<VB : ViewBinding> : SwipeBackActivity() {
    private var _binding: ViewBinding? = null
    abstract val bindingInflater: (LayoutInflater) -> VB
    val cacheManager: CacheManager = CacheManager.getInstance()

    var onRefreshFragments = ArrayList<OnRefreshFragment>()
    var onBackClickFragments = ArrayList<OnBackClick>()

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(requireNotNull(_binding).root)
        onSetBodyView()
        setSwipeBackEnable(false)
    }

    abstract fun onSetBodyView()

    fun closeKeyboard(edt: EditText) {
        edt.requestFocus()
        edt.isCursorVisible = false
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt.windowToken, 0)

    }
    fun showKeyboard(edt: EditText) {
        edt.requestFocus()
        edt.isCursorVisible = true
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        edt.postDelayed({
            edt.requestFocus()
            imm.showSoftInput(edt, InputMethodManager.SHOW_FORCED)
        }, 100)
    }

    fun restaurant() : RestaurantCard {
        var restaurant = RestaurantCard()
        val sharedPreference = PreferenceHelper(this)
        val restaurantJson = sharedPreference.getValueString(TechresEnum.RESTAURANT_CARD.toString())

        if(!restaurantJson.isNullOrBlank()){
            restaurant = Gson().fromJson(restaurantJson, RestaurantCard::class.java)
        }
        return restaurant
    }

    /**
     * Store new Restaurant info into cache
     */
    fun saveRestaurantInfo(ct: Context, restaurant: RestaurantCard?) {
        try {
            val restaurantJson = Gson().toJson(restaurant, RestaurantCard::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.RESTAURANT_CARD.toString(), restaurantJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun setOnBackClick(onBackClick: OnBackClick){
        onBackClickFragments.add(onBackClick)
    }

    fun removeOnBackClick(onBackClick: OnBackClick){
        onBackClickFragments.remove(onBackClick)
    }
    fun removeAllOnBackClick(){
        onBackClickFragments = ArrayList()
    }
    fun setOnRefreshFragment(onRefreshFragment: OnRefreshFragment) {
        onRefreshFragments.add(onRefreshFragment)
    }

    fun removeOnRefreshFragment(onRefreshFragment: OnRefreshFragment) {
        onRefreshFragments.remove(onRefreshFragment)
    }

    fun getOnRefreshFragment(): OnRefreshFragment {
        return if (onRefreshFragments.size > 0)
            onRefreshFragments[onRefreshFragments.size - 1]
        else
            object : OnRefreshFragment {
                override fun onCallBack(bundle: Bundle) {
                    WriteLog.d("onCallBack", "null callback stack")
                }
            }
    }
    fun removeAllOnRefreshFragment(){
        onRefreshFragments = ArrayList()
    }

}