package vn.techres.line.adapter.booking

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vn.techres.line.R
import vn.techres.line.fragment.booking.ProcessedBookingFragment
import vn.techres.line.fragment.booking.ProcessingBookingFragment

class BookingInformationManageAdapter (
    fr: FragmentManager?,
    private val context: Context) : FragmentStatePagerAdapter(fr!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var fragment: Fragment? = null
    private val processingBookingFragment = ProcessingBookingFragment()
    private val processedBookingFragment  = ProcessedBookingFragment()
    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return context.resources.getString(R.string.processing)
            1 -> return context.resources.getString(R.string.processed)
        }
        return ""
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(i: Int): Fragment {
        fragment = when (i) {
            0 -> processingBookingFragment
            1 -> processedBookingFragment
            else -> processingBookingFragment
        }
        return fragment!!
    }

}