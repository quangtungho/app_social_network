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
import vn.techres.line.databinding.ItemAddBestFriendBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.AddBestFriendHandler
import java.util.*
import java.util.stream.Collectors

class AddBestFriendAdapter(val context: Context) :
    RecyclerView.Adapter<AddBestFriendAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var dataSourceTemp = ArrayList<Friend>()
    private var addBestFriendHandler: AddBestFriendHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setChooseBestFriend(addBestFriendHandler: AddBestFriendHandler) {
        this.addBestFriendHandler = addBestFriendHandler
    }

    fun getDataSource(): ArrayList<Friend> {
        return dataSource
    }

    fun searchFullText(keyword: String) {
        try {
            val key = keyword.lowercase(Locale.getDefault())
            dataSource = if (StringUtils.isNullOrEmpty(key)) {
                this.dataSourceTemp
            } else {
                dataSourceTemp.stream().filter {
                    it.full_name!!.lowercase(Locale.ROOT)
                        .contains(key) || it.normalize_name!!.lowercase(Locale.ROOT)
                        .contains(key) || it.prefix!!.lowercase(Locale.ROOT)
                        .contains(key) || it.phone!!.lowercase(Locale.ROOT)
                        .contains(key)
                }.collect(Collectors.toList()) as ArrayList<Friend>
            }
            notifyDataSetChanged()
        } catch (ex: Exception) {
            ex.message.toString()
        }

    }

    inner class ItemHolder(val binding: ItemAddBestFriendBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemAddBestFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val dataFriend = dataSource[position]
            binding.rbAddBestFriend.isChecked = dataFriend.isCheck!!
            binding.imgFriendAvatar.load(
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
            binding.txtFriendName.text = dataSource[position].full_name

            itemView.setOnClickListener {
                addBestFriendHandler?.chooseBestFriend(dataFriend)
            }

            binding.rbAddBestFriend.setOnClickListener {
                addBestFriendHandler?.chooseBestFriend(dataFriend)
            }
        }

    }

}