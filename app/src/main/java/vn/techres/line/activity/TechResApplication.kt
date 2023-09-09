package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.media.AudioAttributes
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.os.Vibrator
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.multidex.BuildConfig
import androidx.multidex.MultiDex
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.emoji.iosprovider.AXIOSEmojiProvider
import com.facebook.ads.AudienceNetworkAds
import com.facebook.drawee.backends.pipeline.Fresco
import com.giphy.sdk.ui.Giphy
import com.google.android.gms.phenotype.PhenotypeFlag
import com.google.firebase.FirebaseApp
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.Polling
import io.socket.engineio.client.transports.PollingXHR
import io.socket.engineio.client.transports.WebSocket
import net.gotev.uploadservice.UploadServiceConfig.initialize
import net.gotev.uploadservice.UploadServiceConfig.retryPolicy
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.data.UploadNotificationAction
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.extensions.getCancelUploadIntent
import okhttp3.OkHttpClient
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.helper.*
import vn.techres.line.helper.glide.MyGlideModule
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.roomdatabase.AppDatabase
import vn.techres.line.roomdatabase.chat.GroupDao
import vn.techres.line.roomdatabase.chat.MessageDao
import vn.techres.line.roomdatabase.contact.ContactDataDao
import vn.techres.line.roomdatabase.contact.FriendDao
import vn.techres.line.roomdatabase.qrcode.QrCodeDao
import vn.techres.line.services.ServiceFactory
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@SuppressLint("StaticFieldLeak")
class TechResApplication : Application() {
    private val tag = "TechResApplication"
    private var mSocketGame: Socket? = null
    private var context: Context? = null
    private var notificationManager: NotificationManager? = null
    private lateinit var channelMessageCalling: NotificationChannel // Kenh thong bao cuoc goi
    private lateinit var channelMessageCallOutside: NotificationChannel // Kenh thong bao cuoc goi
    private lateinit var channelMessageCallInside: NotificationChannel // Kenh thong bao cuoc goi
    private lateinit var channelNotificationSystem: NotificationChannel // Kênh thong báo hệ thống
    private lateinit var channelMessage: NotificationChannel // Kenh thong bao tin nhan
    private lateinit var channelUpload: NotificationChannel// Kenh thong bao upload
    private var username = ""
    private var friendDao: FriendDao? = null
    private var contactDao: ContactDataDao? = null
    private var qrcodeDao: QrCodeDao? = null
    private var appDatabase: AppDatabase? = null

    companion object {
        var mSocket: Socket? = null
        var mSocketLogout: Socket? = null
        var instance: TechResApplication? = null
        var widthView = 0
        var heightView = 0
        var groupDao: GroupDao? = null
        var messageDao: MessageDao? = null

        fun applicationContext(): TechResApplication {
            return instance as TechResApplication
        }

        const val MESSAGE_CHANNEL = "MESSAGE_CHANNEL"
        const val MESSAGE_CALLING_CHANNEL = "MESSAGE_CALLING_CHANNEL"
        const val MESSAGE_CALL_CHANNEL_OUTSIDE = "MESSAGE_CALL_CHANNEL_OUTSIDE"
        const val MESSAGE_CALL_CHANNEL_INSIDE = "MESSAGE_CALL_CHANNEL_INSIDE"
        const val SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION"
        const val UPLOAD_CHANNEL = "UPLOAD_CHANNEL"

        var vibratorCall: Vibrator? = null

    }

    override fun onCreate() {
        super.onCreate()
        vibratorCall = getSystemService(VIBRATOR_SERVICE) as Vibrator

        instance = this

        context = this
        PhenotypeFlag.maybeInit(baseContext)
        AppConfig.loadConfig(this)
        FirebaseApp.initializeApp(this)

//        Firebase.initialize(this)

        LifecycleHandler.instance.init(this)
        Fresco.initialize(this)
        Giphy.configure(this, TechresEnum.GIPHY_ANDROID_SDK_KEY.toString(), true)
        ServiceFactory.instance.setContext(this)
        createNotificationChannels()
        if (CurrentUser.isLogin(this)) {
            mSocket = this.getSocketInstance(this)
            mSocketLogout = this.getSocketLogoutInstance(this)
        }
        initialize(this, UPLOAD_CHANNEL, BuildConfig.DEBUG)
        retryPolicy = RetryPolicyConfig(1, 10, 2, 3)

//        DebugDB.getAddressLog()
        applicationContext()
        isConnected()
//        autoSetOverlayPermission(this, packageName)
//        FacebookSdk.sdkInitialize(applicationContext)

        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this)
        setConfigKeyBoard()
        //registry Glide SSL
        MyGlideModule()
        //
        val builder =
            StrictMode.VmPolicy.Builder()
        builder.detectFileUriExposure()
        StrictMode.setVmPolicy(builder.build())
        //
        val wm = context?.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        widthView = size.x
        heightView = size.y
        WriteLog.d(tag, "height devices : $heightView")
        WriteLog.d(tag, "width devices : $widthView")
        // install rtc
//        SkyEngineKit.init(CallEvent())
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader.init
         */
        FileDownloader.setupOnApplicationOnCreate(this)
            .connectionCreator(
                FileDownloadUrlConnection.Creator(
                    FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15000) // set connection timeout.
                        .readTimeout(15000) // set read timeout.
                )
            )
            .commit()

        appDatabase = AppDatabase.invoke(applicationContext)
        friendDao = appDatabase?.friendDao()
        groupDao = appDatabase?.groupDao()
        messageDao = appDatabase?.messageDao()
        contactDao = appDatabase?.contactDataDao()
        qrcodeDao = appDatabase?.qrCodeDao()
        setupFolderInternalStorage()
        FileUtils.getStorageDir("")
    }
    fun setupFolderInternalStorage() {
        applicationContext.getDir("aloneline", Context.MODE_PRIVATE)
        val folder = File(
            context?.getExternalFilesDir(null) ?: context?.filesDir,
            "aloline"
        )
        if (!folder.mkdirs())
            folder.mkdirs()
    }

    @Synchronized
    fun getInstance(): TechResApplication? {
        return instance
    }

    fun getSocketInstance(context: Context): Socket? {
        val config = CurrentConfigNodeJs.getConfigNodeJs(context)
        return if (mSocket != null) {
            mSocket
        } else {
            try {
                val trustAllCerts: Array<TrustManager> = arrayOf(
                    @SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                )

                try {
                    val sslContext: SSLContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
                    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    builder.hostnameVerifier { _, _ -> true }
                    builder.connectTimeout(20, TimeUnit.SECONDS)
                    builder.writeTimeout(1, TimeUnit.MINUTES)
                    builder.readTimeout(1, TimeUnit.MINUTES)
                    IO.setDefaultOkHttpCallFactory(builder.build())
                    IO.setDefaultOkHttpWebSocketFactory(builder.build())
                } catch (ignored: java.lang.Exception) {
                }
                val opts = IO.Options()
                opts.reconnection = true
                opts.transports = arrayOf(Polling.NAME, WebSocket.NAME, PollingXHR.NAME)
                mSocket = IO.socket(config.api_chat_aloline, opts).connect()
            } catch (e: Exception) {
            }
            mSocket
        }
    }


    fun getSocketLogoutInstance(context: Context): Socket? {
        val config = CurrentConfigJava.getConfigJava(this)
        return if (mSocketLogout != null) {
            mSocketLogout
        } else {
            try {
                val trustAllCerts: Array<TrustManager> = arrayOf(
                    @SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                )

                try {
                    val sslContext: SSLContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
                    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    builder.hostnameVerifier { _, _ -> true }
                    builder.connectTimeout(20, TimeUnit.SECONDS)
                    builder.writeTimeout(1, TimeUnit.MINUTES)
                    builder.readTimeout(1, TimeUnit.MINUTES)
                    IO.setDefaultOkHttpCallFactory(builder.build())
                    IO.setDefaultOkHttpWebSocketFactory(builder.build())
                } catch (ignored: java.lang.Exception) {
                }
                val opts = IO.Options()
                opts.reconnection = true
                opts.transports = arrayOf(Polling.NAME, WebSocket.NAME, PollingXHR.NAME)
                mSocketLogout = IO.socket(config.api_oauth_node, opts).connect()
            } catch (e: Exception) {
            }
            mSocketLogout
        }
    }
    fun getSocketGameInstance(context: Context, idRestaurant: Int): Socket? {
        CurrentConfigNodeJs.getConfigNodeJs(context)
        return if (mSocketGame != null) {
            mSocketGame
        } else {
            try {
                val trustAllCerts: Array<TrustManager> = arrayOf(
                    @SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            chain: Array<out X509Certificate>?,
                            authType: String?
                        ) {

                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                )

                try {
                    val sslContext: SSLContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
                    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                    builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    builder.hostnameVerifier { _, _ -> true }
                    builder.connectTimeout(20, TimeUnit.SECONDS)
                    builder.writeTimeout(1, TimeUnit.MINUTES)
                    builder.readTimeout(1, TimeUnit.MINUTES)
                    IO.setDefaultOkHttpCallFactory(builder.build())
                    IO.setDefaultOkHttpWebSocketFactory(builder.build())
                } catch (ignored: java.lang.Exception) {
                }
                val opts = IO.Options()
                opts.reconnection = true
                opts.transports = arrayOf(Polling.NAME, WebSocket.NAME, PollingXHR.NAME)
//                mSocketGame = IO.socket(config.api_lucky_wheel, opts)

                mSocketGame = IO.socket(
                    String.format(
                        "%s%s",
                        "https://api.vongquay.techres.vn/",
                        idRestaurant
                    ), opts
                ).connect()
            } catch (e: Exception) {
            }
            mSocketGame
        }
    }
    private fun isConnected(): Boolean {
        val cm =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setConfigKeyBoard() {
//        FileUtils.writeFileOnInternalStorage(baseContext, "data_room.json", "")

        AXEmojiManager.install(this, AXIOSEmojiProvider(this))
        UIView.mEmojiView = true
        UIView.mSingleEmojiView = false
        UIView.mStickerView = true
        UIView.mCustomView = false
        UIView.mFooterView = true
        UIView.mCustomFooter = true
        UIView.mWhiteCategory = true
        UIView.loadTheme()
        AXEmojiManager.getEmojiViewTheme().footerSelectedItemColor = ContextCompat.getColor(
            this,
            R.color.main_bg
        )
        AXEmojiManager.getEmojiViewTheme().footerBackgroundColor = ContextCompat.getColor(
            this,
            R.color.white
        )
        AXEmojiManager.getEmojiViewTheme().selectionColor = ContextCompat.getColor(
            this,
            R.color.transparent
        )
        AXEmojiManager.getEmojiViewTheme().selectedColor = ContextCompat.getColor(
            this,
            R.color.main_bg
        )
        AXEmojiManager.getEmojiViewTheme().categoryColor = ContextCompat.getColor(
            this,
            R.color.white
        )
        AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(true)
        AXEmojiManager.getEmojiViewTheme().backgroundColor = ContextCompat.getColor(
            this,
            R.color.white
        )

        AXEmojiManager.getStickerViewTheme().selectionColor = ContextCompat.getColor(
            this,
            R.color.main_bg
        )
        AXEmojiManager.getStickerViewTheme().selectedColor = ContextCompat.getColor(
            this,
            R.color.main_bg
        )
        AXEmojiManager.getStickerViewTheme().backgroundColor = ContextCompat.getColor(
            this,
            R.color.white
        )
        AXEmojiManager.getStickerViewTheme().categoryColor = ContextCompat.getColor(
            this,
            R.color.white
        )
        AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(true)
    }

    private fun createNotificationChannels() {
        val audioAttributesMessage: AudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val alarmSoundMessage: Uri = Uri.parse(
            "android.resource://"
                    + packageName + "/" + R.raw.aloline_notification
        )
        val alarmSoundCall = Uri.parse(
            "android.resource://"
                    + packageName + "/" + R.raw.call_ringtone
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Tien trình tải
            channelUpload = NotificationChannel(
                UPLOAD_CHANNEL,
                "Kênh tiến trình tải",
                NotificationManager.IMPORTANCE_LOW
            )
            channelUpload.description =
                "Kênh thông báo tiến trình tải hình ảnh, video lên ứng dụng khách hàng Alo Line."
            channelUpload.setSound(null, null)

            //message
            channelMessage = NotificationChannel(
                MESSAGE_CHANNEL,
                MESSAGE_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            channelMessage.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channelMessage.description =
                "Kênh hiển thị thông báo tin nhắn trên hệ thống ứng dụng Alo Line."
            channelMessage.enableLights(true)
            channelMessage.enableVibration(true)
            channelMessage.setSound(alarmSoundMessage, audioAttributesMessage)

            //calling
            channelMessageCalling = NotificationChannel(
                MESSAGE_CALLING_CHANNEL,
                MESSAGE_CALLING_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channelMessageCalling.description =
                "Kênh thông báo cuộc gọi thoại và cuộc gọi video trên hệ thống ứng dụng Alo Line."
            channelMessageCalling.setSound(null, null)

            //call outside
            channelMessageCallOutside = NotificationChannel(
                MESSAGE_CALL_CHANNEL_OUTSIDE,
                MESSAGE_CALL_CHANNEL_OUTSIDE,
                NotificationManager.IMPORTANCE_HIGH
            )
            channelMessageCallOutside.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channelMessageCallOutside.description =
                "Kênh thông báo cuộc gọi thoại và cuộc gọi video trên hệ thống ứng dụng Alo Line."
            channelMessageCallOutside.enableLights(true)
            channelMessageCallOutside.enableVibration(true)
            channelMessageCallOutside.vibrationPattern = longArrayOf(500, 500)
            channelMessageCallOutside.setSound(alarmSoundCall, audioAttributesMessage)

            //call inside
            channelMessageCallInside = NotificationChannel(
                MESSAGE_CALL_CHANNEL_INSIDE,
                MESSAGE_CALL_CHANNEL_INSIDE,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channelMessageCallInside.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channelMessageCallInside.description =
                "Kênh thông báo cuộc gọi thoại và cuộc gọi video trên hệ thống ứng dụng Alo Line."
            channelMessageCallInside.enableLights(true)
            channelMessageCallInside.enableVibration(true)
            channelMessageCallInside.vibrationPattern = longArrayOf(500, 500)
            channelMessageCallInside.setSound(alarmSoundCall, audioAttributesMessage)

            // System
            channelNotificationSystem = NotificationChannel(
                SYSTEM_NOTIFICATION,
                "Kênh hệ thống",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelNotificationSystem.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channelNotificationSystem.description = "Kênh thông báo hệ thống ứng dụng Alo Line."
            channelNotificationSystem.enableLights(true)
            channelNotificationSystem.enableVibration(true)
            channelNotificationSystem.vibrationPattern = longArrayOf(500, 500)
            channelNotificationSystem.setSound(alarmSoundMessage, audioAttributesMessage)


            notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channelNotificationSystem)
            notificationManager?.createNotificationChannel(channelMessageCalling)
            notificationManager?.createNotificationChannel(channelMessageCallOutside)
            notificationManager?.createNotificationChannel(channelMessageCallInside)
            notificationManager?.createNotificationChannel(channelMessage)
            notificationManager?.createNotificationChannel(channelUpload)
        }
    }

    fun getNotificationManager(): NotificationManager? {
        return notificationManager
    }

    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getNotificationConfig(
        activity: Activity?,
        uploadId: String?,
        @StringRes title: Int
    ): UploadNotificationConfig {
        val clickIntent = PendingIntent.getActivity(
            activity,
            1,
            Intent(activity, BaseBindingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val autoClear = true
        val clearOnAction = true
        val ringToneEnabled = true
        val noActions = ArrayList<UploadNotificationAction>(1)
        val cancelAction = activity?.getCancelUploadIntent(uploadId!!)?.let {
            UploadNotificationAction(
                R.drawable.ic_cancelled,
                applicationContext().getString(R.string.cancel_upload),
                it
            )
        }
        val progressActions = ArrayList<UploadNotificationAction>(1)
        if (cancelAction != null) {
            progressActions.add(cancelAction)
        }
        val progress = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.uploading),
            R.drawable.ic_upload,
            Color.BLUE,
            null,
            clickIntent,
            progressActions,
            clearOnAction,
            autoClear
        )
        val success = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.upload_success),
            R.drawable.ic_upload_success,
            Color.GREEN,
            null,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val error = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.upload_error),
            R.drawable.ic_upload_error,
            Color.RED,
            null,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val cancelled = UploadNotificationStatusConfig(
            applicationContext().getString(title) ?: "",
            applicationContext().getString(R.string.upload_cancelled),
            R.drawable.ic_cancelled,
            Color.YELLOW,
            null,
            clickIntent,
            noActions,
            clearOnAction
        )
        return UploadNotificationConfig(
            UPLOAD_CHANNEL,
            ringToneEnabled,
            progress,
            success,
            error,
            cancelled
        )
    }

    fun getNotificationConfig(
        context: Context?,
        uploadId: String?,
        @StringRes title: Int
    ): UploadNotificationConfig {
        val clickIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, BaseBindingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val autoClear = true
        val clearOnAction = true
        val ringToneEnabled = true
        val noActions = ArrayList<UploadNotificationAction>(1)
        val cancelAction = context?.getCancelUploadIntent(uploadId!!)?.let {
            UploadNotificationAction(
                R.drawable.ic_cancelled,
                applicationContext().getString(R.string.cancel_upload),
                it
            )
        }
        val progressActions = ArrayList<UploadNotificationAction>(1)
        if (cancelAction != null) {
            progressActions.add(cancelAction)
        }
        val progress = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.uploading),
            R.drawable.ic_upload,
            Color.BLUE,
            null,
            clickIntent,
            progressActions,
            clearOnAction,
            autoClear
        )
        val success = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.upload_success),
            R.drawable.ic_upload_success,
            Color.GREEN,
            null,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val error = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.upload_error),
            R.drawable.ic_upload_error,
            Color.RED,
            null,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val cancelled = UploadNotificationStatusConfig(
            applicationContext().getString(title),
            applicationContext().getString(R.string.upload_cancelled),
            R.drawable.ic_cancelled,
            Color.YELLOW,
            null,
            clickIntent,
            noActions,
            clearOnAction
        )
        return UploadNotificationConfig(
            UPLOAD_CHANNEL,
            ringToneEnabled,
            progress,
            success,
            error,
            cancelled
        )
    }

    fun getFriendDao(): FriendDao? {
        return friendDao
    }


    fun getGroupDao(): GroupDao? {
        return groupDao
    }

    fun getMessageDao(): MessageDao? {
        return messageDao
    }

    fun getContactDao(): ContactDataDao? {
        return contactDao
    }

    fun getQrCodeDao(): QrCodeDao? {
        return qrcodeDao
    }

    fun getAppDatabase(): AppDatabase? {
        return appDatabase
    }

}
