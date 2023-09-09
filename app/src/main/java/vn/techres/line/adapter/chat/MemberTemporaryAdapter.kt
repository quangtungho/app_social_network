package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.chat.request.MembersRequest
import vn.techres.line.databinding.ItemMemberChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs

class MemberTemporaryAdapter(var context: Context) :
    RecyclerView.Adapter<MemberTemporaryAdapter.ItemHolder>() {

    private var memberList = ArrayList<MembersRequest>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(memberList: ArrayList<MembersRequest>) {
        this.memberList = memberList
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemMemberChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemMemberChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.rbChooseMember.visibility = View.GONE
            binding.imgMember.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    memberList[position].avatar
                )
            ) {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvNameMember.text = memberList[position].full_name
        }

    }


}