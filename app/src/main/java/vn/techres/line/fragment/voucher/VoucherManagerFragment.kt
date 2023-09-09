package vn.techres.line.fragment.voucher

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.voucher.VoucherManageAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentVoucherManagerBinding
import vn.techres.line.helper.utils.AlolineColorUtil

class VoucherManagerFragment : BaseBindingFragment<FragmentVoucherManagerBinding>(
    FragmentVoucherManagerBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: VoucherManageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =
            requireActivity().resources.getString(R.string.coupon_code)
        adapter = VoucherManageAdapter(childFragmentManager, requireActivity())
        binding.pagerVoucher.adapter = adapter
        binding.tabLayoutVoucher.setupWithViewPager(binding.pagerVoucher)
        binding.pagerVoucher.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutVoucher))
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