package vn.techres.line.holder.group

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemGroupBinding
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils.getAvatarGroup
import vn.techres.line.helper.utils.ChatUtils.setMessageItemChat
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.GroupChatHandler

class GroupHolder(val binding : ItemGroupBinding) : RecyclerView.ViewHolder(binding.root)  {
    var isClick = false
    fun bind(context : Context, user : User, configNodeJs : ConfigNodeJs, group : Group, groupChatHandler : GroupChatHandler?){
        if (group.conversation_type == context.resources.getString(R.string.two_personal)) {
            val member = group.member
            getAvatarGroup(binding.imageGroupChat, member.avatar?.medium, configNodeJs)
            binding.tvGroupName.text = member.full_name
        } else {
            getAvatarGroup(binding.imageGroupChat, group.avatar, configNodeJs)
            binding.tvGroupName.text = group.name
        }
        binding.tvGroupName.typeface = Typeface.DEFAULT
        binding.tvMessageGroup.typeface = Typeface.DEFAULT
        binding.tvCountChat.hide()

        if(group.stranger == 1){
            binding.lnMessageContent.show()
            binding.chipStranger.show()
            binding.chipStranger.text = context.resources.getString(R.string.stranger)
        }else{
            binding.lnMessageContent.hide()
            binding.chipStranger.hide()
        }

        if ((group.last_message_type ?: "").isNotEmpty()) {
            binding.tvTimeGroupChat.text = group.created_last_message
//            binding.tvTimeGroupChat.text = timeGroupAgoString(group.created_at_time)
            if (user.id == group.user_last_message_id) {
                if (group.status_last_message == 1) {
                    setMessageItemChat(context, user, group, binding.tvMessageGroup, context.getString(R.string.you))
                } else {
                    binding.tvMessageGroup.text = context.resources.getString(R.string.revoke_my)
                }
            } else {
                if (group.status_last_message == 1) {
                    setMessageItemChat(context, user, group, binding.tvMessageGroup, group.user_name_last_message + "")
                } else {
                    binding.tvMessageGroup.text = String.format(
                        "%s %s %s",
                        group.user_name_last_message,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.revoke_you)
                    )
                }
            }
            if((group.number_message_not_seen ?: 0) > 0){
                if((group.number_message_not_seen ?: 0) <= 99){
                    binding.tvCountChat.text = group.number_message_not_seen.toString()
                }else{
                    binding.tvCountChat.text = context.resources.getString(R.string.limit_count_chat)
                }
                binding.tvGroupName.typeface = Typeface.DEFAULT_BOLD
                binding.tvMessageGroup.typeface = Typeface.DEFAULT_BOLD
                binding.tvCountChat.show()
            }
            when(group.last_message_type){
                TechResEnumVideoCall.TYPE_CALL_PHONE.toString() ->{
                    binding.tvCountChat.hide()
                    binding.tvTimeGroupChat.hide()
                    binding.imgCall.show()
                    binding.imgCall.setImageResource(R.drawable.new_incall_answer_voice_button_selector)
                }
                TechResEnumVideoCall.TYPE_CALL_VIDEO.toString() ->{
                    binding.tvCountChat.hide()
                    binding.tvTimeGroupChat.hide()
                    binding.imgCall.show()
                    binding.imgCall.setImageResource(R.drawable.call_accept_switch_video_selector)
                }else ->{
                    binding.tvTimeGroupChat.show()
                    binding.imgCall.hide()
                }
            }
        }else{
            binding.tvMessageGroup.text = group.last_message ?: ""
            binding.tvTimeGroupChat.text = ""
        }

        itemView.setOnClickListener {
            if (!isClick)
                groupChatHandler?.onChat(group)
            isClick = true
            Handler(Looper.getMainLooper()).postDelayed({
                isClick = false
            }, 3000)
        }

        itemView.setOnLongClickListener {
            groupChatHandler?.onBottomSheet(group, bindingAdapterPosition)
            true
        }

        binding.imgCall.setOnClickListener {
            binding.imgCall.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.imgCall.isEnabled = true
            }, 3000)
            groupChatHandler?.onCall(group)
        }
    }
}