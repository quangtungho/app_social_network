package vn.techres.line.holder.vote

import android.content.Context
import android.text.Editable
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.databinding.ItemCreateVoteBinding
import vn.techres.line.interfaces.chat.CreateVoteMessageHandler
import vn.techres.line.data.model.chat.OptionVote

class CreateVoteMessageHolder(val binding : ItemCreateVoteBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(context: Context, optionVote: OptionVote, createVoteMessageHandler : CreateVoteMessageHandler){
        binding.edtPlan.text = Editable.Factory.getInstance().newEditable(optionVote.content ?: "")
        binding.edtPlan.hint = String.format("%s %s", context.resources.getString(R.string.plan), bindingAdapterPosition + 1)
        binding.imgRemovePlan.setOnClickListener {
            createVoteMessageHandler.onRemove(bindingAdapterPosition)
        }
    }
}