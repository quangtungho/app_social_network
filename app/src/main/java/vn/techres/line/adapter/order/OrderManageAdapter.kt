package vn.techres.line.adapter.order

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vn.techres.line.R
import vn.techres.line.fragment.order.OrderCustomerComFirmedFragment
import vn.techres.line.fragment.order.OrdersCustomerFragment

@SuppressLint("WrongConstant")
class   OrderManageAdapter(
    fr: FragmentManager?,
    private val context: Context
) : FragmentPagerAdapter(fr!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragment: Fragment? = null
    private val orderCustomerFragment = OrdersCustomerFragment()
    private val orderCustomerConfirmedFragment = OrderCustomerComFirmedFragment()
    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return context.resources.getString(R.string.title_order)
            1 -> return context.resources.getString(R.string.title_order_com_firmed)
        }
        return ""
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(i: Int): Fragment {
        fragment = when (i) {
            0 -> orderCustomerFragment
            1 -> orderCustomerConfirmedFragment
            else -> orderCustomerFragment
        }
        return fragment!!
    }
}