package vn.techres.line.adapter.comment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.reaction.InteractiveUser
import vn.techres.line.databinding.ItemPersonReactionBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.interfaces.comment.ReactionCommentHandler

class DetailReactionCommentAdapter(val context: Context) :
    RecyclerView.Adapter<DetailReactionCommentAdapter.ItemHolder>() {
    private var dataSource = ArrayList<InteractiveUser>()
    private var reactionCommentHandler: ReactionCommentHandler? = null
    fun setDataSource(dataSource: ArrayList<InteractiveUser>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setReactionCommentHandler(reactionCommentHandler: ReactionCommentHandler) {
        this.reactionCommentHandler = reactionCommentHandler
    }

    inner class ItemHolder(val binding: ItemPersonReactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemPersonReactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val user = dataSource[position]
            try {
                binding.imgAvatar.load(getLinkDataNode(user.avatar.thumb!!)) {
                    crossfade(true)
                    scale(Scale.FIT)
                    placeholder(R.drawable.ic_user_placeholder)
                    error(R.drawable.ic_user_placeholder)
                    size(500, 500)
                }
            } catch (e: Exception) {
            }

            binding.imgReaction.setImageResource(R.drawable.icon_lovely)

            when (user.status) {
                0 -> {
                    //mình
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                1 -> {
                    // đã bạn bè
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.VISIBLE
                }
                2 -> {
                    // đã gửi lời mời kết bạn đến người đó
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.VISIBLE
                    binding.imgChat.visibility = View.GONE
                }
                3 -> {
                    // được người đó gửi lời mời kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                4 -> {
                    // chưa kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
            }

            if (user.user_id == CurrentUser.getCurrentUser(context).id) {
                binding.btnAddFriend.visibility = View.GONE
                binding.txtSentFriend.visibility = View.GONE
            }


            binding.tvName.text = user.full_name

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.visibility = View.GONE
                binding.txtSentFriend.visibility = View.VISIBLE
                reactionCommentHandler!!.clickAddFriend(user)
            }

            binding.imgChat.setOnClickListener {
                reactionCommentHandler!!.onChat(user)
            }

            binding.imgAvatar.setOnClickListener {
                reactionCommentHandler!!.onAvatar(user.avatar.original ?: "", position)
            }
            binding.tvName.setOnClickListener {
                user.user_id?.let { it1 ->
                    reactionCommentHandler!!.clickProfile(
                        position,
                        it1
                    )
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
    private fun getLinkDataNode(url: String): String {
        return String.format(
            "%s%s",
            CurrentConfigNodeJs.getConfigNodeJs(context).api_ads,
            url
        )
    }

}
