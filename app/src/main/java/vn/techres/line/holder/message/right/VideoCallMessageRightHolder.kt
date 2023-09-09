package vn.techres.line.holder.message.right

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemVideoCallChatRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.chat.ChatGroupHandler

class VideoCallMessageRightHolder(private val binding : ItemVideoCallChatRightBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        user: User,
        dataSources: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
    ){
        val message = dataSources[bindingAdapterPosition]
        if(message.is_stroke == 1) {
            binding.rlVideoCall.isSelected = true
        } else {
            binding.rlVideoCall.isSelected = false
        }
//        binding.tvTimeVideoCall.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeVideoCall, bindingAdapterPosition, dataSources)

        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()

        when(message.message_video_call?.call_status){
            //Cuộc gọi thoại/video đi
            TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvContextTimer.text = message.message_video_call?.call_time ?: "0 phút 0 giây"
                binding.tvStatus.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(
                        R.drawable.decor_csc_voicecall_out_small
                    )
                    context.resources.getString(R.string.call_phone_away)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_videocall_out_small)
                    context.resources.getString(R.string.call_video_away)
                }
            }

            //Bạn đã huỷ
            TechResEnumVideoCall.CALL_CANCEL.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvStatus.text = context.resources.getString(R.string.you_cancel)
                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(
                        R.drawable.decor_csc_voicecall_out_miss_small
                    )
                    context.resources.getString(R.string.call_phone)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_videocall_miss_small)
                    context.resources.getString(R.string.call_video)
                }
            }

            //Người nhận từ chối
            TechResEnumVideoCall.CALL_REJECT.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvStatus.text = context.resources.getString(R.string.receiver_refused)

                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(R.drawable.icn_csc_voicecall_cancel_small)
                    context.resources.getString(R.string.call_phone)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.icn_csc_videocall_cancel_small)
                    context.resources.getString(R.string.call_video)
                }
            }

            //Người nhận không bắt máy
            TechResEnumVideoCall.CALL_NO_ANSWER.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))

                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_voicecall_out_miss_small)
                    binding.tvStatus.text = context.resources.getString(R.string.call_phone_away)
                    context.resources.getString(R.string.null_time)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_videocall_miss_small)
                    binding.tvStatus.text = context.resources.getString(R.string.call_video_away)
                    context.resources.getString(R.string.null_time)
                }
            }

            //Người nhận từ chối
            TechResEnumVideoCall.CALL_BUSY.toString() ->{
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvStatus.text = context.resources.getString(R.string.call_busy)

                binding.tvContextTimer.text = if(message.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()){
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_voicecall_out_miss_small)
                    context.resources.getString(R.string.call_phone)
                }else{
                    binding.imgTypeCall.setImageResource(R.drawable.decor_csc_videocall_miss_small)
                    context.resources.getString(R.string.call_video)
                }
            }
        }

        val lp = binding.rlVideoCall.layoutParams as ViewGroup.MarginLayoutParams
        if (bindingAdapterPosition > 0) {
            if (ChatUtils.checkTimeOld(dataSources[bindingAdapterPosition - 1].created_at, message.created_at)
                && dataSources[bindingAdapterPosition].message_type == dataSources[bindingAdapterPosition - 1].message_type) {
                lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(4))
            }else{
                lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(14))
            }
        }else{
            lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(14))
        }
        binding.tvCall.setOnClickListener {
            chatGroupHandler?.onChooseCallVideo(message)
        }
    }
}