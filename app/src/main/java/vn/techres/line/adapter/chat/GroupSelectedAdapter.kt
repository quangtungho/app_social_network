package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemChooseContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.chat.RemoveGroupHandler

/**
 * @Author: Phạm Văn Nhân
 * @Date: 01/07/2022
 */
class GroupSelectedAdapter(var context: Context) :
    RecyclerView.Adapter<GroupSelectedAdapter.ViewHolder>() {

    private var dataSource = ArrayList<Group>()
    private var removeGroupHandler: RemoveGroupHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setRemoveGroupHandler(removeGroupHandler: RemoveGroupHandler) {
        this.removeGroupHandler = removeGroupHandler
    }

    fun setDataSource(chooseMember: ArrayList<Group>) {
        this.dataSource = chooseMember
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChooseContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(nodeJs, dataSource[position], removeGroupHandler, context)
    }

    class ViewHolder(private val binding: ItemChooseContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            configNodeJs: ConfigNodeJs,
            group: Group,
            removeGroupHandler: RemoveGroupHandler?,
            context: Context
        ) {

            if (group.conversation_type == context.resources.getString(R.string.two_personal)) {
                Utils.getImage(binding.imgAvatar, group.member.avatar!!.medium, configNodeJs)
            }else{
                Utils.getImage(binding.imgAvatar, group.avatar, configNodeJs)
            }
            binding.imgRemove.setOnClickListener {
                removeGroupHandler?.removeGroup(group)
            }
        }
    }
}