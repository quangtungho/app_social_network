package vn.techres.line.broadcast

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.text.Html
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.leolin.shortcutbadger.ShortcutBadger
import timber.log.Timber
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.activity.TechResApplication.Companion.MESSAGE_CALL_CHANNEL_INSIDE
import vn.techres.line.activity.TechResApplication.Companion.MESSAGE_CALL_CHANNEL_OUTSIDE
import vn.techres.line.activity.TechResApplication.Companion.MESSAGE_CHANNEL
import vn.techres.line.call.AnswerCallChatActivity
import vn.techres.line.call.CallActivity
import vn.techres.line.call.SocketCallResponse
import vn.techres.line.data.model.CallDataRequest
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.videocall.NotifyVideoCall
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getBitmap
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"
    private val cacheManager = CacheManager.getInstance()
    private var configNodeJs = ConfigNodeJs()
    private var shortcutManager: ShortcutManager? = null
    private var internalStorage = InternalStorage()

    companion object {
        var instance: MyFirebaseMessagingService? = null

        const val EXTRA_BUTTON_CLICKED = "EXTRA_BUTTON_CLICKED"
        const val EXTRA_KEY_ROOM = "EXTRA_KEY_ROOM"
        const val EXTRA_MESSAGE_TYPE = "EXTRA_MESSAGE_TYPE"
        const val EXTRA_CALL_MEMBER_CREATE = "EXTRA_CALL_MEMBER_CREATE"
        const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
        const val EXTRA_SENDER_ID = "EXTRA_SENDER_ID"
        const val EXTRA_NAME_PERSONAL = "EXTRA_NAME_PERSONAL"
        const val EXTRA_AVATAR_PERSONAL = "EXTRA_AVATAR_PERSONAL"

        const val GROUP_KEY_NOTIFY = "GROUP_KEY_NOTIFY"
        const val GROUP_KEY_MESSAGE = "GROUP_KEY_MESSAGE"
        var mMediaPlayer: MediaPlayer? = null

    }

    init {
        instance = this
    }

    // [START receive_message]
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        // WriteLog.d(TAG, "From: " + remoteMessage.from)
        shortcutManager = getSystemService() ?: throw IllegalStateException()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        val type: Int
        var badgeCount = 0
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            WriteLog.d(TAG, "Message data payload: remote " + Gson().toJson(remoteMessage))
            type = Objects.requireNonNull(remoteMessage.data["type"]).toString().toInt()
            when (type) {
                NotificationEnum.TAKECARE_CUSTOMER.toString().toInt(),
                NotificationEnum.TAKECARE_CUSTOMER_NOT_GIFT.toString().toInt(),
                NotificationEnum.CHAT.toString().toInt() -> {
                    val avatar = remoteMessage.data["avatar"].toString()
                    val content = remoteMessage.data["content"].toString()
                    val title = remoteMessage.data["title"].toString()
                    val value = remoteMessage.data["value"].toString()
                    val conversationType = remoteMessage.data["conversation_type"].toString()

                    sendNotificationChat(content, title, avatar, value, type, conversationType)
                }

                NotificationEnum.VIDEO_CALL.toString().toInt() -> {
                    val json = Gson().toJson(remoteMessage.data)
                    val notifyVideoCall = Gson().fromJson<NotifyVideoCall>(
                        json, object :
                            TypeToken<NotifyVideoCall>() {}.type
                    )
                    sentCallVideo(notifyVideoCall)
                }

                else -> {
                    val avatar = remoteMessage.data["avatar"].toString()
                    val content = remoteMessage.data["content"].toString()
                    val title = remoteMessage.data["title"].toString()
                    val value = remoteMessage.data["value"].toString()
                    sentNotificationMainActivity(content, title, type, avatar, value)
                }
            }

            if (!StringUtils.isNullOrEmpty(remoteMessage.data["badgeCount"])) {
                badgeCount = remoteMessage.data["badgeCount"].toString().toInt()
            }
            if (ShortcutBadger.isBadgeCounterSupported(applicationContext)) {
                ShortcutBadger.applyCount(applicationContext, badgeCount)
            }
//            scheduleJob()
        }

        ShortcutBadger.applyCount(applicationContext, badgeCount)
    }

    // [START on_new_token]

    // [END receive_message]
    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        WriteLog.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    // [END on_new_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        WriteLog.d("PUSH_TOKEN: ", token)
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
//        cacheManager.put(TechresEnum.PUSH_TOKEN.toString(), token)

        internalStorage.writeObject(
            this,
            TechresEnum.PUSH_TOKEN.toString(),
            token
        ).toString()
    }

    private fun sentNotificationMainActivity(
        content: String,
        title: String,
        type: Int,
        avatar: String,
        value: String
    ) {

        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        val appRunningBackground = runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        val intent: Intent
        if (appRunningBackground){
            intent = Intent(this, SplashScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            intent.putExtra("type", type)
            intent.putExtra("value", value)
            WriteLog.d("nhanpro", "chưa mở app")
        }else{
            WriteLog.d("nhanpro", "đã mở app")
            when (type){
                //Switch screen profile
                NotificationEnum.CONTACT.toString().toInt(),
                NotificationEnum.UPDATE_INFO.toString().toInt() -> {
                    intent = Intent(this, ProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.ID_USER.toString(), value)
                }

                //Switch screen detail post
                NotificationEnum.COMMENTS.toString().toInt(),
                NotificationEnum.REPLY_COMMENT.toString().toInt(),
                NotificationEnum.CREATE_BRANCH_REVIEW.toString().toInt(),
                NotificationEnum.REACH_POINT_ALOLINE.toString().toInt(),
                NotificationEnum.REACTIONS.toString().toInt(),
                NotificationEnum.REACTIONS_COMMENT.toString().toInt()-> {
                    intent = Intent(this, CommentActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.ID_POST.toString(), value)
                    intent.putExtra(TechresEnum.CHECK_COMMENT_CHOOSE.toString(), TechresEnum.VALUE_COMMENT_NOTIFY.toString())
                    intent.putExtra(TechresEnum.DETAIL_POST_COMMENT.toString(), Gson().toJson(PostReview()))
                }

                //Switch screen point
                NotificationEnum.POINT.toString().toInt() -> {
                    intent = Intent(this, PointCardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.ID_USER.toString(), value)
                }


                //Switch screen order
                NotificationEnum.ORDER.toString().toInt() -> {
                    intent = Intent(this, BillActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), value.toInt())
                }

                //Switch screen advert
                NotificationEnum.ADVERT.toString().toInt() -> {
                    intent = Intent(this, AdvertPackageActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.ID_USER.toString(), value)
                }

                //Switch screen booking
                NotificationEnum.BOOKING.toString().toInt() -> {
                    intent = Intent(this, DetailBookingActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra(TechresEnum.BOOKING.toString(), value)
                }

                //Switch screen...
                NotificationEnum.BIRTHDAY_CONGRATULATION.toString().toInt() -> {
                    intent = Intent(this, SplashScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    intent.putExtra("type", type)
                    intent.putExtra("value", value)
                }

                else -> {
                    intent = Intent(this, SplashScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    intent.putExtra("type", type)
                    intent.putExtra("value", value)
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder =
            NotificationCompat.Builder(this, TechResApplication.SYSTEM_NOTIFICATION)
                .setSmallIcon(R.drawable.notify_logo_aloline)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setLights(Color.MAGENTA, 500, 500)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_KEY_NOTIFY)

        if (avatar != ""){
            notificationBuilder.setLargeIcon(
                getBitmap(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        avatar
                    ), this
                )
            )
        }


        val notification = notificationBuilder.build()
        TechResApplication.applicationContext().getNotificationManager()?.notify(type, notification)
    }

    private fun sendNotificationChat(
        content: String,
        title: String,
        avatar: String,
        value: String,
        type: Int,
        conversationType: String
    ) {
        val replyLabel = "Nhập tin nhắn..."

        val remoteInput = RemoteInput.Builder("KEY_TEXT_REPLY")
            .setLabel(replyLabel)
            .build()
        val resultIntent = Intent(this, BroadCastMessage::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        resultIntent.putExtra("type", type)
        resultIntent.putExtra("value", value)
        resultIntent.putExtra("conversation_type", conversationType)

        val resultPendingIntentAction = PendingIntent.getBroadcast(
            applicationContext,
            10,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.notify_logo_aloline,
                "Trả lời",
                resultPendingIntentAction
            )
                .addRemoteInput(remoteInput)
                .build()

        val group = Group(value, conversationType)
        val jsonGroup = Gson().toJson(group)

        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        val appRunningBackground = runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        val intent: Intent
        if (appRunningBackground){
            intent = Intent(this, SplashScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            intent.putExtra("type", type)
            intent.putExtra("value", value)
            intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroup)
        }else{
            intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroup)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationBuilder = NotificationCompat.Builder(this, MESSAGE_CHANNEL)
            .setSmallIcon(R.drawable.notify_logo_aloline)
            .setContentTitle(title)
            .setContentText(content)
            .addAction(action)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (avatar != ""){
            notificationBuilder.setLargeIcon(
                getBitmap(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        avatar
                    ), this
                )
            )
        }

        val notification = notificationBuilder.build()
        TechResApplication.applicationContext().getNotificationManager()?.notify(type, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sentCallVideo(notifyVideoCall: NotifyVideoCall) {
        var notificationId = notifyVideoCall.group_id_for_notification!!
        TechResApplication.applicationContext().getNotificationManager()!!.cancel(notificationId)
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        var appRunningBackground = runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        when (notifyVideoCall.call_status) {
            //Cuộc gọi đến
            TechResEnumVideoCall.CALL_CONNECT.toString() -> {

                val remoteViews = RemoteViews(
                    baseContext.packageName,
                    R.layout.custom_notification_message_call
                )

                remoteViews.setTextViewText(R.id.txtName, notifyVideoCall.full_name)

                var idIcon = 0

                if (notifyVideoCall.type_message == "call_video") {
                    remoteViews.setTextViewText(R.id.txtStatus, "Đang gọi video đến")
                    idIcon = R.drawable.ic_video_call_incoming
                } else if (notifyVideoCall.type_message == "call_audio") {
                    remoteViews.setTextViewText(R.id.txtStatus, "Đang gọi thoại đến")
                    idIcon = R.drawable.ic_call_incoming
                }

                remoteViews.setImageViewBitmap(
                    R.id.imgAvatarCall, getBitmap(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            notifyVideoCall.avatar
                        ), this
                    )
                )

                val callDataRequest = CallDataRequest()
                callDataRequest.keyRoom = notifyVideoCall.room_id
                callDataRequest.messageType = notifyVideoCall.type_message
                callDataRequest.groupID = notifyVideoCall.group_id
                callDataRequest.memberID = notifyVideoCall.user_id
                callDataRequest.notificationID = notifyVideoCall.group_id_for_notification
                callDataRequest.name = notifyVideoCall.full_name
                callDataRequest.avatar = notifyVideoCall.avatar

                remoteViews.setOnClickPendingIntent(
                    R.id.flReject,
                    onButtonNotificationClick(
                        R.id.flReject,
                        callDataRequest
                    )
                )

                remoteViews.setOnClickPendingIntent(
                    R.id.flAccept,
                    onButtonNotificationClick(
                        R.id.flAccept,
                        callDataRequest
                    )
                )

                val pendingIntent: PendingIntent
                val notification: Notification
                val intent = Intent(this, AnswerCallChatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(
                    "DATA_CALL", Gson().toJson(
                        SocketCallResponse(
                            CurrentUser.getCurrentUser(this).id,
                            notifyVideoCall.full_name,
                            notifyVideoCall.avatar,
                            notifyVideoCall.group_id,
                            notifyVideoCall.type_message,
                            notifyVideoCall.room_id,
                            notifyVideoCall.call_member_create,
                            notifyVideoCall.group_id_for_notification
                        )
                    )
                )
                pendingIntent = PendingIntent.getActivity(
                    this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                if (appRunningBackground) {
                    notification = NotificationCompat.Builder(this, MESSAGE_CALL_CHANNEL_OUTSIDE)
                        .setCustomBigContentView(remoteViews)
                        .setContent(remoteViews)
                        .setColor(resources.getColor(R.color.green, null))
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setTimeoutAfter(60000)
                        .setFullScreenIntent(pendingIntent, true)
                        .setSmallIcon(idIcon)
                        .build()

                } else {
                    Objects.requireNonNull(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    notification = NotificationCompat.Builder(this, MESSAGE_CALL_CHANNEL_INSIDE)
                        .setCustomBigContentView(remoteViews)
                        .setContent(remoteViews)
                        .setColor(resources.getColor(R.color.green, null))
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setTimeoutAfter(60000)
                        .setFullScreenIntent(pendingIntent, true)
                        .setSmallIcon(idIcon)
                        .build()
                }

                val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
                val isScreenOn: Boolean = pm.isScreenOn
                if (isScreenOn) {
                    WriteLog.d("nhanpro", "đang mở màn hình")
                } else {
                    WriteLog.d("nhanpro", "đang tắt màn hình")
                }

                notification.flags = Notification.FLAG_INSISTENT
                TechResApplication.applicationContext().getNotificationManager()!!
                    .notify(notificationId, notification)
            }

            // Từ chối cuộc gọi
            else -> {
                //Khi click notification, click vào nut nhan tin
                val group: Group = notifyVideoCall.conversation_type?.let {
                    notifyVideoCall.group_id?.let { it1 ->
                        Group(
                            it1,
                            it
                        )
                    }
                }!!
                val jsonGroup = Gson().toJson(group)

                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroup)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val pendingIntent = PendingIntent.getActivity(
                    this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                //Khi click notification, click vào gọi
                val intentCall = Intent(this, CallActivity::class.java)
                intentCall.putExtra(TechresEnum.EXTRA_ID_GROUP.toString(), notifyVideoCall.group_id)
                intentCall.putExtra(
                    TechresEnum.EXTRA_NAME_PERSONAL.toString(),
                    notifyVideoCall.full_name
                )
                intentCall.putExtra(
                    TechresEnum.EXTRA_AVATAR_PERSONAL.toString(),
                    notifyVideoCall.avatar
                )
                intentCall.putExtra(TechresEnum.EXTRA_KEY_ROOM.toString(), notifyVideoCall.room_id)
                intentCall.putExtra(TechresEnum.EXTRA_TYPE_OPTION.toString(), 1)
                intentCall.putExtra(
                    TechresEnum.EXTRA_MEMBER_CREATE.toString(),
                    CurrentUser.getCurrentUser(this).id
                )
                intentCall.putExtra(
                    TechresEnum.EXTRA_MESSAGE_TYPE.toString(),
                    notifyVideoCall.type_message
                )

                @SuppressLint("UnspecifiedImmutableFlag")
                val resultPendingIntentCall =
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intentCall,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                try {
                    val imageBitmap: Bitmap = try {
                        //Custom layout
                        Glide.with(this)
                            .asBitmap()
                            .load(
                                getBitmap(
                                    String.format(
                                        "%s%s",
                                        configNodeJs.api_ads,
                                        notifyVideoCall.avatar
                                    ), this
                                )
                            )
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(
                                RequestOptions().placeholder(R.drawable.logo_aloline_placeholder)
                                    .error(R.drawable.logo_aloline_placeholder)
                            )
                            .centerCrop()
                            .circleCrop()
                            .submit()
                            .get()
                    } catch (e: java.lang.Exception) {
                        Glide.with(this)
                            .asBitmap()
                            .load(
                                R.drawable.logo_aloline_placeholder
                            )
                            .circleCrop()
                            .placeholder(R.drawable.logo_aloline_placeholder)
                            .error(R.drawable.logo_aloline_placeholder)
                            .submit(100, 100)
                            .get()
                    }
                    var idIcon = 0
                    if (notifyVideoCall.type_message == "call_video") {
                        idIcon = R.drawable.ic_miss_call_video
                    } else if (notifyVideoCall.type_message == "call_audio") {
                        idIcon = R.drawable.ic_miss_call_audio
                    }

                    //end custom
                    val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setSmallIcon(idIcon)
                        .setColor(resources.getColor(R.color.red, null))
                        .setContentTitle(Html.fromHtml("Cuộc gọi nhỡ", Html.FROM_HTML_MODE_COMPACT))
                        .setContentText(
                            Html.fromHtml(
                                java.lang.String.format(
                                    "<b>%s</b>",
                                    notifyVideoCall.full_name
                                ), Html.FROM_HTML_MODE_COMPACT
                            )
                        )
                        .setStyle(
                            NotificationCompat.BigTextStyle().bigText(
                                Html.fromHtml(
                                    java.lang.String.format(
                                        "<b>%s</b>",
                                        notifyVideoCall.full_name
                                    ), Html.FROM_HTML_MODE_COMPACT
                                )
                            )
                        )
                        .setLargeIcon(imageBitmap)
                        .addAction(0, "Nhắn tin", pendingIntent)
                        .addAction(0, "Gọi lại", resultPendingIntentCall)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false)
                        .setVibrate(longArrayOf(0L))
                        .build()
                    notification.flags = Notification.FLAG_ONLY_ALERT_ONCE
                    TechResApplication.applicationContext().getNotificationManager()
                        ?.notify(notificationId, notification)
                } catch (e: java.lang.Exception) {
                    Timber.d("Error : %s", e.message)
                }
            }
        }
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun onButtonNotificationClick(
        @IdRes id: Int,
        data: CallDataRequest,
    ): PendingIntent? {
        val intent = Intent(
            applicationContext,
            NotificationBroadcast::class.java
        )
        intent.putExtra(
            EXTRA_BUTTON_CLICKED,
            id
        )
        intent.putExtra(
            EXTRA_GROUP_ID,
            data.groupID
        )
        intent.putExtra(
            EXTRA_KEY_ROOM,
            data.keyRoom
        )
        intent.putExtra(
            EXTRA_NOTIFICATION_ID,
            data.notificationID
        )
        intent.putExtra(
            EXTRA_CALL_MEMBER_CREATE,
            data.memberID
        )
        intent.putExtra(
            EXTRA_MESSAGE_TYPE,
            data.messageType
        )
        intent.putExtra(
            EXTRA_SENDER_ID,
            data.senderID
        )
        intent.putExtra(
            EXTRA_NAME_PERSONAL,
            data.name
        )
        intent.putExtra(
            EXTRA_AVATAR_PERSONAL,
            data.avatar
        )
        return PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}