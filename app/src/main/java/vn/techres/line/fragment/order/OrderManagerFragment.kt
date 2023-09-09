package vn.techres.line.fragment.order


import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.order.OrderManageAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentOrderManagerBinding
import vn.techres.line.helper.utils.AlolineColorUtil

class OrderManagerFragment : BaseBindingFragment<FragmentOrderManagerBinding>(FragmentOrderManagerBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: OrderManageAdapter? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =
            requireActivity().resources.getString(R.string.header_order)
        adapter = OrderManageAdapter(childFragmentManager, requireActivity())
        binding.pagerMember.adapter = adapter
        binding.tabLayoutMember.setupWithViewPager(binding.pagerMember)
        binding.pagerMember.addOnPageChangeListener(TabLayoutOnPageChangeListener(binding.tabLayoutMember))
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
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
    
    override fun onBackPress() : Boolean {
        return true
    }
}
