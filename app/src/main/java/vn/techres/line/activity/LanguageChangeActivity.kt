package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import com.giphy.sdk.analytics.GiphyPingbacks.context
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityLanguageChangeBinding
import vn.techres.line.helper.utils.AlolineColorUtil
import java.util.*

class LanguageChangeActivity : BaseBindingActivity<ActivityLanguageChangeBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_change)
    }

    override val bindingInflater: (LayoutInflater) -> ActivityLanguageChangeBinding
        get() = ActivityLanguageChangeBinding::inflate

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = this.getString(R.string.language)
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
    }


    @SuppressLint("UseRequireInsteadOfGet")
    private fun changeLanguage(language: String) {
        val locale = Locale(language)
        val configuration = Configuration()
        configuration.locale = locale
        context.resources
            .updateConfiguration(configuration, context.resources.displayMetrics)
    }

}