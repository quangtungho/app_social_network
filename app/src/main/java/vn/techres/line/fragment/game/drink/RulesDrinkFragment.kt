package vn.techres.line.fragment.game.drink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentRulesDrinkBinding
import vn.techres.line.helper.techresenum.TechresEnum

class RulesDrinkFragment : BaseBindingFragment<FragmentRulesDrinkBinding>(
    FragmentRulesDrinkBinding::inflate
) {
    fun newInstance(isCheck: Int): RulesDrinkFragment {
        val args = Bundle()
        args.putString(TechresEnum.DRINK_GAME.toString(), Gson().toJson(isCheck))
        val fragment = RulesDrinkFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onBackPress(): Boolean {
        return true
    }
}