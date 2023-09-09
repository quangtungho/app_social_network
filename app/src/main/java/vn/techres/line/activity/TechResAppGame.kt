package vn.techres.line.activity

//import com.amitshekhar.DebugDB
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.firebase.FirebaseApp
import io.socket.client.IO
import io.socket.client.Socket
import vn.techres.line.helper.AppConfig

@SuppressLint("Registered")
class TechResAppGame : Application() {
    var mSocket: Socket? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: TechResAppGame? = null
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null
        fun applicationContext() : TechResAppGame {
            return instance as TechResAppGame
        }
    }
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppConfig.loadConfig(this)
        FirebaseApp.initializeApp(this)
//        DebugDB.getAddressLog()
        applicationContext()
        context = applicationContext
        isConnected()
    }

    @Synchronized
    fun getInstance(): TechResAppGame? {
        return instance
    }

    fun getSocketInstance(idRestaurant: String): Socket? {
        return if(mSocket != null){
            mSocket
        }else{
            try{
                mSocket = IO.socket("https://api.vongquay.techres.vn/$idRestaurant")
            }catch (e: Exception){
                Log.e("SOCKET", "Socket init")
            }
            mSocket
        }
    }

    private fun isConnected(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}
