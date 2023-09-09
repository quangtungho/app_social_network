package vn.techres.line.adapter.voucher

import android.content.Context

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vn.techres.line.R
import vn.techres.line.fragment.voucher.VoucherNotUsedFragment
import vn.techres.line.fragment.voucher.VoucherUsedFragment

class VoucherManageAdapter(
    fr: FragmentManager?,
    private val context: Context
) : FragmentPagerAdapter(fr!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragment: Fragment? = null
    private val voucherNotUsedFragment = VoucherNotUsedFragment()
    private val voucherUsedFragment = VoucherUsedFragment()
    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return context.resources.getString(R.string.not_used)
            1 -> return context.resources.getString(R.string.voucher_used)
        }
        return ""
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(i: Int): Fragment {
        fragment = when (i) {
            0 -> voucherNotUsedFragment
            1 -> voucherUsedFragment
            else -> voucherNotUsedFragment
        }
        return fragment!!
    }
}