package vn.techres.line.call

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.socket.emitter.Emitter
import jp.wasabeef.glide.transformations.BlurTransformation
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.RendererCommon.ScalingType
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks
import timber.log.Timber
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.TechResApplication.Companion.mSocket
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.call.helper.*
import vn.techres.line.call.service.MiniCallService
import vn.techres.line.data.model.eventbus.EventBusPutChronometer
import vn.techres.line.data.model.eventbus.EventBusPutIsChange
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityCallBinding
import vn.techres.line.databinding.DialogChangeMessageTypeCallBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.rtc_custom.models.EventBusCallAction
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.emitSocket
import vn.techres.line.videocall.SocketCallEmitDataEnum
import vn.techres.line.videocall.SocketCallOnDataEnum
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class CallActivity : BaseBindingActivity<ActivityCallBinding>(), AppRTCClient.SignalingEvents,
    PeerConnectionClient.PeerConnectionEvents, PermissionCallbacks,
    RationaleCallbacks  {

    private var user = User()
    private var configNodeJs = ConfigNodeJs()

    companion object {
        private const val CAPTURE_PERMISSION_REQUEST_CODE = 1

        // List of mandatory application permissions.
        private val MANDATORY_PERMISSIONS = arrayOf(
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"
        )

        // Peer connection statistics callback period in ms.
        private const val STAT_CALLBACK_PERIOD = 1000

        var messageType = "call_audio"
        var memberCreateID = 0
        var groupID = ""
        var keyRoom = ""
        var avatarSender = ""
        var nameSender = ""
        var dataOffer = ""
        var typeOption = 0 //1 là tạo connect 2 là joincall

        var isChange = false

        private val mediaProjectionPermissionResultData: Intent? = null
        private const val mediaProjectionPermissionResultCode = 0

        private val BLUETOOTH_PERMISSIONS_SCAN = Manifest.permission.BLUETOOTH_SCAN
        private val BLUETOOTH_PERMISSIONS_CONNECT = Manifest.permission.BLUETOOTH_CONNECT
    }

    // List of mandatory application permissions.
    private val MANDATORY_PERMISSIONS = arrayOf(
        "android.permission.MODIFY_AUDIO_SETTINGS",
        "android.permission.RECORD_AUDIO", "android.permission.INTERNET"
    )

    // Peer connection statistics callback period in ms.
    private val STAT_CALLBACK_PERIOD = 1000

    private var elapsedRealtime: Long = 0

    private var onStopCalled = true


    //set isSpeaker
    private var isSpeaker = false
    private var isSplitScreen = false
    private var isCamera = true
    private var isMic = true
    private var isCameraOther = true
    private var isMicOther = true
    private var isSwappedFeeds = false
    private var isChange = false

    private var statusCall: StatusCall? = null

    private var onCallConnect: OnCallConnect? = null

    private var dialogChangeMessageTypeCallBinding: DialogChangeMessageTypeCallBinding? = null

    private var dialogChangeMessageType: Dialog? = null

    // The lock will handle himself the behaviour we want
    private var lock: PowerManager.WakeLock? = null

    private var mIsSensorUpdateEnabled = false


    fun setStatusCall(statusCall: StatusCall?) {
        this.statusCall = statusCall
    }

    fun setOnCallConnect(onCallConnect: OnCallConnect?) {
        this.onCallConnect = onCallConnect
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>) {}

    override fun onRationaleAccepted(requestCode: Int) {}

    override fun onRationaleDenied(requestCode: Int) {}

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Timber.d("Dropping frame in proxy because target is null.")
                return
            }
            target!!.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

    private val remoteProxyRenderer = ProxyVideoSink()
    private val localProxyVideoSink = ProxyVideoSink()
    private var peerConnectionClient: PeerConnectionClient? = null
    private var appRtcClient: AppRTCClient? = null
    private var signalingParameters: AppRTCClient.SignalingParameters? = null
    private var audioManager: AppRTCAudioManager? = null

    private var videoFileRenderer: VideoFileRenderer? = null
    private val remoteSinks: ArrayList<VideoSink> = ArrayList()
    private var activityRunning = false
    private var roomConnectionParameters: AppRTCClient.RoomConnectionParameters? = null
    private var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters? = null
    private var connected = false
    private var isError = false
    private var callStartedTimeMs: Long = 0
    private val screencaptureEnabled = false
    private var mediaProjectionPermissionResultData: Intent? = null
    private var mediaProjectionPermissionResultCode = 0

    private var cpuMonitor: CpuMonitor? = null

    //Set custom
    var loopback = false //EXTRA_LOOPBACK pref_tracing_key

    override val bindingInflater: (LayoutInflater) -> ActivityCallBinding
        get() = ActivityCallBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarOrScreenStatus(this)
        // Power manager will provide the lock
        // Power manager will provide the lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        lock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "simplewakelock:wakelocktag"
        )

        //Setup data
        messageType = intent.getStringExtra(TechresEnum.EXTRA_MESSAGE_TYPE.toString()).toString()
        memberCreateID = intent.getIntExtra(TechresEnum.EXTRA_MEMBER_CREATE.toString(), 0)
        groupID = intent.getStringExtra(TechresEnum.EXTRA_ID_GROUP.toString()).toString()
        keyRoom = intent.getStringExtra(TechresEnum.EXTRA_KEY_ROOM.toString()).toString()
        avatarSender = intent.getStringExtra(TechresEnum.EXTRA_AVATAR_PERSONAL.toString()).toString()
        nameSender = intent.getStringExtra(TechresEnum.EXTRA_NAME_PERSONAL.toString()).toString()
        typeOption = intent.getIntExtra(TechresEnum.EXTRA_TYPE_OPTION.toString(), 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!EasyPermissions.hasPermissions(this, BLUETOOTH_PERMISSIONS_SCAN)) {
                EasyPermissions.requestPermissions(
                    this,
                    "Không có quyền truy cập.",
                    400,
                    BLUETOOTH_PERMISSIONS_SCAN
                )
            }
            if (!EasyPermissions.hasPermissions(this, BLUETOOTH_PERMISSIONS_CONNECT)) {
                EasyPermissions.requestPermissions(
                    this,
                    "Không có quyền truy cập.",
                    400,
                    BLUETOOTH_PERMISSIONS_CONNECT
                )
            }
        }

        if (typeOption == 1) {
            try {
                val obj = JSONObject()
                obj.put("member_id", user.id)
                obj.put("group_id", groupID)
                obj.put("key_room", keyRoom)
                obj.put("message_type", messageType)
                obj.put("call_member_create", memberCreateID)
                emitSocket(SocketCallEmitDataEnum.CALL_CONNECT(), obj)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            try {
                val objAnswer = JSONObject()
                objAnswer.put("member_id", user.id)
                objAnswer.put("group_id", groupID)
                objAnswer.put("key_room", keyRoom)
                objAnswer.put("message_type", messageType)
                objAnswer.put("call_member_create", memberCreateID)
                emitSocket(SocketCallEmitDataEnum.CALL_ACCEPT(), objAnswer)
                TechResApplication.applicationContext().getNotificationManager()!!.cancel(150899)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        connected = false
        signalingParameters = null

        // Controls

        // Controls
        val callFragment = CallFragment()

        remoteSinks.add(remoteProxyRenderer)

        val intent = intent
        val eglBase = EglBase.create()

        // Create video renderers.

        // Create video renderers.
        binding.pipVideoView.init(eglBase.eglBaseContext, null)
        binding.pipVideoView2.init(eglBase.eglBaseContext, null)
        binding.pipVideoView.setScalingType(ScalingType.SCALE_ASPECT_FIT)
        binding.pipVideoView2.setScalingType(ScalingType.SCALE_ASPECT_FIT)

        // When saveRemoteVideoToFile is set we save the video from the remote to a file.

        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        binding.fullscreenVideoView.init(eglBase.eglBaseContext, null)
        binding.fullscreenVideoView2.init(eglBase.eglBaseContext, null)
        binding.fullscreenVideoView.setScalingType(ScalingType.SCALE_ASPECT_FILL)
        binding.fullscreenVideoView2.setScalingType(ScalingType.SCALE_ASPECT_FILL)

        binding.pipVideoView.setZOrderMediaOverlay(true)
        binding.pipVideoView2.setZOrderMediaOverlay(true)
        binding.pipVideoView.setEnableHardwareScaler(true /* enabled */)
        binding.pipVideoView2.setEnableHardwareScaler(true /* enabled */)
        binding.fullscreenVideoView.setEnableHardwareScaler(false /* enabled */)
        binding.fullscreenVideoView2.setEnableHardwareScaler(false /* enabled */)

        // Start with local feed in fullscreen and swap it to the pip when the call is connected.

        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true)

        // Check for mandatory permissions.

        // Check for mandatory permissions.
        for (permission in MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission $permission is not granted")
                setResult(RESULT_CANCELED)
                finish()
                return
            }
        }

        // Get video resolution from settings.

        // Get video resolution from settings.
        var videoWidth = 0
        var videoHeight = 0

        val resolution = "1920 x 1080"
        val dimensions = resolution.split("[ x]+".toRegex()).toTypedArray()
        if (dimensions.size == 2) {
            try {
                videoWidth = dimensions[0].toInt()
                videoHeight = dimensions[1].toInt()
            } catch (e: NumberFormatException) {
                videoWidth = 0
                videoHeight = 0
                Timber.e("Wrong video resolution setting: %s", resolution)
            }
        }

        // If capturing format is not specified for screencapture, use screen resolution.

        // If capturing format is not specified for screencapture, use screen resolution.
        var dataChannelParameters: PeerConnectionClient.DataChannelParameters? = null

        // Get datachannel options

        // Get datachannel options
        val dataChannelEnabled =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_enable_datachannel_default))

        val ordered = java.lang.Boolean.parseBoolean(getString(R.string.pref_ordered_default))

        val maxRetrMs = getString(R.string.pref_max_retransmit_time_ms_default).toInt()

        val maxRetr = getString(R.string.pref_max_retransmits_default).toInt()

        val protocol = ""

        val negotiated = java.lang.Boolean.parseBoolean(getString(R.string.pref_negotiated_default))

        val id = getString(R.string.pref_data_id_default).toInt()

        if (dataChannelEnabled) {
            dataChannelParameters = PeerConnectionClient.DataChannelParameters(
                ordered,
                maxRetrMs,
                maxRetr, protocol,
                negotiated, id
            )
        }

        // Get camera fps from settings.

        // Get camera fps from settings.
        var cameraFps = 0
        val fps = getString(R.string.pref_fps_default)
        val fpsValues = fps.split("[ x]+".toRegex()).toTypedArray()
        if (fpsValues.size == 2) {
            try {
                cameraFps = fpsValues[0].toInt()
            } catch (e: NumberFormatException) {
                Timber.e("Wrong camera fps setting: %s", fps)
            }
        }

        // Get video and audio start bitrate.

        // Get video and audio start bitrate.
        val videoStartBitrate = 0

        // Get default codecs.

        // Get default codecs.
        val videoCodec = getString(R.string.pref_videocodec_default)

        // Check HW codec flag.

        // Check HW codec flag.
        val hwCodec = java.lang.Boolean.parseBoolean(getString(R.string.pref_hwcodec_default))

        // Check FlexFEC.

        // Check FlexFEC.
        val flexfecEnabled =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_flexfec_default))

        val audioStartBitrate = 0

        val audioCodec = getString(R.string.pref_audiocodec_default)

        // Check Disable Audio Processing flag.

        // Check Disable Audio Processing flag.
        val noAudioProcessing =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_noaudioprocessing_default))

        val aecDump = java.lang.Boolean.parseBoolean(getString(R.string.pref_aecdump_default))

        val saveInputAudioToFile =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_enable_save_input_audio_to_file_default))

        // Check OpenSL ES enabled flag.

        // Check OpenSL ES enabled flag.
        val useOpenSLES = java.lang.Boolean.parseBoolean(getString(R.string.pref_opensles_default))

        // Check Disable built-in AEC flag.

        // Check Disable built-in AEC flag.
        val disableBuiltInAEC =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_disable_built_in_aec_default))

        // Check Disable built-in AGC flag.

        // Check Disable built-in AGC flag.
        val disableBuiltInAGC =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_disable_built_in_agc_default))

        // Check Disable built-in NS flag.

        // Check Disable built-in NS flag.
        val disableBuiltInNS =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_disable_built_in_ns_default))

        // Check Disable gain control

        // Check Disable gain control
        val disableWebRtcAGCAndHPF =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_disable_webrtc_agc_default))

        val tracing = java.lang.Boolean.parseBoolean(getString(R.string.pref_tracing_default))

        // Check Enable RtcEventLog.

        // Check Enable RtcEventLog.
        val rtcEventLogEnabled =
            java.lang.Boolean.parseBoolean(getString(R.string.pref_enable_rtceventlog_default))

        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters(
            true, loopback,
            tracing, videoWidth, videoHeight, cameraFps,
            videoStartBitrate, videoCodec,
            hwCodec,
            flexfecEnabled,
            audioStartBitrate, audioCodec,
            noAudioProcessing,
            aecDump,
            saveInputAudioToFile,
            useOpenSLES,
            disableBuiltInAEC,
            disableBuiltInAGC,
            disableBuiltInNS,
            disableWebRtcAGCAndHPF,
            rtcEventLogEnabled, dataChannelParameters
        )

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        appRtcClient = KurentoRTCClient(this)

        // Create connection parameters.

        // Create connection parameters.
        val urlParameters = intent.getStringExtra(TechresEnum.EXTRA_URLPARAMETERS.toString())
        roomConnectionParameters =
            AppRTCClient.RoomConnectionParameters(
                "ass",
                keyRoom,
                loopback,
                urlParameters
            )

        // Create CPU monitor

        // Create CPU monitor
        if (CpuMonitor.isSupported()) {
            cpuMonitor = CpuMonitor(this)
        }

        // Send intent arguments to fragments.

        // Send intent arguments to fragments.
        callFragment.arguments = intent.extras

        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.call_fragment_container, callFragment)
        ft.commit()

        // Create peer connection client.

        // Create peer connection client.
        peerConnectionClient = PeerConnectionClient(
            applicationContext, eglBase, peerConnectionParameters, this
        )
        val options = PeerConnectionFactory.Options()
        if (loopback) {
            options.networkIgnoreMask = 0
        }

        peerConnectionClient!!.createPeerConnectionFactory(options)
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_REJECT(), onCallReject)
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_ACCEPT(), onCallAccept)
        mSocket?.on(SocketCallOnDataEnum.RES_CONNECT_TO_CAMERA(), onConnectToCamera)
        mSocket?.on(SocketCallOnDataEnum.RES_ICE_CANDIDATE(), onIceCandidate)
        mSocket?.on(SocketCallOnDataEnum.RES_MAKE_ANSWER(), onMakeAnswer)
        mSocket?.on(SocketCallOnDataEnum.RES_MAKE_OFFER(), onMakeOffer)
        mSocket?.on(SocketCallOnDataEnum.RES_TURN_ON_OFF_CAMERA(), onTurnOnOffCamera)
        mSocket?.on(SocketCallOnDataEnum.RES_TURN_ON_OFF_MIC(), onTurnOnOffMic)

        binding.fullscreenVideoView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom
            ) {
                // The playerView’s bounds changed, update the source hint rect to
                // reflect its new bounds.
                val sourceRectHint = Rect()
                binding.fullscreenVideoView.getGlobalVisibleRect(sourceRectHint)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setPictureInPictureParams(
                        PictureInPictureParams.Builder()
                            .setSourceRectHint(sourceRectHint)
                            .setAspectRatio(ChatUtils.getPipRatio())
                            .build()
                    )
                }
            }
        }

        if (messageType == "call_video" && typeOption == 1) {
            startCall()
        }

        if (messageType == "call_audio") {
            enableBehaviour()
//            Objects.requireNonNull(peerConnectionClient)!!.setVideoEnabledFontCamera(false)
        } else disableBehaviour()

        showNotificationCalling()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showNotificationCalling(){
        TechResApplication.applicationContext().getNotificationManager()!!.cancel(memberCreateID)

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val thread = Thread {
            try {
                val icon = if (messageType == "call_video"){
                    R.drawable.ic_video_call_incoming
                }else{
                    R.drawable.ic_call_incoming
                }

                val notification =
                    NotificationCompat.Builder(this, TechResApplication.MESSAGE_CALLING_CHANNEL)
                        .setLargeIcon(
                            Utils.getBitmap(
                                String.format(
                                    "%s%s",
                                    configNodeJs.api_ads,
                                    avatarSender
                                ), this
                            )
                        )
                        .setContentTitle(nameSender)
                        .setContentText("Đang gọi...")
                        .setSmallIcon(icon)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_MAX)
                        .build()

                TechResApplication.applicationContext().getNotificationManager()!!
                    .notify(150899, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()

    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    private fun enableBehaviour() {
        // Acquire the lock if it was not already acquired
        if (!lock!!.isHeld) lock!!.acquire(10*60*1000L /*10 minutes*/)
    }

    private fun disableBehaviour() {
        // Release the lock if it was not already released
        if (lock!!.isHeld) lock!!.release()
    }

    private fun startCall() {
        if (appRtcClient == null) {
            Timber.e("AppRTC client is not allocated for a call.")
            return
        }
        callStartedTimeMs = System.currentTimeMillis()

        // Start room connection.
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters!!.roomUrl))
        appRtcClient!!.connectToRoom(roomConnectionParameters)

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(applicationContext)
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Timber.d("Starting the audio manager...")
        // This method will be called each time the number of available audio
        // devices has changed.
        if (messageType == "call_video") {
            audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
            isSpeaker = true
            isCamera = true
        } else {
            audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
            isSpeaker = false
            isCamera = false
        }

        audioManager!!.start { device: AppRTCAudioManager.AudioDevice?, availableDevices: Set<AppRTCAudioManager.AudioDevice?>? ->
            onAudioManagerDevicesChanged(
                device!!, availableDevices!! as Set<AppRTCAudioManager.AudioDevice>
            )
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        activityRunning = true
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient!!.startVideoSource()
        }
        if (cpuMonitor != null) {
            cpuMonitor!!.resume()
        }
        mIsSensorUpdateEnabled = true
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        activityRunning = false
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient!!.stopVideoSource()
        }
        if (cpuMonitor != null) {
            cpuMonitor!!.pause()
        }
        onStopCalled = true
    }

    override fun onPause() {
        super.onPause()
        disableBehaviour()
    }

    override fun onDestroy() {
        disconnect()
        activityRunning = false
        unRegistrySocket()
        super.onDestroy()
        TechResApplication.applicationContext().getNotificationManager()!!.cancel(150899)
    }

    @Subscribe
    fun onCallAction(event: EventBusCallAction) {
        val statusCamera: Int
        when (event.type_action) {
            "CallHangUp" -> disconnect()
            "CameraSwitch" -> if (peerConnectionClient != null) {
                peerConnectionClient!!.switchCamera()
            }
            "ToggleMic" -> if (peerConnectionClient != null) {
                isMic = !isMic
                val statusMic: Int = if (isMic) {
                    1
                } else 0
                showOffCameraOrMic()
                peerConnectionClient!!.setAudioEnabled(isMic)
                val socketChangeCameraOrMicRequest : SocketChangeCameraOrMicRequest? = null
                socketChangeCameraOrMicRequest!!.group_id = groupID
                socketChangeCameraOrMicRequest.status = statusMic
                emitSocket(SocketCallEmitDataEnum.TURN_ON_OFF_MIC(), socketChangeCameraOrMicRequest)
            }
            "OpenSpeaker" -> {
                assert(audioManager != null)
                isSpeaker = if (isSpeaker) {
                    audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
                    false
                } else {
                    audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
                    true
                }
                onAudioManagerDevicesChanged(
                    audioManager!!.selectedAudioDevice,
                    audioManager!!.audioDevices
                )
            }
            "ChangeVideoCall" -> {
                dialogChangeMessageType = Dialog(this, R.style.DialogAnimation)
                dialogChangeMessageTypeCallBinding = DialogChangeMessageTypeCallBinding.inflate(
                    layoutInflater
                )
                dialogChangeMessageType!!.setContentView(dialogChangeMessageTypeCallBinding!!.root)
                dialogChangeMessageType!!.setCancelable(true)
                dialogChangeMessageType!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogChangeMessageType!!.window!!
                    .setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                dialogChangeMessageType!!.window!!.setGravity(Gravity.BOTTOM)
                dialogChangeMessageType!!.show()
                dialogChangeMessageTypeCallBinding!!.btnRemove.setOnClickListener { dialogChangeMessageType!!.cancel() }
                dialogChangeMessageTypeCallBinding!!.btnChange.setOnClickListener {
                    assert(audioManager != null)
                    audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
                    isSpeaker = true
                    onAudioManagerDevicesChanged(
                        audioManager!!.selectedAudioDevice,
                        audioManager!!.audioDevices
                    )
                    dialogChangeMessageType!!.cancel()
                    statusCall!!.onChangeCallMe()
                    isCameraOther = false
                    isCamera = true
                    showOffCameraOrMic()
                    setTurnOnAndOffCameraOther()
                    statusCall!!.onOtherOffMic(isMicOther)
                    statusCall!!.onOtherOffCamera(isCameraOther)
                    emitSocket(
                        SocketCallEmitDataEnum.CHANGE_CALL(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            "call_video",
                            memberCreateID,
                            ""
                        )
                    )
                    emitSocket(
                        SocketCallEmitDataEnum.CONNECT_TO_CAMERA(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            "call_video",
                            memberCreateID,
                            ""
                        )
                    )
                    disableBehaviour()
                }
            }
            "MiniMode" -> onBackPressed()
            "SplitScreen" -> {
                isSplitScreen = !isSplitScreen
                showOffCameraOrMic()
                setTurnOnAndOffCameraOther()
                setSwappedFeeds(isSwappedFeeds)
            }
            "OnOffCamera" -> {
                isCamera = !isCamera
                statusCamera = if (isCamera) {
                    1
                } else 0
                showOffCameraOrMic()
                val socketChangeCameraOrMicRequest = SocketChangeCameraOrMicRequest()
                socketChangeCameraOrMicRequest.member_id = user.id
                socketChangeCameraOrMicRequest.group_id = groupID
                socketChangeCameraOrMicRequest.status = statusCamera
                Objects.requireNonNull(peerConnectionClient)!!.setVideoEnabledFontCamera(isCamera)
                emitSocket(SocketCallEmitDataEnum.TURN_ON_OFF_CAMERA(), socketChangeCameraOrMicRequest)
            }
            "AcceptToCamera" -> {
                audioManager!!.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
                isSpeaker = true
                isCamera = true
                CallActivity.isChange = false
                statusCamera = 1
                binding.cvPipRenderer.visibility = View.VISIBLE
                showOffCameraOrMic()
                val socketChangeCameraOrMicRequest = SocketChangeCameraOrMicRequest()
                socketChangeCameraOrMicRequest.member_id = user.id
                socketChangeCameraOrMicRequest.group_id = groupID
                socketChangeCameraOrMicRequest.status = statusCamera
                Objects.requireNonNull(peerConnectionClient)!!.setVideoEnabledFontCamera(isCamera)
                emitSocket(SocketCallEmitDataEnum.TURN_ON_OFF_CAMERA(), socketChangeCameraOrMicRequest)
            }
            "RejectToCamera" -> {
                isCamera = false
                CallActivity.isChange = false
                binding.cvPipRenderer.visibility = View.VISIBLE
                showOffCameraOrMic()
                Objects.requireNonNull(peerConnectionClient)!!.setVideoEnabledFontCamera(isCamera)
            }
            "ScreenShot" -> if (isSplitScreen) {
                binding.fullscreenVideoView2.addFrameListener({ bitmap ->
                    runOnUiThread {
                        statusCall!!.onScreenShot(
                            bitmap
                        )
                    }
                }, 1f)
            } else {
                binding.fullscreenVideoView.addFrameListener({ bitmap ->
                    runOnUiThread {
                        statusCall!!.onScreenShot(
                            bitmap
                        )
                    }
                }, 1f)
            }
            else -> {
            }
        }
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    @SuppressLint("BinaryOperationInTimber")
    private fun onAudioManagerDevicesChanged(
        device: AppRTCAudioManager.AudioDevice,
        availableDevices: Set<AppRTCAudioManager.AudioDevice>
    ) {
        Timber.d("onAudioManagerDevicesChanged: $availableDevices, selected: $device")
    }

    private val onCallReject =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), false)
                    statusCall!!.onCallReject(args[0].toString())
                    WriteLog.d("onCallReject", args[0].toString())
                }
            }.start()
        }

    private val onCallAccept =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onCallAccept", args[0].toString())
                    if (screencaptureEnabled) {
                        startScreenCapture()
                    } else {
                        startCall()
                    }
                }
            }.start()
        }

    private val onConnectToCamera =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onConnectToCamera", args[0].toString())
                    disableBehaviour()
                    messageType = "call_video"
                    isCamera = false
                    isChange = true
                    statusCall!!.onChangeCallOther()
                    showOffCameraOrMic()
                    setTurnOnAndOffCameraOther()
                    if (dialogChangeMessageType != null) dialogChangeMessageType!!.cancel()
                }
            }.start()
        }

    private val onIceCandidate =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onIceCandidate", args[0].toString())
                    try {
                        val json =
                            JSONObject(args[0].toString())
                        val stringCandidate = json.getString("candidate")
                        val jsonCandidate =
                            JSONObject(stringCandidate)
                        val candidate = IceCandidate(
                            jsonCandidate.getString("sdpMid"),
                            jsonCandidate.getString("sdpMLineIndex").toInt(),
                            jsonCandidate.getString("candidate")
                        )
                        runOnUiThread {
                            if (peerConnectionClient == null) {
                                Timber.e("Received ICE candidate for a non-initialized peer connection.")
                                return@runOnUiThread
                            }
                            peerConnectionClient!!.addRemoteIceCandidate(candidate)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    private val onMakeOffer =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onMakeOffer", args[0].toString())
                    try {
                        val json =
                            JSONObject(args[0].toString())
                        dataOffer = json.getString("offer")
                        if (screencaptureEnabled) {
                            startScreenCapture()
                        } else {
                            startCall()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    private val onMakeAnswer =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onMakeAnswer", args[0].toString())
                    try {
                        val json =
                            JSONObject(args[0].toString())
                        val answer = json.getString("answer")
                        Timber.e("answer %s", answer)
                        if (appRtcClient != null) {
                            PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), true)
                            statusCall!!.onCallAccept(args[0].toString())
                            appRtcClient!!.sendAnswer(answer)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    private val onTurnOnOffCamera =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onTurnOnOffCamera", args[0].toString())
                    try {
                        val json =
                            JSONObject(args[0].toString())
                        val status = json.getInt("status")
                        isCameraOther = status == 1
                        statusCall!!.onOtherOffCamera(isCameraOther)
                        setTurnOnAndOffCameraOther()
                        showOffCameraOrMic()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    private val onTurnOnOffMic =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                runOnUiThread {
                    WriteLog.d("onTurnOnOffMic", args[0].toString())
                    try {
                        val json =
                            JSONObject(args[0].toString())
                        val status = json.getInt("status")
                        isMicOther = status == 1
                        statusCall!!.onOtherOffMic(isMicOther)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }

    override fun onConnectedToRoom(params: AppRTCClient.SignalingParameters?) {
        val delta = System.currentTimeMillis() - callStartedTimeMs

        signalingParameters = params
        logAndToast("Creating peer connection, delay=" + delta + "ms")
        var videoCapturer: VideoCapturer? = null
        assert(peerConnectionParameters != null)
        if (peerConnectionParameters!!.videoCallEnabled) {
            videoCapturer = createVideoCapturer()
        }
        assert(peerConnectionClient != null)
        peerConnectionClient!!.createPeerConnection(
            localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters
        )

        assert(signalingParameters != null)
        if (typeOption == 1) {
            logAndToast("Creating OFFER...")
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient!!.createOffer()
        } else {
            peerConnectionClient!!.setRemoteDescription(
                SessionDescription(
                    SessionDescription.Type.OFFER,
                    dataOffer
                )
            )
            logAndToast("Creating ANSWER...")
            // Create answer. Answer SDP will be sent to offering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient!!.createAnswer()
            if (params!!.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (iceCandidate in params.iceCandidates) {
                    peerConnectionClient!!.addRemoteIceCandidate(iceCandidate)
                }
            }
        }
    }

    override fun onRemoteDescription(sdp: SessionDescription?) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            if (peerConnectionClient == null) {
                Timber.e("Received remote SDP for non-initilized peer connection.")
                return@runOnUiThread
            }
            logAndToast("Received remote " + sdp!!.type + ", delay=" + delta + "ms")
            peerConnectionClient!!.setRemoteDescription(sdp)
            assert(signalingParameters != null)
            if (!signalingParameters!!.initiator) {
                logAndToast("Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient!!.createAnswer()
            }
        }
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate?) {
        runOnUiThread {
            if (peerConnectionClient == null) {
                Timber.e("Received ICE candidate for a non-initialized peer connection.")
                return@runOnUiThread
            }
            peerConnectionClient!!.addRemoteIceCandidate(candidate)
        }
    }

    override fun onRemoteIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
        runOnUiThread {
            if (peerConnectionClient == null) {
                Timber.e("Received ICE candidate removals for a non-initialized peer connection.")
                return@runOnUiThread
            }
            peerConnectionClient!!.removeRemoteIceCandidates(candidates)
        }
    }

    override fun onChannelClose() {
        runOnUiThread {
            logAndToast("Remote end hung up; dropping PeerConnection")
            disconnect()
        }
    }

    override fun onChannelError(description: String?) {
        if (description != null) {
            reportError(description)
        }
    }

    override fun onLocalDescription(sdp: SessionDescription?) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            if (appRtcClient != null) {
                logAndToast("Sending " + sdp!!.type + ", delay=" + delta + "ms")
                assert(signalingParameters != null)
                if (typeOption == 1) {
                    appRtcClient!!.sendOfferSdp(
                        sdp,
                        groupID,
                        keyRoom,
                        memberCreateID,
                        messageType)
                } else {
                    appRtcClient!!.sendAnswerSdp(
                        sdp,
                        groupID,
                        keyRoom,
                        memberCreateID,
                        messageType)
                }
            }
            assert(peerConnectionParameters != null)
            if (peerConnectionParameters!!.videoMaxBitrate > 0) {
                Timber.d(
                    "Set video maximum bitrate: %s",
                    peerConnectionParameters!!.videoMaxBitrate
                )
                assert(peerConnectionClient != null)
                peerConnectionClient!!.setVideoMaxBitrate(peerConnectionParameters!!.videoMaxBitrate)
            }
        }
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        runOnUiThread {
            appRtcClient?.sendLocalIceCandidate(
                candidate,
                groupID,
                keyRoom,
                memberCreateID,
                messageType
            )
        }
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
        runOnUiThread {
            appRtcClient?.sendLocalIceCandidateRemovals(candidates)
        }
    }

    override fun onIceConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread { logAndToast("ICE connected, delay=" + delta + "ms") }
        onCallConnect!!.onIceConnected()
    }

    override fun onIceDisconnected() {
        runOnUiThread { logAndToast("ICE disconnected") }
    }

    override fun onConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            logAndToast("DTLS connected, delay=" + delta + "ms")
            connected = true
            callConnected()
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            logAndToast("DTLS disconnected")
            connected = false
            disconnect()
        }
    }

    override fun onPeerConnectionClosed() {
    }

    override fun onPeerConnectionStatsReady(reports: Array<out StatsReport>?) {
    }

    override fun onPeerConnectionError(description: String?) {
        if (description != null) {
            reportError(description)
        }
    }

    override fun onBackPressed() {
        onBackPressed()
    }

    fun showFloatingView() {
        if (!checkOverlayPermission()) {
            return
        }
        val intent = Intent(this, MiniCallService::class.java)
        intent.putExtra(TechresEnum.EXTRA_AVATAR_PERSONAL.toString(), avatarSender)
        intent.putExtra(TechresEnum.EXTRA_NAME_PERSONAL.toString(), nameSender)
        intent.putExtra(TechresEnum.EXTRA_CHORONOMETER.toString(), elapsedRealtime)
        startService(intent)
        moveTaskToBack(true)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    @Subscribe
    fun getChronometer(event: EventBusPutChronometer) {
        elapsedRealtime = event.chronometer.base
    }

    @Subscribe
    fun putIsChange(event: EventBusPutIsChange) {
        isChange = event.isChange
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun allowCamera2Support(cameraId: Int): Boolean {
        @SuppressLint("WrongConstant") val manager =
            getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val cameraIdS = manager.cameraIdList[cameraId]
            val characteristics = manager.getCameraCharacteristics(cameraIdS)
            val support = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
            if (support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) Timber.d(
                "Camera $cameraId has LEGACY Camera2 support"
            ) else if (support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) Timber.d(
                "Camera $cameraId has LIMITED Camera2 support"
            ) else if (support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) Timber.d(
                "Camera $cameraId has FULL Camera2 support"
            ) else Timber.d("Camera $cameraId has unknown Camera2 support?!")
            return support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkCamera2Support(): Boolean {
        var numberOfCameras = 0
        @SuppressLint("WrongConstant") val manager =
            getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            numberOfCameras = manager.cameraIdList.size
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: AssertionError) {
            e.printStackTrace()
        }
        if (numberOfCameras == 0) {
            Timber.d("0 cameras")
            return false
        } else {
            for (i in 0 until numberOfCameras) {
                return if (!allowCamera2Support(i)) {
                    Timber.d("camera $i doesn't have limited or full support for Camera2 API")
                    false
                } else {
                    true
                    // here you can get ids of cameras that have limited or full support for Camera2 API
                }
            }
        }
        return false
    }

    private fun showOffCameraOrMic() {
        if (isCamera && isMic) {
            if (isSplitScreen) {
                binding.rltSplitScreen1.visibility = View.GONE
                binding.rltSplitScreen2.visibility = View.VISIBLE
                binding.lnState2.visibility = View.GONE
                binding.imvOffCamera2.visibility = View.GONE
                binding.imvOffMic2.visibility = View.GONE
                binding.imgAvatarPip2.visibility = View.GONE
            } else {
                binding.rltSplitScreen1.visibility = View.VISIBLE
                binding.rltSplitScreen2.visibility = View.GONE
                binding.lnState.visibility = View.GONE
                binding.imvOffCamera.visibility = View.GONE
                binding.imvOffMic.visibility = View.GONE
                binding.imgAvatarPip.visibility = View.GONE
            }
            if (isChange) {
                binding.cvPipRenderer.visibility = View.GONE
            } else {
                binding.cvPipRenderer.visibility = View.VISIBLE
            }
        } else if (isCamera) { // May anh bat mic an
            if (isSplitScreen) {
                binding.rltSplitScreen1.visibility = View.GONE
                binding.rltSplitScreen2.visibility = View.VISIBLE
                binding.lnState2.visibility = View.VISIBLE
                binding.imvOffCamera2.visibility = View.GONE
                binding.imvOffMic2.visibility = View.VISIBLE
                binding.tvState2.text = String.format("%s đang tắt mic.", "Bạn")
                binding.imgAvatarPip2.visibility = View.GONE
            } else {
                binding.rltSplitScreen1.visibility = View.VISIBLE
                binding.rltSplitScreen2.visibility = View.GONE
                binding.lnState.visibility = View.VISIBLE
                binding.imvOffCamera.visibility = View.GONE
                binding.imvOffMic.visibility = View.VISIBLE
                binding.imgAvatarPip.visibility = View.GONE
            }
            if (isChange) {
                binding.cvPipRenderer.visibility = View.GONE
            } else {
                binding.cvPipRenderer.visibility = View.VISIBLE
            }
        } else if (isMic) { //Mic bat camera an
            if (isSplitScreen) {
                binding.rltSplitScreen1.visibility = View.GONE
                binding.rltSplitScreen2.visibility = View.VISIBLE
                binding.lnState2.visibility = View.VISIBLE
                binding.imvOffCamera2.visibility = View.VISIBLE
                binding.imvOffMic2.visibility = View.GONE
                binding.tvState2.text = String.format("%s đang tắt camera.", "Bạn")
                binding.imgAvatarPip2.visibility = View.VISIBLE
                Utils.getImageCall(binding.imgAvatarPip2, user.avatar, configNodeJs)
            } else {
                binding.rltSplitScreen1.visibility = View.VISIBLE
                binding.rltSplitScreen2.visibility = View.GONE
                binding.lnState.visibility = View.VISIBLE
                binding.imvOffCamera.visibility = View.VISIBLE
                binding.imvOffMic.visibility = View.GONE
                binding.imgAvatarPip.visibility = View.VISIBLE
                Utils.getImageCall(binding.imgAvatarPip, user.avatar, configNodeJs)
            }
            if (isChange) {
                binding.cvPipRenderer.visibility = View.GONE
            } else {
                binding.cvPipRenderer.visibility = View.VISIBLE
            }
        } else { //May anh tat mic tat
            if (isSplitScreen) {
                binding.rltSplitScreen1.visibility = View.GONE
                binding.rltSplitScreen2.visibility = View.VISIBLE
                binding.lnState2.visibility = View.VISIBLE
                binding.imvOffCamera2.visibility = View.VISIBLE
                binding.imvOffMic2.visibility = View.VISIBLE
                binding.tvState2.text = String.format("%s đang tắt camera và mic.", "Bạn")
                binding.imgAvatarPip2.visibility = View.VISIBLE
                Utils.getImageCall(binding.imgAvatarPip2, user.avatar, configNodeJs)
            } else {
                binding.rltSplitScreen1.visibility = View.VISIBLE
                binding.rltSplitScreen2.visibility = View.GONE
                binding.lnState.visibility = View.VISIBLE
                binding.imvOffCamera.visibility = View.VISIBLE
                binding.imvOffMic.visibility = View.VISIBLE
                binding.imgAvatarPip.visibility = View.VISIBLE
                Utils.getImageCall(binding.imgAvatarPip, user.avatar, configNodeJs)
            }
            if (isChange) {
                binding.cvPipRenderer.visibility = View.GONE
            } else {
                binding.cvPipRenderer.visibility = View.VISIBLE
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        SettingsCompat.setDrawOverlays(this, true)
        if (!SettingsCompat.canDrawOverlays(this)) {
            SettingsCompat.manageDrawOverlays(this)
            return false
        }
        return true
    }
    private fun createVideoCapturer(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        if (screencaptureEnabled) {
            return createScreenCapturer()
        } else if (checkCamera2Support()) {
            if (!captureToTexture()) {
                Toast.makeText(this, "Camera2 not support.", Toast.LENGTH_SHORT).show()
                return null
            }
            Logging.d(TechresEnum.TAG.toString(), "Creating capturer using camera2 API.")
            videoCapturer = createCameraCapturer(Camera2Enumerator(this))
        } else {
            Logging.d(TechresEnum.TAG.toString(), "Creating capturer using camera1 API.")
            videoCapturer = createCameraCapturer(Camera1Enumerator(captureToTexture()))
        }

        if (videoCapturer == null) {
            reportError("Failed to open camera")
            return null
        }
        return videoCapturer
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private fun disconnect() {
        activityRunning = false
        remoteProxyRenderer.setTarget(null)
        localProxyVideoSink.setTarget(null)
        if (appRtcClient != null) {
            appRtcClient!!.disconnectFromRoom()
            appRtcClient = null
        }
        binding.pipVideoView.release()
        binding.pipVideoView2.release()
        if (videoFileRenderer != null) {
            videoFileRenderer!!.release()
            videoFileRenderer = null
        }
        binding.fullscreenVideoView.release()
        binding.fullscreenVideoView2.release()
        if (peerConnectionClient != null) {
            peerConnectionClient!!.close()
            peerConnectionClient = null
        }
        if (audioManager != null) {
            audioManager!!.stop()
            audioManager = null
        }
        if (connected && !isError) {
            setResult(RESULT_OK)
        } else {
            setResult(RESULT_CANCELED)
        }
    }

    private fun reportError(description: String) {
        runOnUiThread {
            if (!isError) {
                isError = true
                disconnectWithErrorMessage(description)
            }
        }
    }

    private fun disconnectWithErrorMessage(errorMessage: String) {
        if (!activityRunning) {
            Timber.e("Critical error: %s", errorMessage)
            disconnect()
        } else {
            AlertDialog.Builder(this)
                .setTitle(getText(R.string.channel_error_title))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setNeutralButton(
                    R.string.arrge
                ) { dialog, id ->
                    dialog.cancel()
                    disconnect()
                }
                .create()
                .show()
        }
    }

    @TargetApi(21)
    private fun createScreenCapturer(): VideoCapturer? {
        if (mediaProjectionPermissionResultCode != RESULT_OK) {
            reportError("User didn't give permission to capture the screen.")
            return null
        }
        return ScreenCapturerAndroid(
            mediaProjectionPermissionResultData, object : MediaProjection.Callback() {
                override fun onStop() {
                    reportError("User revoked permission to capture the screen.")
                }
            })
    }

    // Log |msg| and Toast about it.
    private fun logAndToast(msg: String) {
        Timber.d(msg)
    }

    // Should be called from UI thread
    private fun callConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        Timber.i("Call connected: delay=" + delta + "ms")
        if (peerConnectionClient == null || isError) {
            Timber.tag(TechresEnum.TAG.toString()).w("Call is connected in closed or error state")
            return
        }
        // Enable statistics callback.
        peerConnectionClient!!.enableStatsEvents(true, STAT_CALLBACK_PERIOD)
        setSwappedFeeds(false /* isSwappedFeeds */)
    }

    private fun setSwappedFeeds(isSwappedFeeds: Boolean) {
        Logging.d(TechresEnum.TAG.toString(), "setSwappedFeeds: $isSwappedFeeds")
        this.isSwappedFeeds = isSwappedFeeds
        if (isSplitScreen) {
            localProxyVideoSink.setTarget(if (isSwappedFeeds) binding.fullscreenVideoView2 else binding.pipVideoView2)
            remoteProxyRenderer.setTarget(if (isSwappedFeeds) binding.pipVideoView2 else binding.fullscreenVideoView2)
            binding.fullscreenVideoView2.setMirror(isSwappedFeeds)
            binding.pipVideoView2.setMirror(!isSwappedFeeds)
        } else {
            localProxyVideoSink.setTarget(if (isSwappedFeeds) binding.fullscreenVideoView else binding.pipVideoView)
            remoteProxyRenderer.setTarget(if (isSwappedFeeds) binding.pipVideoView else binding.fullscreenVideoView)
            binding.fullscreenVideoView.setMirror(isSwappedFeeds)
            binding.pipVideoView.setMirror(!isSwappedFeeds)
        }
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // First, try to find front facing camera
        Logging.d(TechresEnum.TAG.toString(), "Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TechresEnum.TAG.toString(), "Creating front facing camera capturer.")
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TechresEnum.TAG.toString(), "Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TechresEnum.TAG.toString(), "Creating other camera capturer.")
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun captureToTexture(): Boolean {
        return java.lang.Boolean.parseBoolean(getString(R.string.pref_capturetotexture_default))
    }

    private fun unRegistrySocket() {
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_REJECT())
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_ACCEPT())
        mSocket?.off(SocketCallOnDataEnum.RES_CONNECT_TO_CAMERA())
        mSocket?.off(SocketCallOnDataEnum.RES_ICE_CANDIDATE())
        mSocket?.off(SocketCallOnDataEnum.RES_MAKE_ANSWER())
        mSocket?.off(SocketCallOnDataEnum.RES_MAKE_OFFER())
        mSocket?.off(SocketCallOnDataEnum.RES_TURN_ON_OFF_CAMERA())
        mSocket?.off(SocketCallOnDataEnum.RES_TURN_ON_OFF_MIC())
    }

    @SuppressLint("WrongConstant")
    @TargetApi(19)
    fun setStatusBarOrScreenStatus(activity: Activity) {
        val window = activity.window
        //全屏+锁屏+常亮显示
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    @TargetApi(21)
    private fun startScreenCapture() {
        @SuppressLint("WrongConstant") val mediaProjectionManager = application.getSystemService(
            MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            CAPTURE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE) return
        mediaProjectionPermissionResultCode = resultCode
        mediaProjectionPermissionResultData = data
        startCall()
    }

    private fun setTurnOnAndOffCameraOther() {
        if (isSplitScreen) {
            if (isCameraOther) {
                binding.imgAvatarFullscreen2.visibility = View.GONE
            } else {
                binding.imgAvatarFullscreen2.visibility = View.VISIBLE
                Glide.with(this)
                    .load(String.format("%s%s", configNodeJs.api_ads, avatarSender))
                    .placeholder(R.drawable.backgroundcallalolineblur)
                    .error(R.drawable.backgroundcallalolineblur)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                    .into(binding.imgAvatarFullscreen2)
            }
        } else {
            if (isCameraOther) {
                binding.imgAvatarFullscreen.visibility = View.GONE
            } else {
                binding.imgAvatarFullscreen.visibility = View.VISIBLE
                Glide.with(this)
                    .load(String.format("%s%s", configNodeJs.api_ads, avatarSender))
                    .placeholder(R.drawable.backgroundcallalolineblur)
                    .error(R.drawable.backgroundcallalolineblur)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                    .into(binding.imgAvatarFullscreen)
            }
        }
    }
}