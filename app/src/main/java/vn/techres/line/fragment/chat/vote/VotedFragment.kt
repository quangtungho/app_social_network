package vn.techres.line.fragment.chat.vote

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import vn.techres.line.adapter.chat.vote.VotedAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.databinding.FragmentVotedBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.chat.VotedHandler

class VotedFragment : BaseBindingChatFragment<FragmentVotedBinding>(FragmentVotedBinding::inflate), VotedHandler{
    private lateinit var adapter : VotedAdapter
    fun newInstance(arrayList : ArrayList<Sender>): VotedFragment{
        val args = Bundle()
        val fragment = VotedFragment()
        args.putString(TechresEnum.VOTE_FRAGMENT.toString(), Gson().toJson(arrayList))
        fragment.arguments = args
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = VotedAdapter()
        adapter.setVoteHandler(this)
        binding.rcVoted.layoutManager = LinearLayoutManager(requireActivity().baseContext, RecyclerView.VERTICAL, false)
        binding.rcVoted.adapter = adapter
    }

    override fun onChooseUser() {

    }

    override fun onBackPress() : Boolean{
        return true
    }
}