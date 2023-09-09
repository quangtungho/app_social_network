package vn.techres.line.holder.contact

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemChooseContactBinding
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.chat.ContactDeviceChatHandler

class ChooseContactHolder(private val binding : ItemChooseContactBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(configNodeJs: ConfigNodeJs, contactData: ContactNodeChat, contactDeviceChatHandler: ContactDeviceChatHandler){
        if(contactData.avatar?.medium != ""){
            binding.imgAvatar.loadImage(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    contactData.avatar?.medium
                ), R.drawable.ic_user_placeholder_circle, true
            )
        }else{
            binding.imgAvatar.setImageResource(contactData.color)
            val listString = contactData.full_name?.split("\\s".toRegex())
            binding.tvNameAvatar.text = if(listString?.size ?: 0 > 1){
                String.format("%s%s", listString?.get(0)?.get(0) ?: "", listString?.get(1)?.get(0) ?: "")
            }else {
                (contactData.full_name?.get(0) ?: "").toString()
            }
        }
        binding.imgRemove.setOnClickListener {
            contactDeviceChatHandler.onRemoveContact(contactData)
        }

    }
}