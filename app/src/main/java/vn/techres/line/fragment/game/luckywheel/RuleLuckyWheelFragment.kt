package vn.techres.line.fragment.game.luckywheel

import android.os.Bundle
import android.view.View
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentRuleLuckyWheelBinding

class RuleLuckyWheelFragment : BaseBindingFragment<FragmentRuleLuckyWheelBinding>(FragmentRuleLuckyWheelBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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