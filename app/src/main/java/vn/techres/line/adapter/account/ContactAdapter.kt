package vn.techres.line.adapter.account

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.base.BaseFragment
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickFriendHandler

class ContactAdapter(val context: BaseFragment) :
    RecyclerView.Adapter<ContactAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var clickFriendHandler: ClickFriendHandler? = null

    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context.mainActivity!!)

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
            binding.imgUserOnline.visibility = View.GONE
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
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name

            itemView.setOnClickListener {
                dataSource[position].user_id?.let { it1 ->
                    clickFriendHandler!!.clickFriendProfile(
                        position,
                        it1
                    )
                }
            }
        }

    }
}