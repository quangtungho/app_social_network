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
import vn.techres.line.interfaces.LongClickFriendHandler

class BestFriendAdapter(val context: Context) :
    RecyclerView.Adapter<BestFriendAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var clickFriendHandler: ClickFriendHandler? = null
    private var longClickFriendHandler: LongClickFriendHandler? = null

    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(clickFriendHandler: ClickFriendHandler) {
        this.clickFriendHandler = clickFriendHandler
    }

    fun setLongClickFriend(longClickFriendHandler: LongClickFriendHandler) {
        this.longClickFriendHandler = longClickFriendHandler
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
            binding.imgAvatar.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    dataSource[position].avatar.thumb
                )
            )
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name
            binding.imgUserOnline.visibility = View.GONE

            itemView.setOnClickListener {
                dataSource[position].user_id?.let { it1 ->
                    clickFriendHandler!!.clickFriendProfile(
                        position,
                        it1
                    )
                }
            }

            itemView.setOnLongClickListener {
                dataSource[position].user_id?.let { it1 ->
                    longClickFriendHandler!!.longClickFriend(
                        position,
                        it1
                    )
                }
                false
            }

            binding.imgChat.setOnClickListener {
                clickFriendHandler!!.clickFriendChat(position, "best_friend")
            }

//        if (dataSource[position].is_close_friend == 0){
//            binding.imgBestFriend.setImageResource(R.drawable.ic_save_best_friend)
//        }else{
//            binding.imgBestFriend.setImageResource(R.drawable.ic_saved_best_friend)
//        }
        }


    }


}