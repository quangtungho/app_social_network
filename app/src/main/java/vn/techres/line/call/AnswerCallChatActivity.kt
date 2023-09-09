package vn.techres.line.call

import android.app.KeyguardManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import io.socket.emitter.Emitter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.TechResApplication.Companion.mSocket
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.eventbus.EvenBusCallVideo
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemCallAnswerBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils
import vn.techres.line.helper.rtc_custom.models.EvenBusFinish
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.emitSocket
import vn.techres.line.videocall.SocketCallEmitDataEnum
import vn.techres.line.videocall.SocketCallOnDataEnum

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/3/2022
 */
class AnswerCallChatActivity : BaseBindingActivity<ItemCallAnswerBinding>() {
    private var user = User()
    private var configNodeJs = ConfigNodeJs()

    var socketCallResponse: SocketCallResponse? = null

    var checkBackNoCheck = true
    override val bindingInflater: (LayoutInflater) -> ItemCallAnswerBinding
        get() = ItemCallAnswerBinding::inflate

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        setUp()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        socketCallResponse = Gson().fromJson(
            intent.getStringExtra("DATA_CALL"),
            SocketCallResponse::class.java
        )

        binding.bottomAction.btnReject.setOnClickListener {
            TechResApplication.applicationContext().getNotificationManager()!!.cancel(
                socketCallResponse!!.group_id_for_notification!!
            )
            binding.bottomAction.btnReject.isEnabled = true
            Handler(Looper.myLooper()!!).postDelayed({
                binding.bottomAction.btnReject.isEnabled = false
            }, 200)
            checkBackNoCheck = false
            socketCallResponse!!.call_member_create = socketCallResponse!!.member_id
            socketCallResponse!!.member_id = user.id
            socketCallResponse!!.member_receiver_id = intent.getIntExtra("MEMBER_RECEIVER_ID", 0)
            emitSocket(SocketCallEmitDataEnum.CALL_REJECT(), socketCallResponse)
            finish()
        }
        binding.bottomAction.btnAnswer.setOnClickListener {
            checkBackNoCheck = false
            socketCallResponse!!.member_id = user.id
            try {
                ChatUtils.connectToRoom(
                    this,
                    socketCallResponse!!.group_id,
                    intent.getIntExtra("MEMBER_RECEIVER_ID", 0),
                    socketCallResponse!!.full_name,
                    socketCallResponse!!.avatar,
                    socketCallResponse!!.call_member_create!!,
                    2,
                    socketCallResponse!!.message_type?:"",
                    "aloline",
                    socketCallResponse!!.key_room
                )
                this.finish()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }

        Utils.getImageCall(binding.content.avatar, socketCallResponse!!.avatar, configNodeJs)
        binding.content.name.text = socketCallResponse!!.full_name
        binding.content.status.visibility = View.VISIBLE
        binding.content.chronometer.visibility = View.GONE
        binding.content.rippleBackground.startRippleAnimation()
        binding.headerAction.btnChangeVideoCall.visibility = View.GONE
        binding.headerAction.btnCallMinimize.isEnabled = false
        binding.bottomAction.rippleBackground.startRippleAnimation()
        val shake = AnimationUtils.loadAnimation(this, R.anim.rotate_call_video)
        binding.bottomAction.btnAnswer.startAnimation(shake)

        if (socketCallResponse!!.message_type == "call_video"
        ) {
            binding.content.status.text = getString(R.string.incoming_video_call)
            binding.bottomAction.btnAnswer.setBackgroundResource(R.drawable.call_accept_switch_video_selector)
        } else {
            binding.content.status.text = getString(R.string.incoming_voice_call)
            binding.bottomAction.btnAnswer.setBackgroundResource(R.drawable.new_incall_answer_voice_button_selector)
        }

        mSocket?.on(SocketCallOnDataEnum.RES_CALL_CANCEL(), onCallCancel)
        mSocket?.on(SocketCallOnDataEnum.RES_CALL_NO_ANSWER(), onCallCancel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegistrySocket()
    }


    private fun unRegistrySocket() {
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_CANCEL())
        mSocket?.off(SocketCallOnDataEnum.RES_CALL_NO_ANSWER())
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    private val onCallCancel =
        Emitter.Listener { args: Array<Any> ->
            Thread {
                TechResApplication.applicationContext().getNotificationManager()!!.cancel(
                    socketCallResponse!!.group_id_for_notification!!
                )
                checkBackNoCheck = false
                Timber.e("onCallReject : %s", args[0].toString())
                finish()
            }.start()
        }


    @Subscribe
    fun onFinish(event: EvenBusFinish) {
        if (event.from_event.equals("CallVoiceFragment")) {
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFinishCallFromNotification(event: EvenBusCallVideo) {
        if (!event.statusCall) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}