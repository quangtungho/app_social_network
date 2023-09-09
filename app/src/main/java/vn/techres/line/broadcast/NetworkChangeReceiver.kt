package vn.techres.line.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.socket.client.Socket
import vn.techres.line.activity.TechResApplication


class NetworkChangeReceiver : BroadcastReceiver() {
    private var mSocket: Socket? = null
    private val application = TechResApplication()

    override fun onReceive(context: Context?, intent: Intent?) {
//        mSocket = application.getSocketInstance(TechResApplication.applicationContext())
//        mSocket?.connect()
//        try {
//            if (isOnline(context!!)) {
//                var messageDB = TechResApplication.applicationContext().getMessageDao()?.getOfflineMessage()
//                val messageList = ArrayList<MessagesByGroup>()
//                messageList.addAll(messageDB!!)
//                socketEmitText(messageList)
//            }
//        } catch (e: java.lang.NullPointerException) {
//            e.printStackTrace()
//        }
    }


}