package vn.techres.line.fragment.setting

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentProductInfomationBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil


/**
 * A simple [Fragment] subclass.
 */
class ProductInformationFragment : BaseBindingFragment<FragmentProductInfomationBinding>(FragmentProductInfomationBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.product_information)
        binding.tvVersion.text = String.format("%s %s", requireActivity().resources.getString(R.string.title_version), getVersionBuild())
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.tvWebsite.setOnClickListener {
            val url = TechresEnum.LINK_WEB.toString()
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this.context, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvSupport.setOnClickListener {
            val url = TechresEnum.LINK_WEB.toString()
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this.context, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }

    }
    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun getVersionBuild(): String? {
        val packageManager = mainActivity!!.packageManager
        val packageName = mainActivity!!.packageName
        val myVersionName: String? // initialize String
        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName
            return myVersionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "1.0.0"
    }

    override fun onBackPress() : Boolean {
        return true
    }


}
