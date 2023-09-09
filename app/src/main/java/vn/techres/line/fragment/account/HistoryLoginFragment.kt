package vn.techres.line.fragment.account

import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentHistoryLoginBinding

class HistoryLoginFragment : BaseBindingFragment<FragmentHistoryLoginBinding>(FragmentHistoryLoginBinding::inflate){
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    fun setDataSource(){

    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }
    override fun onBackPress(): Boolean {
        return true
    }

}