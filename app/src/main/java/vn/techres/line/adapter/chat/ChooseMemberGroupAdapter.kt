package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.UserChat
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemMemberBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.RemoveMemberHandler

class ChooseMemberGroupAdapter(var context: Context) :
    RecyclerView.Adapter<ChooseMemberGroupAdapter.ViewHolder>() {

    private var chooseMemberList = ArrayList<UserChat>()
    private var removeMemberHandler: RemoveMemberHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setRemoveMemberHandler(removeMemberHandler: RemoveMemberHandler) {
        this.removeMemberHandler = removeMemberHandler
    }

    fun setDataSource(chooseMember: ArrayList<UserChat>) {
        this.chooseMemberList = chooseMember
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return chooseMemberList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(nodeJs, chooseMemberList[position], removeMemberHandler)
    }

    class ViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            configNodeJs: ConfigNodeJs,
            userChat: UserChat,
            removeMemberHandler: RemoveMemberHandler?
        ) {
            getImage(binding.imgChooseMember, userChat.avatar, configNodeJs)
            binding.imgClear.setOnClickListener {
                removeMemberHandler?.removeMember(userChat)
            }
        }
    }
}