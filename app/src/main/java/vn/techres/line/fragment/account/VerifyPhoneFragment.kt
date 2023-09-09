package vn.techres.line.fragment.account

import `in`.aabhasjindal.otptextview.OTPListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Base64
import android.view.View
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.LoginActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.main.ConnectNodeRequest
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.VerifyResetPasswordResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.EventBusSMS
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentVerifyPhoneBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.UnsupportedEncodingException


/**
 * A simple [Fragment] subclass.
 */
class VerifyPhoneFragment :
    BaseBindingFragment<FragmentVerifyPhoneBinding>(FragmentVerifyPhoneBinding::inflate) {

    private val loginActivity: LoginActivity?
        get() = activity as LoginActivity?

    private var phoneCustomer = ""
    private var verifyCode = ""
    private var counter = 30

    private var newPassword: String? = ""
    private var passwordOne: String? = ""
    private var passwordTwo: String? = ""
    private var correct = 0
    private var internalStorage = InternalStorage()

    //socket
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var user = User()
    private var dataProfile = User()
    private var configJava = ConfigJava()

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
        setCountDownVerification()

        user = CurrentUser.getCurrentUser(requireActivity())
        mSocket = application.getSocketInstance(requireActivity())
        mSocket?.connect()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        arguments?.let {
            phoneCustomer = it.getString(TechresEnum.CUSTOMER_PHONE.toString(), "")
        }

//        binding.btnResendCode.setOnClickListener {
//            resentCode(phoneCustomer)
//        }

        binding.btnVerifyCode.setOnClickListener {
            Utils.hideKeyboard(binding.btnVerifyCode)
            binding.btnVerifyCode.isEnabled = false
            Handler().postDelayed({ binding.btnVerifyCode.isEnabled = true }, 3000)
            verifyCode()
        }
        binding.head.toolbarBack.setOnClickListener {
            Utils.hideKeyboard(binding.btnVerifyCode)
            loginActivity?.supportFragmentManager?.popBackStack()
        }
        binding.head.toolbarTitle.text = "NHẬP MÃ OTP"
        binding.btnResendCode.setOnClickListener {
            binding.btnResendCode.hide()
            resentCode(phoneCustomer)
            binding.tvCountDown.show()
            setCountDownVerification()
        }


        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {
                // fired when user types something in the Otpbox
            }

            override fun onOTPComplete(otp: String) {
                // fired when user has entered the OTP fully.
                verifyCode = otp

//                verifyCode()

            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    /**
     * Event Bus
     * Note : Sync data message sms
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncDataMessageEnd(event: EventBusSMS) {
        if (!StringUtils.isNullOrEmpty(event.message)) {
            binding.otpView.setOTP(event.message ?: "")
        }
    }

    private fun setCountDownVerification() {
        val timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountDown.text = String.format(
                    "%s %s%s",
                    "Mã của bạn sẽ được gửi trong",
                    counter.toString(),
                    "s"
                )
                counter--
            }

            override fun onFinish() {
                counter = 30
                binding.tvCountDown.hide()
                binding.btnResendCode.show()
            }
        }
        timer.start()
    }

    private fun resentCode(phoneNumber: String) {
        val params = UserParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customers/send-verify-code"
        params.project_id = AppConfig.PROJECT_OAUTH
        params.params.phone = phoneNumber
        params.let {
            ServiceFactory.run {
                createRetrofitService(

                    TechResService::class.java
                )
                    .resentCode(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<BaseResponse> {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {
                            WriteLog.d("ERROR", e.message.toString())
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(response: BaseResponse) {
                            WriteLog.d("Resent Code: ", response.message + "")
                        }

                    })
            }
        }
    }

    private fun checkPasswordCorrect() {
        passwordOne = binding.edtSetPasswordNew.text.toString()
        passwordTwo = binding.edtRetypePasswordNew.text.toString()
        if (passwordOne == passwordTwo && passwordOne!!.length >= 4 && !verifyCode.equals("")) {
            newPassword = passwordOne
            correct = 1
        } else if (passwordOne?.length!! < 4 || passwordTwo?.length!! < 4) {
            correct = 2
        } else if (passwordOne?.length!! >= 50 || passwordTwo?.length!! >= 50) {
            correct = 3
        } else if (verifyCode != "") {
            correct = 4
        } else {
            correct = 0
        }
    }


    private fun verifyCode() {
        checkPasswordCorrect()
        if (correct == 1) {
            pushTokenJava(user)
        } else if (correct == 2) {
            Utils.setSnackBar(
                binding.btnVerifyCode,
                "Mật khẩu tối thiểu 4 đến 50 ký tự và phải có mã hóa"
            )
        } else if (correct == 3) {
            Utils.setSnackBar(
                binding.btnVerifyCode,
                requireActivity().resources.getString(R.string.password_cannot_than_50_character)
            )
        } else if (correct != 4) {
            Utils.setSnackBar(
                binding.btnVerifyCode, "Bạn chưa nhập mã xác nhận"
            )
        } else if (binding.edtSetPasswordNew.text.toString() != binding.edtRetypePasswordNew.text.toString()) {
            Utils.setSnackBar(
                binding.btnVerifyCode, "Mật khẩu nhập lại không đúng"
            )
        } else if (binding.edtRetypePasswordNew.text.toString() != binding.edtRetypePasswordNew.text.toString()) {
            Utils.setSnackBar(
                binding.btnVerifyCode, "Mật khẩu nhập lại không đúng"
            )
        } else {
            Utils.setSnackBar(
                binding.btnVerifyCode,
                requireActivity().resources.getString(R.string.password_not_correct)
            )
        }
    }

    //Call api verify opt from server
    private fun setPasswordForgot() {
        loginActivity?.setLoading(true)
        val reVerifyCodePhoneParams = ReVerifyCodePhoneParams()
        reVerifyCodePhoneParams.http_method = AppConfig.POST
        reVerifyCodePhoneParams.request_url = "/api/customers/verify-change-password"
        reVerifyCodePhoneParams.project_id = AppConfig.PROJECT_OAUTH
        reVerifyCodePhoneParams.params.user_uid = ""
        val data: ByteArray
        var base64 = ""
        try {
            data = newPassword!!.toByteArray(charset("UTF-8"))
            base64 = Base64.encodeToString(
                data,
                Base64.NO_WRAP or Base64.URL_SAFE
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        reVerifyCodePhoneParams.params.new_password = base64
        reVerifyCodePhoneParams.params.verify_code = verifyCode
        reVerifyCodePhoneParams.params.phone = phoneCustomer.removePrefix("+84")
        reVerifyCodePhoneParams.params.let {
            CurrentConfigJava.getConfigJava(requireActivity()).api_domain.toString().let {
                ServiceFactory.createRetrofitService(

                    TechResService::class.java
                )
                    .verifyResetPassword(reVerifyCodePhoneParams)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<VerifyResetPasswordResponse> {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {
                            WriteLog.d("ERROR", e.message.toString())
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(response: VerifyResetPasswordResponse) {
                            loginActivity?.setLoading(false)
                            if (response.status == AppConfig.SUCCESS_CODE) {
                                user = response.data!!
                                user.nodeAccessToken = response.data!!.jwt_token
                                user.node_refresh_token = response.data!!.refresh_token
                                loginActivity?.let {
                                    CurrentUser.saveUserInfo(it, user)
                                }

                                try {
                                    internalStorage.writeObject(
                                        context!!,
                                        TechresEnum.STORAGE_USER_INFO.toString(),
                                        user
                                    )
                                    internalStorage.writeObject(
                                        context!!,
                                        TechresEnum.STORAGE_USER_PASSWORD.toString(),
                                        passwordOne
                                    )
                                    internalStorage.writeObject(
                                        context!!,
                                        TechresEnum.STORAGE_USER_LOGIN_TYPE.toString(),
                                        0
                                    )
                                } catch (e: Exception) {
                                    e.message.toString()
                                }

                                createUserInformationNode()


                            } else {
                                Utils.setSnackBar(
                                    binding.btnVerifyCode, response.message.toString()
                                )
                            }

                        }
                    })
            }
        }
    }

    private fun connectNodeJs(user: User?) {
        val connectNodeRequest = ConnectNodeRequest()
        connectNodeRequest.member_id = user?.id
        try {
            val jsonObject = JSONObject(Gson().toJson(connectNodeRequest))
            mSocket?.emit(TechResEnumChat.CLIENT_CONNECTION_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createUserInformationNode() {
        val createUserInformationNodeParams = CreateUserInformationNodeParams()
        createUserInformationNodeParams.http_method = AppConfig.POST
        createUserInformationNodeParams.project_id = AppConfig.PROJECT_OAUTH_NODE
        createUserInformationNodeParams.request_url = "/api/v2/create-customer"

        createUserInformationNodeParams.params.os_name = "android"
        createUserInformationNodeParams.params.user_id = user.id
        createUserInformationNodeParams.params.device_name = Utils.getDeviceName()
        createUserInformationNodeParams.params.device_uid = Utils.generateID(loginActivity)
        createUserInformationNodeParams.params.ip_address = Utils.getIPAddress(true)

        createUserInformationNodeParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createUserInformationNode(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            pushTokenNode(user)
                            connectNodeJs(user)

                            val intent = Intent(loginActivity, MainActivity::class.java)
                            startActivity(intent)
                            loginActivity?.overridePendingTransition(
                                R.anim.fade_in,
                                R.anim.fade_out
                            )
                        }
                    }
                })
        }
    }

    private fun pushTokenNode(user: User?) {
        val pushTokenNodeParams = PushTokenNodeParams()
        pushTokenNodeParams.http_method = AppConfig.POST
        pushTokenNodeParams.project_id = AppConfig.getProjectChat()
        pushTokenNodeParams.request_url = "/api/push-token"
        pushTokenNodeParams.params.push_token = internalStorage.readObject(
            loginActivity!!,
            TechresEnum.PUSH_TOKEN.toString()
        ).toString()
        pushTokenNodeParams.params.device_uid = Utils.generateID(requireActivity())
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
                    override fun onNext(response: BaseResponse) {}
                })
        }
    }

    private fun pushTokenJava(user: User?) {
        val pushTokenParams = PushTokenParams()
        pushTokenParams.http_method = AppConfig.POST
        pushTokenParams.request_url = "/api/register-customer-device"
        pushTokenParams.project_id = AppConfig.PROJECT_OAUTH
        pushTokenParams.params.device_uid = Utils.generateID(requireActivity())
        pushTokenParams.params.push_token = internalStorage.readObject(
            loginActivity!!,
            TechresEnum.PUSH_TOKEN.toString()
        ).toString()
        pushTokenParams.params.device_name = Utils.getDeviceName()
        pushTokenParams.params.os_name = "android"
        pushTokenParams.params.customer_id = user?.id ?: 0
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
                            setPasswordForgot()
                        }

                    })
            }
        }

    }


    override fun onBackPress(): Boolean {
        return true
    }
}

