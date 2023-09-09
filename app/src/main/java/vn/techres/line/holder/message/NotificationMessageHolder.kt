package vn.techres.line.holder.message

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemNotificationChatBinding
import vn.techres.line.helper.utils.ChatUtils.makeLinks
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs

class NotificationMessageHolder(private val binding: ItemNotificationChatBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bin(
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val message = dataSource[position]
        binding.lnNotification.visibility = View.VISIBLE
        getImage(binding.imgMemberNotification, message.sender.avatar?.medium, configNodeJs)
        setClickDialogProfile(message, binding.tvNotification, chooseNameUserHandler)
    }

    private fun setClickDialogProfile(
        message: MessagesByGroup,
        tvMessage: TextView,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        val links = ArrayList<Pair<String, View.OnClickListener>>()
        tvMessage.text = message.message
        if ((message.message ?: "").contains(message.sender.full_name ?: "")) {
            message.sender.full_name?.let {
                val pair = Pair(
                    it, View.OnClickListener {
                        chooseNameUserHandler!!.onChooseNameUser(message.sender)
                    }
                )
                links.add(pair)
            }
            tvMessage.makeLinks(links)
        }
    }
}