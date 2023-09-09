package vn.techres.line.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.ProfileActivity
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.chat.OnBackClick
import kotlin.random.Random

@SuppressLint("UseRequireInsteadOfGet")
abstract class BaseFragment : Fragment(), OnBackClick {
    private var mContainer: ViewGroup? = null
    val cacheManager: CacheManager = CacheManager.getInstance()
    protected abstract val layoutResourceId: Int
    protected abstract fun onMapping(view: View)
    protected abstract fun onSetBodyView(
        view: View?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
    protected abstract fun onBackPress() : Boolean

    override fun onBack() : Boolean{
        return onBackPress()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mBaseView = inflater.inflate(layoutResourceId, container, false)
        mContainer = container
        onMapping(mBaseView)
        onSetBodyView(mBaseView, mContainer, savedInstanceState)
        mainActivity?.setOnBackClick(this)
        return mBaseView
    }

    val mainActivity: MainActivity?
        get() = activity as MainActivity?

    fun closeKeyboard(edt: EditText) {
        edt.requestFocus()

//        val imm = mainActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt.windowToken, 0)
    }
    fun showKeyboard(edt: EditText) {
        edt.requestFocus()
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            mainActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }
//    fun replace(fragment: Fragment, frameId: Int){
//        fragmentManager!!.beginTransaction()
//            .replace(frameId, fragment)
//            .commit()
//    }
    fun restaurant() : RestaurantCard{
        var restaurant = RestaurantCard()
        val sharedPreference = PreferenceHelper(mainActivity!!)
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

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("HardwareIds")
    fun generateID(): String? {
        @SuppressLint("HardwareIds")
        var deviceId = Settings.Secure.getString(
                mainActivity?.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        if ("9774d56d682e549c" == deviceId || deviceId == null) {
//            if (ActivityCompat.checkSelfPermission(
//                    mainActivity!!,
//                    Manifest.permission.READ_PHONE_STATE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//            }
            deviceId =
                (mainActivity!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                    .deviceId
            if (deviceId == null) {
                val tmpRand = Random
                deviceId = java.lang.String.valueOf(tmpRand.nextLong())
            }

            return deviceId
        }
        return deviceId
    }

    fun setOnClickProfile(userID : Int){
        val intent = Intent(mainActivity, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userID)
        startActivity(intent)
        mainActivity?.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}