package vn.techres.line.adapter.chat

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.techres.line.fragment.chat.vote.VotedFragment
import vn.techres.line.data.model.chat.Sender

class DetailVotedAdapter(val fragment : Fragment, var dataSource : ArrayList<Sender>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun createFragment(position: Int): Fragment {
        return VotedFragment().newInstance(dataSource)
    }
}


