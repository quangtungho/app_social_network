package vn.techres.line.call

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.*
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.emitter.Emitter
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.TechResApplication.Companion.mSocket
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.call.CallActivity.Companion.avatarSender
import vn.techres.line.call.CallActivity.Companion.groupID
import vn.techres.line.call.CallActivity.Companion.keyRoom
import vn.techres.line.call.CallActivity.Companion.memberCreateID
import vn.techres.line.call.CallActivity.Companion.messageType
import vn.techres.line.call.CallActivity.Companion.nameSender
import vn.techres.line.call.CallActivity.Companion.typeOption
import vn.techres.line.call.helper.DialogCallHandler
import vn.techres.line.call.helper.OnCallConnect
import vn.techres.line.call.helper.StatusCall
import vn.techres.line.call.service.MiniCallService
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.chat.request.personal.ChatImagePersonalRequest
import vn.techres.line.data.model.chat.response.FileNodeJsResponse
import vn.techres.line.data.model.eventbus.EventBusBusyCall
import vn.techres.line.data.model.eventbus.EventBusPutChronometer
import vn.techres.line.data.model.eventbus.EventBusPutIsChange
import vn.techres.line.data.model.notification.EventBusCloseCall
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.DialogCustomKeyboardCallBinding
import vn.techres.line.databinding.DialogScreenshotCallVideoBinding
import vn.techres.line.databinding.ItemCallVoiceBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.splitMaxName
import vn.techres.line.helper.rtc_custom.models.EvenBusFinish
import vn.techres.line.helper.rtc_custom.models.EventBusCallAction
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils.callPhotoAvatar
import vn.techres.line.helper.utils.ChatUtils.emitSocket
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.videocall.SocketCallEmitDataEnum
import vn.techres.line.videocall.SocketCallOnDataEnum
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class CallFragment : BaseBindingFragment<ItemCallVoiceBinding>(ItemCallVoiceBinding::inflate),
    OnCallConnect, StatusCall, DialogCallHandler {

    private val mainActivity: CallActivity?
        get() = activity as CallActivity?

    private var user = User()
    private var configNodeJs = ConfigNodeJs()

    private var isAnswer = false

    private var soundPool: SoundPool? = null

    private var loaded = false

    private val MAX_STREAMS = 1

    private val streamType = AudioManager.STREAM_MUSIC

    private var soundIdRingBack = 0
    private var volume = 0f

    private var idCurrentSound = 0

    //button click boolean
    private var isMic = false
    private var isSpeaker = false
    private var isChangeCamera = false
    private var isSplitScreen = false
    private var isCamera = false

    private var isCameraOther = true
    private var isMicOther = true

    private var noAnswerCountDownTimer: CountDownTimer? = null

    private var soundMedia: MediaPlayer? = null

    private var isChange = false

    private var vibrator: Vibrator? = null

    private var dialogScreenShot: Dialog? = null

    private var dialogScreenshotCallVideoBinding: DialogScreenshotCallVideoBinding? = null

    private var randomKeyFile = ""

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = mainActivity?.let { CurrentUser.getCurrentUser(it) }!!
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //set rung
        vibrator = mainActivity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        EventBus.getDefault().post(EvenBusFinish("CallVoiceFragment"))

        //Set data default
        PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), true)
        callPhotoAvatar(avatarSender, binding.content.avatar, configNodeJs.api_ads)
        binding.content.name.text = nameSender
        binding.content.rippleBackground.stopRippleAnimation()
        setupSound()
        if (activity is CallActivity) (activity as CallActivity?)!!.setStatusCall(this)
        if (messageType == "call_video") {
            binding.headerAction.chronometerVideo.visibility = View.GONE
            binding.headerAction.btnChangeVideoCall.visibility = View.GONE
            binding.bottomAction.lnTakeAShot.visibility = View.GONE
            isSpeaker = true
            binding.bottomAction.tvSpeaker.text = getString(R.string.add)
            binding.bottomAction.tvMute.text = getString(R.string.take_a_shot)
            binding.bottomAction.tvMute.isEnabled = false
        } else {
            binding.lnViewCall.setBackgroundColor(resources.getColor(R.color.main_bg, null))
            binding.headerAction.chronometerVideo.visibility = View.GONE
            binding.headerAction.btnChangeVideoCall.visibility = View.VISIBLE
            binding.bottomAction.btnSpeaker.isSelected = false
            isSpeaker = false
        }

        //Create call
        if (typeOption == 1) {
            binding.content.rippleBackground.startRippleAnimation()
            binding.content.status.text = getString(R.string.ringing)
            binding.content.status.visibility = View.VISIBLE
            binding.content.chronometer.visibility = View.GONE
            binding.headerAction.btnChangeVideoCall.visibility = View.GONE
            binding.content.status.setTextColor(Color.WHITE)
            binding.headerAction.btnCallMinimize.isEnabled = true
            binding.headerAction.btnChangeVideoCall.isEnabled = false
            binding.bottomAction.btnMute.isEnabled = false
            binding.bottomAction.btnSpeaker.isEnabled = true
            noAnswerCountDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.content.progressBarCountDown.progress = (millisUntilFinished / 600).toInt()
                }

                override fun onFinish() {
                    if (!isAnswer) {
                        PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), false)
                        playNoAnswer()
                    }
                }
            }.start()
        } else {
            mainActivity!!.runOnUiThread {
                binding.headerAction.btnCallMinimize.isEnabled = true
                binding.headerAction.btnChangeVideoCall.isEnabled = false
                binding.bottomAction.btnSpeaker.isEnabled = false
                binding.bottomAction.btnMute.isEnabled = false
                binding.content.rippleBackground.stopRippleAnimation()
                binding.content.status.text = getString(R.string.connecting)
                binding.content.status.visibility = View.VISIBLE
                binding.content.status.setTextColor(Color.WHITE)
                binding.content.chronometer.visibility = View.GONE
                binding.content.rippleBackground.stopRippleAnimation()
            }
        }
        if (activity is CallActivity) (activity as CallActivity?)!!.setOnCallConnect(this)
        if (messageType == "call_video") {
            binding.bottomAction.btnSpeaker.setBackgroundDrawable(
                resources.getDrawable(
                    R.drawable.ic_call_more_menu_selector,
                    null
                )
            )

            binding.bottomAction.btnMute.setBackgroundDrawable(
                resources.getDrawable(
                    R.drawable.ic_call_snapshot_selector,
                    null
                )
            )
            binding.bottomAction.btnSpeaker.setOnClickListener { view ->
                binding.bottomAction.btnSpeaker.isSelected = true
                val originalPos = IntArray(2)
                view.getLocationInWindow(originalPos)
                val x = originalPos[0]
                val y = (originalPos[1] - view.getHeight() * 4.3).toInt()
                showDialogMoreCall(x, y, this)
            }
        } else {
            //set mở loa
            binding.bottomAction.btnSpeaker.setOnClickListener {
                isSpeaker = if (isSpeaker) {
                    binding.bottomAction.btnSpeaker.isSelected = false
                    false
                } else {
                    binding.bottomAction.btnSpeaker.isSelected = true
                    true
                }
                EventBus.getDefault().post(EventBusCallAction("OpenSpeaker"))
            }
        }
        binding.headerAction.btnCallMinimize.setOnClickListener {
            EventBus.getDefault().post(EventBusCallAction("MiniMode"))
        }
        binding.headerAction.btnChangeVideoCall.setOnClickListener {
            EventBus.getDefault().post(EventBusCallAction("ChangeVideoCall"))
        }
        binding.bottomAction.btnDecline.setOnClickListener {
            binding.bottomAction.btnDecline.isEnabled = true
            Handler(Looper.myLooper()!!).postDelayed({
                binding.bottomAction.btnDecline.isEnabled = false
            }, 200)
            binding.content.status.visibility = View.VISIBLE
            binding.content.status.text = "Cuộc gọi kết thúc"//getString(R.string.end)
            PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), false)
            if (!isAnswer) {
                emitSocket(
                    SocketCallEmitDataEnum.CALL_CANCEL(),
                    SocketNewCallRequest(
                        groupID,
                        keyRoom,
                        messageType,
                        memberCreateID,
                        showInfo(binding.headerAction.chronometerVideo.text.toString())
                    )
                )
            } else {
                if (messageType == "call_video") {
                    emitSocket(
                        SocketCallEmitDataEnum.CALL_COMPLETE(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            messageType,
                            memberCreateID,
                            showInfo(binding.headerAction.chronometerVideo.text.toString())
                        )
                    )
                } else {
                    emitSocket(
                        SocketCallEmitDataEnum.CALL_COMPLETE(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            messageType,
                            memberCreateID,
                            showInfo(binding.content.chronometer.text.toString())
                        )
                    )
                }
            }
            playSuccess()
            soundPool!!.pause(idCurrentSound)
        }
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_CANCEL(), onCallCancel)
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_COMPLETE(), onConnectComplete)
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_NO_ANSWER(), onNoAnswer)
    }

    override fun onStop() {
        val activity: Activity? = activity
        if (activity != null) {
            if (typeOption == 1) {
                noAnswerCountDownTimer!!.cancel()
            }
            soundPool!!.release()
            if (soundMedia != null) {
                soundMedia!!.release()
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PrefUtils.getInstance().getBoolean(TechresEnum.IS_CALL.toString())) {
            if (isAnswer) {
                if (messageType == "call_video") {
                    emitSocket(
                        SocketCallEmitDataEnum.CALL_COMPLETE(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            messageType,
                            memberCreateID,
                            showInfo(binding.headerAction.chronometerVideo.text.toString())
                        )
                    )
                } else {
                    emitSocket(
                        SocketCallEmitDataEnum.CALL_COMPLETE(),
                        SocketNewCallRequest(
                            groupID,
                            keyRoom,
                            messageType,
                            memberCreateID,
                            showInfo(binding.content.chronometer.text.toString())
                        )
                    )
                }
            }
        }
        turnOffVibrator()
        unRegistrySocket()
    }

    private fun setupSound() {
        // AudioManager audio settings for adjusting the volume
        val audioManager = mainActivity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Current volumn Index of particular stream type.
        val currentVolumeIndex = audioManager.getStreamVolume(streamType).toFloat()

        // Get the maximum volume index for a particular stream type.
        val maxVolumeIndex = audioManager.getStreamMaxVolume(streamType).toFloat()
        audioManager.isSpeakerphoneOn = false

        // Volumn (0 --> 1)
        volume = currentVolumeIndex / maxVolumeIndex

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        mainActivity!!.volumeControlStream = streamType

        // For Android SDK >= 21
        val audioAttrib = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val builder = SoundPool.Builder()
        builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS)
        soundPool = builder.build()

        // Load sound when đợi trả lời
        soundIdRingBack = soundPool!!.load(mainActivity!!, R.raw.call_ringback, 1)

        // When Sound Pool load complete.
        soundPool!!.setOnLoadCompleteListener { soundPool: SoundPool?, sampleId: Int, status: Int ->
            loaded = true
            if (typeOption == 1) {
                playRingBack()
            }
        }
    }

    fun playRingBack() {
        if (loaded) {
            val leftVolumn = volume
            val rightVolumn = volume
            idCurrentSound = soundPool!!.play(soundIdRingBack, leftVolumn, rightVolumn, 1, -1, 1f)
        }
    }

    fun playSuccess() {
        val activity: Activity? = activity
        if (activity != null) {
            mainActivity!!.runOnUiThread {
                soundMedia = MediaPlayer.create(
                    mainActivity!!.baseContext,
                    R.raw.call_success
                )
                if (!soundMedia!!.isPlaying) {
                    soundMedia!!.start()
                    soundMedia!!.isLooping = false
                }
                soundMedia!!.setOnCompletionListener { mp: MediaPlayer? -> Handler().postDelayed({
                    mainActivity!!.finish()
                }, 2000) }
            }
        }
    }

    fun playBusy() {
        val activity: Activity? = activity
        if (activity != null) {
            mainActivity!!.runOnUiThread {
                soundMedia = MediaPlayer.create(
                    mainActivity!!.baseContext,
                    R.raw.call_ring_busy
                )
                if (!soundMedia!!.isPlaying) {
                    soundMedia!!.start()
                    soundMedia!!.isLooping = false
                }
                soundMedia!!.setOnCompletionListener { mp: MediaPlayer? -> mainActivity!!.finish() }
            }
        }
    }

    fun playNoAnswer() {
        mainActivity!!.runOnUiThread {
            soundPool!!.pause(idCurrentSound)
            soundMedia = MediaPlayer.create(
                mainActivity,
                R.raw.call_user_no_answer
            )
            if (!soundMedia!!.isPlaying) {
                soundMedia!!.start()
                soundMedia!!.isLooping = false
            }
            soundMedia!!.setOnCompletionListener { mp: MediaPlayer? ->
                emitSocket(
                    SocketCallEmitDataEnum.CALL_NO_ANSWER(),
                    SocketNewCallRequest(
                        groupID,
                        keyRoom,
                        messageType,
                        memberCreateID,
                        ""
                    )
                )
                mainActivity!!.finish()
            }
        }
    }

    fun playUserCancel() {
        mainActivity!!.runOnUiThread {
            soundMedia = MediaPlayer.create(
                mainActivity,
                R.raw.call_ring_busy
            )
            if (!soundMedia!!.isPlaying) {
                soundMedia!!.start()
                soundMedia!!.isLooping = false
            }
            soundMedia!!.setOnCompletionListener { mp: MediaPlayer? -> mainActivity!!.finish() }
        }
    }

    private val onCallCancel =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                val activity: Activity? = activity
                if (activity != null) {
                    mainActivity!!.runOnUiThread {
                        WriteLog.d("onCallCancel : %s", args[0].toString())
                        if (typeOption == 1) {
                            noAnswerCountDownTimer!!.cancel()
                        }
                        playSuccess()
                    }
                }
            }.start()
        }

    private val onConnectComplete =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                val myService =
                    Intent(requireActivity(), MiniCallService::class.java)
                requireActivity().stopService(myService)
                val activity: Activity? = activity
                if (activity != null) {
                    PrefUtils.getInstance().putBoolean(TechresEnum.IS_CALL.toString(), false)
                    requireActivity().runOnUiThread {
                        WriteLog.d("onConnectComplete : %s", args[0].toString())
                        binding.content.status.visibility = View.VISIBLE
                        binding.content.status.text = getString(R.string.end_call)
                        playSuccess()
                    }
                }
            }.start()
        }


    @Subscribe()
    fun onBusy(event: EventBusBusyCall) {
        if (event.message != "") {
            binding.bottomAction.btnDecline.isEnabled = true
            emitSocket(
                SocketCallEmitDataEnum.CALL_CANCEL(),
                SocketNewCallRequest(
                    groupID,
                    keyRoom,
                    messageType,
                    memberCreateID,
                    showInfo(binding.headerAction.chronometerVideo.text.toString())
                )
            )
            binding.content.rippleBackground.stopRippleAnimation()
            binding.content.status.text = event.message
            soundPool!!.pause(idCurrentSound)
            playSuccess()
        }
    }

    @Subscribe()
    fun onCloseCall(event: EventBusCloseCall) {
        if (event.message != "") {
            binding.bottomAction.btnDecline.isEnabled = true
            binding.content.rippleBackground.stopRippleAnimation()
            binding.content.status.text = event.message
            soundPool!!.pause(idCurrentSound)
            playBusy()
        }
    }

    private val onNoAnswer =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                WriteLog.d("onCallReject : %s", args[0].toString())
                EventBus.getDefault()
                    .post(EvenBusFinish("CallVoiceFragment"))
                mainActivity!!.finish()
            }.start()
        }

    @SuppressLint("DefaultLocale")
    private fun showInfo(time: String): String? {
        val separated = time.split(":".toRegex()).toTypedArray()
        return if (separated.size == 2) {
            java.lang.String.format(
                getString(R.string.time_format_1),
                separated[0].toInt(),
                separated[1].toInt()
            )
        } else String.format(
            "%s giời %s phút %s giây",
            separated[0].toInt(),
            separated[1].toInt(),
            separated[2].toInt()
        )
    }

    private fun unRegistrySocket() {
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_CANCEL())
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_COMPLETE())
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_NO_ANSWER())
    }

    private fun showDialogScreenShotCall(x: Int, y: Int) {
        dialogScreenShot = Dialog(mainActivity!!)
        dialogScreenShot!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogScreenShot!!.setCancelable(true)
        dialogScreenshotCallVideoBinding = DialogScreenshotCallVideoBinding.inflate(layoutInflater)
        dialogScreenShot!!.setContentView(dialogScreenshotCallVideoBinding!!.root)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialogScreenShot!!.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP or Gravity.START
        lp.x = x
        lp.y = y
        dialogScreenShot!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogScreenShot!!.window!!.attributes = lp
        dialogScreenShot!!.show()
    }

    private fun showDialogMoreCall(x: Int, y: Int, dialogCallHandler: DialogCallHandler) {
        val dialog = Dialog(mainActivity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        val dialogCustomKeyboardCallBinding: DialogCustomKeyboardCallBinding =
            DialogCustomKeyboardCallBinding.inflate(
                layoutInflater
            )
        dialog.setContentView(dialogCustomKeyboardCallBinding.root)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP or Gravity.START
        lp.x = x
        lp.y = y
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes = lp
        dialog.show()
        dialog.setOnDismissListener {
            binding.bottomAction.btnSpeaker.isSelected = false
        }
        dialogCustomKeyboardCallBinding.btnFlipCam.isSelected = isChangeCamera
        dialogCustomKeyboardCallBinding.btnSplitScreen.isSelected = isSplitScreen
        dialogCustomKeyboardCallBinding.btnCamera.isSelected = isCamera
        dialogCustomKeyboardCallBinding.btnMicCall.isSelected = isMic
        dialogCustomKeyboardCallBinding.btnFlipCam.isEnabled = !isCamera
        dialogCustomKeyboardCallBinding.btnFlipCam.setOnClickListener {
            dialogCallHandler.onFlipCam()
            if (isChangeCamera) {
                isChangeCamera = false
                dialogCustomKeyboardCallBinding.btnFlipCam.isSelected = false
            } else {
                isChangeCamera = true
                dialogCustomKeyboardCallBinding.btnFlipCam.isSelected = true
            }
            dialog.dismiss()
        }
        dialogCustomKeyboardCallBinding.btnSplitScreen.setOnClickListener {
            dialogCallHandler.onSplitScreen()
            if (isSplitScreen) {
                isSplitScreen = false
                dialogCustomKeyboardCallBinding.btnSplitScreen.isSelected = false
            } else {
                isSplitScreen = true
                dialogCustomKeyboardCallBinding.btnSplitScreen.isSelected = true
            }
            dialog.dismiss()
        }
        dialogCustomKeyboardCallBinding.btnCamera.setOnClickListener {
            dialogCallHandler.onCamera()
            if (isCamera) {
                isCamera = false
                dialogCustomKeyboardCallBinding.btnCamera.isSelected = false
                dialogCustomKeyboardCallBinding.btnFlipCam.isEnabled = true
            } else {
                isCamera = true
                dialogCustomKeyboardCallBinding.btnCamera.isSelected = true
                dialogCustomKeyboardCallBinding.btnFlipCam.isEnabled = false
            }
            dialog.dismiss()
        }
        dialogCustomKeyboardCallBinding.btnMicCall.setOnClickListener {
            dialogCallHandler.onMicCall()
            if (isMic) {
                isMic = false
                dialogCustomKeyboardCallBinding.btnMicCall.isSelected = false
            } else {
                isMic = true
                dialogCustomKeyboardCallBinding.btnMicCall.isSelected = true
            }
            dialog.dismiss()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun turnOnChangeCallMessage() {
        val activity: Activity? = activity
        if (activity != null) {
            mainActivity!!.runOnUiThread {
                binding.lnViewCall.setBackgroundColor(
                    resources.getColor(
                        R.color.transparent,
                        null
                    )
                )
                binding.headerAction.chronometerVideo.visibility = View.GONE
                binding.headerAction.btnChangeVideoCall.visibility = View.GONE
                binding.content.rippleBackground.visibility = View.GONE
                binding.content.chronometer.visibility = View.GONE
                binding.content.name.visibility = View.GONE
                binding.headerAction.chronometerVideo.visibility = View.VISIBLE
                binding.headerAction.btnCallMinimize.isEnabled = true
                binding.bottomAction.btnMute.isSelected = false
                binding.bottomAction.btnMute.isEnabled = true
                binding.bottomAction.btnMute.setBackgroundDrawable(
                    resources.getDrawable(
                        R.drawable.ic_call_snapshot_selector,
                        null
                    )
                )
                binding.bottomAction.btnSpeaker.setBackgroundDrawable(
                    resources.getDrawable(
                        R.drawable.ic_call_more_menu_selector,
                        null
                    )
                )
                binding.bottomAction.btnSpeaker.setOnClickListener { view ->
                    binding.bottomAction.btnSpeaker.isSelected = true
                    val originalPos = IntArray(2)
                    view.getLocationInWindow(originalPos)
                    val x = originalPos[0]
                    val y = (originalPos[1] - view.height * 4.3).toInt()
                    showDialogMoreCall(x, y, this)
                }
                binding.bottomAction.btnMute.setOnClickListener { view ->
                    EventBus.getDefault().post(EventBusCallAction("ScreenShot"))
                    val originalPos = IntArray(2)
                    view.getLocationInWindow(originalPos)
                    val x = originalPos[0]
                    val y = (originalPos[1] - view.height * 3.6).toInt()
                    showDialogScreenShotCall(x, y)
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun turnOnChangeCallMessageSocket() {
        isChange = true
        EventBus.getDefault().post(EventBusPutIsChange(isChange))
        binding.lnViewCall.visibility = View.GONE
        binding.lnChangeCall.visibility = View.VISIBLE
        binding.bottomAction.btnMute.isSelected = false
        binding.bottomAction.btnMute.isEnabled = true
        binding.rippleBackground.startRippleAnimation()
        val shake: Animation =
            AnimationUtils.loadAnimation(mainActivity!!, R.anim.rotate_call_video)
        binding.btnAnswer.startAnimation(shake)
        callPhotoAvatar(avatarSender, binding.avatar, configNodeJs.api_ads)
        binding.name.text = nameSender
        binding.btnReject.setOnClickListener { view ->
            isChange = false
            EventBus.getDefault().post(EventBusPutIsChange(isChange))
            turnOffVibrator()
            binding.lnViewCall.visibility = View.VISIBLE
            binding.lnChangeCall.visibility = View.GONE
            EventBus.getDefault().post(EventBusCallAction("RejectToCamera"))
            val activity: Activity? = activity
            if (activity != null) {
                mainActivity!!.runOnUiThread {
                    binding.lnViewCall.setBackgroundColor(
                        resources.getColor(
                            R.color.transparent,
                            null
                        )
                    )
                    binding.headerAction.chronometerVideo.visibility = View.GONE
                    binding.headerAction.btnChangeVideoCall.visibility = View.GONE
                    binding.content.rippleBackground.visibility = View.GONE
                    binding.content.chronometer.visibility = View.GONE
                    binding.content.name.visibility = View.GONE
                    binding.headerAction.chronometerVideo.visibility = View.VISIBLE
                    binding.headerAction.btnCallMinimize.isEnabled = true
                    isCamera = true
                    binding.bottomAction.btnSpeaker.setBackgroundDrawable(
                        resources.getDrawable(
                            R.drawable.ic_call_more_menu_selector,
                            null
                        )
                    )
                    binding.bottomAction.btnSpeaker.setOnClickListener { v1 ->
                        binding.bottomAction.btnSpeaker.isSelected = true
                        val originalPos = IntArray(2)
                        v1.getLocationInWindow(originalPos)
                        val x = originalPos[0]
                        val y = (originalPos[1] - view.height * 4.3).toInt()
                        showDialogMoreCall(x, y, this)
                    }
                }
            }
        }
        binding.btnAnswer.setOnClickListener { view ->
            isChange = false
            EventBus.getDefault().post(EventBusPutIsChange(isChange))
            turnOffVibrator()
            binding.lnViewCall.visibility = View.VISIBLE
            binding.lnChangeCall.visibility = View.GONE
            EventBus.getDefault().post(EventBusCallAction("AcceptToCamera"))
            val activity: Activity? = activity
            if (activity != null) {
                mainActivity!!.runOnUiThread {
                    binding.lnViewCall.setBackgroundColor(
                        resources.getColor(
                            R.color.transparent,
                            null
                        )
                    )
                    binding.headerAction.chronometerVideo.visibility = View.GONE
                    binding.headerAction.btnChangeVideoCall.visibility = View.GONE
                    binding.content.rippleBackground.visibility = View.GONE
                    binding.content.chronometer.visibility = View.GONE
                    binding.content.name.visibility = View.GONE
                    binding.headerAction.chronometerVideo.visibility = View.VISIBLE
                    binding.headerAction.btnCallMinimize.isEnabled = true
                    binding.bottomAction.btnSpeaker.setBackgroundDrawable(
                        resources.getDrawable(
                            R.drawable.ic_call_more_menu_selector,
                            null
                        )
                    )
                    binding.bottomAction.btnSpeaker.setOnClickListener { v1 ->
                        binding.bottomAction.btnSpeaker.isSelected = true
                        val originalPos = IntArray(2)
                        v1.getLocationInWindow(originalPos)
                        val x = originalPos[0]
                        val y = (originalPos[1] - view.height * 4.3).toInt()
                        showDialogMoreCall(x, y, this)
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun turnOnVibrator() {
        val VIBRATE_PATTERN = longArrayOf(500, 500)
        // API 26 and above
        vibrator!!.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, 0))
    }

    private fun turnOffVibrator() {
        if (vibrator != null) vibrator!!.cancel()
    }

    override fun onCamera() {
        EventBus.getDefault().post(EventBusCallAction("OnOffCamera"))
    }

    override fun onSplitScreen() {
        EventBus.getDefault().post(EventBusCallAction("SplitScreen"))
    }

    override fun onMicCall() {
        EventBus.getDefault().post(EventBusCallAction("ToggleMic"))
    }

    override fun onFlipCam() {
        EventBus.getDefault().post(EventBusCallAction("CameraSwitch"))
    }

    override fun onIceConnected() {
        isAnswer = true
        val activity: Activity? = activity
        if (activity != null) {
            mainActivity!!.runOnUiThread {
                binding.content.progressBarCountDown.visibility = View.GONE
                if (messageType == "call_video") {
                    binding.lnViewCall.setBackgroundColor(
                        resources.getColor(
                            R.color.transparent,
                            null
                        )
                    )
                    binding.content.rippleBackground.visibility = View.GONE
                    binding.content.chronometer.visibility = View.GONE
                    binding.content.name.visibility = View.GONE
                    binding.headerAction.chronometerVideo.visibility = View.VISIBLE
                    isSpeaker = true
                    binding.bottomAction.btnMute.setOnClickListener { view ->
                        EventBus.getDefault().post(EventBusCallAction("ScreenShot"))
                        val originalPos = IntArray(2)
                        view.getLocationInWindow(originalPos)
                        val x = originalPos[0]
                        val y = (originalPos[1] - view.height * 3.6).toInt()
                        showDialogScreenShotCall(x, y)
                    }
                } else {
                    binding.content.chronometer.visibility = View.VISIBLE
                    binding.headerAction.chronometerVideo.visibility = View.GONE
                    binding.headerAction.btnChangeVideoCall.isEnabled = true
                    binding.headerAction.btnChangeVideoCall.visibility = View.VISIBLE
                    isSpeaker = false
                    binding.bottomAction.btnMute.setOnClickListener { v ->
                        if (isMic) {
                            binding.bottomAction.btnMute.isSelected = false
                            isMic = false
                        } else {
                            binding.bottomAction.btnMute.isSelected = true
                            isMic = true
                        }
                        EventBus.getDefault().post(EventBusCallAction("ToggleMic"))
                    }
                }

                //Stop timer
                if (typeOption == 1) {
                    noAnswerCountDownTimer!!.cancel()
                }
                binding.headerAction.btnCallMinimize.isEnabled = true
                binding.bottomAction.btnMute.isEnabled = true
                binding.bottomAction.btnSpeaker.isEnabled = true
                binding.content.status.visibility = View.GONE
                val elapsedRealtime = SystemClock.elapsedRealtime()
                // Set the time that the count-up timer is in reference to.
                binding.content.chronometer.base = elapsedRealtime
                binding.content.chronometer.start()
                binding.content.chronometer.setOnChronometerTickListener { chronometer ->
                    EventBus.getDefault().post(EventBusPutChronometer(chronometer))
                }
                binding.headerAction.chronometerVideo.base = elapsedRealtime
                binding.headerAction.chronometerVideo.start()
                binding.content.rippleBackground.stopRippleAnimation()
                soundPool!!.pause(idCurrentSound)
            }
        }
    }

    override fun onCallReject(data: String?) {
        val activity: Activity? = activity
        if (activity != null) {
            mainActivity!!.runOnUiThread {
                Timber.e("onCallReject : %s", data)
                if (typeOption == 1) {
                    noAnswerCountDownTimer!!.cancel()
                }
                binding.content.status.text = getString(R.string.recipient_rejects)
                playUserCancel()
            }
        }
    }

    override fun onCallAccept(data: String?) {
    }

    override fun onChangeCallMe() {
        messageType = "call_video"
        binding.bottomAction.tvSpeaker.text = getString(R.string.add)
        binding.bottomAction.tvMute.text = getString(R.string.take_a_shot)
        binding.bottomAction.lnTakeAShot.visibility = View.GONE
        turnOnChangeCallMessage()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onChangeCallOther() {
        messageType = "call_video"
        binding.bottomAction.tvSpeaker.text = getString(R.string.add)
        binding.bottomAction.tvMute.text = getString(R.string.take_a_shot)
        binding.bottomAction.lnTakeAShot.visibility = View.GONE
        binding.bottomAction.btnMute.setBackgroundDrawable(
            resources.getDrawable(
                R.drawable.ic_call_snapshot_selector,
                null
            )
        )
        binding.bottomAction.btnMute.setOnClickListener { view ->
            EventBus.getDefault().post(EventBusCallAction("ScreenShot"))
            val originalPos = IntArray(2)
            view.getLocationInWindow(originalPos)
            val x = originalPos[0]
            val y = (originalPos[1] - view.height * 3.6).toInt()
            showDialogScreenShotCall(x, y)
        }
        turnOnChangeCallMessageSocket()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            turnOnVibrator()
        }
    }

    override fun onFloatingChangeCall(isInPictureInPictureMode: Boolean) {
        if (dialogScreenShot != null) dialogScreenShot!!.cancel()
        if (isInPictureInPictureMode) {
            binding.lnChangeCall.visibility = View.GONE
            binding.lnViewCall.visibility = View.GONE
            turnOffVibrator()
        } else {
            if (isChange) {
                binding.lnViewCall.visibility = View.GONE
                binding.lnChangeCall.visibility = View.VISIBLE
            } else {
                binding.lnViewCall.visibility = View.VISIBLE
                binding.lnChangeCall.visibility = View.GONE
            }
        }
    }

    override fun onOtherOffMic(status: Boolean) {
        isMicOther = status
        if (isCameraOther && isMicOther) {
            binding.bottomAction.lnState.visibility = View.GONE
        } else if (isCameraOther) {
            binding.bottomAction.imvOffCamera.visibility = View.GONE
            binding.bottomAction.imvOffMic.visibility = View.VISIBLE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(getString(R.string.user_off_mic), splitMaxName(nameSender))
        } else if (isMicOther) {
            binding.bottomAction.imvOffCamera.visibility = View.VISIBLE
            binding.bottomAction.imvOffMic.visibility = View.GONE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(
                    getString(R.string.user_off_camera),
                    splitMaxName(nameSender)
                )
        } else {
            binding.bottomAction.imvOffCamera.visibility = View.VISIBLE
            binding.bottomAction.imvOffMic.visibility = View.VISIBLE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(
                    getString(R.string.user_off_camera_and_mic),
                    splitMaxName(nameSender)
                )
        }
    }

    override fun onOtherOffCamera(status: Boolean) {
        isCameraOther = status
        if (isCameraOther && isMicOther) {
            binding.bottomAction.lnState.visibility = View.GONE
        } else if (isCameraOther) {
            binding.bottomAction.imvOffCamera.visibility = View.GONE
            binding.bottomAction.imvOffMic.visibility = View.VISIBLE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(getString(R.string.user_off_mic), splitMaxName(nameSender))
        } else if (isMicOther) {
            binding.bottomAction.imvOffCamera.visibility = View.VISIBLE
            binding.bottomAction.imvOffMic.visibility = View.GONE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(
                    getString(R.string.user_off_camera),
                    splitMaxName(nameSender)
                )
        } else {
            binding.bottomAction.imvOffCamera.visibility = View.VISIBLE
            binding.bottomAction.imvOffMic.visibility = View.VISIBLE
            binding.bottomAction.lnState.visibility = View.VISIBLE
            binding.bottomAction.tvState.text =
                java.lang.String.format(
                    getString(R.string.user_off_camera_and_mic),
                    splitMaxName(nameSender)
                )
        }
    }

    override fun onScreenShot(bitmap: Bitmap?) {
        dialogScreenshotCallVideoBinding!!.imgScreenShot.setImageBitmap(bitmap)
        dialogScreenshotCallVideoBinding!!.btnSend.setOnClickListener {
            uploadFileChatToServer(
//                saveBitmap(bitmap!!),
                bitmapToFile(bitmap!!, String.format(
                    "/screenshot_%s%s",
                    Calendar.getInstance().timeInMillis,
                    ".png"
                ))!!.path,
                groupID,
                0,
                bitmap.width,
                bitmap.height
            )
            dialogScreenShot!!.cancel()
        }
    }

    fun uploadFileChatToServer(
        path: String?,
        groupID: String?,
        size: Long,
        width: Int,
        height: Int
    ) {
        getLinkFile(getNameFileToPath(path?:""),1, height, width)
        uploadFileChat(path, groupID)
    }

    fun uploadFileChat(photo: String?, groupID: String?) {
        val serverUrlString = String.format(
            "%s/api-upload/upload-file-server-chat/%s/%s/%s",
            configNodeJs.api_ads,
            resources.getString(R.string.type_personal),
            groupID,
            getNameFileToPath(photo?:"")
        )
        val paramNameString = "send_file"
        try {
            MultipartUploadRequest(mainActivity!!, serverUrlString)
                .setMethod("POST")
                .addFileToUpload(photo!!, paramNameString)
                .addHeader("Authorization", user.nodeAccessToken)
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        context,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .startUpload()
        } catch (exc: java.lang.Exception) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getNameFileToPath(path: String): String {
        return path.substring(path.lastIndexOf("/") + 1)
    }

//    fun saveBitmap(bitmap: Bitmap): String? {
//        val imagePath = Environment.getExternalStorageDirectory().absolutePath.toString() + String.format(
//            "/screenshot_%s",
//            Calendar.getInstance().timeInMillis
//        )
//        val dir = File(imagePath)
//        if (!dir.exists()) dir.mkdirs()
//        val file = File(dir, "sketchpad" + pad.t_id.toString() + ".png")
//        val fOut = FileOutputStream(file)
//
//        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
//        fOut.flush()
//        fOut.close()
//
//        return dir.path
//    }

    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    private fun  getLinkFile(
        name: String,
        type: Int,
        height: Int,
        width: Int
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            String.format(
                "%s%s%s%s%s%s%s%s%s%s",
                "/api-upload/get-link-server-chat?group_id=",
                groupID,
                "&type=",
                TechResEnumChat.TYPE_PERSONAL_FILE.toString(),
                "&name_file=",
                name,
                "&width=",
                width,
                "&height=",
                height
            )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getImageChat(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FileNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FileNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val imageList = ArrayList<FileNodeJs>()
                        imageList.add(response.data)
                        chatImage(imageList)
                    }
                }
            })

    }

    private fun chatImage(file: ArrayList<FileNodeJs>) {
        val chatImagePersonalRequest = ChatImagePersonalRequest()
        chatImagePersonalRequest.message_type = TechResEnumChat.TYPE_IMAGE.toString()
        chatImagePersonalRequest.group_id = groupID
        chatImagePersonalRequest.member_id = user.id
        chatImagePersonalRequest.files = file
        chatImagePersonalRequest.key_message_error = Utils.getRandomString(10)
        chatImagePersonalRequest.key_message = randomKeyFile

        try {
            val jsonObject = JSONObject(Gson().toJson(chatImagePersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_PERSONAL_IMAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_PERSONAL_IMAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onBackPress(): Boolean {
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)

    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }
}