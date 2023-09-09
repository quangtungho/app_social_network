package vn.techres.line.holder.message

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemUtilsMessageBinding
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs

class UtilsMessageHolder(private val binding : ItemUtilsMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(configNodeJs: ConfigNodeJs, messagesByGroup: MessagesByGroup){
        when(messagesByGroup.message_type){
            TechResEnumChat.TYPE_LOAD_PAGE.toString() -> {
                binding.tvTimeHeader.visibility = View.GONE
                binding.rlTypingChat.visibility = View.GONE
                binding.pbChat.visibility = View.VISIBLE
            }
            TechResEnumChat.TYPE_TYPING.toString() -> {
                binding.tvTimeHeader.visibility = View.GONE
                binding.pbChat.visibility = View.GONE
                binding.rlTypingChat.visibility = View.VISIBLE
                getGlide(binding.imgAvatar, messagesByGroup.sender.avatar?.medium, configNodeJs)
            }
        }
    }
}