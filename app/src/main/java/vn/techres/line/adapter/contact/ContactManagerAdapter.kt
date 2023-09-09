package vn.techres.line.adapter.contact

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.techres.line.fragment.chat.ContactDeviceChatFragment
import vn.techres.line.fragment.contact.RecentCallsFragment

class ContactManagerAdapter(fr: FragmentActivity) :  FragmentStateAdapter(fr) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ContactDeviceChatFragment()
            }
            1 -> {
                RecentCallsFragment()
            }
            else -> {
                ContactDeviceChatFragment()
            }
        }
    }


}