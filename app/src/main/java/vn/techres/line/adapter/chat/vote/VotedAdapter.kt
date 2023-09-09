package vn.techres.line.adapter.chat.vote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemVotedBinding
import vn.techres.line.holder.vote.VotedHolder
import vn.techres.line.interfaces.chat.VotedHandler

class VotedAdapter : RecyclerView.Adapter<VotedHolder>() {
    private var votedHandler : VotedHandler? = null

    fun setVoteHandler(votedHandler : VotedHandler){
        this.votedHandler = votedHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotedHolder {
        return VotedHolder(ItemVotedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VotedHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 0
    }
}