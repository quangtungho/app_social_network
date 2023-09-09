package vn.techres.line.fragment.game.luckywheel

import android.Manifest
import android.animation.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.emojiview.listener.PopupListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.facebook.FacebookSdk.getApplicationContext
import com.google.zxing.integration.android.IntentIntegrator
import com.leocardz.link.preview.library.TextCrawler
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.game.LuckyWheelActivity
import vn.techres.line.adapter.game.luckywheel.ListUserWinnerAdapter
import vn.techres.line.adapter.game.luckywheel.MessageChatGameLuckyWheelAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.LuckyItem
import vn.techres.line.data.model.chat.CategorySticker
import vn.techres.line.data.model.chat.Sticker
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.data.model.game.luckywheel.response.ReactionLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.RoundLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.StopLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.TotalCustomerJoinRoomResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.UserWinnerParams
import vn.techres.line.data.model.response.game.MessageGameLuckyWheelResponse
import vn.techres.line.data.model.response.game.UserWinner
import vn.techres.line.data.model.response.game.UserWinnerResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentLuckyWheelGameBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.helper.socket.SocketLuckyWheelManager
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.StickerHandler
import vn.techres.line.interfaces.game.BackLuckyWheel
import vn.techres.line.interfaces.game.ListWinnerHandler
import vn.techres.line.interfaces.socket.SocketLuckyWheelHandler
import vn.techres.line.services.ServiceFactory.Companion.createRetrofitServiceNode
import vn.techres.line.services.TechResService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.random.Random.Default.nextInt


@Suppress("UNREACHABLE_CODE")
class LuckyWheelGameFragment :
    BaseBindingFragment<FragmentLuckyWheelGameBinding>(FragmentLuckyWheelGameBinding::inflate),
    StickerHandler, SocketLuckyWheelHandler, ListWinnerHandler, PopupListener,
    UtilitiesChatHandler, BackLuckyWheel {

    private val luckyWheelActivity: LuckyWheelActivity?
        get() = activity as LuckyWheelActivity?
    private var roomId = ""
    private var restaurantId = 0
    private var branchId = 0
    private var articleGameId = ""
    private var totalMessage = 0f
    private val currentLimit = 20
    private var currentPage = 1
    private var totalPage = 0
    private var isCheckStopGame = 0
    private var isBack = true
    private var isPause = false
    private var isFist = false

    //keyboard
    private var isShowingKeyBoard = false
    private var isKeyBoard = false
    private var onViewHeightChanged = 0
    private var row = 0

    //dialog
    private var dialog: Dialog? = null
    private var imgClose: ImageView? = null
    private var imgGift: ImageView? = null
    //

    var isOn = false
    var handler: Handler = Handler()
    var camManager: CameraManager? = null
    var cameraId = ""
    var splashThread: Thread? = null
    private var priceNumber = 0

    private var listMessageGameLuckyWheel: ArrayList<MessageGameLuckyWheel> = ArrayList()
    private var listUser = ArrayList<UserWinner>()
    private var chatAdapter: MessageChatGameLuckyWheelAdapter? = null
    private var listUserWinnerAdapter: ListUserWinnerAdapter? = null
    private var textCrawler: TextCrawler? = null
    private var mPlayer1: MediaPlayer? = null
    private var mPlayer2: MediaPlayer? = null
    private var mPlayer3: MediaPlayer? = null
    private var mPlayer4: MediaPlayer? = null
    private var mPlayer5: MediaPlayer? = null
    private var user = User()
    private var configNodeJs = ConfigNodeJs()
    private var socketLuMessageGameLuckyWheel: SocketLuckyWheelManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        user = CurrentUser.getCurrentUser(requireActivity())
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        //Get Data Bundle
        socketLuMessageGameLuckyWheel = SocketLuckyWheelManager(requireActivity())
        socketLuMessageGameLuckyWheel?.setSocketLuckyWheelHandler(this)
        //
        dialog = luckyWheelActivity?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_award_winning_li_xi)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog?.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.attributes = lp
        imgGift = dialog?.findViewById(R.id.imgGift) as ImageView
        imgClose = dialog?.findViewById(R.id.imgClose) as ImageView
        //
        arguments?.let {
            roomId = it.getString(TechresEnum.ROOM_ID.toString(), "")
            restaurantId = restaurant().restaurant_id ?: 0
            branchId = it.getInt(TechresEnum.BRANCH_ID.toString(), 0)
            articleGameId = it.getString(TechresEnum.ID_ARTICLE_GAME.toString(), "").toString()
            row = it.getInt(TechresEnum.ROW_GAME.toString(), 0)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        luckyWheelActivity?.setBackLuckyWheel(this)
        setWakeLock(true)
        //blink splash
        camManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        //Set RecyclerView
        val mLayoutManager = LinearLayoutManager(luckyWheelActivity)
        mLayoutManager.reverseLayout = true
        binding.commentGame.rcvChat.setHasFixedSize(true)
        binding.commentGame.rcvChat.layoutManager = mLayoutManager
        val recyclerViewState = binding.commentGame.rcvChat.layoutManager?.onSaveInstanceState()
        binding.commentGame.rcvChat.layoutManager?.onRestoreInstanceState(recyclerViewState)

        chatAdapter =
            MessageChatGameLuckyWheelAdapter(
                requireActivity()
            )
        listUserWinnerAdapter = ListUserWinnerAdapter(requireActivity())
        listUserWinnerAdapter?.setListWinnerHandler(this)
        binding.commentGame.rcvChat.adapter = chatAdapter
        binding.commentGame.edtComment.addTextChangedListener(textMessageWatcher)
        textCrawler = TextCrawler()
        val pager = UIView.loadView(requireActivity(), binding.commentGame.edtComment)
        binding.commentGame.layoutKeyboard.initPopupView(
            pager,
            UIView.loadUtilities(requireActivity(), binding.commentGame.edtComment)
        )
        UIView.setUtilitiesChatHandler(this)
        binding.commentGame.layoutKeyboard.setPopupListener(this)
        setData()
        setListener()
//        val imageClone = cloneImage(2f)
//        imageClone?.let { animateFlying(it, 2f) }
//        val imageClone1 = cloneImage(3f)
//        imageClone1?.let { animateFlying(it, 3f) }
//        val imageClone2 = cloneImage(4f)
//        imageClone2?.let { animateFlying(it, 4f) }
//        val imageClone3 = cloneImage(3.5f)
//        imageClone3?.let { animateFlying(it, 3.5f) }
//        val imageClone4 = cloneImage(5f)
//        imageClone4?.let { animateFlying(it, 5f) }
//        val imageClone5 = cloneImage(6f)
//        imageClone5?.let { animateFlying(it, 6f) }
//        val imageClone6 = cloneImage(1.5f)
//        imageClone6?.let { animateFlying(it,1.5f) }
//        val imageClone7 = cloneImage(1.2f)
//        imageClone7?.let { animateFlying(it,1.2f) }
//        val imageClone8 = cloneImage(1.1f)
//        imageClone8?.let { animateFlying(it,1.1f) }
//        val imageClone9 = cloneImage(11f)
//        imageClone9?.let { animateFlying(it,11f) }
//        val imageClone10 = cloneImage(15f)
//        imageClone10?.let { animateFlying(it,15f) }
    }

    private fun setData() {
        socketLuMessageGameLuckyWheel?.joinRoom(
            branchId,
            restaurantId,
            roomId,
            "customer",
            articleGameId
        )

        setLuckyWheel()

        setCountDown()

        setMusicBackground()

        giftLayout()

        getMessagePaginationByGroup(currentPage)

        Glide.with(binding.imvBgWheel)
            .load(String.format("%s%s", configNodeJs.api_ads, "/public/icons/wheel-frame.gif"))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imvBgWheel)

        Glide.with(this).asGif()
            .load(R.raw.src_assets_images_game_giftmission)
            .into(binding.imvGiftWin)
        binding.tvNumberRound.text = "Vòng $row"
//        binding.imvGiftWin.setImageResource(R.drawable.giftanimation)

//        Glide.with(this)
//            .load(R.drawable.giftanimation)
//            .into(binding.imvGiftWin)

//         Set light bar
//        lantern =  Lantern(requireActivity())
//                .checkAndRequestSystemPermission()
//                .observeLifecycle(this)
//
//        if (lantern?.initTorch() == false) {
//            // Request camera permission if it is not granted
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.CAMERA),
//                IntentIntegrator.REQUEST_CODE
//            )
//        }
    }

    private fun setListener() {
        binding.commentGame.rcvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y <= 0) {
                        if (currentPage < totalPage) {
                            currentPage += 1
                            getMessagePaginationByGroup(
                                currentPage
                            )
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.imvShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "Application Link : https://play.google.com/store/apps/details?id=${luckyWheelActivity?.packageName}"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
        }

        binding.commentGame.edtComment.setOnClickListener {
            binding.commentGame.layoutKeyboard.openKeyboard()
            binding.commentGame.edtComment.isCursorVisible = true
        }

        binding.commentGame.edtComment.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if ((binding.commentGame.edtComment.text ?: "").trim { it <= ' ' }.isNotEmpty()) {
                    socketLuMessageGameLuckyWheel?.chatText(
                        v.text.toString(),
                        restaurantId,
                        branchId,
                        roomId
                    )
                    binding.commentGame.edtComment.setText("")
                    handled = true
                }
            }
            handled
        }

        binding.commentGame.imgSend.setOnClickListener {
            if ((binding.commentGame.edtComment.text ?: "").trim { it <= ' ' }.isNotEmpty()) {
                socketLuMessageGameLuckyWheel?.chatText(
                    binding.commentGame.edtComment.text.toString(),
                    restaurantId,
                    branchId,
                    roomId
                )
                binding.commentGame.edtComment.setText("")
            }
        }
        binding.commentGame.imageReactLike.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt(), roomId
            )
        }

        binding.commentGame.imageReactLove.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt(), roomId
            )
        }

        binding.commentGame.imageReactHaha.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt(), roomId
            )
        }

        binding.commentGame.imageReactWow.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_WOW.toString().toInt(), roomId
            )
        }

        binding.commentGame.imageReactCry.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_SAD.toString().toInt(), roomId
            )
        }

        binding.commentGame.imageReactAngry.setOnClickListener {
            socketLuMessageGameLuckyWheel?.onPushReaction(
                TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt(), roomId
            )
        }

        binding.imgListMemberWinner.setOnClickListener {
            showDiaWriteLogListGameWin()
        }

        binding.imvBack.setOnClickListener {
            if (onViewHeightChanged == 0 && !isKeyBoard) {
                if (isBack) {
                    socketLuMessageGameLuckyWheel?.leaveRoom(branchId, restaurantId, roomId)
                    socketLuMessageGameLuckyWheel?.offSocket()
                    setWakeLock(false)
                    leaveRoom()
                    luckyWheelActivity?.finish()
                    luckyWheelActivity?.overridePendingTransition(
                        R.anim.translate_from_right,
                        R.anim.translate_to_right
                    )
                } else {
                    dialogLeaveWhenStartingGame()
                }
            } else {
                binding.commentGame.layoutKeyboard.onBackPressed()
            }
        }

        binding.imgClose.setOnClickListener {
            binding.rltGiftDialog.hide()
            setMusicBackground()
        }

//        binding.imgRuleGame.setOnClickListener {
//            dialogRule()
//        }

        binding.commentGame.imgSticker.setOnClickListener {
            if (isShowingKeyBoard) {
                binding.commentGame.layoutKeyboard.openKeyboard()
            } else {
                binding.commentGame.layoutKeyboard.setView(1)
                binding.commentGame.layoutKeyboard.show()
            }
        }
        binding.commentGame.imgMoreAction.setOnClickListener {
            if (binding.commentGame.lnReaction.visibility == View.VISIBLE) {
                binding.commentGame.imgMoreAction.setImageResource(R.drawable.ic_keyboard_arrow_right_gray_24dp)
                binding.commentGame.lnReaction.hide()
            } else {
                binding.commentGame.imgMoreAction.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
                binding.commentGame.lnReaction.show()
            }
        }
    }

    private fun setPrice(number: Int): Int {
        priceNumber = when (number) {
            10 -> {
                0
            }
            20 -> {
                1
            }
            50 -> {
                2
            }
            100 -> {
                3
            }
            200 -> {
                4
            }
            500 -> {
                5
            }
            else -> {
                0
            }
        }
        return priceNumber
    }

    private fun dialogGift(money: Int) {
        WriteLog.d("LuckyWheelTetActivity", money.toString() + "")
        val drawable = when (money) {
            10 -> {
                R.drawable.money_10k
            }
            20 -> {
                R.drawable.money_20k
            }
            50 -> {
                R.drawable.money_50k
            }
            100 -> {
                R.drawable.money_100k
            }
            200 -> {
                R.drawable.money_200k
            }
            500 -> {
                R.drawable.money_500k
            }
            else -> {
                R.drawable.money_50k
            }
        }

        imgGift?.let {
            Glide.with(it)
                .load(drawable)
                .into(it)
        }

        imgClose?.setOnClickListener {
            if (mPlayer1 != null && mPlayer1?.isPlaying == false) {
                setMusicBackground()
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun startBlinkPlashLight() {
        splashThread = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        sleep(200)
                        if (!isOn) {
                            onFlash()
                            isOn = true
                        } else {
                            isOn = false
                            offFlash()
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
        splashThread?.start()
    }

    private fun stopBlinkSplashLight() {
        if (splashThread != null) {
            // stop blinking
            splashThread?.interrupt()
            offFlash()
        }
    }

    private fun onFlash() {
        try {
            cameraId = camManager?.cameraIdList?.get(0) ?: ""
            camManager?.setTorchMode(cameraId, true) //Turn ON
            isOn = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun offFlash() {
        try {
            cameraId = camManager?.cameraIdList?.get(0) ?: ""
            camManager?.setTorchMode(cameraId, false) //Turn ON
            isOn = false
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setWakeLock(isBoolean: Boolean) {
        if (isBoolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                luckyWheelActivity?.setShowWhenLocked(true)
                luckyWheelActivity?.setTurnScreenOn(true)
            } else {
                luckyWheelActivity?.window?.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                luckyWheelActivity?.setShowWhenLocked(false)
                luckyWheelActivity?.setTurnScreenOn(false)
            } else {
                luckyWheelActivity?.window?.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        socketLuMessageGameLuckyWheel?.joinRoom(
//            branchId,
//            restaurantId,
//            roomId,
//            "customer",
//            articleGameId
//        )
        luckyWheelActivity?.setOnBackClick(this)
//        if (socketLuMessageGameLuckyWheel?.isConnectSocket() == false) {
        socketLuMessageGameLuckyWheel?.registrySocket(requireActivity())
//        }
    }

    override fun onPause() {
        super.onPause()
        try {
            isPause = true
            dialogLeaveWhenStartingGame()
            leaveRoom()
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socketLuMessageGameLuckyWheel?.leaveRoom(branchId, restaurantId, roomId)
            socketLuMessageGameLuckyWheel?.offSocket()
            luckyWheelActivity?.removeOnBackClick(this)
            leaveRoom()
        } catch (e: Exception) {
        }
    }

    private fun leaveRoom() {
        try {
            if (mPlayer1 != null && mPlayer1?.isPlaying == true) {
                mPlayer1?.stop()
            }

            if (mPlayer2 != null && mPlayer2?.isPlaying == true) {
                mPlayer2?.stop()
            }

            if (mPlayer3 != null && mPlayer3?.isPlaying == true) {
                mPlayer3?.stop()
            }

            if (mPlayer4 != null && mPlayer4?.isPlaying == true) {
                mPlayer4?.stop()
            }

            if (mPlayer5 != null && mPlayer5?.isPlaying == true) {
                mPlayer5?.stop()
            }
//            stopBlinkSplashLight()
        } catch (e: Exception) {
        }
    }

    private fun setMusicBackground() {
//        mPlayer1 = MediaPlayer.create(luckyWheelActivity, R.raw.src_gamevoucher_sound_bg)
//        mPlayer1?.isLooping = true
//        mPlayer1?.start()
    }

    private fun setMusicGameStart() {
        mPlayer2 = MediaPlayer.create(luckyWheelActivity, R.raw.game_start)
        mPlayer2?.start()
        mPlayer2?.setOnCompletionListener {
            setMusicGamePlay()
        }
    }

    private fun setMusicGameWin() {
        mPlayer5 = MediaPlayer.create(luckyWheelActivity, R.raw.game_win)
        mPlayer5?.start()
    }

    private fun setMusicGameLose() {
        mPlayer3 = MediaPlayer.create(luckyWheelActivity, R.raw.game_lost)
        mPlayer3?.start()
//        mPlayer3?.setOnCompletionListener {
//            sleep(500)
//            setMusicBackground()
//        }
    }

    private fun setMusicGamePlay() {
        mPlayer4 = MediaPlayer.create(luckyWheelActivity, R.raw.game_play)
        mPlayer4?.isLooping = true
        mPlayer4?.start()
    }

    private fun setCountDown() {
        binding.cvCountdownViewTest.tag = "test2"
        val time2 = 30.toLong() * 60 * 1000
        binding.cvCountdownViewTest.start(time2)
    }

    private val textMessageWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                val st = s.toString()
                val arrS = st.split("\\s".toRegex()).toTypedArray()
                if (arrS.isNotEmpty()) {
                    binding.commentGame.imgSend.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_live_streeam_send_emty
                        )
                    )
                    binding.commentGame.imgSend.show()
                    binding.commentGame.imgMoreAction.show()
                    binding.commentGame.lnReaction.hide()
                    binding.commentGame.imgMoreAction.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
                }
            } else {
                binding.commentGame.imgSend.hide()
                binding.commentGame.imgMoreAction.hide()
                binding.commentGame.lnReaction.show()
            }
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
        }
    }

    private fun setLuckyWheel() {
        val data: ArrayList<LuckyItem> = ArrayList()
        for (i in 0 until 8) {
            val luckyItem = LuckyItem()
            data.add(luckyItem)
        }
        binding.luckyWheel1.setData(data)
//        Glide.with(this).load(R.drawable.lucky_wheel_turn).into(binding.luckyWheel1)
    }

    private fun addItem(item: MessageGameLuckyWheel?) {
        listMessageGameLuckyWheel.add(0, item!!)
        chatAdapter?.notifyItemInserted(0)

    }

    /**
     * Status Keyboard
     * */
    private fun updateButton(emo: Boolean) {
        if (isShowingKeyBoard == emo) return
        isShowingKeyBoard = emo
        if (emo) {
            binding.commentGame.imgSticker.setImageResource(R.drawable.ic_keyboard_chat_message)
        } else {
            binding.commentGame.imgSticker.setImageResource(R.drawable.ic_sticker_tab)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(
                    luckyWheelActivity!!,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
    }

    private fun getMessagePaginationByGroup(page: Int) {
        val baseParams = BaseParams()
        baseParams.http_method = AppConfig.GET
        baseParams.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        baseParams.request_url = String.format(
            "%s%s%s%s%s%s",
            "api/message-room/get-message-pagination?room_id=",
            roomId,
            "&limit=",
            currentLimit,
            "&page=",
            page
        )
        createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagePaginationGameLuckyWheel(baseParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessageGameLuckyWheelResponse> {
                override fun onError(e: Throwable) {}
                override fun onComplete() {}
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: MessageGameLuckyWheelResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        if (listMessageGameLuckyWheel.size == 0) {
                            listMessageGameLuckyWheel = data.list
                        } else {
                            listMessageGameLuckyWheel.addAll(
                                listMessageGameLuckyWheel.size,
                                data.list
                            )
                        }
                        totalMessage = (data.totalMessage ?: 0) + 0f
                        totalPage = ceil(totalMessage / currentLimit.toDouble()).toInt()
                        chatAdapter?.setDataSource(listMessageGameLuckyWheel)
                        if (isFist) {
                            binding.commentGame.rcvChat.smoothScrollToPosition(0)
                            socketLuMessageGameLuckyWheel?.chatText(
                                requireActivity().resources.getString(R.string.title_me_joining_room),
                                restaurantId,
                                branchId,
                                roomId
                            )
                            isFist = false
                        }

                    }
                }
            }
            )
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun giftLayout() {
        val animFade: Animation =
            AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_rotate_game)
        binding.imvShawdowGame.startAnimation(animFade)

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            binding.imvCrab,
            PropertyValuesHolder.ofFloat("scaleX", 0.95f),
            PropertyValuesHolder.ofFloat("scaleY", 0.95f)
        )
        scaleDown.duration = 2000
        scaleDown.repeatMode = ValueAnimator.REVERSE
        scaleDown.repeatCount = ValueAnimator.INFINITE
        scaleDown.start()
        val scaleDown1 = ObjectAnimator.ofPropertyValuesHolder(
            binding.imvTrungthuong,
            PropertyValuesHolder.ofFloat("scaleX", 0.98f),
            PropertyValuesHolder.ofFloat("scaleY", 0.98f)
        )
        scaleDown1.duration = 5000
        scaleDown1.repeatMode = ValueAnimator.REVERSE
        scaleDown1.repeatCount = ValueAnimator.INFINITE
        scaleDown1.start()
    }

    private fun showDiaWriteLogListGameWin() {
        getListGameWin()
        val diaWriteLogBuilder = AlertDialog.Builder(luckyWheelActivity)
        val layoutView: View = layoutInflater.inflate(R.layout.dialog_list_win, null)
        val diaWriteLogListWinner: RecyclerView = layoutView.findViewById(R.id.listWinner)
        val imgClose: ImageView = layoutView.findViewById(R.id.imgClose)
        val tvDate: TextView = layoutView.findViewById(R.id.tvDate)
        diaWriteLogListWinner.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        diaWriteLogListWinner.adapter = listUserWinnerAdapter
        diaWriteLogListWinner.smoothScrollToPosition(0)
        diaWriteLogBuilder.setView(layoutView)
        val alertDiaWriteLog = diaWriteLogBuilder.create()
        alertDiaWriteLog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        tvDate.text =
            String.format("Ngày %s Tháng %s Năm %s", currentDay, currentMonth, currentYear)
        imgClose.setOnClickListener {
            alertDiaWriteLog.dismiss()
        }
        alertDiaWriteLog.show()
    }

    private fun dialogRule() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_rule_lucky_wheel)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = lp
        val imgClose: ImageView = dialog.findViewById(R.id.imgClose)
        imgClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun dialogLeaveWhenStartingGame() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_leave_lucky_wheel)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = lp
        val btnBack: Button = dialog.findViewById(R.id.btnBack)
        val btnLeave: Button = dialog.findViewById(R.id.btnLeave)
        btnBack.setOnClickListener {
            if (isPause && mPlayer4?.isPlaying == false && mPlayer1?.isPlaying == false) {
                setMusicBackground()
            }
            isBack = false
            isPause = false
            dialog.dismiss()
        }
        btnLeave.setOnClickListener {
            socketLuMessageGameLuckyWheel?.leaveRoom(branchId, restaurantId, roomId)
            socketLuMessageGameLuckyWheel?.offSocket()
            setWakeLock(false)
            leaveRoom()
            luckyWheelActivity?.finish()
            luckyWheelActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        dialog.show()
    }

    private fun getListGameWin() {
        val params = UserWinnerParams()
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        params.request_url = "/api/listder-board/get-all-list-winner"
        params.params.branch_id = branchId
        params.params.restaurant_id = restaurantId
        params.params.current_type = "topTen"
        params.params.room_id = roomId
        createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListUserWinner(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UserWinnerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("StaticFieldLeak")
                override fun onNext(response: UserWinnerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listUser = response.data
                        listUserWinnerAdapter?.setDataSource(listUser)
                    } else Toast.makeText(luckyWheelActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }

    override fun onClick(sticker: CategorySticker) {

    }

    override fun onNumber(number: Int) {
        luckyWheelActivity?.runOnUiThread {

        }

    }

    override fun onStartLuckyWheel() {
        luckyWheelActivity?.runOnUiThread {
            binding.luckyWheel1.setRound(1000)
            binding.luckyWheel1.startLuckyWheelWithTargetIndex(0)
//            mPlayer1?.stop()
            setMusicGameStart()
            isCheckStopGame = 1
//            binding.lnGiftLoading.hide()
//            binding.rltGiftDialog.hide()
            dialog?.dismiss()
            isBack = false
//            startBlinkPlashLight()
        }
    }

    override fun onStopLuckyWheel(stopLuckyWheelResponse: StopLuckyWheelResponse) {
        luckyWheelActivity?.runOnUiThread {
            val handler = Handler(Looper.getMainLooper())
            binding.luckyWheel1.setRound(5)
            binding.luckyWheel1.startLuckyWheelWithTargetIndex(0)

            Glide.with(binding.imvCrab)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        stopLuckyWheelResponse.gift_avatar
                    )
                )
                .transform(CenterCrop(), RoundedCorners(10))
                .into(binding.imvCrab)

            mPlayer2?.stop()
            handler.postDelayed({
//                stopBlinkSplashLight()
                mPlayer4?.stop()
                if (isCheckStopGame == 1) {
                    if (stopLuckyWheelResponse.user_id == user.id) {
                        setMusicGameWin()
                        binding.imvGiftWin.visibility = View.VISIBLE
//                        binding.lnGiftLoading.show()
//                        binding.rltGiftDialog.hide()
//                        onFlash()
                        handler.postDelayed({
                            dialogGift(stopLuckyWheelResponse.money ?: 10)
//                            binding.lnGiftLoading.hide()
//                            binding.rltGiftDialog.show()
                            binding.imvGiftWin.visibility = View.GONE
                            isBack = true
//                            binding.konfettiView.build()
//                                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
//                                .setDirection(0.0, 359.0)
//                                .setSpeed(4f, 7f)
//                                .setFadeOutEnabled(true)
//                                .setTimeToLive(2000)
//                                .addShapes(Shape.Square, Shape.Circle)
//                                .addSizes(Size(12), Size(16, 6f))
//                                .setPosition(
//                                    -50f,
//                                    binding.konfettiView.width + 50f,
//                                    -50f,
//                                    -50f
//                                )
//                                .streamFor(300, 5000L)
                        }, 3000)
//                        handler.postDelayed({
//                            offFlash()
//                        }, 10000)
                    } else {
                        Toast.makeText(activity, "Chúc bạn may mắn lần sau", Toast.LENGTH_SHORT)
                            .show()
                        setMusicGameLose()
                    }
                    isCheckStopGame = 0
                    binding.luckyWheel1.setRound(0)
                }
            }, 5000)
        }

    }

    override fun onTotalMemberJoinRoom(totalCustomerJoinRoomResponse: TotalCustomerJoinRoomResponse) {
        luckyWheelActivity?.runOnUiThread {

        }
    }

    override fun onReaction(reactionLuckyWheelResponse: ReactionLuckyWheelResponse) {
        when (reactionLuckyWheelResponse.type_reaction) {
            TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_like
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_heart
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_smile
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_WOW.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_wow
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_angry
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_SAD.toString().toInt() -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_sad
                        )
                    )
                }
            }
            else -> {
                binding.commentGame.heartLayout.post {
                    binding.commentGame.heartLayout.addHeart(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_like
                        )
                    )
                }
            }
        }
    }

    override fun onGameRound(roundLuckyWheelResponse: RoundLuckyWheelResponse) {
        roundLuckyWheelResponse.turn?.let {
            binding.tvNumberRound.text =
                String.format("%s %s", requireActivity().resources.getString(R.string.round), it)
        }
    }

    override fun onCurrentMoney(money: Int) {

    }

    override fun onNextMoney(money: Int) {

    }

    override fun onChatText(messageGameLuckyWheel: MessageGameLuckyWheel) {
        luckyWheelActivity?.runOnUiThread {
            addItem(messageGameLuckyWheel)
            chatAdapter?.notifyItemChanged(0)
            binding.commentGame.rcvChat.smoothScrollToPosition(0)
        }
    }

    override fun onChatSticker(messageGameLuckyWheel: MessageGameLuckyWheel) {
        luckyWheelActivity?.runOnUiThread {
            addItem(messageGameLuckyWheel)
            chatAdapter?.notifyItemChanged(0)
            binding.commentGame.rcvChat.smoothScrollToPosition(0)
        }
    }

    override fun onUserWinner(userWinner: UserWinner) {

    }

    override fun onDismiss() {
        updateButton(false)
    }

    override fun onShow() {
        updateButton(true)
    }

    override fun onKeyboardOpened(height: Int) {
        isKeyBoard = true
        updateButton(false)
    }

    override fun onKeyboardClosed() {
        isKeyBoard = false
        updateButton(binding.commentGame.layoutKeyboard.isShowing)
    }

    override fun onViewHeightChanged(height: Int) {
        onViewHeightChanged = height
    }

    override fun onVote() {
    }

    override fun onChooseFile() {
    }

    override fun onChooseBusinessCard() {
    }

    override fun onChooseSticker(sticker: Sticker) {
        socketLuMessageGameLuckyWheel?.chatSticker(
            sticker.link_original ?: "",
            restaurantId,
            branchId,
            roomId
        )
    }

    override fun onImportantMessage() {
    }

    override fun onBackLuckyWheel() {
        binding.commentGame.layoutKeyboard.onBackPressed()
        if (isBack) {
            socketLuMessageGameLuckyWheel?.leaveRoom(branchId, restaurantId, roomId)
            socketLuMessageGameLuckyWheel?.offSocket()
            setWakeLock(false)
            leaveRoom()
        } else {
            dialogLeaveWhenStartingGame()
        }
    }

    override fun onBackPress(): Boolean {
        return if (onViewHeightChanged == 0 && !isKeyBoard) {
            if (isBack) {
                socketLuMessageGameLuckyWheel?.leaveRoom(branchId, restaurantId, roomId)
                socketLuMessageGameLuckyWheel?.offSocket()
                setWakeLock(false)
                leaveRoom()
                true
            } else {
                dialogLeaveWhenStartingGame()
                false
            }
        } else {
            binding.commentGame.layoutKeyboard.onBackPressed()
            false
        }
    }

    private fun animateFlying(image: ImageView, type: Float) {
        val x: Float
//        x = nextInt(100, 1000).toFloat()
        x = (TechResApplication.widthView / type).toFloat()
//        val y: Float = (TechResApplication.heightView - nextInt(10, 100)).toFloat()
        val y = 0f
        val r = nextInt(2000, 4000).toFloat()
        val angle = 25f
        val path = Path()
        path.arcTo(RectF(x, y, x + 10 * r, y + r), 180f, angle)

        val objectAnimator = ObjectAnimator.ofFloat(image, View.Y, View.Y, path)
        objectAnimator.duration = 5000
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            objectAnimator,
            ObjectAnimator.ofFloat(image, "scaleX", 0.2f, 1f).setDuration(1500),
            ObjectAnimator.ofFloat(image, "scaleY", 0.2f, 1f).setDuration(1500),
            ObjectAnimator.ofFloat(image, "alpha", 0f, 1f).setDuration(500)
        )
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animatorSet.start()
            }
        })
        animatorSet.start()
    }

    private fun cloneImage(type: Float): ImageView? {
        val clone = ImageView(getApplicationContext())
        clone.layoutParams = binding.heartImage.getLayoutParams()
        clone.setImageDrawable(binding.heartImage.getDrawable())
        clone.x = (TechResApplication.widthView / type)
//        clone.y = (TechResApplication.heightView - nextInt(10, 150)).toFloat()
        clone.y = (TechResApplication.heightView - nextInt(50, 100)).toFloat()
        binding.cloneContainer.addView(clone)
        return clone
    }
}