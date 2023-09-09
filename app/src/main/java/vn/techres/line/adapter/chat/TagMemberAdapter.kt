package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.UserChat
import vn.techres.line.databinding.ItemMemberChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.TagMemberHandler
import java.util.*
import java.util.stream.Collectors

class TagMemberAdapter(var context: Context) : RecyclerView.Adapter<TagMemberAdapter.ItemHolder>() {
    private var tagMemberList = ArrayList<UserChat>()
    private var tagMemberHandler: TagMemberHandler? = null
    private var dataSourceTemp = ArrayList<UserChat>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setTagMemberHandler(tagMemberHandler: TagMemberHandler) {
        this.tagMemberHandler = tagMemberHandler
    }

    fun setDataSource(tagMember: ArrayList<UserChat>) {
        this.tagMemberList = tagMember
        dataSourceTemp = tagMember
        notifyDataSetChanged()
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        tagMemberList = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.normalize_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.prefix!!.lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<UserChat>
        }
        notifyDataSetChanged()
    }

    fun getDataSource() : ArrayList<UserChat>{
        return tagMemberList
    }
    inner class ItemHolder(val binding: ItemMemberChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemMemberChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return tagMemberList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val tagMember = tagMemberList[position]
            binding.imgMember.load(String.format("%s%s", nodeJs.api_ads, tagMember.avatar))
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(300, 300)
            }
            binding.tvNameMember.text = tagMember.name
            binding.rbChooseMember.isChecked = tagMember.isCheck!!

            itemView.setOnClickListener {
                tagMemberHandler?.chooseMember(tagMember)
            }

            binding.rbChooseMember.setOnClickListener {
                tagMemberHandler?.chooseMember(tagMember)
            }
        }

    }

}