package vn.techres.line.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.data.model.notification.StatusNotifyResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.ConfigParams
import vn.techres.line.data.model.params.ConfigsNodeJsParams
import vn.techres.line.data.model.response.ConfigNodeJsResponse
import vn.techres.line.data.model.response.ConfigResponse
import vn.techres.line.data.model.version.Version
import vn.techres.line.data.model.version.response.VersionResponse
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getVersionBuild
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SplashScreenActivity : AppCompatActivity() {
    val mainActivity = MainActivity()
    private var count = 0
    val cacheManager: CacheManager = CacheManager.getInstance()
    //Data notify
    private var typeNotify = 0
    private var valueNotify = ""
    private var jsonGroupNotify = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val info = packageManager?.getPackageInfo(
                "vn.techres.line",
                PackageManager.GET_SIGNATURES
            )
            if (info != null) {
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

        intent?.let {
            typeNotify = it.getIntExtra("type", 0)
            valueNotify = it.getStringExtra("value")?:""
            jsonGroupNotify = it.getStringExtra(TechresEnum.GROUP_CHAT.toString())?:""
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            getConfigNodeJs()
            getConfigs()
        }, 200)
    }

    private fun getConfigNodeJs() {
        val configsNodeJsParams = ConfigsNodeJsParams()
        configsNodeJsParams.http_method = AppConfig.GET
        configsNodeJsParams.project_id = AppConfig.PROJECT_OAUTH_NODE
        configsNodeJsParams.request_url = "/api/oauth-configs-nodejs/get-configs"
        configsNodeJsParams.params.secret_key = TechresEnum.SECRET_KEY.toString()
        configsNodeJsParams.params.type = getString(R.string.type_chat_demo)

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
                    setNavigator()
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ConfigNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        CurrentConfigNodeJs.saveConfigNodeJs(
                            this@SplashScreenActivity,
                            response.data
                        )
                        setNavigator()
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
                    setNavigator()
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ConfigResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        CurrentConfigJava.saveConfigJava(this@SplashScreenActivity, response.data)
                        setNavigator()
                    }
                }
            })
    }

    private fun setNavigator() {
        if (count == 0) {
            count++
        } else {
            checkUpdate()
        }
    }

    private fun navigatorHome() {
        val prefManager = PrefManager(this)
        if (CurrentUser.isLogin(this)) {
            getStatusNotify()

            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            intent.putExtra("type", typeNotify)
            intent.putExtra("value", valueNotify)
            intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroupNotify)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            if (!prefManager.isFirstTimeLaunch) {
                val intent = Intent(this, LoginActivity::class.java)
                    .apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(
                    R.anim.translate_from_right,
                    R.anim.translate_to_right
                )
            }
        }
        finish()
    }

    private fun checkUpdate() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/check-version?os_name=aloline_android"
        baseRequest.project_id = AppConfig.PROJECT_OAUTH
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )

            .checkUpdate(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VersionResponse> {
                override fun onComplete() {

                }

                override fun onError(e: Throwable) {
                    WriteLog.e("TAG", e.message.toString())
                    navigatorHome()
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onNext(versionResponse: VersionResponse) {
                    if (versionResponse.status == AppConfig.SUCCESS_CODE) {
                        if (getVersionBuild(this@SplashScreenActivity) != versionResponse.data.version &&
                            versionResponse.data.is_require_update == TechresEnum.STATUS_ACTIVE.toString()
                                .toInt()
                        ) {
                            checkVersion(versionResponse.data)
                        } else {
                            navigatorHome()
                        }
                    } else {
                        navigatorHome()
                    }
                }
            })
    }

    private fun checkVersion(version: Version) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_check_version)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.attributes = lp
        val btnUpdateVersion = dialog.findViewById(R.id.btnUpdateVersion) as Button
        val btnCloseCheckVersion = dialog.findViewById(R.id.btnCloseCheckVersion) as Button
        val tvDescriptionVersionNew = dialog.findViewById(R.id.tvDescriptionVersionNew) as TextView
        tvDescriptionVersionNew.text = version.message ?: ""
        btnCloseCheckVersion.setOnClickListener {
            if (version.is_require_update == 1)
                navigatorHome()
            dialog.dismiss()
        }
        btnUpdateVersion.setOnClickListener {
            val url =
                String.format("%s%s", "https://play.google.com/store/apps/details?id=", packageName)
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.show()
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
                        val sharedPreference = PreferenceHelper(this@SplashScreenActivity)
                        sharedPreference.save(TechresEnum.NOTIFY_ALL.toString(), response.data ?: 0)
                    }
                }
            })
    }

}