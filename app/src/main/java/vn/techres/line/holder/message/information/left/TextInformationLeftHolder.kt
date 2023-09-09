package vn.techres.line.holder.message.information.left

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemTextInfomationLeftBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class TextInformationLeftHolder(private val binding : ItemTextInfomationLeftBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context : Context,
        configNodeJs: ConfigNodeJs,
        user: User,
        dataSource : ArrayList<MessagesByGroup>,
        receivedViewer : ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?,
        viewerMessageHandler : ViewerMessageHandler?,
        receivedViewerNotSeen : ArrayList<MessageViewer>
    ){
        val message = dataSource[bindingAdapterPosition]

        ChatUtils.setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )
        binding.tvTimeText.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        binding.tvNameText.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)

        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (bindingAdapterPosition >= 0 && (bindingAdapterPosition + 1) < dataSource.size) {
                if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition + 1].sender.member_id
                    && message.today != 1
                    && dataSource[bindingAdapterPosition].message_type == dataSource[bindingAdapterPosition + 1].message_type) {
                    binding.tvNameText.visibility = View.GONE
                    binding.cvText.visibility = View.INVISIBLE
                } else {
                    binding.tvNameText.visibility = View.VISIBLE
                    binding.cvText.visibility = View.VISIBLE
                }
            }
        } else {
            binding.cvText.visibility = View.GONE
            binding.tvNameText.visibility = View.GONE
        }

//        if (message.reactions.reactions_count == 0) {
//            binding.reaction.lnReaction.visibility = View.GONE
//            binding.reaction.imgReactionPress.visibility = View.VISIBLE
//            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
//            if (bindingAdapterPosition > 0) {
//                if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition - 1].sender.member_id) {
//                    binding.lnReactionContainer.visibility = View.GONE
//                } else {
//                    binding.lnReactionContainer.visibility = View.VISIBLE
//                }
//            } else {
//                binding.lnReactionContainer.visibility = View.VISIBLE
//            }
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
//        binding.reaction.imgReactionPress.visibility = View.GONE

        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.visibility = View.VISIBLE
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.visibility = View.GONE
        /**
         * Message viewed
         * */
        binding.tvSeen.hide()
        binding.tvReceived.hide()
        binding.rcViewerSeen.hide()
        binding.rcViewerReceived.hide()
        if(bindingAdapterPosition == 0){
            binding.tvStatusView.text = context.getString(R.string.received)
            if(receivedViewerNotSeen.size > 0){
                binding.tvReceived.show()
                binding.rcViewerReceived.show()
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerReceived, receivedViewerNotSeen, viewerMessageHandler)
            }
            if (receivedViewer.size > 0){
                binding.tvSeen.show()
                binding.rcViewerSeen.show()
                binding.tvStatusView.text = context.getString(R.string.seen)
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerSeen, receivedViewer, viewerMessageHandler)
            }
        }
    }
}