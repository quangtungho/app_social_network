package vn.techres.line.fragment.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentOrderSuccessBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

class OrderSuccessFragment : BaseBindingFragment<FragmentOrderSuccessBinding>(FragmentOrderSuccessBinding::inflate){
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)

        arguments?.let {
            binding.tvIDOrderCustomer.text = it.getInt(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString()).toString()
        }

        binding.tvBackMenu.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

    }

    override fun onBackPress() : Boolean {
        return true
    }

}