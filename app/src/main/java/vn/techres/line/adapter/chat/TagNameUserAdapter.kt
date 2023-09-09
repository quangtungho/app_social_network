package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import kotlinx.android.synthetic.main.item_tag_member.view.*
import vn.techres.line.R
import vn.techres.line.data.model.chat.Members
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.chat.TagNameUserHandler
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class TagNameUserAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val header = 0
    private val body = 1
    private var dataSource = ArrayList<Members>()
    private var dataSourceTemp = ArrayList<Members>()
    private var tagNameUserHandler : TagNameUserHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource : ArrayList<Members>){
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun setTagNameUserHandler(tagNameUserHandler : TagNameUserHandler){
        this.tagNameUserHandler = tagNameUserHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) : Boolean {
        val key = keyword.lowercase(Locale.ROOT).trim()
        val isCheck: Boolean
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            isCheck = false
            dataSourceTemp
        } else {
            isCheck = true
            dataSourceTemp.stream().filter {
                it.full_name!!.lowercase(Locale.ROOT)
                    .contains(key) || it.prefix!!
                    .contains(key) || it.normalize_name!!
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<Members>
        }
        notifyDataSetChanged()
        return isCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            header ->{
                val view = LayoutInflater.from(context).inflate(R.layout.item_tag_all, parent, false)
                HeaderHolder(view)
            }
            body ->{
                val view = LayoutInflater.from(context).inflate(R.layout.item_tag_member, parent, false)
                BodyHolder(view)
            }
            else ->
            {
                val view = LayoutInflater.from(context).inflate(R.layout.item_tag_all, parent, false)
                HeaderHolder(view)
            }
        }
    }

    private fun onBindHeader(member: Members, holder: HeaderHolder){
        holder.itemView.setOnClickListener {
            tagNameUserHandler!!.onTagName(member)
        }
    }

    private fun onBindBody(member: Members, holder: BodyHolder){
        holder.imgAvatarMember.load(String.format("%s%s", nodeJs.api_ads, member.avatar?.medium)){
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
        }
        holder.tvNameMember.text = member.full_name
        holder.itemView.setOnClickListener {
            tagNameUserHandler?.onTagName(member)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val member = dataSource[position]
        when(getItemViewType(position)){
            header ->{
                onBindHeader(member, holder as HeaderHolder)
            }
            body ->{
                onBindBody(member, holder as BodyHolder)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataSource[position].member_id == -1)
            header else body
    }

    class HeaderHolder(view : View) : RecyclerView.ViewHolder(view)

    class BodyHolder(view : View) : RecyclerView.ViewHolder(view){
        var imgAvatarMember = view.imgAvatarMember
        var tvNameMember = view.tvNameMember
    }
}