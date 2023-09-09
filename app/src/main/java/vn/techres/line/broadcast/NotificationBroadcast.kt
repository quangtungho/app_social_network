package vn.techres.line.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.TechResApplication.Companion.vibratorCall
import vn.techres.line.broadcast.MyFirebaseMessagingService.Companion.mMediaPlayer
import vn.techres.line.call.CallActivity
import vn.techres.line.call.SocketNewCallRequest
import vn.techres.line.data.model.eventbus.EvenBusCallVideo
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.videocall.SocketCallEmitDataEnum

/**
 * @Author: Phạm Văn Nhân
 * @Date: 22/04/2022
 */
class NotificationBroadcast : BroadcastReceiver() {
    @SuppressLint("NonConstantResourceId", "UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_BUTTON_CLICKED, -1)
        val notificationID = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val groupID = intent.getStringExtra(EXTRA_GROUP_ID)
        val keyRoom = intent.getStringExtra(EXTRA_KEY_ROOM)
        val callMemberCreate = intent.getIntExtra(EXTRA_CALL_MEMBER_CREATE, 0)
        val messageType = intent.getStringExtra(EXTRA_MESSAGE_TYPE)
        val namePersonal = intent.getStringExtra(EXTRA_NAME_PERSONAL)
        val avatarPersonal = intent.getStringExtra(EXTRA_AVATAR_PERSONAL)
        mMediaPlayer?.stop()
        vibratorCall?.cancel()
        when (id) {
            R.id.flReject -> {
                TechResApplication.applicationContext().getNotificationManager()?.cancel(notificationID)
                ChatUtils.emitSocket(SocketCallEmitDataEnum.CALL_REJECT(), SocketNewCallRequest(
                    groupID,
                    keyRoom,
                    messageType,
                    callMemberCreate,
                    ""
                ))
                EventBus.getDefault().post(EvenBusCallVideo(false))
            }
            R.id.flAccept -> {
                TechResApplication.applicationContext().getNotificationManager()?.cancel(notificationID)
                try {
                    val i = Intent(
                        context,
                        CallActivity::class.java
                    )
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    ChatUtils.connectToRoomFromBroadcast(
                        i,
                        groupID,
                        namePersonal,
                        avatarPersonal,
                        callMemberCreate,
                        2,
                        keyRoom,
                        messageType
                    )
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
                EventBus.getDefault().post(EvenBusCallVideo(true))
            }
        }
    }

    companion object {
        const val EXTRA_BUTTON_CLICKED = "EXTRA_BUTTON_CLICKED"
        const val EXTRA_KEY_ROOM = "EXTRA_KEY_ROOM"
        const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
        const val EXTRA_CALL_MEMBER_CREATE = "EXTRA_CALL_MEMBER_CREATE"
        const val EXTRA_MESSAGE_TYPE = "EXTRA_MESSAGE_TYPE"
        const val EXTRA_NAME_PERSONAL = "EXTRA_NAME_PERSONAL"
        const val EXTRA_AVATAR_PERSONAL = "EXTRA_AVATAR_PERSONAL"
    }
}