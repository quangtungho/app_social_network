package vn.techres.line.activity

//import com.amitshekhar.DebugDB
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.FirebaseApp
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.WriteLog

@SuppressLint("Registered")
class TechResApplicationNodeJs : Application() {
    var mSocket: Socket? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: TechResApplicationNodeJs? = null
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null
        fun applicationContext() : TechResApplicationNodeJs {
            return instance as TechResApplicationNodeJs
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
    fun getInstance(): TechResApplicationNodeJs? {
        return instance
    }

    fun getSocketInstance(context: Context): Socket? {
        return if(mSocket != null){
            mSocket
        }else{
            try{
                val configJava = CurrentConfigJava.getConfigJava(context)
                val opts = IO.Options()
                opts.transports = arrayOf(WebSocket.NAME)
                mSocket = IO.socket(configJava.realtime_domain + "/customers", opts)
            }catch (e: Exception){
                WriteLog.e("SOCKET", "Socket init")
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
