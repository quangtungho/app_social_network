package vn.techres.line.roomdatabase.chat

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.getRoundedRectBitmap

object MockDatabase {
    fun getMessagingStyleData(context: Context?): MessagingStyleCommsAppData? {
        return MessagingStyleCommsAppData().getInstance(context)
    }

    open class MockNotificationData{
        // Standard notification values:
        var numberMessage = 0
        var mContentTitle: String = ""
        var mContentText: String = ""
        var mPriority = 0

        // Notification channel values (O and above):
        var mChannelId: String? = null
        var mChannelName: CharSequence? = null
        var mChannelDescription: String? = null
        var mChannelImportance = 0
        var mChannelEnableVibrate = false
        var mChannelLockscreenVisibility = 0

        // Notification Standard notification get methods:
        fun getContentTitle(): String {
            return mContentTitle
        }

        fun getContentText(): String {
            return mContentText
        }

        fun getPriority(): Int {
            return mPriority
        }

        // Channel values (O and above) get methods:
        fun getChannelId(): String? {
            return mChannelId
        }

        fun getChannelName(): CharSequence? {
            return mChannelName
        }

        fun getChannelDescription(): String? {
            return mChannelDescription
        }

        fun getChannelImportance(): Int {
            return mChannelImportance
        }

        fun isChannelEnableVibrate(): Boolean {
            return mChannelEnableVibrate
        }

        fun getChannelLockscreenVisibility(): Int {
            return mChannelLockscreenVisibility
        }
    }
    open class MessagingStyleCommsAppData() : MockNotificationData(){
        private var sInstance: MessagingStyleCommsAppData? = null
        private var context : Context? = null
        private var configNodeJs = ConfigNodeJs()
        private var user = User()
        // Unique data for this Notification.Style:
        private var mMessages: ArrayList<NotificationCompat.MessagingStyle.Message>? = null

        // String of all mMessages.
        private var mFullConversation: String = ""

        // Name preferred when replying to chat.
        private var mMe: Person? = null
        private var mPerson: Person? = null
        private var mParticipants: ArrayList<Person>? = null

        private var mReplyChoicesBasedOnLastMessages: Array<CharSequence> = arrayOf()

        fun getInstance(context: Context?): MessagingStyleCommsAppData? {
            if (sInstance == null) {
                sInstance = getSync(context)
            }
            return sInstance
        }

        @Synchronized
        private fun getSync(context: Context?): MessagingStyleCommsAppData? {
            if (sInstance == null) {
                sInstance = MessagingStyleCommsAppData(context)
            }
            return sInstance
        }
        constructor(context: Context?):this(){
            context?.let {
                this.context = it
                configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(it)
                user = CurrentUser.getCurrentUser(it)
            }
            mChannelId = TechResApplication.MESSAGE_CHANNEL
            mReplyChoicesBasedOnLastMessages = arrayOf("Ok", "Um", "Đúng vậy", "Đồng ý")
            // String version of the mMessages above.

            // String version of the mMessages above.
            mFullConversation = """
                Famous: [Picture of Moon]
                
                Me: Visiting the moon again? :P
                
                Wendy: HEY, I see my house! :)
                
                
                """.trimIndent()
            mPriority = NotificationCompat.PRIORITY_HIGH
            mParticipants = ArrayList()
            mMessages = ArrayList()
            mChannelImportance = NotificationManager.IMPORTANCE_MAX
            mChannelEnableVibrate = true
            mChannelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE

        }
        fun createMember(notifyBubbleChat: vn.techres.line.data.model.chat.NotifyBubbleChat){
            numberMessage++
            mContentTitle = String.format("%s %s", numberMessage, "tin nhắn mới")
            if(mMessages == null){
                mMessages = ArrayList()
            }

            if(mParticipants == null){
                mParticipants = ArrayList()
            }

            val avatarTemp = Gson().fromJson<Avatar>(
                notifyBubbleChat.avatar,
                object : TypeToken<Avatar>() {}.type
            )
            val iconCompat = if(avatarTemp.original != ""){
                val bitmap = Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .load(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            avatarTemp.original
                        )
                    )
                    .placeholder(R.drawable.ic_user_placeholder_circle)
                    .error(R.drawable.ic_user_placeholder_circle)
                    .submit(100, 100)
                    .get()
                IconCompat.createWithAdaptiveBitmap(getRoundedRectBitmap(bitmap, 100))
//                IconCompat.createWithContentUri(String.format("%s%s", configNodeJs.api_ads, avatarTemp.original))
            }else{
                IconCompat.createWithResource(context, R.drawable.ic_user_placeholder_circle)
            }
            val personalCache = mParticipants?.find { it.key ==  notifyBubbleChat.member_id?.toString() + ""}
            val personal = if(personalCache != null){
                personalCache
            }else{
                mPerson = Person.Builder()
                    .setName(notifyBubbleChat.full_name ?: "Yor Name")
                    .setKey(notifyBubbleChat.member_id?.toString() + "")
                    .setUri("tel:1234567890")
                    .setIcon(iconCompat)
                    .build()
                mPerson
            }
            if (personal != null) {
                mParticipants?.add(personal)
            }
            mMessages?.add(
                NotificationCompat.MessagingStyle.Message(
                    notifyBubbleChat.body ?: "",
                    System.currentTimeMillis(),
                    personal
                ))
        }

        override fun toString(): String {
            return getFullConversation()
        }

        fun createMe(replyMessage : String){
            numberMessage++
            mContentTitle = String.format("%s %s", numberMessage, "tin nhắn mới")
            if(mMessages == null){
                mMessages = ArrayList()
            }

            if(mParticipants == null){
                mParticipants = ArrayList()
            }
            val iconCompat = if(user.avatar_three_image.original != ""){
                val bitmap = Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .load(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            user.avatar_three_image.original
                        )
                    )
                    .placeholder(R.drawable.ic_user_placeholder_circle)
                    .error(R.drawable.ic_user_placeholder_circle)
                    .submit(100, 100)
                    .get()
                IconCompat.createWithAdaptiveBitmap(getRoundedRectBitmap(bitmap, 100))
            }else{
                IconCompat.createWithResource(context, R.drawable.ic_user_placeholder_circle)
            }
            val person = if(mMe != null){
                mMe
            }else{
                mMe = Person.Builder()
                    .setName(user.name ?: "")
                    .setKey(user._id + "")
                    .setUri("tel:1234567890")
                    .setIcon(iconCompat)
                    .build()
                mMe
            }
            if(mParticipants?.find { it.key ==  user._id } == null){
                if (person != null) {
                    mParticipants?.add(person)
                }
            }
            mMessages?.add(
                NotificationCompat.MessagingStyle.Message(
                    replyMessage,
                    System.currentTimeMillis(),
                    person
                ))
        }
        fun getMe() : Person?{
            return mMe
        }
        fun getPerson() : Person?{
            return mPerson
        }
        fun isGroupConversation(): Boolean {
            return mParticipants?.size ?: 0 > 1
        }
        fun getReplyChoicesBasedOnLastMessage() : Array<CharSequence>{
            return mReplyChoicesBasedOnLastMessages
        }
        fun getNumberOfNewMessages(): Int {
            return mMessages?.size ?: 0
        }
        fun getParticipants(): ArrayList<Person>? {
            return mParticipants
        }
        fun getMessages(): ArrayList<NotificationCompat.MessagingStyle.Message>? {
            return mMessages
        }
        open fun getFullConversation(): String {
            return mFullConversation
        }
        fun isMessagePerson(notifyBubbleChat: vn.techres.line.data.model.chat.NotifyBubbleChat) : Boolean{
            return if(mParticipants?.isNotEmpty() == true){
                val personalCache = mParticipants?.find { it.key ==  notifyBubbleChat.member_id?.toString() + ""}
                personalCache != null
            }else{
                false
            }
        }
    }

}