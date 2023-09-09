package vn.techres.line.fragment.newsfeed


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import vn.techres.line.R
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentDetailNewfeedBinding
import vn.techres.line.helper.utils.AlolineColorUtil


/**
 * A simple [Fragment] subclass.
 */
@Suppress("DEPRECATED_IDENTITY_EQUALS", "DEPRECATION")
class DetailNewsfeedFragment :  BaseBindingFragment<FragmentDetailNewfeedBinding>(
    FragmentDetailNewfeedBinding::inflate){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
    }
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onBackPress() : Boolean{
        return true
    }
}
