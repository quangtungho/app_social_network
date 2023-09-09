package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.Group
import vn.techres.line.databinding.ItemGroupBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.StringUtils
import vn.techres.line.holder.group.GroupHolder
import vn.techres.line.interfaces.GroupChatHandler
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class GroupAdapter(var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var dataSource = ArrayList<Group>()
    private var groupChatHandler: GroupChatHandler? = null
    private var dataSourceTemp = ArrayList<Group>()
    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setGroupChatHandler(groupChatHandler: GroupChatHandler) {
        this.groupChatHandler = groupChatHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Group>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GroupHolder(
            ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GroupHolder).bind(context, user, configNodeJs, dataSource[position], groupChatHandler)
    }



}