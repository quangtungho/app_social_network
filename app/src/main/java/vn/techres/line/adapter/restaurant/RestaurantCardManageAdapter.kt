package vn.techres.line.adapter.restaurant

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vn.techres.line.R
import vn.techres.line.fragment.card.RestaurantCardFragment
import vn.techres.line.fragment.card.RestaurantMembershipRegisterFragment

@SuppressLint("WrongConstant")
class RestaurantCardManageAdapter (fr: FragmentManager?, private val context: Context) : FragmentPagerAdapter(fr!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragment: Fragment? = null
    private val restaurantCardFragment = RestaurantCardFragment()
    private val restaurantRegisterMemberCardFragment = RestaurantMembershipRegisterFragment()
    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return context.resources.getString(R.string.member_card)
            1 -> return context.resources.getString(R.string.restaurant)
        }
        return ""
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(i: Int): Fragment {
        fragment = when (i) {
            0 -> restaurantCardFragment
            1 -> restaurantRegisterMemberCardFragment
            else -> restaurantCardFragment
        }
        return fragment!!
    }
}
