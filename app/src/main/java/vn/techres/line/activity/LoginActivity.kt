package vn.techres.line.activity

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.main.ConnectNodeRequest
import vn.techres.line.data.model.notification.StatusNotifyResponse
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.UserResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityLoginBinding
import vn.techres.line.fragment.account.ForgotPasswordFragment
import vn.techres.line.fragment.account.RegistryFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.hideKeyboard
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.UnsupportedEncodingException
import java.util.concurrent.Executor


class LoginActivity : BaseBindingActivity<ActivityLoginBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityLoginBinding
        get() = ActivityLoginBinding::inflate

    private var internalStorage = InternalStorage()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var names: String = "1234567890"
    private var userName: String? = ""
    private var password: String? = ""

    lateinit var info: String
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onSetBodyView() {
        mSocket = application.getSocketInstance(this)
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        mSocket?.connect()

        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // only for OREO and newer versions
            disableAutoFill()
        }

        when (cacheManager.get(TechresEnum.KEY_LOGOUT.toString())) {
            "1" -> {
                setDataLogin()
            }
            "0" -> {
                getStatusNotify()
                val intent = Intent(this, MainActivity::class.java)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                startActivity(intent)
                binding.btnLogin.isEnabled = false
            }
            null -> {
                setDataLogin()
            }
        }
        binding.edtPhoneNumber.addTextChangedListener(textPhoneWatcher)
        binding.edtPassword.addTextChangedListener(textPassWatcher)

        binding.btnFinger.setOnClickListener {
            hideKeyboard(binding.edtPassword)
            checkDeviceHasBiometric()
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
//                    Toast.makeText(applicationContext,
//                        "Authentication error: $errString", Toast.LENGTH_SHORT)
//                        .show()
                    binding.edtPassword.requestFocus()
                    showKeyboard(binding.edtPassword)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
//                    Toast.makeText(applicationContext,
//                        "Authentication succeeded!", Toast.LENGTH_SHORT)
//                        .show()
                    onLogin(userName!!, password!!)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
//                    Toast.makeText(applicationContext, "Authentication failed",
//                        Toast.LENGTH_SHORT)
//                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(resources.getString(R.string.biometric_title))
            .setSubtitle(resources.getString(R.string.biometric_subtitle))
            .setNegativeButtonText(resources.getString(R.string.biometric_negative_button_text))
            .setConfirmationRequired(false)
            .build()
    }

    private fun checkDeviceHasBiometric() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                WriteLog.d("MY_APP_TAG", "App can authenticate using biometrics.")
                biometricPrompt.authenticate(promptInfo)

            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                WriteLog.d("MY_APP_TAG", "No biometric features available on this device.")
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                WriteLog.d("MY_APP_TAG", "Biometric features are currently unavailable.")
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }

                startActivityForResult(enrollIntent, 100)
            }
        }
    }

    private fun checkShowButtonFinger(s: String){
        if (userName == s){
            binding.btnFinger.visibility = View.VISIBLE
        }else{
            binding.btnFinger.visibility = View.GONE
        }
    }

    private fun checkButtonLogin() {
        if (binding.edtPhoneNumber.text.toString().length < 10  && binding.edtPassword.text.toString().isEmpty()) {
            binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_empty)
            binding.btnLogin.isEnabled = false
        } else {
            binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login)
            binding.btnLogin.isEnabled = true
        }
    }

    private val textPhoneWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun afterTextChanged(s: Editable) {
            checkShowButtonFinger(s.toString())
            checkButtonLogin()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }
    private val textPassWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun afterTextChanged(s: Editable) {
            checkButtonLogin()
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
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
                .add(R.id.frameLayout, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun disableAutoFill() {
        window?.decorView?.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }

    private fun setDataLogin() {
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        cacheManager.put(
            TechresEnum.GAME_ID.toString(),
            "0"
        )
        try {
            userName = internalStorage.readObject(
                this,
                TechresEnum.STORAGE_USER_PHONE.toString()
            ).toString()
            password = internalStorage.readObject(
                this,
                TechresEnum.STORAGE_USER_PASSWORD.toString()
            ).toString()

            if (userName != "kotlin.Unit" && password != "kotlin.Unit") {
                binding.edtPhoneNumber.text = Editable.Factory.getInstance().newEditable(userName)
            } else {
                binding.edtPhoneNumber.text = Editable.Factory.getInstance().newEditable("")
            }

            checkShowButtonFinger(binding.edtPhoneNumber.text.toString().trim())

        } catch (e: Exception) { }
        binding.btnLogin.setOnClickListener {
            hideKeyboard(binding.btnLogin)
            if (checkValid()) {
                onLogin(
                    (binding.edtPhoneNumber.text ?: "").toString(),
                    (binding.edtPassword.text ?: "").toString()
                )
            }
        }

        binding.txtRegister.setOnClickListener {
            hideKeyboard(binding.txtRegister)
            supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                add<RegistryFragment>(R.id.frameContainer)
                addToBackStack(RegistryFragment().tag)
            }
            binding.txtRegister.isEnabled = false
            Handler().postDelayed({ binding.txtRegister.isEnabled = true }, 2000)
        }
        binding.tvForgotPassword.setOnClickListener {
            hideKeyboard(binding.tvForgotPassword)
            supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                add<ForgotPasswordFragment>(R.id.frameContainer)
                addToBackStack(ForgotPasswordFragment().tag)
            }
            binding.tvForgotPassword.isEnabled = false
            Handler().postDelayed({ binding.tvForgotPassword.isEnabled = true }, 2000)
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

    private fun checkValid(): Boolean {
        if ((binding.edtPhoneNumber.text ?: "").toString().isEmpty()) {
            Utils.setSnackBar(
                binding.btnLogin,
                this.resources.getString(R.string.login_label_phone_must_not_be_empty)
            )
            return false
        }
        if (binding.edtPhoneNumber.text!!.length < 10) {
            Utils.setSnackBar(
                binding.btnLogin,
                "Số điện thoại không thể ít hơn 10 kí tự, vui lòng nhập lại"
            )
            return false
        }
        if ((binding.edtPassword.text ?: "").toString().isEmpty()) {
            Utils.setSnackBar(
                binding.btnLogin,
                resources.getString(R.string.login_label_password_must_not_be_empty)
            )
            return false
        }
        if ((binding.edtPassword.text ?: "").toString().length >= 50) {
            Utils.setSnackBar(
                binding.btnLogin,
                "Mật khẩu tối thiểu 4 đến 50 ký tự và phải có mã hóa"
            )
            return false
        }
        return true
    }

    private fun onLogin(userName: String, passwordLogin: String) {
        setLoading(true)
        val params = UserParams()
        params.http_method = AppConfig.POST
        params.request_url = "api/customers/login"
        params.project_id = AppConfig.PROJECT_OAUTH

        val data: ByteArray
        var base64 = ""
        val stringTest: String = passwordLogin
        try {
            data = passwordLogin.toByteArray(charset("UTF-8"))
            password = stringTest
            base64 = Base64.encodeToString(
                data,
                Base64.NO_WRAP or Base64.URL_SAFE
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        params.params.username = userName
        params.params.password = base64
        params.params.device_uid = Utils.generateID(this)
        params.params.os_name = "android"

        params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .login(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<UserResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: UserResponse) {

                        if (response.status == AppConfig.SUCCESS_CODE) {
                            user = response.data!!
                            user.nodeAccessToken = response.data!!.jwt_token
                            user.node_refresh_token = response.data!!.refresh_token

                            CurrentUser.saveUserInfo(this@LoginActivity, user)

                            closeKeyboard(binding.edtPhoneNumber)
                            closeKeyboard(binding.edtPassword)

                            try {
                                internalStorage.writeObject(
                                    this@LoginActivity,
                                    TechresEnum.STORAGE_USER_PHONE.toString(),
                                    userName
                                )
                                internalStorage.writeObject(
                                    this@LoginActivity,
                                    TechresEnum.STORAGE_USER_PASSWORD.toString(),
                                    passwordLogin
                                )
                                internalStorage.writeObject(
                                    this@LoginActivity,
                                    TechresEnum.STORAGE_USER_LOGIN_TYPE.toString(),
                                    0
                                )
                            } catch (e: Exception) { }

                           // pushTokenJava()
                           // createUserInformationNode()

                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                            finish()

                        } else {
                            Utils.setSnackBar(
                                binding.btnLogin,
                                response.message.toString()
                            )
                            setLoading(false)
                        }
                    }
                })
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
        createUserInformationNodeParams.params.device_uid = Utils.generateID(this)
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
                        if (response.status == AppConfig.SUCCESS_CODE){
                            pushTokenNode(user)
                            connectNodeJs(user)

                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                            finish()
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
            this,
            TechresEnum.PUSH_TOKEN.toString()
        ).toString()

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
                    override fun onNext(response: BaseResponse) {}
                })
        }
    }

    private fun pushTokenJava() {
        val pushTokenParams = PushTokenParams()
        pushTokenParams.http_method = AppConfig.POST
        pushTokenParams.request_url = "/api/register-customer-device"
        pushTokenParams.project_id = AppConfig.PROJECT_OAUTH
        pushTokenParams.params.device_uid = Utils.generateID(this)
        pushTokenParams.params.push_token = internalStorage.readObject(
            this,
            TechresEnum.PUSH_TOKEN.toString()
        ).toString()
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

    private fun getStatusNotify() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_ALOLINE
        baseRequest.request_url = "/api/customers/status-notification"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getStatusNotify(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<StatusNotifyResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: StatusNotifyResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val sharedPreference = PreferenceHelper(this@LoginActivity)
                        sharedPreference.save(
                            TechresEnum.NOTIFY_ALL.toString(),
                            response.data ?: 0
                        )
                    }
                }
            })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
            moveTaskToBack(true)
            overridePendingTransition(R.anim.picture_anim_modal_in, R.anim.picture_anim_modal_out)
        }
        super.onBackPressed()
    }
}