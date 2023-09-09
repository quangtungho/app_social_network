package vn.techres.line.adapter.account

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import vn.techres.line.fragment.chat.GroupChatFragment
import vn.techres.line.fragment.friend.ContactsFragment
import vn.techres.line.fragment.main.AccountFragment
import vn.techres.line.fragment.main.HomeFragment
import vn.techres.line.fragment.newsfeed.NewsFeedFragment


@SuppressLint("WrongConstant")
class MainAdapter(fr: FragmentManager) : FragmentStatePagerAdapter(
    fr,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private val homeFragment = HomeFragment()
    private val contactsFragment = ContactsFragment()
    private val newsFeedFragment = NewsFeedFragment()
    private val groupChatFragment = GroupChatFragment()
    private val accountFragment = AccountFragment()

    override fun getCount(): Int {
        return 5
    }

    override fun getItem(i: Int): Fragment {
        return when(i){
            0 -> homeFragment
            1 -> contactsFragment
            2 -> newsFeedFragment
            3 -> groupChatFragment
            4 -> accountFragment
            else -> newsFeedFragment
        }
    }
}
