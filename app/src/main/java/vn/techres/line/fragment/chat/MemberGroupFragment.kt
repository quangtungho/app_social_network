package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.ChatActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.MemberGroupAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.MemberGroup
import vn.techres.line.data.model.chat.Members
import vn.techres.line.data.model.chat.params.ViceGroupParams
import vn.techres.line.data.model.chat.request.LeaveChatRequest
import vn.techres.line.data.model.chat.request.MembersRequest
import vn.techres.line.data.model.chat.request.group.AuthorizeMemberGroupRequest
import vn.techres.line.data.model.chat.request.group.DemotedGroupViceRequest
import vn.techres.line.data.model.chat.request.group.KickMemberGroupRequest
import vn.techres.line.data.model.chat.request.group.PromotedGroupViceRequest
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.chat.response.MemberGroupResponse
import vn.techres.line.data.model.eventbus.EventBusChangeStatusVice
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.Avatar
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMemberGroupBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.dismissLLoadingDialog
import vn.techres.line.helper.utils.showLoadingDialog
import vn.techres.line.interfaces.MemberGroupHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import java.util.stream.Collectors


class MemberGroupFragment :
    BaseBindingChatFragment<FragmentMemberGroupBinding>(FragmentMemberGroupBinding::inflate, true),
    MemberGroupHandler,
    OnRefreshFragment {
    private var adapter: MemberGroupAdapter? = null
    private var data = MemberGroup()
    private var group = Group()
    private var memberMe = Members()

    //socket
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var configNodeJs = ConfigNodeJs()
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        mSocket?.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityChat?.setOnRefreshFragment(this)
        binding.rcMemberGroup.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rcMemberGroup.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcMemberGroup.itemAnimator) as
                SimpleItemAnimator).supportsChangeAnimations = false
        adapter = MemberGroupAdapter(requireActivity().baseContext)
        adapter?.setMemberGroupHandler(this)
        binding.rcMemberGroup.adapter = adapter
        arguments?.let {
            group = Gson().fromJson(
                it.getString(
                    TechresEnum.GROUP_CHAT.toString()
                ), object :
                    TypeToken<Group>() {}.type
            )
            getMember(group._id ?: "")
        }
        binding.lnAddMember.setOnClickListener {
            val bundleMember = Bundle()
            try {
                bundleMember.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
                bundleMember.putInt(TechResEnumChat.NUM_MEMBER.toString(), data.members.size)
                bundleMember.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
                cacheManager.put(
                    TechresEnum.IS_CHECK_MEMBERS.toString(),
                    TechresEnum.IS_CHECK_MEMBERS.toString()
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            val addMemberGroupChatFragment = AddMemberGroupFragment()
            addMemberGroupChatFragment.arguments = bundleMember
            activityChat?.addOnceFragment(this, addMemberGroupChatFragment)
        }

        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
        }

        binding.svNameUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter?.searchFullText(it) }
                return true
            }
        })
    }

    private fun promotedVice(idMember: Int) {
        val promotedGroupViceRequest = PromotedGroupViceRequest()
        promotedGroupViceRequest.admin_id = user.id
        promotedGroupViceRequest.group_id = group._id
        promotedGroupViceRequest.member_id = idMember
        promotedGroupViceRequest.message_type = TechResEnumChat.TYPE_PROMOTE_VICE.toString()
        promotedGroupViceRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(promotedGroupViceRequest))
            mSocket?.emit(TechResEnumChat.PROMOTE_GROUP_VICE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("PROMOTE_GROUP_VICE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun revokeVice(idMember: Int) {
        val demotedGroupViceRequest = DemotedGroupViceRequest()
        demotedGroupViceRequest.admin_id = user.id
        demotedGroupViceRequest.group_id = group._id
        demotedGroupViceRequest.member_id = idMember
        demotedGroupViceRequest.message_type = TechResEnumChat.TYPE_DEMOTED_VICE.toString()
        demotedGroupViceRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(demotedGroupViceRequest))
            mSocket?.emit(TechResEnumChat.DEMOTED_GROUP_VICE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("DEMOTED_GROUP_VICE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun kickMemberGroup(group: Group, members: Members) {
        val arrayList = ArrayList<Int>()
        members.member_id?.let { arrayList.add(it) }
        val kickMemberGroupRequest = KickMemberGroupRequest()
        kickMemberGroupRequest.member_id = user.id
        kickMemberGroupRequest.group_id = group._id
        kickMemberGroupRequest.members = arrayList
        kickMemberGroupRequest.message_type = TechResEnumChat.TYPE_KICK_MEMBER.toString()
        kickMemberGroupRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(kickMemberGroupRequest))
            mSocket?.emit(TechResEnumChat.REMOVE_USER_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REMOVE_USER_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun authorizeMemberGroup(group: Group, members: Members) {
        val authorizeMemberRequest = AuthorizeMemberGroupRequest()
        authorizeMemberRequest.admin_id = user.id
        authorizeMemberRequest.group_id = group._id
        authorizeMemberRequest.member_id = members.member_id
        authorizeMemberRequest.message_type = TechResEnumChat.TYPE_AUTHORIZE.toString()
        authorizeMemberRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(authorizeMemberRequest))
            mSocket?.emit(TechResEnumChat.ADMIN_AUTHORIZE_MEMBER_ALO_LINE.toString(), jsonObject)
            WriteLog.d("ADMIN_AUTHORIZE_MEMBER_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addFriendSocket(idMember: Int) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = idMember
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun acceptFriendSocket(idMember: Int) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = idMember
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit(TechresEnum.APPROVE_TO_CONTACT.toString(), jsonObject)
            WriteLog.d(TechresEnum.APPROVE_TO_CONTACT.toString(), jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createChatPersonal(memberId: Int) {
        val createChatPersonalRequest = CreateGroupPersonalRequest()
        createChatPersonalRequest.member_id = memberId
        createChatPersonalRequest.admin_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(createChatPersonalRequest))
            mSocket?.emit(
                TechResEnumChat.NEW_GROUP_CREATE_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun checkAuthorizeAdmin(member: Members) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_branch_check)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val tvNoteConFirm = dialog.findViewById(R.id.tvNoteConFirm) as TextView
        val btnDeleteBranch = dialog.findViewById(R.id.btnDeleteBranch) as Button
        val btnBackBranch = dialog.findViewById(R.id.btnBackBranch) as Button
        tvNoteConFirm.text =
            resources.getString(R.string.note_authorize_admin) + member.full_name + "?"
        btnBackBranch.apply {
            text = resources.getString(R.string.move_on)
            setOnClickListener {
                authorizeAdmin(group._id ?: "", member.member_id ?: 0)
                authorizeMemberGroup(group, member)
//                data.members.forEach {
//                    if (it.permissions?.equals(resources.getString(R.string.admin)) == true) {
//                        it.permissions = resources.getString(R.string.member)
//                    }
//                    if (it.member_id == member.member_id) {
//                        it.permissions = resources.getString(R.string.admin)
//                    }
//                }
//                adapter?.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        btnDeleteBranch.apply {
            text = resources.getString(R.string.title_return)
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun checkKickMember(member: Members) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_branch_check)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val tvNoteConFirm = dialog.findViewById(R.id.tvNoteConFirm) as TextView
        val btnDeleteBranch = dialog.findViewById(R.id.btnDeleteBranch) as Button
        val btnBackBranch = dialog.findViewById(R.id.btnBackBranch) as Button
//        tvNoteConFirm.text = resources.getString(R.string.admin_kick_member)
        tvNoteConFirm.text = "Bạn có chắc muốn mời ${member.full_name} ra khỏi nhóm?"
        btnBackBranch.apply {
            text = resources.getString(R.string.kick)
            setOnClickListener {
                val position = data.members.indexOfFirst { it.member_id == member.member_id }
                kickMemberGroup(group, member)
                kickMember(group._id ?: "", member.member_id ?: 0)
                data.members.removeAt(position)
                adapter?.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
        btnDeleteBranch.apply {
            text = resources.getString(R.string.title_return)
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun checkPromotedViceMember(member: Members) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_branch_check)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val tvNoteConFirm = dialog.findViewById(R.id.tvNoteConFirm) as TextView
        val btnDeleteBranch = dialog.findViewById(R.id.btnDeleteBranch) as Button
        val btnBackBranch = dialog.findViewById(R.id.btnBackBranch) as Button
//        tvNoteConFirm.text = resources.getString(R.string.admin_kick_member)
        tvNoteConFirm.text = "Bạn có chắc muốn bổ nhiệm ${member.full_name} làm phó nhóm?"
        btnBackBranch.apply {
            text = "Bổ nhiệm"
            setOnClickListener {
                promotedViceGroup(member.member_id ?: 0)
                dialog.dismiss()
            }
        }
        btnDeleteBranch.apply {
            text = resources.getString(R.string.title_return)
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun bottomSheetMember(member: Members) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_member_group)
        bottomSheetDialog.setCancelable(true)
        val imgClose = bottomSheetDialog.findViewById<ImageButton>(R.id.imgClose)
        val imgAvatar = bottomSheetDialog.findViewById<ImageView>(R.id.imgAvatar)
        val tvName = bottomSheetDialog.findViewById<TextView>(R.id.tvName)
        val imgMessage = bottomSheetDialog.findViewById<ImageButton>(R.id.imgMessage)
        val tvSeenProfile = bottomSheetDialog.findViewById<TextView>(R.id.tvSeenProfile)
        val tvChangePermission = bottomSheetDialog.findViewById<TextView>(R.id.tvChangePermission)
        val tvChangeAuthorize = bottomSheetDialog.findViewById<TextView>(R.id.tvChangeAuthorize)
        val tvKickMember = bottomSheetDialog.findViewById<TextView>(R.id.tvKickMember)
        getImage(imgAvatar, member.avatar?.medium, configNodeJs)
        when (memberMe.permissions) {
            TechResEnumChat.TYPE_MEMBER_ADMIN.toString() -> {
                tvChangeAuthorize?.show()
                tvChangePermission?.show()
                tvKickMember?.show()
                if (member.permissions == TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString()) {
                    tvChangePermission?.apply {
                        text =
                            requireActivity().resources.getString(R.string.quit_the_role_of_vice_group)
                        setOnClickListener {
                            revokeViceGroup(member.member_id ?: 0)
                            bottomSheetDialog.dismiss()
                        }
                    }
                } else {
                    tvChangePermission?.apply {
                        text = requireActivity().resources.getString(R.string.promoted_vice_group)
                        setOnClickListener {
                            checkPromotedViceMember(member)
                            bottomSheetDialog.dismiss()
                        }
                    }
                }
            }
            TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString() -> {
                tvChangeAuthorize?.hide()
                if (member.permissions == TechResEnumChat.TYPE_MEMBER_ADMIN.toString() || member.permissions == TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString()) {
                    tvChangePermission?.hide()
                    tvKickMember?.hide()
                } else {
                    tvChangePermission?.show()
                    tvKickMember?.show()
                }
            }
            TechResEnumChat.TYPE_MEMBER.toString() -> {
                tvChangeAuthorize?.hide()
                tvChangePermission?.hide()
                tvKickMember?.hide()
            }
        }
        tvName?.text = member.full_name ?: ""
        imgClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        tvSeenProfile?.setOnClickListener {
            activityChat?.toProfile(member.member_id ?: 0)
            bottomSheetDialog.dismiss()
        }
        imgMessage?.setOnClickListener {
            val memberList = ArrayList<Int>()
            memberList.add(user.id)
            memberList.add(member.member_id ?: 0)

            createGroupPersonal(memberList, member.member_id ?: 0)
            bottomSheetDialog.dismiss()
        }
        tvChangeAuthorize?.setOnClickListener {
            checkAuthorizeAdmin(member)
            bottomSheetDialog.dismiss()
        }
        tvKickMember?.setOnClickListener {
            checkKickMember(member)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun getMember(idGroup: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url =
            String.format("%s%s%s", "/api/groups/", idGroup, "/get-all-member")
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMemberGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MemberGroupResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())

                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: MemberGroupResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data
                        adapter?.setDataSource(data.members)
                        val memberList = data.members.stream()
                            .filter { x -> user.id == x.member_id }
                            .collect(Collectors.toList())
                        memberMe = memberList[0]
                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    private fun authorizeAdmin(idGroup: String, idMember: Int) {
        val params = AuthorizeAdminParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s%s", "api/groups/", idGroup, "/authorized")
        params.project_id = AppConfig.getProjectChat()
        params.params.admin_id = user.id
        params.params.member_id = idMember
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .authorizeMember(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        getMember(idGroup)
                        org.greenrobot.eventbus.EventBus.getDefault().post(EventBusChangeStatusVice(1))
//                        data.members.forEachIndexed { index, members ->
//                            if (members.member_id == memberMe.member_id) {
//                                members.permissions =
//                                    TechResEnumChat.TYPE_MEMBER.toString()
//                                adapter?.notifyItemChanged(index)
//                            }
//                            return@forEachIndexed
//                        }
//                        data.members.forEachIndexed { index, members ->
//                            if (members.member_id == idMember) {
//                                members.permissions =
//                                    TechResEnumChat.TYPE_MEMBER_ADMIN.toString()
//                                adapter?.notifyItemChanged(index)
//                            }
//                            return@forEachIndexed
//                        }
                    }
                })
        }
    }

    private fun promotedViceGroup(idMember: Int) {
        val params = ViceGroupParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s%s", "/api/groups/", group._id ?: "", "/vice-group")
        params.project_id = AppConfig.getProjectChat()
        params.params.member_id = idMember
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .viceGroup(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            promotedVice(idMember)
//                            data.members.forEachIndexed { index, members ->
//                                if (members.member_id == idMember) {
//                                    members.permissions =
//                                        TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString()
//                                    adapter?.notifyItemChanged(index)
//                                }
//                                return@forEachIndexed
//                            }
                            group._id?.let { it1 -> getMember(it1) }

//                            val position = data.members.indexOfFirst { it.member_id == idMember }
//                            data.members[position].permissions = TechResEnumChat.TYPE_MEMBER_GROUP_VICE.toString()
//                            adapter?.notifyItemChanged(position)
//                            group._id?.let { it1 -> getMember(it1) }
                        }
                    }
                })
        }

    }

    private fun revokeViceGroup(idMember: Int) {
        val params = ViceGroupParams()
        params.http_method = AppConfig.POST
        params.request_url =
            String.format("%s%s%s", "/api/groups/", group._id ?: "", "/demote-vice-group")
        params.project_id = AppConfig.getProjectChat()
        params.params.member_id = idMember
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .viceGroup(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            revokeVice(idMember)
//                            data.members.forEachIndexed { index, members ->
//                                if (members.member_id == idMember) {
//                                    members.permissions =
//                                        TechResEnumChat.TYPE_MEMBER.toString()
//                                    adapter?.notifyItemChanged(index)
//                                }
//                                return@forEachIndexed
//                            }
                            group._id?.let { it1 -> getMember(it1) }

//                            val position = data.members.indexOfFirst { it.member_id == idMember }
//                            data.members[position].permissions = TechResEnumChat.TYPE_MEMBER.toString()
//                            adapter?.notifyItemChanged(position)
//                            group._id?.let { it1 -> getMember(it1) }
                        }
                    }
                })
        }
    }

    private fun kickMember(idGroup: String, idMember: Int) {
        val params = KickMemberParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s%s", "api/groups/", idGroup, "/remove-user")
        params.project_id = AppConfig.getProjectChat()

        val memberList = ArrayList<Int>()
        memberList.add(idMember)

        params.params.member_ids = memberList
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .kickMemberGroup(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
//                            getMember(idGroup)
                            data.members.forEachIndexed { index, members ->
                                if (members.member_id == idMember) {
                                    data.members.removeAt(index)
                                    adapter?.notifyItemRemoved(index)
                                    adapter?.notifyItemRangeChanged(index, data.members.size)
                                }
                                return@forEachIndexed
                            }
                        }
                    }
                })
        }
    }

    private fun acceptFriend(userID: Int?) {
        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = userID


        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .acceptFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            FancyToast.makeText(
                                requireActivity().baseContext,
                                requireActivity().resources.getString(R.string.send_friend_invitation),
                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                false
                            ).show()
                            data.members.forEachIndexed { index, members ->
                                if (members.member_id == userID) {
                                    members.status = 1
                                    adapter?.notifyItemChanged(index)
                                    return@forEachIndexed
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun addFriend(userID: Int?) {
        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = userID
        addFriendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .addFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            Toast.makeText(
                                requireActivity().baseContext,
                                requireActivity().resources.getString(R.string.send_friend_invitation),
                                Toast.LENGTH_LONG
                            ).show()
                            data.members.forEachIndexed { index, members ->
                                if (members.member_id == userID) {
                                    members.status = 2
                                    adapter?.notifyItemChanged(index)
                                    return@forEachIndexed
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireActivity().baseContext,
                                requireActivity().resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    private fun sendRequestFriend(userID: Int?) {
        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = userID


        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .acceptFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            FancyToast.makeText(
                                requireActivity().baseContext,
                                requireActivity().resources.getString(R.string.send_friend_invitation),
                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                false
                            ).show()
                        }
                    }
                })
        }
    }

    override fun onAddFriend(member: Members) {
        addFriendSocket(member.member_id ?: 0)
        addFriend(member.member_id ?: 0)
    }

    override fun onAcceptFriend(member: Members) {
        acceptFriendSocket(member.member_id ?: 0)
        acceptFriend(member.member_id ?: 0)
    }

    override fun onChooseMember(member: Members) {
        if (user.id != member.member_id) {
            bottomSheetMember(member)
        } else {
            activityChat?.toProfile(member.member_id ?: 0)
        }
    }

    override fun onCallBack(bundle: Bundle) {
        val memberList = bundle.getString(TechresEnum.MEMBER_CHAT.toString())
        if (memberList != null) {
            val membersRequest = Gson().fromJson<ArrayList<MembersRequest>>(
                memberList, object :
                    TypeToken<ArrayList<MembersRequest>>() {}.type
            )
            val size = data.members.size
            membersRequest.forEach {
                data.members.add(
                    Members(
                        it.member_id, it.full_name,
                        Avatar(it.avatar, it.avatar, it.avatar),
                        TechResEnumChat.TYPE_MEMBER.toString(), 0
                    )
                )
            }
            adapter?.notifyItemRangeInserted(size, membersRequest.size)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityChat?.removeOnRefreshFragment(this)
    }

    override fun onBackPress(): Boolean {
        return true
    }

    private fun createGroupPersonal(memberList: ArrayList<Int>, memberId: Int) {
        activityChat?.showLoadingDialog()

        val groupPersonalParams = GroupPersonalParams()
        groupPersonalParams.http_method = AppConfig.POST
        groupPersonalParams.project_id = AppConfig.getProjectChat()
        groupPersonalParams.request_url = "/api/groups/create-personal"
        groupPersonalParams.params.members = memberList
        groupPersonalParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createChatPersonal(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<GroupPersonalResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        dismissLLoadingDialog()
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
//                            createChatPersonal(memberId)
                            leaveGroup(group, user)
                            cacheManager.put(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.CHAT_PERSONAL.toString(),
                                TechresEnum.GROUP_CHAT.toString()
                            )
                            val intent = Intent(activityChat, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(group)
                            )
                            startActivity(intent)
                            activityChat?.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        dismissLLoadingDialog()
                    }
                })
        }
    }

    private fun leaveGroup(group: Group, user: User) {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = group._id
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name ?: ""
        leaveChatRequest.avatar = user.avatar_three_image.original
        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_GROUP_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

}