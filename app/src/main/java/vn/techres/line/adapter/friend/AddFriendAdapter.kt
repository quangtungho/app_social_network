package vn.techres.line.adapter.friend

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemAddFriendBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.SystemContactHandler
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import java.util.*

@SuppressLint("UseRequireInsteadOfGet")
class AddFriendAdapter(var context: Context) :
    RecyclerView.Adapter<AddFriendAdapter.ViewHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var systemContactHandler: SystemContactHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var user = CurrentUser.getCurrentUser(context)

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(systemContactHandler: SystemContactHandler) {
        this.systemContactHandler = systemContactHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, user, configNodeJs, dataSource[position], systemContactHandler, position)
    }
    class ViewHolder(private val binding : ItemAddFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, user: User, configNodeJs: ConfigNodeJs, friend: Friend, systemContactHandler: SystemContactHandler?, position: Int){
            when(friend.status){
                0 ->{
                    //mình
                    binding.btnAddFriend.hide()
                    binding.tvStatus.hide()
                    binding.imgChat.hide()
                    binding.imgNotAccept.hide()
                }
                1 ->{
                    // đã bạn bè
                    binding.btnAddFriend.hide()
                    binding.tvStatus.hide()
                    binding.imgChat.show()
                    binding.imgNotAccept.hide()
                }
                2 ->{
                    // đã gửi lời mời kết bạn đến người đó
                    binding.btnAddFriend.hide()
                    binding.tvStatus.show()
                    binding.imgChat.hide()
                    binding.imgNotAccept.hide()
                    binding.tvStatus.text = context.getString(R.string.sent_friend)
                }
                3 ->{
                    // được người đó gửi lời mời kết bạn
                    binding.btnAddFriend.hide()
                    binding.tvStatus.show()
                    binding.imgChat.hide()
                    binding.imgNotAccept.hide()
                    binding.tvStatus.text = context.getString(R.string.status_member_3)
                }
                4 ->{
                    // chưa kết bạn
                    binding.btnAddFriend.show()
                    binding.tvStatus.hide()
                    binding.imgChat.hide()
                    binding.imgNotAccept.hide()
                }
            }

            when (friend.status){
                1 ->{
                    binding.tvStatusFriend.text = context.resources.getString(R.string.friends)
                }
                2 ->{
                    binding.tvStatusFriend.text = context.resources.getString(R.string.status_member_4)
                }
                3 ->{
                    binding.tvStatusFriend.text = context.resources.getString(R.string.status_member_4)
                }
                4 ->{
                    if (user.id != friend.status){
                        binding.tvStatusFriend.text = context.resources.getString(R.string.status_member_4)
                    }else{
                        binding.tvStatusFriend.text = context.resources.getString(R.string.i)
                    }
                }
            }

            binding.imgAvatar.load(String.format("%s%s", configNodeJs.api_ads, friend.avatar.thumb))
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
            binding.tvName.text = friend.full_name

            binding.root.setOnClickListener {
                systemContactHandler?.clickItem(bindingAdapterPosition, friend.user_id ?: 0)
            }

            binding.imgAvatar.setOnClickListener {
                systemContactHandler?.clickAvatar(friend.avatar.original?:"", position)
            }

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.hide()
                binding.tvStatus.show()
                binding.imgChat.hide()
                binding.imgNotAccept.hide()
                binding.tvStatus.text = context.getString(R.string.sent_friend)
                systemContactHandler?.clickAddFriend(bindingAdapterPosition, friend.user_id)
            }

            binding.imgChat.setOnClickListener {
                systemContactHandler?.clickChatFriend(bindingAdapterPosition, friend.avatar.original, friend.full_name, friend.user_id)
            }

            binding.imgNotAccept.setOnClickListener {
                binding.btnAddFriend.hide()
                binding.tvStatus.show()
                binding.imgChat.hide()
                binding.imgNotAccept.hide()
                binding.tvStatus.text = context.getString(R.string.declined)
                systemContactHandler?.clickNotAcceptFriend(bindingAdapterPosition, friend.avatar.original, friend.full_name, friend.user_id)
            }
        }
        
    }
}