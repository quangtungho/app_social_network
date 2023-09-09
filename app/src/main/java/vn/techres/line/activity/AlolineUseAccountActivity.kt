package vn.techres.line.activity

import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityAlolineUseAccountBinding
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.setClick
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.privacyprolicy.WebAppInterface
import vn.techres.line.helper.techresenum.TechresEnum

class AlolineUseAccountActivity : BaseBindingActivity<ActivityAlolineUseAccountBinding>() {
    private var isBack = true
    private var urlWebView = ""
    override val bindingInflater: (LayoutInflater) -> ActivityAlolineUseAccountBinding
        get() = ActivityAlolineUseAccountBinding::inflate

    override fun onSetBodyView() {
        urlWebView = TechresEnum.ALOLINE_USE_ACCOUNT.toString()
//        intent?.let {
//            val checkLink = it.getIntExtra(TechresEnum.WEBVIEW_TYPE.toString(), 0)
//            if (checkLink == 0) {
//                urlWebView = TechresEnum.PRIVACY_POLICY_URL_PLUS.toString()
//                binding.txtTitleHomeHeader.text = this.resources.getText(R.string.privacy_policy)
//            }else{
//                urlWebView = TechresEnum.PRIVACY_POLICY_URL.toString()
//                binding.txtTitleHomeHeader.text = this.resources.getText(R.string.terms_use)
//            }
//        }


        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.wvAlolineUseAccount.apply {
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if(isBack){
                        binding.progressBar.progress = newProgress
                        if (newProgress == 100) {
                            binding.progressBar.hide()
                            binding.tvLoading.hide()
                        } else {
                            binding.tvLoading.show()
                            binding.progressBar.show()
                        }
                    }
                }
            }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setAppCacheEnabled(false)
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
            }

            addJavascriptInterface(
                WebAppInterface(this@AlolineUseAccountActivity),
                "AndroidShareHandler"
            )

            loadUrl(urlWebView)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && binding.wvAlolineUseAccount.canGoBack()) {
                    binding.wvAlolineUseAccount.goBack()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }

        binding.imgShare.setOnClickListener {
            binding.imgShare.setClick()
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "Application Link : ${TechresEnum.ALOLINE_USE_ACCOUNT}"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        isBack = false
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}