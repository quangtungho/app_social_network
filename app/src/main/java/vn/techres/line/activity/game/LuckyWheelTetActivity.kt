package vn.techres.line.activity.game

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.game.luckywheel.ListUserWinnerAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.LuckyItem
import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.data.model.game.luckywheel.response.ReactionLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.RoundLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.StopLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.TotalCustomerJoinRoomResponse
import vn.techres.line.data.model.params.UserWinnerParams
import vn.techres.line.data.model.response.game.UserWinner
import vn.techres.line.data.model.response.game.UserWinnerResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityLuckyWheelTetBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.socket.SocketLuckyWheelManager
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.socket.SocketLuckyWheelHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.text.SimpleDateFormat
import java.util.*

class LuckyWheelTetActivity : BaseBindingActivity<ActivityLuckyWheelTetBinding>(), SocketLuckyWheelHandler {
    private var isBack = true
    private var roomId = ""
    private var restaurantId = 0
    private var branchId = 0
    private var articleGameId = ""
    private var isCheckStopGame = 0
    private var priceNumber = 0
    private var data =  ArrayList<LuckyItem>()
    private var listUser = ArrayList<UserWinner>()
    private var listUserWinnerAdapter: ListUserWinnerAdapter? = null
    private var mPlayer: MediaPlayer? = null
    private var mPlayerStart: MediaPlayer? = null
    private var mPlayerPlay: MediaPlayer? = null
    //dialog
    private var dialog : Dialog? = null
    private var imgClose : ImageView? = null
    private var imgGift : ImageView? = null
    //
    private var socketLuckyWheelManager : SocketLuckyWheelManager? = null
    private var user = User()
    private var configNodeJs = ConfigNodeJs()
    override val bindingInflater: (LayoutInflater) -> ActivityLuckyWheelTetBinding
        get() = ActivityLuckyWheelTetBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        dialog = Dialog(this)
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
        listUserWinnerAdapter = ListUserWinnerAdapter(this)
        intent?.let {
            roomId = it.getStringExtra(TechresEnum.ROOM_ID.toString()).toString()
            restaurantId = restaurant().restaurant_id ?: 0
            branchId = it.getIntExtra(TechresEnum.BRANCH_ID.toString(), 0)
            articleGameId = it.getStringExtra(TechresEnum.ID_ARTICLE_GAME.toString()).toString()
        }
        socketLuckyWheelManager = SocketLuckyWheelManager(this)
        socketLuckyWheelManager?.setSocketLuckyWheelHandler(this)
        setData()
        val luckyItem1 = LuckyItem()
        luckyItem1.topText = "10.000 đồng"
        luckyItem1.icon = R.drawable.money_10
        luckyItem1.color = -0xc20
        data.add(luckyItem1)

        val luckyItem2 = LuckyItem()
        luckyItem2.topText = "20.000 đồng"
        luckyItem2.icon = R.drawable.money_20
        luckyItem2.color = -0x1f4e
        data.add(luckyItem2)

        val luckyItem3 = LuckyItem()
        luckyItem3.topText = "50.000 đồng"
        luckyItem3.icon = R.drawable.money_50
        luckyItem3.color = -0x3380
        data.add(luckyItem3)

        //////////////////

        //////////////////
        val luckyItem4 = LuckyItem()
        luckyItem4.topText = "100.000 đồng"
        luckyItem4.icon = R.drawable.money_100
        luckyItem4.color = -0xc20
        data.add(luckyItem4)

        val luckyItem5 = LuckyItem()
        luckyItem5.topText = "200.000 đồng"
        luckyItem5.icon = R.drawable.money_200
        luckyItem5.color = -0x1f4e
        data.add(luckyItem5)

        val luckyItem6 = LuckyItem()
        luckyItem6.topText = "500.000 đồng"
        luckyItem6.icon = R.drawable.money_500
        luckyItem6.color = -0x3380
        data.add(luckyItem6)

        binding.luckyWheel.setData(data)

        Glide.with(binding.imgBg).load(String.format("%s%s", configNodeJs.api_ads, "/public/icons/wheel-frame.gif"))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imgBg)

//        binding.luckyWheel.setLuckyRoundItemSelectedListener { index ->
//            Toast.makeText(
//                applicationContext,
//                data[index].topText,
//                Toast.LENGTH_SHORT
//            ).show()
//        }

        binding.imgListMemberWinner.setOnClickListener {
            showDiaWriteLogListGameWin()
        }

    }

    override fun onResume() {
        super.onResume()
        setMusicBackground()
    }

    override fun onPause() {
        super.onPause()
        if(socketLuckyWheelManager?.isConnectSocket() == false){
            socketLuckyWheelManager?.registrySocket(this)
        }
        mPlayer?.stop()
        mPlayerStart?.stop()
        mPlayerPlay?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(socketLuckyWheelManager?.isConnectSocket() == false){
            socketLuckyWheelManager?.registrySocket(this)
        }
        mPlayer?.stop()
        mPlayerStart?.stop()
        mPlayerPlay?.stop()
    }
    private fun setData(){
        socketLuckyWheelManager?.joinRoom(
            branchId,
            restaurantId,
            roomId,
            "customer",
            articleGameId
        )
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }
    private fun setPrice(number : Int) : Int{
        priceNumber = when(number){
            10 ->{
                0
            }
            20 ->{
               1
            }
            50 ->{
               2
            }
            100 ->{
               3
            }
            200 ->{
               4
            }
            500 ->{
               5
            }
            else ->{
                0
            }
        }
        return priceNumber
    }
    private fun setMusicBackground() {
        mPlayer = MediaPlayer.create(this, R.raw.soundlwo)
        mPlayer?.isLooping = true
        mPlayer?.start()
    }
    private fun setMusicGameStart() {
        mPlayerStart = MediaPlayer.create(this, R.raw.game_start)
        mPlayerStart?.start()
        mPlayerStart?.setOnCompletionListener {
            setMusicGamePlay()
        }
    }
    private fun setMusicGamePlay() {
        mPlayerPlay = MediaPlayer.create(this, R.raw.game_play)
        mPlayerPlay?.isLooping = true
        mPlayerPlay?.start()
    }

    private fun dialogGift(money: Int){
        val drawable = when(money){
            10 ->{
                R.drawable.money_10k
            }
            20 ->{
                R.drawable.money_20k
            }
            50 ->{
                R.drawable.money_50k
            }
            100 ->{
                R.drawable.money_100k
            }
            200 ->{
                R.drawable.money_200k
            }
            500 ->{
                R.drawable.money_500k
            }
            else ->{
                R.drawable.money_50k
            }
        }

        imgGift?.let {
            Glide.with(it)
                .load(drawable)
                .into(it)
        }

        imgClose?.setOnClickListener {
            if(mPlayer != null && mPlayer?.isPlaying == false){
                setMusicBackground()
            }
            dialog?.dismiss()
        }
        dialog?.show()
    }

    private fun showDiaWriteLogListGameWin() {
        getListGameWin()
        val diaWriteLogBuilder = AlertDialog.Builder(this)
        val layoutView: View = layoutInflater.inflate(R.layout.dialog_list_win, null)
        val diaWriteLogListWinner: RecyclerView = layoutView.findViewById(R.id.listWinner)
        val imgClose: ImageView = layoutView.findViewById(R.id.imgClose)
        val tvDate: TextView = layoutView.findViewById(R.id.tvDate)
        diaWriteLogListWinner.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        diaWriteLogListWinner.adapter = listUserWinnerAdapter
        diaWriteLogListWinner.smoothScrollToPosition(0)
        diaWriteLogBuilder.setView(layoutView)
        val alertDiaWriteLog = diaWriteLogBuilder.create()
        alertDiaWriteLog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val currentYear = SimpleDateFormat("yyyy",Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("MM",Locale.getDefault()).format(Date())
        val currentDay = SimpleDateFormat("dd",Locale.getDefault()).format(Date())
        tvDate.text = String.format("Ngày %s Tháng %s Năm %s", currentDay, currentMonth, currentYear)
        imgClose.setOnClickListener {
            alertDiaWriteLog.dismiss()
        }
        alertDiaWriteLog.show()
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
        ServiceFactory.createRetrofitServiceNode(
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
                    } else Toast.makeText(this@LuckyWheelTetActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }
    override fun onNumber(number: Int) {

    }

    override fun onStartLuckyWheel() {
        runOnUiThread {
            binding.luckyWheel.setRound(1000)
            binding.luckyWheel.startLuckyWheelWithTargetIndex(priceNumber)
            mPlayer?.stop()
            setMusicGameStart()
            isCheckStopGame = 1
            dialog?.dismiss()
            isBack = false
        }
    }

    override fun onStopLuckyWheel(stopLuckyWheelResponse: StopLuckyWheelResponse) {
        runOnUiThread {
            val handler = Handler(Looper.getMainLooper())
            binding.luckyWheel.startLuckyWheelWithTargetIndex(setPrice(stopLuckyWheelResponse.money ?: 10), 3)
//            dialogGift(stopLuckyWheelResponse.money ?: 10)
            mPlayerStart?.stop()
            handler.postDelayed({
                mPlayerPlay?.stop()
                if (isCheckStopGame == 1) {
                    if (stopLuckyWheelResponse.user_id == user.id) {
                        handler.postDelayed({
                            dialogGift(stopLuckyWheelResponse.money ?: 10)
                            isBack = true
                        }, 2000)
                    } else {
//                        setMusicGameLose()
                    }
                    isCheckStopGame = 0
//                    binding.luckyWheel.startLuckyWheelWithTargetIndex(setPrice(stopLuckyWheelResponse.money ?: 10), 0)
                }
            }, 5000)
        }
    }

    override fun onTotalMemberJoinRoom(totalCustomerJoinRoomResponse: TotalCustomerJoinRoomResponse) {
        
    }

    override fun onReaction(reactionLuckyWheelResponse: ReactionLuckyWheelResponse) {
        
    }

    override fun onGameRound(roundLuckyWheelResponse: RoundLuckyWheelResponse) {
        
    }

    override fun onChatText(messageGameLuckyWheel: MessageGameLuckyWheel) {
        
    }

    override fun onChatSticker(messageGameLuckyWheel: MessageGameLuckyWheel) {
        
    }

    override fun onNextMoney(money: Int) {
        runOnUiThread {
            setPrice(money)
        }
    }

    override fun onCurrentMoney(money: Int) {
        runOnUiThread {
            setPrice(money)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mPlayer?.stop()
        mPlayerStart?.stop()
        mPlayerPlay?.stop()
        socketLuckyWheelManager?.leaveRoom(branchId, restaurantId, roomId)
        socketLuckyWheelManager?.offSocket()
        finish()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}