package vn.techres.line.holder.message.left

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemVideoCallChatLeftBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.invisible
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.chat.ChatGroupHandler

class VideoCallMessageLeftHolder(private val binding : ItemVideoCallChatLeftBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        user: User,
        dataSources: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?
    ){
        val message = dataSources[bindingAdapterPosition]
        if(message.is_stroke == 1) {
            binding.rlVideoCall.isSelected = true
        } else {
            binding.rlVideoCall.isSelected = false
        }
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
//        binding.tvTimeVideoCall.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeVideoCall, bindingAdapterPosition, dataSources)

        binding.tvName.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (bindingAdapterPosition >= 0 && (bindingAdapterPosition + 1) < dataSources.size) {
                if (dataSources[bindingAdapterPosition].sender.member_id == dataSources[bindingAdapterPosition + 1].sender.member_id
                    && message.today != 1
                    && dataSources[bindingAdapterPosition + 1].message_type.equals(
                        TechResEnumVideoCall.TYPE_CALL_PHONE.toString()
                    )&& dataSources[bindingAdapterPosition + 1].message_type.equals(
                        TechResEnumVideoCall.TYPE_CALL_VIDEO.toString()
                    )) {
                    binding.tvName.hide()
                    binding.cvVideoCall.invisible()
                } else {
                    binding.tvName.show()
                    binding.cvVideoCall.show()
                }
            }
        } else {
            binding.cvVideoCall.hide()
            binding.tvName.hide()
        }
        when(message.message_video_call?.call_status){
            //Cuộc gọi thoại/video Đến
            TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvContextTimer.text = message.message_video_call?.call_time ?: "0 phút 0 giây"
                binding.tvStatus.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(
                        R.drawable.decor_csc_voicecall_in_small
                    )
                    context.resources.getString(R.string.call_phone_to)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_videocall_in_small)
                    context.resources.getString(R.string.call_video_to)
                }
            }

            //Bạn bị nhỡ
            TechResEnumVideoCall.CALL_CANCEL.toString(), TechResEnumVideoCall.CALL_NO_ANSWER.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.tvStatus.text = context.resources.getString(R.string.you_missed)
                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(
                        R.drawable.ic_miss_call_audio
                    )
                    context.resources.getString(R.string.call_phone)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.icn_csc_videocall_miss_small)
                    context.resources.getString(R.string.call_video)
                }
            }

            //Bạn đã từ chối
            TechResEnumVideoCall.CALL_REJECT.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.tvStatus.text = context.resources.getString(R.string.you_refused)

                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(R.drawable.icn_csc_voicecall_cancel_small)
                    context.resources.getString(R.string.call_phone)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.icn_csc_videocall_cancel_small)
                    context.resources.getString(R.string.call_video)
                }
            }
        }
        val lp = binding.rlVideoCall.layoutParams as ViewGroup.MarginLayoutParams
        if (bindingAdapterPosition > 0) {
            if (ChatUtils.checkTimeOld(dataSources[bindingAdapterPosition - 1].created_at, message.created_at)
                && dataSources[bindingAdapterPosition].message_type == dataSources[bindingAdapterPosition - 1].message_type) {
                lp.setMargins(dpToPx(8), 0, 0, dpToPx(4))
            }else{
                lp.setMargins(dpToPx(8), 0, 0, dpToPx(14))
            }
        }else{
            lp.setMargins(dpToPx(8), 0, 0, dpToPx(14))
        }
        binding.tvCall.setOnClickListener {
            chatGroupHandler?.onChooseCallVideo(message)
        }
    }
}