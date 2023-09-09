package vn.techres.line.holder.message

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemNotificationHappyBirthdayBinding
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.chat.ChatGroupHandler

class NotificationHappyBirthDayMessageHolder(private val binding: ItemNotificationHappyBirthdayBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        dataSource: ArrayList<MessagesByGroup>,
        position: Int,
        configNodeJs: ConfigNodeJs,
        chatGroupHandler: ChatGroupHandler
    ) {
        if(dataSource[position].sender.member_id == CurrentUser.getCurrentUser(context).id) {
            itemView.visibility = View.GONE
        } else {
            itemView.visibility = View.VISIBLE
            Utils.getGlide(binding.imvAvatar,dataSource[position].sender.avatar?.original,configNodeJs)
            Utils.getGlide(binding.background,dataSource[position].files[0].link_original,configNodeJs)
            binding.txtTitle.text = dataSource[position].message
            binding.btnIntent.setOnClickListener { chatGroupHandler.onIntentSendBirthDayCard(dataSource[position]) }
        }
    }
}