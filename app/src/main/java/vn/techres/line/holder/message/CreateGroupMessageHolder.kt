package vn.techres.line.holder.message

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.adapter.chat.MemberCreateGroupAdapter
import vn.techres.line.adapter.chat.StickerSuggestAdapter
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemCreateGroupMessageBinding
import vn.techres.line.helper.utils.ChatUtils.getAvatarGroup
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.interfaces.chat.ChatGroupHandler

class CreateGroupMessageHolder(private val binding: ItemCreateGroupMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNode: ConfigNodeJs,
        message: MessagesByGroup,
        chatGroupHandler: ChatGroupHandler?,
        utilitiesChatHandler: UtilitiesChatHandler?
    ) {
        getAvatarGroup(binding.imgAvatarGroup, message.avatar, configNode)
        binding.tvNameGroup.text = message.group_name
        val adapter = message.list_sticker?.let { StickerSuggestAdapter(context, it) }
        binding.rcStickerWelcome.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rcStickerWelcome.adapter = adapter
        adapter?.setUtilitiesChatHandler(utilitiesChatHandler)
        val adapterMember = MemberCreateGroupAdapter(context)
        if (message.list_member.size > 5) {
            val list = message.list_member.slice(0..4).toList()
            adapterMember.setDataSource(list as ArrayList<Sender>, list.size)
        } else {
            adapterMember.setDataSource(message.list_member, message.list_member.size)
        }

        binding.rcMemberGroup.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        binding.rcMemberGroup.adapter = adapterMember

        binding.imgAddFriend.setOnClickListener {
            chatGroupHandler?.onAddMember()
        }
    }
}