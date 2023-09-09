package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import vn.techres.line.data.model.chat.ReactionItem
import vn.techres.line.data.model.chat.UserReactionChat
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemUserReactionChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.helper.utils.ChatUtils

class UserReactionAdapter (var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var dataSource = ArrayList<UserReactionChat>()
    private var dataSourceTemp = ArrayList<UserReactionChat>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    var binding: ItemUserReactionChatBinding? = null
    fun setDataSource(dataSource : ArrayList<UserReactionChat>){
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemUserReactionChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserReactionHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as UserReactionHolder).bin(dataSource[position], position, context, nodeJs)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
    class UserReactionHolder(val binding: ItemUserReactionChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bin(
            data: UserReactionChat,
            position: Int,
            context: Context,
            configNodeJs : ConfigNodeJs
        ) {
            var reaction: ArrayList<ReactionItem> = data.userReaction?.let {
                ChatUtils.getReactionItem(
                    it
                )
            }!!
            var count = 0
            reaction.filter {
                it.number != 0
            }
            Utils.getImage(binding.imgAvatar, data.userReaction?.avatar?.medium, configNodeJs)
            binding.txtUsername.setText(data.userReaction?.full_name)
            binding.imgEmoji1.visibility = View.GONE
            binding.imgEmoji2.visibility = View.GONE
            binding.imgEmoji3.visibility = View.GONE
            binding.imgEmoji4.visibility = View.GONE
            binding.imgEmoji5.visibility = View.GONE
            binding.imgEmoji6.visibility = View.GONE
            if (data.type == 0) {
                for (s in reaction) {
                    count = count + s.number!!
                }
                if (reaction.size == 0) {
                    binding.imgEmoji1.visibility = View.GONE
                    binding.imgEmoji2.visibility = View.GONE
                    binding.imgEmoji3.visibility = View.GONE
                    binding.imgEmoji4.visibility = View.GONE
                    binding.imgEmoji5.visibility = View.GONE
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 1) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    binding.imgEmoji2.visibility = View.GONE
                    binding.imgEmoji3.visibility = View.GONE
                    binding.imgEmoji4.visibility = View.GONE
                    binding.imgEmoji5.visibility = View.GONE
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 2) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    binding.imgEmoji2.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    ChatUtils.setImageReaction(binding.imgEmoji2, reaction[1])
                    binding.imgEmoji3.visibility = View.GONE
                    binding.imgEmoji4.visibility = View.GONE
                    binding.imgEmoji5.visibility = View.GONE
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 3) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    binding.imgEmoji2.visibility = View.VISIBLE
                    binding.imgEmoji3.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    ChatUtils.setImageReaction(binding.imgEmoji2, reaction[1])
                    ChatUtils.setImageReaction(binding.imgEmoji3, reaction[2])
                    binding.imgEmoji4.visibility = View.GONE
                    binding.imgEmoji5.visibility = View.GONE
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 4) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    binding.imgEmoji2.visibility = View.VISIBLE
                    binding.imgEmoji3.visibility = View.VISIBLE
                    binding.imgEmoji4.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    ChatUtils.setImageReaction(binding.imgEmoji2, reaction[1])
                    ChatUtils.setImageReaction(binding.imgEmoji3, reaction[2])
                    ChatUtils.setImageReaction(binding.imgEmoji4, reaction[3])
                    binding.imgEmoji5.visibility = View.GONE
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 5) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    binding.imgEmoji2.visibility = View.VISIBLE
                    binding.imgEmoji3.visibility = View.VISIBLE
                    binding.imgEmoji4.visibility = View.VISIBLE
                    binding.imgEmoji5.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    ChatUtils.setImageReaction(binding.imgEmoji2, reaction[1])
                    ChatUtils.setImageReaction(binding.imgEmoji3, reaction[2])
                    ChatUtils.setImageReaction(binding.imgEmoji4, reaction[3])
                    ChatUtils.setImageReaction(binding.imgEmoji5, reaction[4])
                    binding.imgEmoji6.visibility = View.GONE
                } else if (reaction.size == 6) {
                    binding.imgEmoji1.visibility = View.VISIBLE
                    binding.imgEmoji2.visibility = View.VISIBLE
                    binding.imgEmoji3.visibility = View.VISIBLE
                    binding.imgEmoji4.visibility = View.VISIBLE
                    binding.imgEmoji5.visibility = View.VISIBLE
                    binding.imgEmoji6.visibility = View.VISIBLE
                    ChatUtils.setImageReaction(binding.imgEmoji1, reaction[0])
                    ChatUtils.setImageReaction(binding.imgEmoji2, reaction[1])
                    ChatUtils.setImageReaction(binding.imgEmoji3, reaction[2])
                    ChatUtils.setImageReaction(binding.imgEmoji4, reaction[3])
                    ChatUtils.setImageReaction(binding.imgEmoji5, reaction[4])
                    ChatUtils.setImageReaction(binding.imgEmoji6, reaction[5])
                }
                binding.txtCount.text = count.toString() + ""
            } else {
                for (s in reaction) {
                    if (data.type == s.name?.toInt()) {
                        ChatUtils.setImageReaction(binding.imgEmoji1, s)
                        binding.imgEmoji1.visibility = View.VISIBLE
                        Timber.d("type reaction : %s", s.number)
                        binding.txtCount.setText(s.number.toString() + "")
                        break
                    }
                }
            }
        }
    }
}