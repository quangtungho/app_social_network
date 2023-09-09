package vn.techres.line.fragment.paymem

import android.os.Bundle
import android.view.View
import vn.techres.line.R
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentPayMemMethodBinding
import vn.techres.line.helper.utils.AlolineColorUtil

class PayMemMethodFragment : BaseBindingFragment<FragmentPayMemMethodBinding>(FragmentPayMemMethodBinding::inflate){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
    }

    override fun onBackPress() : Boolean{
        return true
    }

}