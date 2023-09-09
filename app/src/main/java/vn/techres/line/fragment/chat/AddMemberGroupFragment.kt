package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.ChooseMemberGroupAdapter
import vn.techres.line.adapter.chat.TagMemberAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.local.contact.myfriend.MyFriendResponse
import vn.techres.line.data.model.UserChat
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.request.MembersRequest
import vn.techres.line.data.model.chat.request.group.AddMembersGroupRequest
import vn.techres.line.data.model.friend.response.FriendResponse
import vn.techres.line.data.model.params.AddNewUserParams
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentAddMemberGroupChatBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.RemoveMemberHandler
import vn.techres.line.interfaces.TagMemberHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import kotlin.math.ceil

/**
 * A simple [Fragment] subclass.
 */
class AddMemberGroupFragment :
    BaseBindingChatFragment<FragmentAddMemberGroupChatBinding>(FragmentAddMemberGroupChatBinding::inflate),
    TagMemberHandler, RemoveMemberHandler, OnRefreshFragment {
    private var userList = ArrayList<UserChat>()
    private var chooseMemberList = ArrayList<UserChat>()
    private var members = ArrayList<MembersRequest>()
    private var tagMemberAdapter: TagMemberAdapter? = null
    private var chooseMemberGroupAdapter: ChooseMemberGroupAdapter? = null
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var group = Group()
    private var total = 0F
    private var limit = 20
    private var page = 1
    private var numMember = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityChat?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)

        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        mSocket?.connect()
        hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.rclTag.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        binding.rclTag.itemAnimator?.changeDuration = 0

        (Objects.requireNonNull(binding.rclTag.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false

        binding.rcChooseMember.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.HORIZONTAL, false) }
        binding.rcChooseMember.itemAnimator?.changeDuration = 0

        (Objects.requireNonNull(binding.rcChooseMember.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false

        tagMemberAdapter = TagMemberAdapter(requireActivity().baseContext)
        chooseMemberGroupAdapter = ChooseMemberGroupAdapter(requireActivity().baseContext)
        tagMemberAdapter?.setTagMemberHandler(this)
        chooseMemberGroupAdapter?.setRemoveMemberHandler(this)
        binding.rclTag.adapter = tagMemberAdapter
        binding.rcChooseMember.adapter = chooseMemberGroupAdapter
        activityChat?.setOnRefreshFragment(this)

        numMember = arguments!!.getInt(TechResEnumChat.NUM_MEMBER.toString())

        binding.txtNumMember.text = "0/${200-numMember}"

        if (userList.size == 0) {
            if (cacheManager.get(TechresEnum.IS_CHECK_MEMBERS.toString()) != TechresEnum.IS_CHECK_MEMBERS.toString()) {
                getListFriend(page)
            } else {
                group = Gson().fromJson(
                    cacheManager.get(TechresEnum.GROUP_CHAT.toString()), object :
                        TypeToken<Group>() {}.type
                )

                getListFriendOutGroup(page, group)
            }
        } else {
            if (members.isNotEmpty()) {
                loop@ for (i in userList.indices) {
                    for (j in members.indices) {
                        if (userList[i].id == members[j].member_id) {
                            userList[i].isCheck = true
                            continue@loop
                        }
                    }
                }
            }
            tagMemberAdapter?.setDataSource(userList)
        }

        chooseMemberGroupAdapter?.setDataSource(chooseMemberList)

        setListener()
    }

    private fun setListener() {
        binding.rclTag.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState && page <= ceil(
                        (total / limit).toDouble()
                    )
                ) {
                    page++
                    if (cacheManager.get(TechresEnum.IS_CHECK_MEMBERS.toString()) != TechresEnum.IS_CHECK_MEMBERS.toString()) {
                        getListFriend(page)
                    } else {
                        getListFriendOutGroup(page, group)
                    }
                }
            }
        })
        binding.svNameUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { tagMemberAdapter?.searchFullText(it) }
                return true
            }
        })

        binding.tvContinue.setOnClickListener {
            hideKeyboard()
            val bundle = Bundle()
            val memberList = addMember()
            val member = MembersRequest()
            member.avatar = user.avatar_three_image.original
            member.member_id = user.id
            member.full_name = user.name
            memberList.add(member)
            bundle.putString(TechresEnum.MEMBER_CHAT.toString(), Gson().toJson(memberList))
            when (cacheManager.get(TechresEnum.IS_CHECK_MEMBERS.toString())) {
                TechresEnum.IS_CHECK_MEMBERS.toString() -> {
                    activityChat?.removeOnRefreshFragment(this)
                    addNewUser()
                    bundle.putString(TechresEnum.MEMBER_CHAT.toString(), Gson().toJson(addMember()))
                    activityChat?.getOnRefreshFragment()?.onCallBack(bundle)
                    activityChat?.supportFragmentManager?.popBackStack()
                }
                else -> {
                    if (memberList.size > 2) {
                        cacheManager.put(
                            TechresEnum.CREATE_PROFILE_GROUP_CHAT.toString(),
                            TechresEnum.CREATE_GROUP_CHAT.toString()
                        )
                        val createProfileGroupFragment = CreateProfileGroupFragment()
                        createProfileGroupFragment.arguments = bundle
                        activityChat?.addOnceFragment(
                            this,
                            createProfileGroupFragment
                        )
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            requireActivity().baseContext.getString(R.string.please_choose_2_or_more_members),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        binding.imgBack.setOnClickListener {
            activityChat?.onBackPressed()
        }
    }

    private fun addMemberGroup(group: Group) {
        val addMembersGroup = AddMembersGroupRequest()
        addMembersGroup.members = chooseMemberList.map { it.id } as ArrayList<Int>
        addMembersGroup.member_id = user.id
        addMembersGroup.group_id = group._id
        addMembersGroup.message_type = TechResEnumChat.TYPE_ADD_NEW_USER.toString()
        addMembersGroup.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(addMembersGroup))
            mSocket!!.emit(TechResEnumChat.ADD_NEW_USER_ALO_LINE.toString(), jsonObject)
            WriteLog.e("ADD_NEW_USER_ALO_LINE ", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addMember(): ArrayList<MembersRequest> {
        val members = ArrayList<MembersRequest>()
        chooseMemberList.forEach {
            val member = MembersRequest()
            member.avatar = it.avatar
            member.member_id = it.id
            member.full_name = it.name
            members.add(member)
        }
        return members
    }


    private fun getListFriend(page: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s",
            "/api/friends?user_id=",
            user.id,
            "&limit=",
            limit,
            "&page=",
            page
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListFriend(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MyFriendResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }

                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUBSCRIBE", "Subscribe")
                }

                override fun onNext(response: MyFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        total = data.total_record!!.toFloat()
                        data.list.forEach { item ->
                            val userChat = UserChat()
                            userChat.id = item.user_id
                            userChat.avatar = item.avatar.original
                            userChat.name = item.full_name
                            userChat.isCheck = false
                            userList.add(userChat)
                        }
                        tagMemberAdapter?.setDataSource(userList)
                    }
                }
            })
    }

    private fun getListFriendOutGroup(page: Int, group: Group) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT

        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s",
            "/api/friends/",
            group._id,
            "/friend-not-in-group-chat?page=",
            page,
            "&limit=",
            limit
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getFriendNotInGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }

                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUBSCRIBE", "Subscribe")
                }

                override fun onNext(response: FriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        total = data.total_record!!.toFloat()
                        data.list.forEach { item ->
                            val userChat = UserChat()
                            userChat.id = item.user_id
                            userChat.avatar = item.avatar.original
                            userChat.name = item.full_name
                            userChat.normalize_name = item.normalize_name
                            userChat.prefix = item.prefix
                            userChat.isCheck = false
                            userList.add(userChat)
                        }
                        tagMemberAdapter?.setDataSource(userList)
                    }
                }
            })
    }

    private fun addNewUser() {
        val params = AddNewUserParams()
        params.http_method = AppConfig.POST
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = String.format("%s%s%s", "/api/groups/", group._id, "/add-user")
        val members = chooseMemberList.map { it.id ?: 0 }

        if (members.isNotEmpty()) {
            params.params.members = members

            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .addNewUser(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {
                        WriteLog.d("COMPLETE", "Complete")
                    }

                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {
                        WriteLog.d("SUBSCRIBE", "Subscribe")
                    }

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            addMemberGroup(group)
                        }
                    }
                })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun chooseMember(userChat: UserChat) {
        val position = userList.indexOfFirst { it.id == userChat.id }
        if (userChat.isCheck == true) {
            chooseMemberList.removeAt(chooseMemberList.indexOfFirst { it.id == userChat.id })
            userList[position].isCheck = false
        } else {
            if (chooseMemberList.size < 200-numMember){
                chooseMemberList.add(userChat)
                userList[position].isCheck = true
            }else{
                Toast.makeText(context, "Chỉ cho phép tối đa 200 thành viên trong nhóm", Toast.LENGTH_SHORT).show()
            }

        }
        chooseMemberGroupAdapter?.notifyDataSetChanged()
        tagMemberAdapter?.notifyItemChanged(position)
        if (chooseMemberList.size > 0) {
            binding.cartView.show()
            binding.rcChooseMember.scrollToPosition(chooseMemberList.size - 1)
        } else {
            binding.cartView.hide()
        }
        showNumMember()

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun removeMember(userChat: UserChat) {
        val position = chooseMemberList.indexOfFirst { it.id == userChat.id }
        val positionTag = userList.indexOfFirst { it.id == userChat.id }
        chooseMemberList.removeAt(position)
        userList[positionTag].isCheck = false
        tagMemberAdapter?.notifyItemChanged(positionTag)
        chooseMemberGroupAdapter?.notifyDataSetChanged()
        if (chooseMemberList.size > 0) {
            binding.cartView.show()
        } else {
            binding.cartView.hide()
        }
        showNumMember()
    }

    private fun showNumMember(){
        if (numMember == 0){
            binding.txtNumMember.text = "${chooseMemberList.size}/200"
        }else{
            binding.txtNumMember.text = "${chooseMemberList.size}/${200-numMember}"
        }
    }

    override fun onCallBack(bundle: Bundle) {

        val memberList = bundle.getString(TechresEnum.MEMBER_CHAT.toString())
        if (memberList != null) {
            this.members = Gson().fromJson(memberList, object :
                TypeToken<ArrayList<MembersRequest>>() {}.type)
        }
    }


    override fun onBackPress(): Boolean {
        return true
    }

}