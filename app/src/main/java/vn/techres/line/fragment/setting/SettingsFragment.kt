package vn.techres.line.fragment.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.main.DisconnectNodeRequest
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.NotifyAppParams
import vn.techres.line.data.model.params.PushTokenNodeParams
import vn.techres.line.data.model.params.PushTokenParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.stranger.PermissionStranger
import vn.techres.line.data.model.stranger.response.PermissionStrangerResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentSettingsBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SettingsFragment :
    BaseBindingFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var internalStorage = InternalStorage()
    private var user = User()
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var nodeJs = ConfigNodeJs()
    private var configJava = ConfigJava()
    private var sharedPreference: PreferenceHelper? = null
    private var default: Int? = 0
    private var tokenNodeJs = ""
    private var permissionStranger: PermissionStranger? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity())
        user = CurrentUser.getCurrentUser(requireActivity())
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
        tokenNodeJs = user.nodeAccessToken
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.setting)
        sharedPreference = PreferenceHelper(requireActivity())
        default = sharedPreference?.getValueInt(TechresEnum.NOTIFY_ALL.toString())
        binding.switchButtonNotify.isChecked = default == 1
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(
            TechresEnum.ALO_LINE.toString(),
            Context.MODE_PRIVATE
        )
        sharedPreferences.getInt(TechresEnum.NOTIFY_ALL.toString(), 0)
        getPermissionStranger()
        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.lnChangePassword.setOnClickListener {
            val intent = Intent(mainActivity, ChangePasswordActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        binding.lnDisableAccount.setOnClickListener {
            val intent = Intent(mainActivity, DisableAccountActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        binding.btnLogout.setOnClickListener {
            pushTokenNode(user)
            pushTokenJava()
        }
        binding.edtLanguages.setOnClickListener {
            val intent = Intent(mainActivity, LanguageChangeActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        binding.switchButtonNotify.setOnCheckedChangeListener { _, isCheckedDefault ->
            default = when (isCheckedDefault) {
                true -> {
                    turnNotify()
                    1
                }
                false -> {
                    turnNotify()
                    0
                }
            }
        }
        binding.lnSettingBubbleChat.setOnClickListener {
            val intent = Intent(mainActivity, TurnOnBubbleChatActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        binding.lnMessageStranger.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(
                TechresEnum.PERMISSION_STRANGER_CALL_PHONE.toString(),
                permissionStranger?.is_call_phone ?: 0
            )
            bundle.putInt(
                TechresEnum.PERMISSION_STRANGER_CALL_VIDEO.toString(),
                permissionStranger?.is_call_video ?: 0
            )
            val permissionStrangerFragment = PermissionStrangerFragment()
            permissionStrangerFragment.arguments = bundle
            mainActivity?.addOnceFragment(this, PermissionStrangerFragment())
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun logoutToken() {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            TechResApplication.applicationContext().getContactDao()?.deleteAllContact()
            TechResApplication.applicationContext().getFriendDao()?.deleteAllData()
            TechResApplication.applicationContext().getQrCodeDao()?.deleteAllQrCode()
        }
        try {
            internalStorage.writeObject(
                context!!,
                TechresEnum.STORAGE_USER_PHONE.toString(),
                CurrentUser.getCurrentUser(mainActivity!!.baseContext).phone
            )
//            internalStorage.writeObject(
//                context!!,
//                TechresEnum.STORAGE_USER_PASSWORD.toString(),
//                ""
//            )
            internalStorage.writeObject(
                context!!,
                TechresEnum.STORAGE_USER_LOGIN_TYPE.toString(),
                1
            )
        } catch (e: Exception) {
        }

        setDataLogout()

    }

    private fun getPermissionStranger() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/user/permission-call"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getPermissionStranger(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PermissionStrangerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: PermissionStrangerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        permissionStranger = response.data
                    }
                }
            })
    }

    private fun setDataLogout() {
        disconnectNodeJs()
        CurrentUser.saveUserInfo(requireActivity(), User())
        cacheManager.clear()
        saveRestaurantInfo(RestaurantCard())
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
        mainActivity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        mainActivity?.finish()
    }

    private fun turnNotify() {
        val params = NotifyAppParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customers/turn-notification"
        params.params.chat_token = user.nodeAccessToken
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .setTurnNotify(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun disconnectNodeJs() {
        val disconnectNodeRequest = DisconnectNodeRequest()
//        disconnectNodeRequest.avatar = user.avatar_three_image.original
//        disconnectNodeRequest.full_name = user.name
        disconnectNodeRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(disconnectNodeRequest))
            mSocket?.emit(TechResEnumChat.CLIENT_DISCONNECTION_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onBackPress(): Boolean {
        sharedPreference?.save(TechresEnum.NOTIFY_ALL.toString(), default ?: 0)
        return true
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
                        logoutToken()
                    }
                })
        }
    }

    private fun pushTokenJava() {
        val pushTokenParams = PushTokenParams()
        pushTokenParams.http_method = AppConfig.POST
        pushTokenParams.request_url = "/api/register-customer-device"


        pushTokenParams.project_id = AppConfig.PROJECT_OAUTH
        pushTokenParams.params.device_uid = Utils.generateID(mainActivity)
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
}