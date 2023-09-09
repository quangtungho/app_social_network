package vn.techres.line.helper

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.data.model.utils.User


object CurrentUser {
    private var cacheManager: CacheManager? = null

    private var mUserInfo: User? = User()

    fun isLogin(it: Context): Boolean {
        return try {
            val sharedPreference = PreferenceHelper(it)
            val userJson = sharedPreference.getValueString(TechresEnum.CACHE_USER_INFO.toString())
            mUserInfo = Gson().fromJson(userJson, User::class.java)
            (mUserInfo?.id ?: 0) > 0
        } catch (e: Exception) {
            e.stackTrace
            false
        }
    }

    fun initCacheManager(cacheManager: CacheManager) {
        CurrentUser.cacheManager = cacheManager
    }

    fun getAccessToken(context: Context): String {
        val sharedPreference = PreferenceHelper(context)
        val configJava = CurrentConfigJava.getConfigJava(context)
        val userJson = sharedPreference.getValueString(TechresEnum.CACHE_USER_INFO.toString())
        val user = Gson().fromJson<User>(userJson, object :
            TypeToken<User>() {}.type)
        return if(isLogin(context)){
            return if (user.access_token.isNotEmpty()) {
                "Bearer ${user.access_token}"
            } else {
                "Basic " + configJava.api_key + ""
            }
        }else{
            "Basic " + configJava.api_key + ""
        }

    }

    fun getAccessTokenNodeJs(context: Context): String {
        val sharedPreference = PreferenceHelper(context)
        val userJson = sharedPreference.getValueString(TechresEnum.CACHE_USER_INFO.toString())
        val user = Gson().fromJson<User>(userJson, object :
            TypeToken<User>() {}.type)
        return if(isLogin(context)){
            if (user.nodeAccessToken.isNotEmpty()) {
                user.nodeAccessToken
            } else {
                ""
            }
        }else{
            ""
        }
    }

    fun saveUserInfo(ct: Context, userInfo: User?) {
        try {
            val userJson = Gson().toJson(userInfo, User::class.java)
            val sharedPreference = PreferenceHelper(ct)
            sharedPreference.save(TechresEnum.CACHE_USER_INFO.toString(), userJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentUser(ct: Context): User {
        try {
            val sharedPreference = PreferenceHelper(ct)
            val userJson = sharedPreference.getValueString(TechresEnum.CACHE_USER_INFO.toString())
            mUserInfo = Gson().fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            e.stackTrace
            return User()
        }
        if (mUserInfo == null)
            mUserInfo = User()
        return mUserInfo!!
    }

    fun saveFistRunApp(ct: Context,isFirst: Int) {
        PrefUtils.getInstance().putInt(TechresEnum.KEY_FIRST_RUN_APP.toString(), isFirst)
    }

    fun getFistRunApp(ct: Context): Int {
        return PrefUtils.getInstance().getInt(TechresEnum.KEY_FIRST_RUN_APP.toString(), 0)
    }


//    fun saveCreateBookingInfo(createBookingInfo: CreateBooking?) {
//        val createBookingInf = createBookingInfo
//        try {
//            //Luu thong tin đặt bàn
//            var userJson = cacheManager?.get(TechresEnum.BOOKING.toString())
////            var   gSon = Gson()
//
//            cacheManager?.put(TechresEnum.BOOKING.toString(), LoganSquare.serialize(createBookingInfo))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }

}
