package vn.techres.line.holder.message.information.left

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemLinkInfomationLeftBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class LinkInformationLeftHolder(private val binding: ItemLinkInfomationLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        dataSource: ArrayList<MessagesByGroup>,
        receivedViewer : ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?,
        viewerMessageHandler : ViewerMessageHandler?,
        receivedViewerNotSeen : ArrayList<MessageViewer>
    ) {
        val message = dataSource[bindingAdapterPosition]
        Utils.getMediaGlide(binding.link.imgLinkPreview, message.message_link?.media_thumb)
        binding.tvTimeLink.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        binding.tvNameLink.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        if(message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?.random_key)){
            setLink(context, message.message_share?.message ?: "", message.message_share?.message_link?.author ?: "", message.message_share?.message_link?.title ?: "",
                message.message_share?.message_link?.description ?: "", message.message_share?.message_link?.cannonical_url ?: ""
            )
        }else{
            setLink(context, message.message ?: "", message.message_link?.author ?: "", message.message_link?.title ?: "",
                message.message_link?.description ?: "", message.message_link?.cannonical_url ?: ""
            )
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
        /**
         * Tag name
         * */
        ChatUtils.setClickTagName(
            message.message ?: "",
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )
    }

    private fun setLink(context: Context, message : String, author : String, title : String, description : String, cannonical_url : String){
        if(StringUtils.isNullOrEmpty(message)){
            binding.tvMessage.hide()
        }else{
            binding.tvMessage.show()
            binding.tvMessage.text = message
        }
        if(StringUtils.isNullOrEmpty(author)){
            binding.link.tvAuthorLink.hide()
        }else{
            binding.link.tvAuthorLink.show()
            binding.link.tvAuthorLink.text = author
        }
        if(StringUtils.isNullOrEmpty(title)){
            binding.link.tvTitleLink.hide()
        }else{
            binding.link.tvTitleLink.show()
            binding.link.tvTitleLink.text = title
        }

        if(StringUtils.isNullOrEmpty(description)){
            binding.link.tvDescriptionLink.hide()
        }else{
            binding.link.tvDescriptionLink.show()
            binding.link.tvDescriptionLink.text = description
        }
        binding.link.rlLink.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(cannonical_url))
            context.startActivity(browserIntent)
        }
    }
}