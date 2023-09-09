package vn.techres.line.adapter.chat.vote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemVoteMessageBinding
import vn.techres.line.holder.vote.ChooseVoteHolder

class ChooseVoteAdapter : RecyclerView.Adapter<ChooseVoteHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseVoteHolder {
        return ChooseVoteHolder(ItemVoteMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChooseVoteHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 0
    }
}