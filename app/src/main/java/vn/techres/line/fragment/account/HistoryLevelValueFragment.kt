package vn.techres.line.fragment.account

import android.os.Bundle
import android.view.View
import vn.techres.line.R
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentHistoryLevelValueBinding
import vn.techres.line.helper.utils.AlolineColorUtil

class HistoryLevelValueFragment : BaseBindingFragment<FragmentHistoryLevelValueBinding>(FragmentHistoryLevelValueBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =  requireActivity().getString(R.string.history_value)
    }

    override fun onBackPress() : Boolean{
        return true
    }

}