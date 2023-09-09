package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.Members
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemMemberGroupBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.MemberGroupHandler
import java.util.*
import java.util.stream.Collectors

class MemberGroupAdapter(var context: Context) :
    RecyclerView.Adapter<MemberGroupAdapter.ViewHolder>() {
    private var memberList = ArrayList<Members>()
    private var dataSourceTemp = ArrayList<Members>()
    private var memberGroupHandler: MemberGroupHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(memberList: ArrayList<Members>) {
        this.memberList = memberList
        dataSourceTemp = memberList
        notifyDataSetChanged()
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        memberList = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.full_name!!.lowercase(Locale.ROOT)
                    .contains(key)
            }.collect(Collectors.toList()) as ArrayList<Members>
        }
        notifyDataSetChanged()
    }

    fun setMemberGroupHandler(memberGroupHandler: MemberGroupHandler) {
        this.memberGroupHandler = memberGroupHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemberGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, nodeJs, memberList[position], memberGroupHandler)
    }

    class ViewHolder(private val binding: ItemMemberGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            context: Context,
            configNodeJs: ConfigNodeJs,
            member: Members,
            memberGroupHandler: MemberGroupHandler?
        ) {
            getImage(binding.imgMemberGroup, member.avatar?.thumb, configNodeJs)

            binding.tvNameMemberGroup.text = member.full_name
            when (member.permissions) {
                TechResEnumChat.TYPE_MEMBER_ADMIN.toString() -> {
                    binding.imgPermission.show()
                    binding.tvMemberPermission.text = context.resources.getString(R.string.admin)
                    binding.imgPermission.setBackgroundResource(R.drawable.ic_key_admin)
                }
                TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString() -> {
                    binding.imgPermission.show()
                    binding.tvMemberPermission.text =
                        context.resources.getString(R.string.group_vice)
                    binding.imgPermission.setBackgroundResource(R.drawable.ic_key_vice_group)
                }
                TechResEnumChat.TYPE_MEMBER.toString() -> {
                    binding.imgPermission.hide()
                    binding.tvMemberPermission.text = context.resources.getString(R.string.member)
                }
            }
            if (member.status == 4 || member.status == 3 || member.status == 2) {
                binding.imgAddFriend.show()
                when (member.status) {
                    4 -> {
                        binding.imgAddFriend.background = context.resources.getDrawable(R.drawable.ic_add_user, null)
                        binding.imgAddFriend.setOnClickListener {
                            memberGroupHandler?.onAddFriend(member)
                        }
                    }
                    3 -> {
                        binding.imgAddFriend.background = context.resources.getDrawable(R.drawable.ic_add_user, null)
                        binding.imgAddFriend.setOnClickListener {
                            memberGroupHandler?.onAcceptFriend(member)
                        }
                    }
                    2 -> {
                        binding.imgAddFriend.background = context.resources.getDrawable(R.drawable.ic_accept_friend, null)
                    }
                }
            } else {
                binding.imgAddFriend.hide()
            }
            binding.root.setOnClickListener {
                memberGroupHandler?.onChooseMember(member)
            }
        }
    }
}