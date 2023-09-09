package vn.techres.line.fragment.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.ChatActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.MainActivity.Companion.isConnection
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.FriendsOnlineChatAdapter
import vn.techres.line.adapter.chat.GroupAdapter
import vn.techres.line.base.BaseBindingStubFragment
import vn.techres.line.data.local.contact.friendonline.FriendOnlineResponse
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.GroupsResponse
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.chat.params.GroupParams
import vn.techres.line.data.model.chat.request.DeleteGroupChatRequest
import vn.techres.line.data.model.chat.request.RemoveGroupParty
import vn.techres.line.data.model.chat.request.group.GroupRequest
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.chat.response.MessageNotSeenResponse
import vn.techres.line.data.model.chat.response.RemoveGroupResponse
import vn.techres.line.data.model.eventbus.EventBusGroup
import vn.techres.line.data.model.eventbus.EventBusLeaveGroup
import vn.techres.line.data.model.eventbus.EventBusLeaveRoom
import vn.techres.line.data.model.eventbus.EventBusRemoveGroupInGroup
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.EventBusFinishSyncGroup
import vn.techres.line.data.model.utils.ScrollScreen
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentGroupChatBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.permissionsIsGranted
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.FriendOnlineHandler
import vn.techres.line.interfaces.GroupChatHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.interfaces.util.EventBusConnectionInternet
import vn.techres.line.persentation.GroupViewModel
import vn.techres.line.roomdatabase.sync.SyncDataUpdateGroup
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import java.util.stream.Collectors


@SuppressLint("UseRequireInsteadOfGet")
class GroupChatFragment :
    BaseBindingStubFragment<FragmentGroupChatBinding>(R.layout.fragment_group_chat, true),
    SearchView.OnQueryTextListener, GroupChatHandler,
    FriendOnlineHandler {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private val groupViewModel by activityViewModels<GroupViewModel>()

    //    private lateinit var groupViewModel: GroupViewModel
    private lateinit var layoutManagerGroup: LinearLayoutManager

    private lateinit var layoutManagerFriend: LinearLayoutManager
    //permission

    private val requiredPermissionAudio = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private val requiredPermissionAudioCode = 102

    private val requiredPermissionsVideoCall = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        //        Manifest.permission.USE_FULL_SCREEN_INTENT
    )
    private val requiredPermissionsVideoCallCode = 100

    private val requiredPermissionsAudio = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private val requiredPermissionsAudioCode = 200

    private var scrollScreen = ScrollScreen()
    private var group = Group()
    private var adapter: GroupAdapter? = null
    private var friendsOnlineChatAdapter: FriendsOnlineChatAdapter? = null
    private var groups = ArrayList<Group>()
    private var friendListOnline = ArrayList<Friend>()

    private var page = 1
    private var currentLimit = 30
    private var totalRecord = 0


    //socket
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var user = User()
    private var init = 0
    private var lastPosition = 0
    private var firstPosition = 0
    private var listGroup = ArrayList<Group>()
    private var isLoading = false
    private var isConnectionInternet = true
    private var isLocal = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity().application)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        mSocket?.connect()
        if (cacheManager.get(TechresEnum.LOCK_SOCKET.toString()) == TechresEnum.LOCK_SOCKET.toString()) {
            onSocket()
        }
        isConnectionInternet = isConnection
//        isConnectionInternet = true
    }

    override fun onCreateViewAfterViewStubInflated(savedInstanceState: Bundle?) {
        mainActivity?.setOnBackClick(this)
        adapter = GroupAdapter(requireActivity())
        adapter?.setDataSource(groups)
        friendsOnlineChatAdapter = FriendsOnlineChatAdapter(requireActivity())
        friendsOnlineChatAdapter?.setFriendOnlineHandler(this)
        adapter?.setGroupChatHandler(this)
        layoutManagerGroup = LinearLayoutManager(
            requireActivity().baseContext,
            RecyclerView.VERTICAL,
            false
        )
        stubBinding?.rcGroup?.layoutManager = layoutManagerGroup

        stubBinding?.rcFriend?.layoutManager =
            LinearLayoutManager(requireActivity().baseContext, RecyclerView.HORIZONTAL, false)

        stubBinding?.rcGroup?.itemAnimator?.changeDuration = 0
        (stubBinding?.rcGroup?.itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations =
            false
        stubBinding?.rcGroup?.adapter = adapter
        stubBinding?.rcGroup?.setHasFixedSize(true)
        stubBinding?.rcFriend?.adapter = friendsOnlineChatAdapter

        getListFriendOnline()
        if (isConnectionInternet) {
            getGroups(true, currentLimit, "")
            setupDataSyncLocal()
        } else
            getMessageDB()
        setListener()
        init = 1
    }

    override fun onResume() {
        super.onResume()
        if (init == 1) {
            getListFriendOnline()
//            getGroups(page, currentLimit)
//            if (isConnectionInternet)
//            {
//                getGroups(true, currentLimit, "")
//                setupDataSyncLocal()
//            }
        }
        onClickTab(scrollScreen)
        mainActivity?.setOnBackClick(this)
        stubBinding?.searchView?.clearFocus()
    }

    @Subscribe()
    fun onClickTab(scrollScreen: ScrollScreen) {
        if (scrollScreen.currentPage == 3) {
            if (scrollScreen.click == 3) {
                stubBinding?.rcGroup?.scrollToPosition(0)
            }
        }
    }


    private fun setListener() {
        stubBinding?.swipeRefresh?.isEnabled = isConnectionInternet
        stubBinding?.swipeRefresh?.setOnRefreshListener {
            getListFriendOnline()
            isLoading = false
            if (isConnectionInternet) {
                getGroups(true, currentLimit, "")
                setupDataSyncLocal()
            }
        }

        stubBinding?.searchView?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapter?.searchFullText(newText)
                }
                return true
            }
        })

        stubBinding?.rlCreateGroup?.setOnClickListener {
            cacheManager.put(
                TechresEnum.IS_CHECK_MEMBERS.toString(),
                TechresEnum.CREATE_GROUP_CHAT.toString()
            )
            val intent = Intent(mainActivity, ChatActivity::class.java)
            intent.putExtra(
                TechresEnum.CREATE_GROUP_CHAT.toString(),
                TechresEnum.CREATE_GROUP_CHAT.toString()
            )
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.rcGroup?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastPosition =
                    (Objects.requireNonNull(stubBinding?.rcGroup?.layoutManager) as LinearLayoutManager).findLastVisibleItemPosition()
                firstPosition =
                    (Objects.requireNonNull(stubBinding?.rcGroup?.layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
                WriteLog.d("lastPosition ", "$lastPosition")
                if (!isLoading)
                    if (lastPosition >= groups.size - 5) {
                        isLoading = true
                        if (isLocal)
                            getListGroupNext()
                        else {
                            if (groups.size < totalRecord)
                                groups[groups.size - 1]._id?.let {
                                    getGroups(
                                        false,
                                        currentLimit,
                                        it
                                    )
                                }
                            else
                                isLoading = false
                        }
                    }
            }
        })
    }

    private fun setupDataSyncLocal() {
        val builder = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
        val dataWorker = Data.Builder()
            .build()
        val syncDataUpdateGroup: WorkRequest =
            OneTimeWorkRequestBuilder<SyncDataUpdateGroup>()
                .addTag("SyncDataUpdateGroup")
                .setConstraints(builder.build())
                .setInputData(dataWorker)
                .build()
        WorkManager
            .getInstance(TechResApplication.applicationContext())
            .enqueue(syncDataUpdateGroup)
    }

    private fun requestMultiplePermissionAudio() {
        requestPermissions(requiredPermissionAudio, requiredPermissionAudioCode,
            object : RequestPermissionListener {
                override fun onCallPermissionFirst(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
//                    dialogPermission(
//                        String.format(
//                            requireActivity().resources.getString(R.string.title_permission),
//                            namePermission
//                        ),
//                        String.format(
//                            requireActivity().resources.getString(R.string.note_permission_denied),
//                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
//                            namePermission,
//                            namePermission
//                        ),
//                        requireActivity().resources.getString(R.string.title_permission_micro_step_two),
//                        R.drawable.ic_baseline_mic_24, "", 0, object : DialogPermissionHandler {
//                            override fun confirmDialog() {
//                                openAppSetting.invoke()
//                            }
//
//                            override fun dismissDialog() {
//                            }
//
//                        }
//                    )
                }

                override fun onPermissionGranted() {
//                    val roomId = Utils.getRandomString(20)
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_PHONE.toString(),
//                        memberCreate = user.id
//                    )
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_PHONE.toString()
//                    )
//                    ChatUtils.connectToRoom(
//                        requireContext(),
//                        group._id,
//                        group.member.member_id ?: 0,
//                        group.member.full_name,
//                        group.member.avatar!!.original,
//                        user.id,
//                        1,
//                        "call_audio",
//                        "aloline"
//                    )
                }

            })
    }

    private fun requestMultiplePermissionVideoCall() {
        requestPermissions(requiredPermissionsVideoCall, requiredPermissionsVideoCallCode,
            object : RequestPermissionListener {
                override fun onCallPermissionFirst(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_baseline_mic_24,
                        requireActivity().resources.getString(R.string.title_permission_micro_step_three),
                        R.drawable.ic_pink_camera,
                        object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionGranted() {
//                    val roomId = Utils.getRandomString(20)
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_VIDEO.toString(),
//                        memberCreate = user.id
//                    )
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_VIDEO.toString()
//                    )
//                    ChatUtils.connectToRoom(
//                        requireContext(),
//                        group._id,
//                        group.member.member_id ?: 0,
//                        group.member.full_name,
//                        group.member.avatar!!.original,
//                        user.id,
//                        1,
//                        "call_video",
//                        "aloline"
//                    )
                }

            }
        )
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

    private fun deleteGroupParty(group: Group) {
        val deleteGroupChatRequest = DeleteGroupChatRequest()
        deleteGroupChatRequest.group_id = group._id
        deleteGroupChatRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(deleteGroupChatRequest))
            mSocket?.emit(TechresEnum.DELETE_GROUP_PARTY.toString(), jsonObject)
            WriteLog.d("DELETE_GROUP_PARTY", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun removeGroupParty(group: Group) {
        val removeGroupParty = RemoveGroupParty()
        val sender = Sender()
        sender.full_name = user.full_name
        sender.member_id = user.id
        sender.avatar?.medium = user.avatar_three_image.original
        removeGroupParty.group_id = group._id
        removeGroupParty.sender = sender
        try {
            val jsonObject = JSONObject(Gson().toJson(removeGroupParty))
            mSocket?.emit(TechResEnumChat.REMOVE_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REMOVE_GROUP_PARTY", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onSocket() {

        cacheManager.put(TechresEnum.LOCK_SOCKET.toString(), "")
        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.RES_NEW_GROUP_CREATE_ALO_LINE.toString(),
                user.id
            )
        ) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("RES_NEW_GROUP_CREATE_ALO_LINE", args[0].toString())
                var group = Gson().fromJson<Group>(args[0].toString(), object :
                    TypeToken<Group>() {}.type)
                val g = groups.stream().filter { it._id == group._id }.collect(Collectors.toList()) as ArrayList<Group>
                if(g.size == 0) {
                    if (group.admin_id == user.id) {
                        group.number_message_not_seen = 0
                    }
                    group.user_id = user.id
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        TechResApplication.applicationContext().getGroupDao()?.insertOne(group)
                    }
                    groups.add(0, group)
                    adapter?.notifyItemInserted(0)
                    if (firstPosition < 3)
                        stubBinding?.rcGroup?.smoothScrollToPosition(0)
                }
            }
        }

        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.RES_NEW_GROUP_CREATE_ALO_LINE_PERSONAL.toString(),
                user.id
            )
        ) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("RES_NEW_GROUP_CREATE_ALO_LINE_PERSONAL", args[0].toString())
                var group = Gson().fromJson(args[0].toString(), Group::class.java)
                val g = groups.stream().filter { it._id == group._id }.collect(Collectors.toList()) as ArrayList<Group>
                if(g.size == 0) {
                    if (group.admin_id == user.id) {
                        group.number_message_not_seen = 0
                    }
                    group.user_id = user.id
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        TechResApplication.applicationContext().getGroupDao()?.insertOne(group)
                    }
                    groups.add(0, group)
                    adapter?.notifyItemInserted(0)
                    if (firstPosition < 3)
                        stubBinding?.rcGroup?.smoothScrollToPosition(0)
                }
            }
        }
        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.MESSAGE_NOT_SEEN_BY_ONE_GROUP.toString(),
                user.id
            )
        ) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("MESSAGE_NOT_SEEN_BY_ONE_GROUP", args[0].toString())
                val messageNotSeenResponse = Gson().fromJson<MessageNotSeenResponse>(
                    args[0].toString(),
                    object :
                        TypeToken<MessageNotSeenResponse>() {}.type
                )
                var group: Group
                TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                    val newGroup = messageNotSeenResponse._id?.let { it1 ->
                        TechResApplication.applicationContext().getGroupDao()?.getOneGroup(
                            it1, user.id
                        )
                    }
                    if (newGroup != null && newGroup.size > 0) {
                        group = newGroup[0]
                        group.number_message_not_seen = messageNotSeenResponse.number
                        group.last_message = messageNotSeenResponse.last_message
                        group.last_message_type = messageNotSeenResponse.last_message_type
                        group.created_last_message = messageNotSeenResponse.created_last_message
                        group.status_last_message = messageNotSeenResponse.status_last_message
                        group.user_name_last_message = messageNotSeenResponse.user_name_last_message
                        group.user_last_message_id = messageNotSeenResponse.user_last_message_id
                        group.name = messageNotSeenResponse.name
                        group.user_id = user.id
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            group._id?.let {
                                TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                                    it,
                                    user.id
                                )
                            }
                            TechResApplication.applicationContext().getGroupDao()?.insertOne(
                                group
                            )
                        }
                        var positionGroup = -1
                        var groupChange = Group()
                        groups.forEachIndexed { index, element ->
                            if (element._id == group._id) {
                                element.number_message_not_seen = group.number_message_not_seen
                                element.last_message = group.last_message
                                element.last_message_type = group.last_message_type
                                element.created_last_message = group.created_last_message
                                element.status_last_message = group.status_last_message
                                element.user_name_last_message = group.user_name_last_message
                                element.user_last_message_id = group.user_last_message_id
                                element.name = group.name
                                positionGroup = index
                                groupChange = element
                                return@forEachIndexed
                            }
                        }
                        if (positionGroup != -1) {
                            groups.removeAt(positionGroup)
                            adapter?.notifyItemRemoved(positionGroup)
                            adapter?.notifyItemRangeChanged(positionGroup, groups.size)
                            groups.add(0, groupChange)
                            adapter?.notifyItemInserted(0)
                        }
                    }
                }
                adapter?.notifyDataSetChanged()
                if (firstPosition < 3)
                    stubBinding?.rcGroup?.smoothScrollToPosition(0)

            }
        }
        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.MESSAGE_NOT_SEEN_BY_ONE_GROUP_PERSONAL.toString(),
                user.id
            )
        ) {
            mainActivity?.runOnUiThread {
                WriteLog.d("MESSAGE_NOT_SEEN_BY_ONE_GROUP_PERSONAL", it[0].toString())
                val messageNotSeenResponse = Gson().fromJson<MessageNotSeenResponse>(
                    it[0].toString(),
                    object :
                        TypeToken<MessageNotSeenResponse>() {}.type
                )
                var group: Group
                TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                    val newGroup = messageNotSeenResponse._id?.let { it1 ->
                        TechResApplication.applicationContext().getGroupDao()?.getOneGroup(
                            it1, user.id
                        )
                    }
                    if (newGroup != null && newGroup.size > 0) {
                        group = newGroup[0]
                        group.number_message_not_seen = messageNotSeenResponse.number
                        group.last_message = messageNotSeenResponse.last_message
                        group.last_message_type = messageNotSeenResponse.last_message_type
                        group.created_last_message = messageNotSeenResponse.created_last_message
                        group.status_last_message = messageNotSeenResponse.status_last_message
                        group.user_name_last_message = messageNotSeenResponse.user_name_last_message
                        group.user_last_message_id = messageNotSeenResponse.user_last_message_id
                        group.name = messageNotSeenResponse.name
                        group.user_id = user.id
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            group._id?.let {
                                TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                                    it,
                                    user.id
                                )
                            }
                            TechResApplication.applicationContext().getGroupDao()?.insertOne(
                                group
                            )
                        }
                        var positionGroup = -1
                        var groupChange = Group()
                        groups.forEachIndexed { index, element ->
                            if (element._id == group._id) {
                                element.number_message_not_seen = group.number_message_not_seen
                                element.last_message = group.last_message
                                element.last_message_type = group.last_message_type
                                element.created_last_message = group.created_last_message
                                element.status_last_message = group.status_last_message
                                element.user_name_last_message = group.user_name_last_message
                                element.user_last_message_id = group.user_last_message_id
                                element.name = group.name
                                positionGroup = index
                                groupChange = element
                                return@forEachIndexed
                            }
                        }
                        if (positionGroup != -1) {
                            groups.removeAt(positionGroup)
                            adapter?.notifyItemRemoved(positionGroup)
                            adapter?.notifyItemRangeChanged(positionGroup, groups.size)
                            groups.add(0, groupChange)
                            adapter?.notifyItemInserted(0)
                        }
                    }
                }
                if (firstPosition < 3)
                    stubBinding?.rcGroup?.smoothScrollToPosition(0)
//                groupViewModel.changePosition(messageNotSeenResponse)
            }
        }
        mSocket?.on(
            String.format(
                "%s/%s",
                TechresEnum.RES_REMOVE_USER_OUT_ROOM_ALOLINE.toString(),
                user.id
            )
        ) {
            mainActivity?.runOnUiThread {
                WriteLog.e("RES_REMOVE_USER_OUT_ROOM_ALOLINE", it[0].toString())
                val group = Gson().fromJson(it[0].toString(), Group::class.java)
//                groupViewModel.deleteItem(group._id ?: "")
                TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                    group._id?.let {
                        TechResApplication.applicationContext().getMessageDao()
                            ?.deleteMessageGroup(it, user.id)
                        TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                            it, user.id
                        )
                    }
                }
                loop@ for (i in groups.indices) {
                    if (groups[i]._id == group._id) {
                        groups.removeAt(i)
                        adapter?.notifyItemRemoved(i)
                        adapter?.notifyItemRangeChanged(i, groups.size)
                        break@loop
                    }
                }
            }
        }

        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.RES_REMOVE_GROUP_ALO_LINE.toString(),
                user.id
            )
        ) { arg ->
            mainActivity?.runOnUiThread {
                WriteLog.d("RES_REMOVE_GROUP_PARTY", arg[0].toString())
                val removeGroup =
                    Gson().fromJson(
                        arg[0].toString(),
                        RemoveGroupResponse::class.java
                    )
                TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                    removeGroup.receiver_id?.let {
                        TechResApplication.applicationContext().getMessageDao()
                            ?.deleteMessageGroup(it, user.id)
                        TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                            it, user.id
                        )
                    }
                }
                removeGroup.receiver_id?.let { WriteLog.d("group id ", it) }
                if (group._id.equals(removeGroup.receiver_id)) {
                    EventBus.getDefault().post(EventBusRemoveGroupInGroup(true))
                }
                loop@ for (i in groups.indices) {
                    if (groups[i]._id == removeGroup.receiver_id) {
                        groups.removeAt(i)
                        adapter?.notifyItemRemoved(i)
                        adapter?.notifyItemRangeChanged(i, groups.size)
                        break@loop
                    }
                }
//                groupViewModel.deleteItem(removeGroup.group_id ?: "")
            }
        }
    }


    private fun getGroups(isLoadNew: Boolean, limit: Int, position: String) {
        stubBinding?.loading?.rlLoading?.show()
        var request = GroupRequest(position, limit)
        val baseRequest = GroupParams(request)
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/group-pagination"
//            String.format("%s%s%s%s", "/api/groups?limit=", limit, "&page=", page)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getGroups(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GroupsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    stubBinding?.loading?.rlLoading?.hide()
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: GroupsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        isLoading = false
                        val data = response.data.list
                        if (isLoadNew) {
                            groups.clear()
                            groups.addAll(data)
                            adapter?.notifyDataSetChanged()
                        } else {
                            val position = groups.size
                            groups.addAll(data)
                            if (groups.size > 0)
                                adapter?.notifyItemRangeInserted(position, groups.size - position)
                        }
//                        data.forEach {
//                            it.conversation_type?.let { it1 -> it._id?.let { it2 ->
//                                setupDataSyncLocal(
//                                    it2, it1)
//                            } }
//                        }
                        isLocal = false
                        totalRecord = response.data.total_records ?: 0
                    }
                    if (groups.size == 0) {
                        stubBinding?.rcGroup?.hide()
                        stubBinding?.lnEmptyMessage?.show()
                    } else {
                        stubBinding?.rcGroup?.show()
                        stubBinding?.lnEmptyMessage?.hide()
                    }
                    stubBinding?.loading?.rlLoading?.hide()

                    stubBinding?.swipeRefresh?.isRefreshing = false

                }
            })
    }

    private fun deleteGroup(group: Group) {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format("%s%s%s", "/api/groups/", group._id, "/hide")

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .deleteGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        deleteGroupParty(group)
                        loop@ for (i in groups.indices) {
                            if (groups[i]._id == group._id) {
                                groups.removeAt(i)
                                adapter?.notifyItemRemoved(i)
                                adapter?.notifyItemRangeChanged(i, groups.size)
                                break@loop
                            }
                        }
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            group._id?.let {
                                TechResApplication.applicationContext().getMessageDao()
                                    ?.deleteMessageGroup(it, user.id)
                                TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                                    it, user.id
                                )
                            }
                        }
//                        groupViewModel.deleteItem(group._id ?: "")
                        FancyToast.makeText(
                            context,
                            resources.getString(R.string.delete_group_success),
                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                            false
                        ).show()
                    }
                    mainActivity?.setLoading(false)
                }
            })

    }

    private fun dissolutionGroup(group: Group) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format("%s%s%s", "/api/groups/", group._id, "/remove")
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .dissolutionGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        removeGroupParty(group)
                        loop@ for (i in groups.indices) {
                            if (groups[i]._id == group._id) {
                                groups.removeAt(i)
                                adapter?.notifyItemRemoved(i)
                                adapter?.notifyItemRangeChanged(i, groups.size)
                                break@loop
                            }
                        }
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            group._id?.let {
                                TechResApplication.applicationContext().getMessageDao()
                                    ?.deleteMessageGroup(it)
                                TechResApplication.applicationContext().getGroupDao()?.deleteGroup(
                                    it, user.id
                                )
                            }
                        }
                        FancyToast.makeText(
                            context,
                            getString(R.string.dissolution_group_success),
                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                            false
                        ).show()
//                        val pDialog = SweetAlertDialog(mainActivity!!, SweetAlertDialog.SUCCESS_TYPE)
//                        pDialog.setCancelable(false)
//                        pDialog.progressHelper.barColor = ContextCompat.getColor(
//                            mainActivity!!,
//                            R.color.green_home_item
//                        )
//                        pDialog.titleText = getString(R.string.dissolution_group_success)
//                        pDialog.confirmText = getString(R.string.yes)
//                        pDialog.setConfirmClickListener {
//                            pDialog.dismissWithAnimation()
//                        }
//                        pDialog.show()
                    } else {
                        Toast.makeText(
                            mainActivity,
                            getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun getListFriendOnline() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/friends/online?limit=20&page=1"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getListFriendOnline(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendOnlineResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    mainActivity?.setLoading(false)
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: FriendOnlineResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {

                        friendListOnline = response.data.list
                        friendsOnlineChatAdapter?.setDataSource(friendListOnline)
                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun createGroupPersonal(memberList: ArrayList<Int>) {
        mainActivity?.setLoading(true)
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
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
//                            createChatPersonal(memberId)
                            cacheManager.put(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.CHAT_PERSONAL.toString(),
                                TechresEnum.GROUP_CHAT.toString()
                            )
                            val intent = Intent(mainActivity, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(group)
                            )
                            startActivity(intent)
                            mainActivity?.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            Toast.makeText(
                                mainActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionAudioCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_micro),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_micro_step_two),
                        R.drawable.ic_baseline_mic_24, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_micro),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_micro_step_two),
                        R.drawable.ic_baseline_mic_24, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionGranted() {
//                    val roomId = Utils.getRandomString(20)
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_PHONE.toString(),
//                        memberCreate = user.id
//                    )
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_PHONE.toString()
//                    )
                    ChatUtils.connectToRoom(
                        requireContext(),
                        group._id,
                        group.member.member_id ?: 0,
                        group.member.full_name,
                        group.member.avatar!!.original,
                        user.id,
                        1,
                        "call_audio",
                        "aloline",
                        Utils.getRandomString(20)

                    )
                }
            }
        )
        handleOnRequestPermissionResult(
            requiredPermissionsVideoCallCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    if (requiredPermissionsVideoCall.size > 1) {
                        dialogPermission(
                            String.format(
                                requireActivity().resources.getString(R.string.title_permission),
                                namePermission
                            ),
                            String.format(
                                requireActivity().resources.getString(R.string.note_permission_reject),
                                requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                                namePermission,
                                namePermission
                            ),
                            requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                            R.drawable.ic_baseline_mic_24,
                            requireActivity().resources.getString(R.string.title_permission_micro_step_three),
                            R.drawable.ic_pink_camera,
                            object : DialogPermissionHandler {
                                override fun confirmDialog() {
                                    requestPermission.invoke()
                                }

                                override fun dismissDialog() {
                                }

                            }
                        )
                    } else {
                        dialogPermission(
                            String.format(
                                requireActivity().resources.getString(R.string.title_permission),
                                namePermission
                            ),
                            String.format(
                                requireActivity().resources.getString(R.string.note_permission_reject),
                                requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                                namePermission,
                                namePermission
                            ),
                            if (requiredPermissionsVideoCall.contains(Manifest.permission.CAMERA)) {
                                requireActivity().resources.getString(R.string.title_permission_camera_step_two)
                            } else {
                                requireActivity().resources.getString(R.string.title_permission_micro_step_three)
                            },
                            if (requiredPermissionsVideoCall.contains(Manifest.permission.CAMERA)) {
                                R.drawable.ic_baseline_mic_24
                            } else {
                                R.drawable.ic_pink_camera
                            },
                            "",
                            0,
                            object : DialogPermissionHandler {
                                override fun confirmDialog() {
                                    requestPermission.invoke()
                                }

                                override fun dismissDialog() {
                                }
                            }
                        )
                    }
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    if (requiredPermissionsVideoCall.size > 1) {
                        dialogPermission(
                            String.format(
                                requireActivity().resources.getString(R.string.title_permission),
                                namePermission
                            ),
                            String.format(
                                requireActivity().resources.getString(R.string.note_permission_denied),
                                requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                                namePermission,
                                namePermission
                            ),
                            requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                            R.drawable.ic_baseline_mic_24,
                            requireActivity().resources.getString(R.string.title_permission_micro_step_three),
                            R.drawable.ic_pink_camera,
                            object : DialogPermissionHandler {
                                override fun confirmDialog() {
                                    openAppSetting.invoke()
                                }

                                override fun dismissDialog() {
                                }

                            }
                        )
                    } else {
                        dialogPermission(
                            String.format(
                                requireActivity().resources.getString(R.string.title_permission),
                                namePermission
                            ),
                            String.format(
                                requireActivity().resources.getString(R.string.note_permission_denied),
                                requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                                namePermission,
                                namePermission
                            ),
                            if (requiredPermissionsVideoCall.contains(Manifest.permission.CAMERA)) {
                                requireActivity().resources.getString(R.string.title_permission_camera_step_two)
                            } else {
                                requireActivity().resources.getString(R.string.title_permission_micro_step_three)
                            },
                            if (requiredPermissionsVideoCall.contains(Manifest.permission.CAMERA)) {
                                R.drawable.ic_baseline_mic_24
                            } else {
                                R.drawable.ic_pink_camera
                            },
                            "",
                            0,
                            object : DialogPermissionHandler {
                                override fun confirmDialog() {
                                    openAppSetting.invoke()
                                }

                                override fun dismissDialog() {
                                }
                            }
                        )
                    }
                }

                override fun onPermissionGranted() {
//                    val roomId = Utils.getRandomString(20)
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_VIDEO.toString()
//                    )
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_VIDEO.toString(),
//                        memberCreate = user.id
//                    )
                    ChatUtils.connectToRoom(
                        requireContext(),
                        group._id,
                        group.member.member_id ?: 0,
                        group.member.full_name,
                        group.member.avatar!!.original,
                        user.id,
                        1,
                        "call_video",
                        "aloline",
                        Utils.getRandomString(20)
                    )
                }

            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onChat(group: Group) {
        val jsonGroup = Gson().toJson(group)
        val intent = Intent(mainActivity, ChatActivity::class.java)
        intent.putExtra(TechresEnum.GROUP_CHAT.toString(), jsonGroup)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
        this.group = group
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            group._id?.let {
                TechResApplication.applicationContext().getGroupDao()?.updateSeenGroup(
                    user.id, it
                )
            }
        }
        groups.forEachIndexed { index, element ->
            if (element._id == group._id) {
                element.number_message_not_seen = 0
                adapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }

        stubBinding?.searchView?.clearFocus()
        cacheManager.put(TechresEnum.GROUP_CHAT.toString(), jsonGroup)
        cacheManager.put(
            TechresEnum.CHAT_PERSONAL.toString(),
            TechresEnum.GROUP_CHAT.toString()
        )
    }

    override fun onBottomSheet(group: Group, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_group_chat)
        bottomSheetDialog.setCancelable(true)
        val btnDeleteGroupChat = bottomSheetDialog.findViewById<Button>(R.id.btnDeleteGroupChat)
        val btnDissolutionGroup =
            bottomSheetDialog.findViewById<Button>(R.id.btnDissolutionGroup)
        btnDeleteGroupChat?.setOnClickListener {
            deleteGroup(group)
            bottomSheetDialog.dismiss()
        }

        if (group.admin_id == user.id && group.conversation_type == TechResEnumChat.GROUP.toString()) {
            btnDissolutionGroup?.show()
        } else {
            btnDissolutionGroup?.hide()
        }

        btnDissolutionGroup?.setOnClickListener {
            dissolutionGroup(group)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onCall(group: Group) {
        this.group = group
        if (group.conversation_type == requireActivity().resources.getString(R.string.two_personal)) {
            if (group.last_message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()) {
                if (!permissionsIsGranted(requireActivity(), requiredPermissionsAudio)) {
                    requestMultiplePermissionAudio()
                } else {
//                    val roomId = Utils.getRandomString(20)
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_PHONE.toString()
//                    )
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_PHONE.toString(),
//                        memberCreate = user.id
//                    )
                    ChatUtils.connectToRoom(
                        requireContext(),
                        group._id,
                        group.member.member_id ?: 0,
                        group.member.full_name,
                        group.member.avatar!!.original,
                        user.id,
                        1,
                        "call_audio",
                        "aloline",
                        Utils.getRandomString(20)
                    )
                }
            } else {
                if (!permissionsIsGranted(requireActivity(), requiredPermissionsVideoCall)) {
                    requestMultiplePermissionVideoCall()
                } else {
//                    val roomId = Utils.getRandomString(20)
//                    connectVideoCallPersonal(
//                        group,
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_VIDEO.toString()
//                    )
//                    ChatUtils.connectToRoomVideoCall(
//                        requireActivity(),
//                        group,
//                        roomId,
//                        commandLineRun = false,
//                        loopback = false,
//                        runTimeMs = 0,
//                        option = 0,
//                        type = TechResEnumChat.PERSONAL.toString(),
//                        typeCall = TechResEnumVideoCall.TYPE_CALL_VIDEO.toString(),
//                        memberCreate = user.id
//                    )
                    ChatUtils.connectToRoom(
                        requireContext(),
                        group._id,
                        group.member.member_id ?: 0,
                        group.member.full_name,
                        group.member.avatar!!.original,
                        user.id,
                        1,
                        "call_video",
                        "aloline",
                        Utils.getRandomString(20)
                    )
                }

            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onCreateChat(position: Int) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(friendListOnline[position].user_id ?: 0)

        createGroupPersonal(memberList)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        mainActivity?.removeOnBackClick(this)
        super.onDestroy()
    }

    override fun onBackPress(): Boolean {
        return true
    }

    @Subscribe()
    fun onCreateGroup(group: EventBusGroup) {
        onChat(group.data)
    }

    @Subscribe()
    fun onLeaveGroup(group: EventBusLeaveGroup) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            TechResApplication.applicationContext().getGroupDao()
                ?.deleteGroup(group.groupID, user.id)
        }
        loop@ for (i in groups.indices) {
            if (groups[i]._id == group.groupID) {
                groups.removeAt(i)
                adapter?.notifyItemRemoved(i)
                adapter?.notifyItemRangeChanged(i, groups.size)
                break@loop
            }
        }
    }

    @Subscribe()
    fun onLeaveRoom(event: EventBusLeaveRoom) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            event.groupId.let {
                TechResApplication.applicationContext().getGroupDao()?.updateSeenGroup(
                    user.id, it
                )
            }
        }
        groups.forEachIndexed { index, group ->
            if (event.groupId == group._id) {
                group.number_message_not_seen = 0
                adapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe()
    fun onFinishSyncGroup(event: EventBusFinishSyncGroup) {
        if (event.finish) {
            stubBinding?.swipeRefresh?.isRefreshing = false
            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                val data = TechResApplication.applicationContext().getGroupDao()
                    ?.getGroup(user.id)
                if (data != null) {
                    groups.clear()
                    if (data.size > currentLimit) {
                        val position = currentLimit - 1
                        for (i in 0..position) {
                            groups.add(data[i])
                        }
                    } else {
                        groups.addAll(data)
                    }
                }
            }
            adapter?.notifyDataSetChanged()
            if (groups.size == 0) {
                stubBinding?.rcGroup?.hide()
                stubBinding?.lnEmptyMessage?.show()
            } else {
                stubBinding?.rcGroup?.show()
                stubBinding?.lnEmptyMessage?.hide()
            }
            stubBinding?.loading?.rlLoading?.hide()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe()
    fun onConnectionInternet(event: EventBusConnectionInternet) {
        if (event.status) {
            isConnectionInternet = true
            stubBinding?.swipeRefresh?.isEnabled = true
            isLoading = false
            getGroups(true, currentLimit, "")
            setupDataSyncLocal()
            getListFriendOnline()
        } else {
            isConnectionInternet = false
            stubBinding?.swipeRefresh?.isEnabled = false
        }
    }

    fun getListGroupNext() {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val data = TechResApplication.applicationContext().getGroupDao()
                ?.getGroup(user.id)
            if (data != null) {
                if (groups.size + currentLimit > data.size) {
                    val position = data.size - 1
                    for (i in groups.size..position) {
                        groups.add(data[i])
                    }
                } else {
                    val position = groups.size + currentLimit - 1
                    for (i in groups.size..position) {
                        groups.add(data[i])
                    }
                }
            }
        }
        adapter?.notifyDataSetChanged()

        isLoading = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getMessageDB() {
        isLocal = true
        stubBinding?.swipeRefresh?.isRefreshing = false
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val data = TechResApplication.applicationContext().getGroupDao()
                ?.getGroup(user.id)
            if (data != null) {
                groups.clear()
                if (data.size > currentLimit) {
                    val position = currentLimit - 1
                    for (i in 0..position) {
                        groups.add(data[i])
                    }
                } else {
                    groups.addAll(data)
                }
            }

        }
        adapter?.notifyDataSetChanged()
        if (groups.size == 0) {
            stubBinding?.rcGroup?.hide()
            stubBinding?.lnEmptyMessage?.show()
        } else {
            stubBinding?.rcGroup?.show()
            stubBinding?.lnEmptyMessage?.hide()
        }
        stubBinding?.loading?.rlLoading?.hide()
    }
}
