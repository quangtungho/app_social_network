package vn.techres.line.holder.message

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemAuthorizeMessageBinding
import vn.techres.line.helper.utils.ChatUtils.makeLinks
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.utils.ConfigNodeJs

class MemberMessageHolder(private val binding : ItemAuthorizeMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    private var listName = ArrayList<Sender>()

    fun bind(configNodeJs: ConfigNodeJs, message : MessagesByGroup, chooseNameUserHandler: ChooseNameUserHandler?){
        listName = ArrayList()
        if(message.message_type == TechResEnumChat.TYPE_AUTHORIZE.toString()){
            listName.add(message.sender)
            listName.addAll(message.list_member)
        }else{
            listName.addAll(message.list_member)
            listName.add(message.sender)
        }
        setClickMemberName(message.message ?: "", listName, binding.tvContentAuthorize, chooseNameUserHandler)
        getImage(binding.imgMemberAuthorize, message.list_member[0].avatar?.medium, configNodeJs)
    }

    private fun setClickMemberName(
        message: String,
        listMember: ArrayList<Sender>,
        tvMessage: TextView,
        chooseNameUserHandler : ChooseNameUserHandler?
    ) {
        val links = ArrayList<Pair<String, View.OnClickListener>>()
        if (listMember.isNotEmpty()) {
            listMember.forEach { member ->
                val pair = Pair(
                    member.full_name + "", View.OnClickListener {
                        chooseNameUserHandler?.onChooseNameUser(member)
                    }
                )
                links.add(pair)
            }
        }
        tvMessage.text = message
        if (links.isNotEmpty()) {
            tvMessage.makeLinks(links)
        }
    }

}