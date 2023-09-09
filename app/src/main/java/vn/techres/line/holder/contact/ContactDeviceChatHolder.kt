package vn.techres.line.holder.contact

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemContactDeviceChatBinding
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.ContactDeviceChatHandler

class ContactDeviceChatHolder(private val binding: ItemContactDeviceChatBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        configNodeJs: ConfigNodeJs,
        contactChat: ContactNodeChat,
        contactDeviceChatHandler: ContactDeviceChatHandler
    ) {

        if(contactChat.avatar?.medium != ""){
            binding.imgContact.loadImage(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    contactChat.avatar?.medium
                ), R.drawable.ic_user_placeholder_circle, true
            )
            binding.cvAvatarColor.hide()
        }else{
            binding.cvAvatarColor.show()
            binding.imgAvatarColor.setImageResource(contactChat.color)
            val listString = contactChat.full_name?.split("\\s".toRegex())
            binding.tvNameAvatar.text = if(listString?.size ?: 0 > 1){
                String.format("%s%s", listString?.get(0)?.get(0) ?: "", listString?.get(1)?.get(0) ?: "")
            }else {
                (contactChat.full_name?.get(0) ?: "").toString()
            }
        }

        binding.tvNameContact.text = contactChat.full_name ?: ""

        binding.tvPhoneContact.text = contactChat.phone ?: ""
        binding.rbChooseContact.isChecked = contactChat.isCheck

        binding.rbChooseContact.setOnClickListener {
            contactDeviceChatHandler.onChoosePhone(contactChat)
        }

        binding.root.setOnClickListener {
            contactDeviceChatHandler.onChoosePhone(contactChat)
        }

    }
}