package vn.techres.line.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import com.giphy.sdk.analytics.GiphyPingbacks.context
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityProductInformationBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

class ProductInformationActivity : BaseBindingActivity<ActivityProductInformationBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityProductInformationBinding
        get() = ActivityProductInformationBinding::inflate

    override fun onSetBodyView() {
        this.window?.statusBarColor =
            AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.product_information)
        binding.tvVersion.text = String.format(
            "%s %s",
            resources.getString(R.string.title_version),
            getVersionBuild()
        )
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvWebsite.setOnClickListener {
            val url = TechresEnum.LINK_WEB.toString()
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvSupport.setOnClickListener {
            val url = TechresEnum.LINK_WEB.toString()
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun getVersionBuild(): String? {
        val packageManager = packageManager
        val packageName = packageName
        val myVersionName: String? // initialize String
        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName
            return myVersionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "1.0.0"
    }


}