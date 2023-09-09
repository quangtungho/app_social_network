package vn.techres.line.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.chat.request.JoinChatRequest
import vn.techres.line.data.model.chat.request.LeaveChatRequest
import vn.techres.line.data.model.chat.request.group.ChatTextGroupRequest
import vn.techres.line.data.model.chat.request.personal.ChatTextPersonalRequest
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat

class BroadCastMessage : BroadcastReceiver(){
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        mSocket = application.getSocketInstance(TechResApplication.applicationContext())
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(TechResApplication.applicationContext())
        user = CurrentUser.getCurrentUser(TechResApplication.applicationContext())
        val remoteInput = androidx.core.app.RemoteInput.getResultsFromIntent(intent)
        val extras = intent!!.extras
        val notificationID = extras?.getInt("type", NotificationEnum.CHAT.toString().toInt())
        if(remoteInput != null){
                val replyText = remoteInput.getCharSequence("KEY_TEXT_REPLY")
                val groupId = extras?.getString("value")
                WriteLog.d("vao day chua ", replyText.toString())
                if(extras?.getString("conversation_type") == TechResEnumChat.PERSONAL.toString()){
                    joinPersonal(groupId ?: "")
                    chatTextPersonal(groupId ?: "", replyText.toString())
                    leavePersonal(groupId ?: "")
                }else{
                    joinGroup(groupId ?: "")
                    chatTextGroup(groupId ?: "", replyText.toString())
                    leaveGroup(groupId ?: "")
                }
        }
        if (notificationID != null) {
            TechResApplication.applicationContext().getNotificationManager()?.cancel(notificationID)
        }
    }
    /**
     * Group
     * */
    private fun joinGroup(groupId: String) {
        val joinGroup = JoinChatRequest()
        joinGroup.group_id = groupId
        joinGroup.member_id = user.id
        joinGroup.avatar = user.avatar_three_image.original
        joinGroup.full_name = user.name
        joinGroup.os_name = "android"
        try {
            val jsonObject = JSONObject(Gson().toJson(joinGroup))
            mSocket?.emit(TechResEnumChat.JOIN_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("JOIN_ROOM_GROUP_ALO_LINE", jsonObject.toString())

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun leaveGroup(groupId: String) {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = groupId
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name
        leaveChatRequest.avatar = user.avatar_three_image.original
        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_GROUP_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatTextGroup(groupId: String, message : String) {
        val chatTextRequest = ChatTextGroupRequest()
        chatTextRequest.member_id = user.id
        chatTextRequest.message_type = TechResEnumChat.TYPE_TEXT.toString()
        chatTextRequest.group_id = groupId
        chatTextRequest.message = message
        chatTextRequest.list_tag_name = ArrayList()
        chatTextRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatTextRequest))
            mSocket?.emit(TechResEnumChat.CHAT_TEXT_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_TEXT_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    /**
     * Personal
     * */
    private fun joinPersonal(groupId: String) {
        val joinChatRequest = JoinChatRequest()
        joinChatRequest.group_id = groupId
        joinChatRequest.member_id = user.id
        joinChatRequest.full_name = user.name
        joinChatRequest.avatar = user.avatar_three_image.original
        joinChatRequest.os_name = "android"
        try {
            val jsonObject = JSONObject(Gson().toJson(joinChatRequest))
            mSocket?.emit(TechResEnumChat.JOIN_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("JOIN_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun leavePersonal(groupId: String) {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = groupId
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name
        leaveChatRequest.avatar = user.avatar_three_image.original
        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatTextPersonal(groupId: String, message: String) {
        val chatTextPersonalRequest = ChatTextPersonalRequest()
        chatTextPersonalRequest.message_type = TechResEnumChat.TYPE_TEXT.toString()
        chatTextPersonalRequest.group_id = groupId
        chatTextPersonalRequest.message = message
        chatTextPersonalRequest.member_id = user.id
        chatTextPersonalRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatTextPersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_TEXT_PERSONAL_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}