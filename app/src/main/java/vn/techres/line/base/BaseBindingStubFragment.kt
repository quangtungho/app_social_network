package vn.techres.line.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.databinding.FragmentViewstubBinding
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.chat.OnBackClick

abstract class BaseBindingStubFragment<VB: ViewBinding>(@LayoutRes private val idLayout: Int, private val loading : Boolean) : Fragment(),
    OnBackClick {

    protected abstract fun onBackPress() : Boolean

    private var hasInflated = false
    private var visible = false
    private var binding: FragmentViewstubBinding? = null
    private var container: ViewGroup? = null
    private var mSavedInstanceState: Bundle? = null
    private var _stubBinding : VB? = null
    val stubBinding get() = _stubBinding

    val cacheManager: CacheManager = CacheManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = container
        binding = FragmentViewstubBinding.inflate(LayoutInflater.from(context))
        binding?.fragmentViewStub?.viewStub?.layoutResource = idLayout
        binding?.fragmentViewStub?.setOnInflateListener { _, inflated ->
            _stubBinding = DataBindingUtil.bind(inflated)
        }
        binding?.lifecycleOwner = this
        mSavedInstanceState = savedInstanceState
        if(!loading){
            binding?.lnLoading?.hide()
        }else{
            binding?.lnLoading?.show()
        }
        if (visible && !hasInflated) {
            binding?.fragmentViewStub?.viewStub?.inflate()
            onCreateViewAfterViewStubInflated(mSavedInstanceState)
            afterViewStubInflated()
        }
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        visible = true
        if (binding?.fragmentViewStub != null && !hasInflated) {
            binding?.fragmentViewStub?.viewStub?.inflate()
            onCreateViewAfterViewStubInflated(mSavedInstanceState)
            afterViewStubInflated()
        }
    }

    override fun onPause() {
        super.onPause()
        visible = false
    }

    override fun onDetach() {
        super.onDetach()
        hasInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        hasInflated = false
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onBack() : Boolean{
        return onBackPress()
    }

    protected abstract fun onCreateViewAfterViewStubInflated(savedInstanceState: Bundle?)

    @CallSuper
    protected fun afterViewStubInflated(){
        hasInflated = true
        if (binding != null) {
            binding?.lnLoading?.hide()
        }
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