package vn.techres.line.holder.message

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemPinnedMessageBinding
import vn.techres.line.helper.utils.ChatUtils.makeLinks
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs

class PinnedMessageHolder(private val binding: ItemPinnedMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        configNodeJs: ConfigNodeJs,
        messagesByGroup: MessagesByGroup,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        setClickDialogProfile(messagesByGroup,  binding.tvMessagePinned, chooseNameUserHandler)

        getGlide(binding.imgPinned, messagesByGroup.sender.avatar?.medium, configNodeJs)
    }

    private fun setClickDialogProfile(message: MessagesByGroup, tvMessage: TextView, chooseNameUserHandler : ChooseNameUserHandler?){
        val links = ArrayList<Pair<String, View.OnClickListener>>()
        message.sender.full_name?.let {
            val pair = Pair(
                it, View.OnClickListener {
                    chooseNameUserHandler!!.onChooseNameUser(message.sender)
                }
            )
            links.add(pair)
        }
        tvMessage.text = message.message
        tvMessage.makeLinks(links)
    }
}