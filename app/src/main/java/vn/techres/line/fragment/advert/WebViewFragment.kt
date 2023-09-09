package vn.techres.line.fragment.advert

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import vn.techres.line.R
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentWebViewBinding
import vn.techres.line.helper.utils.AlolineColorUtil

@SuppressLint("UseRequireInsteadOfGet")
class WebViewFragment : BaseBindingFragment<FragmentWebViewBinding>(FragmentWebViewBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.webViewFragment.loadUrl("https://github.com/")
    }
    override fun onBackPress() : Boolean{
        return true
    }

}