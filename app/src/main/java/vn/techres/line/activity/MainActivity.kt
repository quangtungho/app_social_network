package vn.techres.line.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.seismic.ShakeDetector
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import me.piruin.quickaction.ActionItem
import me.piruin.quickaction.QuickAction
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.broadcast.ContactContentObserver
import vn.techres.line.broadcast.NetworkChangeReceiver
import vn.techres.line.data.model.account.InformationLogin
import vn.techres.line.data.model.account.InformationLoginResponse
import vn.techres.line.data.model.chat.data.MessageUpdateData
import vn.techres.line.data.model.eventbus.EventBusBusyCall
import vn.techres.line.data.model.main.ConnectNodeRequest
import vn.techres.line.data.model.main.DisconnectNodeRequest
import vn.techres.line.data.model.notification.EventBusCloseCall
import vn.techres.line.data.model.notification.MessageStatusCall
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.PushTokenNodeParams
import vn.techres.line.data.model.params.PushTokenParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityMainBinding
import vn.techres.line.databinding.DialogClockAccountBinding
import vn.techres.line.fragment.card.RestaurantCardManageFragment
import vn.techres.line.fragment.chat.GroupChatFragment
import vn.techres.line.fragment.delivery.DeliveryOrderFragment
import vn.techres.line.fragment.friend.ContactsFragment
import vn.techres.line.fragment.game.luckywheel.LuckyWheelGameFragment
import vn.techres.line.fragment.main.AccountFragment
import vn.techres.line.fragment.main.HomeFragment
import vn.techres.line.fragment.main.MainFragment
import vn.techres.line.fragment.newsfeed.NewsFeedFragment
import vn.techres.line.fragment.qr.CodeBarFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.permissionsIsGranted
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ConnectionType
import vn.techres.line.helper.utils.NetworkMonitorUtil
import vn.techres.line.interfaces.OnMenuMoreClick
import vn.techres.line.interfaces.OnPostClick
import vn.techres.line.interfaces.OnUpdateProfileClick
import vn.techres.line.interfaces.util.EventBusConnectionInternet
import vn.techres.line.interfaces.util.OnBackHome
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.videocall.SocketCallOnDataEnum

open class MainActivity : BaseBindingActivity<ActivityMainBinding>(), View.OnClickListener,
    QuickAction.OnActionItemClickListener, ShakeDetector.Listener, LifecycleHandler.Listener {
    private var onPostClick: OnPostClick? = null
    private var onMenuMoreClick: OnMenuMoreClick? = null

    private var onUpdateProfileClick: OnUpdateProfileClick? = null
    private var onBackHome: OnBackHome? = null
    private var quickAction: QuickAction? = null
    private val idEdit = 1
    private var isCheckConnect = true

    private var mNetworkReceiver: BroadcastReceiver? = null


    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )

    //socket
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var mSocketLogout: Socket? = null
    private var init = false
    private var numberCheckConnectionInternet = 0

    //Data notify
    private var typeNotify = 0
    private var valueNotify = ""
    private var jsonGroupNotify = ""

    companion object {
        lateinit var mainActivity: MainActivity
        var isConnection = false
    }

    private val networkMonitor = NetworkMonitorUtil(this)

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNetworkReceiver = NetworkChangeReceiver()
        registerNetworkBroadcastForNougat()
        networkMonitor.result = { isAvailable, type ->
            runOnUiThread {
                when (isAvailable) {
                    true -> {
                        when (type) {
                            ConnectionType.Wifi -> {
                                if (!isConnection) {
                                    if (numberCheckConnectionInternet > 0) {
                                        showSnackbar(
                                            binding.lnMainHeader,
                                            "Đã có kết nối mạng",
                                            true
                                        )
                                        EventBus.getDefault().post(EventBusConnectionInternet(true))
                                    } else if (numberCheckConnectionInternet == 0)
                                        EventBus.getDefault().post(EventBusConnectionInternet(true))
                                }
                                isConnection = true

                            }
                            ConnectionType.Cellular -> {
                                if (!isConnection) {
                                    if (numberCheckConnectionInternet > 0) {
                                        showSnackbar(
                                            binding.lnMainHeader,
                                            "Đã có kết nối mạng",
                                            true
                                        )
                                        EventBus.getDefault().post(EventBusConnectionInternet(true))
                                    } else if (numberCheckConnectionInternet == 0)
                                        EventBus.getDefault().post(EventBusConnectionInternet(true))
                                }
                                isConnection = true
                            }
                            else -> {}
                        }
                    }
                    false -> {
                        if (numberCheckConnectionInternet > 0) {
                            if (isConnection) {
                                isConnection = false
                                showSnackbar(binding.lnMainHeader, "Không có kết nối mạng", false)
                                EventBus.getDefault().post(EventBusConnectionInternet(false))
                            }
                        } else {
                            isConnection = false
                            showSnackbar(binding.lnMainHeader, "Không có kết nối mạng", false)
                            EventBus.getDefault().post(EventBusConnectionInternet(false))
                        }
                    }
                }
                numberCheckConnectionInternet++
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (CurrentUser.isLogin(this) && mSocket != null && init) {
            connectNodeJs()
        }
        networkMonitor.register()
    }

    override fun onSetBodyView() {
        mainActivity = this
        mSocketLogout?.connect()
        Utils.saveCacheManagerMainActivity()

        intent?.let {
            typeNotify = it.getIntExtra("type", 0)
            valueNotify = it.getStringExtra("value") ?: ""
            jsonGroupNotify = it.getStringExtra(TechresEnum.GROUP_CHAT.toString()) ?: ""
        }

        handleNotification()

        setupQuickAction()
        LifecycleHandler.instance.addListener(this)
        quickAction?.setOnActionItemClickListener(this)
        quickAction?.setOnActionItemClickListener(this)
        cacheManager.put(TechresEnum.LOCK_SOCKET.toString(), TechresEnum.LOCK_SOCKET.toString())
        cacheManager.put(
            TechresEnum.CHECK_VERSION_APP.toString(),
            TechresEnum.CHECK_VERSION_APP.toString()
        )
        if (CurrentUser.isLogin(this)) {
            getLastUserLogin()
            if (permissionsIsGranted(this, requiredPermissions)) {
                val contentObserver = ContactContentObserver()
                applicationContext.contentResolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI,
                    true,
                    contentObserver
                )
            }
            mSocket = application.getSocketInstance(this)
            mSocketLogout = application.getSocketLogoutInstance(this)
            Handler(Looper.getMainLooper()).postDelayed({
                connectNodeJs()
            }, 1500)
            mSocket?.on(
                String.format(
                    "%s/%s",
                    TechResEnumChat.RES_LIST_MESSAGE_WHEN_USER_ONLINE.toString(),
                    CurrentUser.getCurrentUser(mainActivity).id
                )
            ) { args ->
                mainActivity.runOnUiThread {
                    WriteLog.d("res_list_message", args[0].toString())
                    val data =
                        Gson().fromJson<MessageUpdateData>(args[0].toString(), object :
                            TypeToken<MessageUpdateData>() {}.type)
                    WriteLog.d("data size ", "${data.list_message_offline.size}")
                    data.list_message_offline.forEach {
                        if (it.type_message == 1) {
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataReaction(
                                            it.group_id,
                                            it.random_key,
                                            it.reactions,
                                            CurrentUser.getCurrentUser(applicationContext).id
                                        )
                                }
                        } else if (it.type_message == 2) {
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
//                    TechResApplication.applicationContext().getMessageDao()?.revokeMessageByGroup(group_id,random_key, CurrentUser.getCurrentUser(applicationContext).id)
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataRevoke(
                                            it.group_id,
                                            it.random_key,
                                            0,
                                            CurrentUser.getCurrentUser(applicationContext).id,
                                            ArrayList(),
                                            TechResEnumChat.TYPE_REVOKE.toString()
                                        )
                                }
                        } else {
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
                                    it.message_reply.status = 0
//                    TechResApplication.applicationContext().getMessageDao()?.revokeMessageByGroup(group_id,random_key, CurrentUser.getCurrentUser(applicationContext).id)
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataRevokeReply(
                                            it.group_id,
                                            it.random_key,
                                            CurrentUser.getCurrentUser(applicationContext).id,
                                            it.message_reply
                                        )
                                }
                        }
                    }
                }
            }

            mSocket?.on(SocketCallOnDataEnum.RES_BUSY_USER()) { args ->
                WriteLog.d("RES_BUSY_USER", args[0].toString())
                val data = Gson().fromJson<MessageStatusCall>(args[0].toString(), object :
                    TypeToken<MessageStatusCall>() {}.type)
                mainActivity.runOnUiThread {
                    EventBus.getDefault().post(EventBusBusyCall(data.message))
                }
            }

            mSocket?.on(SocketCallOnDataEnum.RES_CLOSE_CALL()) { args ->
                WriteLog.d("RES_CLOSE_CALL", args[0].toString())
                val data = Gson().fromJson<MessageStatusCall>(args[0].toString(), object :
                    TypeToken<MessageStatusCall>() {}.type)
                mainActivity.runOnUiThread {
                    EventBus.getDefault().post(EventBusCloseCall(data.message))
                }
            }

            if ((restaurant().restaurant_id ?: 0) > 0) {
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<MainFragment>(R.id.frameContainer)
                }
            } else {
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<RestaurantCardManageFragment>(R.id.frameContainer)
                }
            }
            if (mSocketLogout?.connected() != true)
                WriteLog.i("đã kết nối socket chặn thiết bị lạ : ", "false")
            else
                WriteLog.i("đã kết nối socket chặn thiết bị lạ  : ", "true")
            mSocketLogout?.on(
                String.format(
                    "%s/%s",
                    TechResEnumChat.RES_LOG_OUT_ALOLINE.toString(),
                    CurrentUser.getCurrentUser(mainActivity).id
                )
            ) { args ->
                mainActivity.runOnUiThread {
                    WriteLog.d("RES_LOG_OUT_ALOLINE", args[0].toString())
                    val informationLogin =
                        Gson().fromJson<InformationLogin>(args[0].toString(), object :
                            TypeToken<InformationLogin>() {}.type)
                    if (!informationLogin.device_uid.equals(Utils.getIDDevice())) {
                        showDialogWarringLogin(informationLogin)
                    }
                }
            }
        }
        PrefUtils.getInstance().putString(TechresEnum.LIST_MESSAGE_UPDATE_FOREGROUND.toString(), "")
        init = true
    }

    private fun handleNotification() {
        when (typeNotify) {
            //Switch screen profile
            NotificationEnum.CONTACT.toString().toInt(),
            NotificationEnum.UPDATE_INFO.toString().toInt() -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.ID_USER.toString(), valueNotify)
                startActivity(intent)
            }

            //Switch screen detail post
            NotificationEnum.COMMENTS.toString().toInt(),
            NotificationEnum.REPLY_COMMENT.toString().toInt(),
            NotificationEnum.CREATE_BRANCH_REVIEW.toString().toInt(),
            NotificationEnum.REACH_POINT_ALOLINE.toString().toInt(),
            NotificationEnum.REACTIONS.toString().toInt(),
            NotificationEnum.REACTIONS_COMMENT.toString().toInt() -> {
                val intent = Intent(this, CommentActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.ID_POST.toString(), valueNotify)
                intent.putExtra(
                    TechresEnum.CHECK_COMMENT_CHOOSE.toString(),
                    TechresEnum.VALUE_COMMENT_NOTIFY.toString()
                )
                intent.putExtra(
                    TechresEnum.DETAIL_POST_COMMENT.toString(),
                    Gson().toJson(PostReview())
                )
                startActivity(intent)
            }

            //Switch screen point
            NotificationEnum.POINT.toString().toInt() -> {
                intent = Intent(this, PointCardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.ID_USER.toString(), valueNotify)
                startActivity(intent)
            }

            //Switch screen order
            NotificationEnum.ORDER.toString().toInt() -> {
                intent = Intent(this, BillActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), valueNotify.toInt())
                startActivity(intent)
            }

            //Switch screen advert
            NotificationEnum.ADVERT.toString().toInt() -> {
                intent = Intent(this, AdvertPackageActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.ID_USER.toString(), valueNotify)
                startActivity(intent)
            }

            //Switch screen booking
            NotificationEnum.BOOKING.toString().toInt() -> {
                intent = Intent(this, DetailBookingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.BOOKING.toString(), valueNotify)
            }

            //Switch screen chat
            NotificationEnum.CHAT.toString().toInt() -> {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroupNotify)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

    }

    private fun showSnackbar(view: View?, message: String?, isConnection: Boolean) {
        // Create snackbar
        val snackbar: Snackbar = if (!isConnection)
            Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(R.color.gray_snackbar))
                .setTextColor(resources.getColor(R.color.white))
        else
            Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(R.color.green_snackbar))
                .setTextColor(resources.getColor(R.color.white))
        val view: View = snackbar.view
        val params: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackbar.show()
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.rlLoading.show()
        } else if (!isLoading) {
            run {
                binding.loading.rlLoading.hide()
            }
        }
    }

    fun setOnMenuMoreClick(onMenuMoreClick: OnMenuMoreClick) {
        this.onMenuMoreClick = onMenuMoreClick
    }

    private fun setupQuickAction() {
        val acEdit = ActionItem(idEdit, "Sửa bài đánh giá", R.drawable.ic_edit)
        //val acCancel = ActionItem(ID_CANCEL, "Xóa bài đánh giá ", R.drawable.ic_cancel)
        quickAction = QuickAction(this, QuickAction.VERTICAL)
        quickAction!!.addActionItem(acEdit)
        //quickAction!!.addActionItem(acCancel)
    }

    fun setOnBackHome(onBackHome: OnBackHome) {
        this.onBackHome = onBackHome
    }

    private fun disconnectNodeJs() {
        val user = CurrentUser.getCurrentUser(this)
        val disconnectNodeRequest = DisconnectNodeRequest()
//        disconnectNodeRequest.avatar = user.avatar_three_image.original
//        disconnectNodeRequest.full_name = user.name
        disconnectNodeRequest.member_id = user.id
        isCheckConnect = true
        try {
            val jsonObject = JSONObject(Gson().toJson(disconnectNodeRequest))
            WriteLog.d("CLIENT_DISCONNECTION_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CLIENT_DISCONNECTION_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun connectNodeJs() {
        val user = CurrentUser.getCurrentUser(this)
        val connectNodeRequest = ConnectNodeRequest()
//        connectNodeRequest.avatar = user.avatar_three_image.original
//        connectNodeRequest.full_name = user.name
        connectNodeRequest.member_id = user.id
        isCheckConnect = false
        try {
            val jsonObject = JSONObject(Gson().toJson(connectNodeRequest))
            mSocket?.emit(TechResEnumChat.CLIENT_CONNECTION_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CLIENT_CONNECTION_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun addOnceFragment(fromFragment: Fragment, toFragment: Fragment) {
        val isInBackStack =
            supportFragmentManager.findFragmentByTag(toFragment.javaClass.simpleName)
        if (isInBackStack == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                .add(R.id.frameContainer, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    fun clearBackstack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            val entry: FragmentManager.BackStackEntry = supportFragmentManager.getBackStackEntryAt(
                0
            )
            supportFragmentManager.popBackStack(
                entry.id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            supportFragmentManager.executePendingTransactions()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imgBackChat -> onBackPressed()
            R.id.btnBack -> onBackPressed()
            R.id.btnPost -> onPostClick?.onPost()
            R.id.btnAdd -> onPostClick?.onPost()
            R.id.btnMenuMore -> quickAction?.show(view)
            R.id.btnUpdateProfile -> onUpdateProfileClick?.onUpdateProfile()
        }
    }

    override fun onItemClick(item: ActionItem?) {
        val idClick = item!!.actionId
        onMenuMoreClick?.onMenuMore(idClick)
    }

    override fun onBackPressed() {
        if (onBackClickFragments.size > 0) {
            if (onBackClickFragments[onBackClickFragments.size - 1].onBack()) {
                when (onBackClickFragments[onBackClickFragments.size - 1]) {
                    is HomeFragment, is ContactsFragment, is GroupChatFragment, is AccountFragment, is NewsFeedFragment -> {
                        onBackHome?.onBackHome()
                    }
                    else -> {
                        when (supportFragmentManager.fragments[supportFragmentManager.backStackEntryCount - 1]) {
                            is DeliveryOrderFragment -> {
                                onBackClickFragments[onBackClickFragments.size - 1].onBack()
                            }
                            is LuckyWheelGameFragment -> {
                                onBackClickFragments[onBackClickFragments.size - 1].onBack()
                            }
                            else -> {
                                onBackClickFragments[onBackClickFragments.size - 1].onBack()
                                super.onBackPressed()
                            }
                        }
                    }
                }
            } else {
                moveTaskToBack(true)
                overridePendingTransition(
                    R.anim.picture_anim_modal_in,
                    R.anim.picture_anim_modal_out
                )
            }
            removeOnBackClick(onBackClickFragments[onBackClickFragments.size - 1])
        } else {
            super.onBackPressed()
        }
    }

    override fun hearShake() {
        if (CurrentUser.isLogin(this) && !cacheManager.get(TechresEnum.MAIN_FRAGMENT.toString())
                .isNullOrBlank()
        ) {
            val isInBackStack =
                supportFragmentManager.findFragmentByTag(CodeBarFragment().javaClass.simpleName)
            if (isInBackStack != null) {
                PrefUtils.getInstance().putInt(TechresEnum.CALLBACK_TYPE.name, 1)
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<CodeBarFragment>(
                        R.id.frameContainer,
                        CodeBarFragment().javaClass.simpleName
                    )
                    addToBackStack(CodeBarFragment().javaClass.simpleName)
                }
            }
        }
    }

    override fun onBecameForeground(activity: Activity) {
//        if (CurrentUser.isLogin(this) && mSocket != null && !mainActivity.isFinishing) {
//            connectNodeJs()
//        }
    }

    override fun onBecameBackground(activity: Activity) {
//        if (CurrentUser.isLogin(this) && mSocket != null && !mainActivity.isFinishing) {
//            disconnectNodeJs()
//        }
    }

    override fun onDestroy() {
        if (CurrentUser.isLogin(this) && mSocket != null) {
            disconnectNodeJs()
        }
        networkMonitor.unregister()
        unregisterNetworkChanges()
        super.onDestroy()
    }

    private fun showDialogWarringLogin(informationLogin: InformationLogin) {
        val dialog = Dialog(mainActivity, R.style.DialogCustomTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val binding: DialogClockAccountBinding = DialogClockAccountBinding.inflate(layoutInflater)
        dialog.setContentView(binding.getRoot())
        val text =
            getString(R.string.account_logged) + " " + "<font color=\"#ffa233\">" + informationLogin.device_name + "</font>" + " " + getString(
                R.string.at
            ) + " " + "<font color=\"#ffa233\">" + informationLogin.last_login_time + "</font>" + " " + getString(
                R.string.with_ip_
            ) + " " + "<font color=\"#ffa233\">" + informationLogin.ip_address + "</font>"
        binding.titleWarring.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE)
        binding.btnCancel.setOnClickListener {
            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                TechResApplication.applicationContext().getContactDao()?.deleteAllContact()
            }
            var user = CurrentUser.getCurrentUser(mainActivity)
            pushTokenNode(user)
            pushTokenJava(user)
            dialog.dismiss()
            CurrentUser.saveUserInfo(mainActivity, User())
            cacheManager.clear()
            disconnectNodeJs()
            saveRestaurantInfo(mainActivity, RestaurantCard())
            if (user.id == 0) {
                cacheManager.put(
                    TechresEnum.KEY_LOGOUT.toString(), "0"
                )
            } else {
                cacheManager.put(
                    TechresEnum.KEY_LOGOUT.toString(), "1"
                )
            }
            val intent = Intent(mainActivity, LoginActivity::class.java)
            startActivity(intent)
            mainActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            mainActivity.finish()
        }
        binding.btnLockAccount.setOnClickListener {
            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                TechResApplication.applicationContext().getContactDao()?.deleteAllContact()
                TechResApplication.applicationContext().getFriendDao()?.deleteAllData()
                TechResApplication.applicationContext().getQrCodeDao()?.deleteAllQrCode()
            }
            var user = CurrentUser.getCurrentUser(mainActivity)
            pushTokenNode(user)
            pushTokenJava(user)
            dialog.dismiss()
            CurrentUser.saveUserInfo(mainActivity, User())
            cacheManager.clear()
            disconnectNodeJs()
            saveRestaurantInfo(mainActivity, RestaurantCard())
            if (user.id == 0) {
                cacheManager.put(
                    TechresEnum.KEY_LOGOUT.toString(), "0"
                )
            } else {
                cacheManager.put(
                    TechresEnum.KEY_LOGOUT.toString(), "1"
                )
            }
            val intent = Intent(mainActivity, LoginActivity::class.java)
            startActivity(intent)
            mainActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            mainActivity.finish()
        }
        dialog.show()
    }

    private fun pushTokenNode(user: User?) {
        val pushTokenNodeParams = PushTokenNodeParams()
        pushTokenNodeParams.http_method = AppConfig.POST
        pushTokenNodeParams.project_id = AppConfig.getProjectChat()
        pushTokenNodeParams.request_url = "/api/push-token"
        pushTokenNodeParams.params.push_token = ""
        pushTokenNodeParams.params.device_uid = Utils.getIDDevice()
        pushTokenNodeParams.params.os_name = "android"
        pushTokenNodeParams.params.customer_id = user?.id ?: 0
        pushTokenNodeParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .pushToken(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                    }
                })
        }
    }

    private fun pushTokenJava(user: User) {
        val pushTokenParams = PushTokenParams()
        pushTokenParams.http_method = AppConfig.POST
        pushTokenParams.request_url = "/api/register-customer-device"


        pushTokenParams.project_id = AppConfig.PROJECT_OAUTH
        pushTokenParams.params.device_uid = Utils.generateID(this)
        pushTokenParams.params.push_token = ""
        pushTokenParams.params.device_name = Utils.getDeviceName()
        pushTokenParams.params.os_name = "android"
        pushTokenParams.params.customer_id = user.id
        pushTokenParams.let {
            ServiceFactory.run {
                createRetrofitService(

                    TechResService::class.java
                )
                    .sendPushToken(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<BaseResponse> {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {
                            WriteLog.d("ERROR", e.message.toString())
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(response: BaseResponse) {
                        }

                    })
            }
        }
    }

    private fun getLastUserLogin() {
        val params = BaseParams()
        params.project_id = AppConfig.PROJECT_OAUTH_NODE
        params.request_url =
            String.format(
                "api/oauth-login-nodejs/info-last-login-aloline?user_id=%s&os_name=android",
                CurrentUser.getCurrentUser(mainActivity).id
            )
        ServiceFactory.run {
            createRetrofitServiceNode(
                TechResService::class.java
            )
                .getLastUserLogin(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<InformationLoginResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(data: InformationLoginResponse) {
                        if (!data.data.device_uid.equals(Utils.getIDDevice()) && !data.data.device_uid.equals(
                                ""
                            ) && data.data.device_uid != null
                        ) {
                            showDialogWarringLogin(data.data)
                        }
                    }

                })
        }

    }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    private fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

}