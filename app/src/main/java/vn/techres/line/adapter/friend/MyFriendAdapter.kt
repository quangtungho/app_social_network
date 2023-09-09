package vn.techres.line.adapter.friend

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickFriendHandler

class MyFriendAdapter(val context: Context) :
    RecyclerView.Adapter<MyFriendAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var clickFriendHandler: ClickFriendHandler? = null

    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(clickFriendHandler: ClickFriendHandler) {
        this.clickFriendHandler = clickFriendHandler
    }

    inner class ItemHolder(val binding: ItemFriendContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFriendContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.txtSection.visibility = View.VISIBLE
            binding.viewLineHeader.visibility = View.VISIBLE
            dataSource.sortWith { o1, o2 -> o1.full_name!!.trim().compareTo(o2.full_name!!.trim()) }
            val users: Friend = dataSource[position]

            binding.txtSection.text = users.full_name!!.trim().substring(0, 1)
            if (position > 0) {
                val i = position - 1
                if (i < dataSource.size && users.full_name!!.trim()
                        .substring(0, 1) == dataSource[i].full_name!!.trim().substring(0, 1)
                ) {
                    binding.txtSection.visibility = View.GONE
                    binding.viewLineHeader.visibility = View.GONE
                }
            }


            binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, users.avatar.thumb))
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
            binding.tvName.text = users.full_name
            binding.imgUserOnline.visibility = View.GONE

            itemView.setOnClickListener {
                users.user_id?.let { it1 ->
                    clickFriendHandler!!.clickFriendProfile(
                        position,
                        it1
                    )
                }
            }

            binding.imgChat.setOnClickListener {
                clickFriendHandler!!.clickFriendChat(position, "my_friend")
            }

//        if (dataSource[position].is_close_friend == 0){
//            binding.imgBestFriend.setImageResource(R.drawable.ic_save_best_friend)
//        }else{
//            binding.imgBestFriend.setImageResource(R.drawable.ic_saved_best_friend)
//        }
        }

    }

}