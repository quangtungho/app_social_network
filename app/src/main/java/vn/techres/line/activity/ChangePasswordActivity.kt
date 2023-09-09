package vn.techres.line.activity

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.ConfigNodeJsResponse
import vn.techres.line.data.model.response.ConfigResponse
import vn.techres.line.data.model.response.UserResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityChangePasswordBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.UnsupportedEncodingException

class ChangePasswordActivity : BaseBindingActivity<ActivityChangePasswordBinding>() {
    private var correct: Int = 0
    private var configJava = ConfigJava()
    private var user = User()
    private var internalStorage = InternalStorage()
    override val bindingInflater: (LayoutInflater) -> ActivityChangePasswordBinding
        get() = ActivityChangePasswordBinding::inflate

    override fun onSetBodyView() {
        this.window?.statusBarColor =
            AlolineColorUtil(context).convertColor(R.color.white)
        configJava = CurrentConfigJava.getConfigJava(this@ChangePasswordActivity)
        user = CurrentUser.getCurrentUser(this@ChangePasswordActivity)

        binding.edtPassword.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.toString() == binding.edtConfirmPassword.text.toString() && s.toString().length >= 4 && binding.edtConfirmPassword.text!!.length >= 4) {
                    binding.btnConfirmPasswordTemp.visibility = View.GONE
                    binding.btnConfirmPassword.visibility = View.VISIBLE
                } else {
                    binding.btnConfirmPasswordTemp.visibility = View.VISIBLE
                    binding.btnConfirmPassword.visibility = View.GONE
                }
            }
        })

        binding.edtConfirmPassword.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (binding.edtPassword.text.toString() == s.toString() && binding.edtPassword.text!!.length >= 4 && s.toString().length >= 4) {
                    binding.btnConfirmPasswordTemp.visibility = View.GONE
                    binding.btnConfirmPassword.visibility = View.VISIBLE
                } else {
                    binding.btnConfirmPasswordTemp.visibility = View.VISIBLE
                    binding.btnConfirmPassword.visibility = View.GONE
                }
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnConfirmPassword.setOnClickListener {
            if (binding.edtOldPassword.text.toString().isEmpty()) {
                Utils.setSnackBar(
                    binding.btnConfirmPassword,
                    resources.getString(R.string.password_not)
                )
            } else if (binding.edtOldPassword.text.toString().length >= 50) {
                Utils.setSnackBar(
                    binding.btnConfirmPassword,
                    resources.getString(R.string.password_cannot_than_50_character)
                )
            } else {
                checkPasswordCorrect()
                val lengthPassword = binding.edtPassword.text.toString().length
                if (binding.edtPassword.text.toString() == binding.edtConfirmPassword.text.toString()) {
                    when {
                        lengthPassword < 4 -> {
                            Utils.setSnackBar(
                                binding.btnConfirmPassword,
                                resources.getString(R.string.password_not_four_char)
                            )
                        }
                        lengthPassword > 50 -> {
                            Utils.setSnackBar(
                                binding.btnConfirmPassword,
                                resources.getString(R.string.password_cannot_than_50_character)
                            )
                        }
                        lengthPassword == 0 -> {
                            Utils.setSnackBar(
                                binding.btnConfirmPassword,
                                resources.getString(R.string.password_not)
                            )
                        }
                        else -> {
                            setPassword()
                        }
                    }
                } else {
                    Utils.setSnackBar(
                        binding.btnConfirmPassword,
                        resources.getString(R.string.check_password_new)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            disableAutofill()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun disableAutofill() {
        window?.decorView?.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }

    private fun checkPasswordCorrect() {
        correct =
            if (binding.edtPassword.text.toString() == binding.edtConfirmPassword.text.toString()) {
                1
            } else {
                0
            }
    }

    private fun setPassword() {
        val changedPasswordParams = ChangedPasswordParams()
        changedPasswordParams.http_method = AppConfig.POST
        changedPasswordParams.request_url =
            String.format("%s%s%s", "/api/customers/", user.id, "/change-password")
        changedPasswordParams.params.new_password =
            Utils.base64(binding.edtPassword.text.toString())
        changedPasswordParams.params.password = Utils.base64(binding.edtOldPassword.text.toString())
        changedPasswordParams.params.node_access_token = user.nodeAccessToken
        context.let { CurrentUser.getAccessTokenNodeJs(it) }
        changedPasswordParams.let(fun(it: ChangedPasswordParams) {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .changedPassword(
                    it
                )

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }


                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        when (response.status) {
                            AppConfig.SUCCESS_CODE -> {
                                FancyToast.makeText(
                                    this@ChangePasswordActivity,
                                    this@ChangePasswordActivity.getString(R.string.change_password_success),
                                    FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                    false
                                ).show()
                                getConfigNodeJs()
                            }
                            AppConfig.INTERNAL_SERVER_ERROR -> {
                                Utils.setSnackBar(
                                    binding.btnConfirmPassword,
                                    this@ChangePasswordActivity.getString(R.string.api_error)
                                )
                            }
                            else -> {
                                Utils.setSnackBar(
                                    binding.btnConfirmPassword,
                                    response.message.toString()
                                )
                            }
                        }
                    }
                })
        })
    }

    private fun getConfigNodeJs() {
        val configsNodeJsParams = ConfigsNodeJsParams()
        configsNodeJsParams.http_method = AppConfig.GET
        configsNodeJsParams.project_id = AppConfig.PROJECT_OAUTH_NODE
        configsNodeJsParams.request_url = "/api/oauth-configs-nodejs/get-configs"
        configsNodeJsParams.params.secret_key = TechresEnum.SECRET_KEY.toString()
        configsNodeJsParams.params.type =
            this.baseContext?.getString(R.string.type_chat_demo) ?: "demo"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getConfigsNodeJs(

                configsNodeJsParams
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ConfigNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ConfigNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {

                        CurrentConfigNodeJs.saveConfigNodeJs(
                            TechResApplication.applicationContext(),
                            response.data
                        )
                        getConfigs()
                    }
                }
            })
    }

    private fun getConfigs() {
        val params = ConfigParams()
        params.request_url = "/api/configs"
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_OAUTH
        params.params.project_id = AppConfig.project_id

        ServiceFactory.createRetrofitService(

            TechResService::class.java
        )

            .getConfig(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ConfigResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ConfigResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        CurrentUser.saveUserInfo(TechResApplication.applicationContext(), null)
                        CurrentConfigJava.saveConfigJava(
                            TechResApplication.applicationContext(),
                            response.data
                        )
                        onLogin(user.phone, (binding.edtPassword.text ?: "").toString())
                    }
//                    else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun onLogin(userName: String, passwordLogin: String) {
        val params = UserParams()
        params.http_method = AppConfig.POST
        params.request_url = "api/customers/login"
        params.project_id = AppConfig.PROJECT_OAUTH

        val data: ByteArray
        var base64: String? = ""
        try {
            data = passwordLogin.toByteArray(charset("UTF-8"))
            base64 = Base64.encodeToString(
                data,
                Base64.NO_WRAP or Base64.URL_SAFE
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        params.params.username = userName
        params.params.password = base64.toString()
        params.params.device_uid = Utils.generateID(this@ChangePasswordActivity)
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

                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: UserResponse) {

                        if (response.status == AppConfig.SUCCESS_CODE) {
                            user = response.data!!
                            user.nodeAccessToken = response.data!!.jwt_token
                            user.node_refresh_token = response.data!!.refresh_token

                            CurrentUser.saveUserInfo(this@ChangePasswordActivity, user)

                            closeKeyboard(binding.edtOldPassword)
                            closeKeyboard(binding.edtConfirmPassword)
                            closeKeyboard(binding.edtPassword)

                            try {
                                internalStorage.writeObject(
                                    this@ChangePasswordActivity,
                                    TechresEnum.STORAGE_USER_PHONE.toString(),
                                    userName
                                )
                                internalStorage.writeObject(
                                    this@ChangePasswordActivity,
                                    TechresEnum.STORAGE_USER_PASSWORD.toString(),
                                    passwordLogin
                                )
                                internalStorage.writeObject(
                                    this@ChangePasswordActivity,
                                    TechresEnum.STORAGE_USER_LOGIN_TYPE.toString(),
                                    0
                                )
                            } catch (e: Exception) {
                                e.message.toString()
                            }

                            createUserInformationNode()

                        } else {

                            Utils.setSnackBar(
                                binding.btnConfirmPassword,
                                response.message.toString()
                            )
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
                            pushTokenJava()
                            pushTokenNode(user)
                        }
                    }
                })
        }
    }

    private fun pushTokenJava() {

        val pushTokenParams = PushTokenParams()
        pushTokenParams.http_method = AppConfig.POST
        pushTokenParams.request_url = "/api/register-customer-device"
        pushTokenParams.project_id = AppConfig.PROJECT_OAUTH
        pushTokenParams.params.device_uid = Utils.generateID(this@ChangePasswordActivity)
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

    private fun pushTokenNode(user: User) {

        val pushTokenNodeParams = PushTokenNodeParams()
        pushTokenNodeParams.http_method = AppConfig.POST
        pushTokenNodeParams.project_id = AppConfig.PROJECT_CHAT
        pushTokenNodeParams.request_url = "/api/push-token"
        pushTokenNodeParams.params.push_token = internalStorage.readObject(
            this,
            TechresEnum.PUSH_TOKEN.toString()
        ).toString()
        pushTokenNodeParams.params.device_uid = Utils.generateID(this@ChangePasswordActivity)
        pushTokenNodeParams.params.os_name = "android"
        pushTokenNodeParams.params.customer_id = user.id
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
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            onBackPressed()
                        }
                    }
                })
        }
    }
}