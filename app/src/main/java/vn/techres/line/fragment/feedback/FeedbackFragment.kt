package vn.techres.line.fragment.feedback

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentFeedbackBinding
import vn.techres.line.helper.utils.AlolineColorUtil

/**
 * A simple [Fragment] subclass.
 */
class FeedbackFragment : BaseBindingFragment<FragmentFeedbackBinding>(FragmentFeedbackBinding::inflate) {

    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.tvBackMenu.setOnClickListener {
            onBackPress()
        }
    }


    override fun onBackPress() : Boolean{
        return true
    }

}
