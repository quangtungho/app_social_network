package vn.techres.line.holder.message

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemAddMemberMessageBinding
import vn.techres.line.helper.utils.ChatUtils.makeLinks
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.utils.ConfigNodeJs

class AddMemberMessageHolder(private val binding : ItemAddMemberMessageBinding) : RecyclerView.ViewHolder(binding.root) {
    private var listName = ArrayList<Sender>()

    fun bind(configNodeJs: ConfigNodeJs, message : MessagesByGroup, chooseNameUserHandler: ChooseNameUserHandler?) {
        listName = ArrayList()
        when(message.list_member.size) {
            1 -> {
                binding.cvAvatarOne.visibility = View.VISIBLE
                binding.cvAvatarTwo.visibility = View.GONE
                binding.cvAvatarThree.visibility = View.GONE
                getImage(binding.imgAvatarOne, message.list_member[0].avatar?.medium, configNodeJs)
            }
            2 -> {
                binding.cvAvatarOne.visibility = View.VISIBLE
                binding.cvAvatarTwo.visibility = View.VISIBLE
                binding.cvAvatarThree.visibility = View.GONE
                getImage(binding.imgAvatarOne, message.list_member[0].avatar?.medium, configNodeJs)
                getImage(binding.imgAvatarTwo, message.list_member[1].avatar?.medium, configNodeJs)
            }
            3 -> {
                binding.cvAvatarOne.visibility = View.VISIBLE
                binding.cvAvatarTwo.visibility = View.VISIBLE
                binding.cvAvatarThree.visibility = View.VISIBLE
                getImage(binding.imgAvatarOne, message.list_member[0].avatar?.medium, configNodeJs)
                getImage(binding.imgAvatarTwo, message.list_member[1].avatar?.medium, configNodeJs)
                getImage(binding.imgAvatarThree, message.list_member[2].avatar?.medium, configNodeJs)
            }
        }
        listName.addAll(message.list_member)
        listName.add(message.sender)
        setClickMemberName(message.message ?: "", listName, binding.tvNotificationAddUser, chooseNameUserHandler)
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