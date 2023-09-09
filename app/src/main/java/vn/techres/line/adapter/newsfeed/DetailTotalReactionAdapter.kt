package vn.techres.line.adapter.newsfeed

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
import vn.techres.line.interfaces.ClickProfile
import vn.techres.line.interfaces.DetailTotalReactionHandler

class DetailTotalReactionAdapter(val context: Context) : RecyclerView.Adapter<DetailTotalReactionAdapter.ItemHolder>() {
    private var dataSource = ArrayList<InteractiveUser>()
    private var detailTotalReactionHandler: DetailTotalReactionHandler? = null
    private var clickProfile: ClickProfile? = null

    fun setDataSource(dataSource: ArrayList<InteractiveUser>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(detailTotalReactionHandler : DetailTotalReactionHandler){
        this.detailTotalReactionHandler = detailTotalReactionHandler
    }

    fun setClickProfile(clickProfile : ClickProfile){
        this.clickProfile = clickProfile
    }

    inner class ItemHolder(val binding: ItemPersonReactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemPersonReactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val user = dataSource[position]
            try {
                binding.imgAvatar.load(getLinkDataNode(user.avatar.thumb!!)){
                    crossfade(true)
                    scale(Scale.FIT)
                    placeholder(R.drawable.ic_user_placeholder)
                    error(R.drawable.ic_user_placeholder)
                    size(500, 500)
                }
            }
            catch (e: Exception) {
            }

            when (user.reaction_id){
                1 -> binding.imgReaction.setImageResource(R.drawable.icon_lovely)
                2 -> binding.imgReaction.setImageResource(R.drawable.icon_exciting)
                3 -> binding.imgReaction.setImageResource(R.drawable.icon_sad)
                4 -> binding.imgReaction.setImageResource(R.drawable.ic_diamond)
                5 -> binding.imgReaction.setImageResource(R.drawable.icon_nagative)
                6 -> binding.imgReaction.setImageResource(R.drawable.icon_nothing)
            }

            when(user.status){
                0 ->{
                    //mình
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                1 ->{
                    // đã bạn bè
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.VISIBLE
                }
                2 ->{
                    // đã gửi lời mời kết bạn đến người đó
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.VISIBLE
                    binding.imgChat.visibility = View.GONE
                }
                3 ->{
                    // được người đó gửi lời mời kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                4 ->{
                    // chưa kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
            }

            if (user.user_id == CurrentUser.getCurrentUser(context).id){
                binding.btnAddFriend.visibility = View.GONE
                binding.txtSentFriend.visibility = View.GONE
            }


            binding.tvName.text = user.full_name

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.visibility = View.GONE
                binding.txtSentFriend.visibility = View.VISIBLE
                detailTotalReactionHandler!!.clickAddFriend(user)
            }

            binding.imgChat.setOnClickListener {
                detailTotalReactionHandler!!.onChat(user)
            }

            binding.imgAvatar.setOnClickListener {
                clickProfile!!.onAvatar(user.avatar.original ?: "", position)
            }
            binding.tvName.setOnClickListener {
                user.user_id?.let { it1 ->
                    clickProfile!!.clickProfile(position,
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