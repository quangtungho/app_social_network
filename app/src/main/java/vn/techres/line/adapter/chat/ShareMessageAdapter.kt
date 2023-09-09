package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.chat.Group
import vn.techres.line.databinding.ItemShareMessageBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.chat.ShareMessageHandler
import java.util.*
import java.util.stream.Collectors

class ShareMessageAdapter(var context: Context) :
    RecyclerView.Adapter<ShareMessageAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Group>()
    private var dataSourceTemp = ArrayList<Group>()
    private var shareMessageHandler: ShareMessageHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var type = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Group>, type: Int) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        this.type = type
        notifyDataSetChanged()
    }

    fun setShareMessageHandler(shareMessageHandler: ShareMessageHandler) {
        this.shareMessageHandler = shareMessageHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault())
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.prefix!!.lowercase(Locale.ROOT)
                    .contains(key) || it.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.member.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.member.prefix!!.lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<Group>
        }
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemShareMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemShareMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            var group = dataSource[position]
            if (type == 1) {
                binding.txtSection.visibility = View.VISIBLE
                dataSource.sortWith { o1, o2 ->
                    o1.member.full_name!!.trim().compareTo(o2.member.full_name!!.trim())
                }
                group = dataSource[position]
                if (!group.member.full_name?.trim().equals(""))
                    binding.txtSection.text = group.member.full_name!!.trim().substring(0, 1)
                else
                    binding.txtSection.text = group.member.full_name
                if (position > 0) {
                    val i = position - 1
                    if (!group.member.full_name?.trim()
                            .equals("") && !dataSource[i].member.full_name?.trim()
                            .equals("")
                    )
                        if (i < dataSource.size && group.member.full_name!!.trim()
                                .substring(0, 1) == dataSource[i].member.full_name!!.trim()
                                .substring(0, 1)
                        ) {
                            binding.txtSection.visibility = View.GONE
                        }
                }

                binding.imgGroup.load(String.format("%s%s", nodeJs.api_ads, group.member.avatar?.medium))
                {
                    crossfade(true)
                    scale(Scale.FIT)
                    placeholder(R.drawable.logo_aloline_placeholder)
                    error(R.drawable.logo_aloline_placeholder)
                    size(500, 500)
                }
                binding.tvNameGroup.text = group.member.full_name

            } else {
                binding.imgGroup.load(String.format("%s%s", nodeJs.api_ads, group.avatar))
                {
                    crossfade(true)
                    scale(Scale.FIT)
                    placeholder(R.drawable.logo_aloline_placeholder)
                    error(R.drawable.logo_aloline_placeholder)
                    size(500, 500)
                }
                binding.tvNameGroup.text = dataSource[position].name
            }

            binding.rbChooseGroup.isChecked = group.isCheck!!
            itemView.setOnClickListener {
                shareMessageHandler!!.onChooseGroup(group, type)
            }
            binding.rbChooseGroup.setOnClickListener {
                shareMessageHandler!!.onChooseGroup(group, type)
            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

}