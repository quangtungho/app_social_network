package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket
import me.piruin.quickaction.ActionItem
import me.piruin.quickaction.QuickAction
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.NotifyBubbleChat
import vn.techres.line.data.model.chat.request.JoinChatRequest
import vn.techres.line.data.model.eventbus.EventBusRemoveGroupInGroup
import vn.techres.line.data.model.request.DisconnectRoomNodeRequest
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityChatBinding
import vn.techres.line.fragment.chat.AddMemberGroupFragment
import vn.techres.line.fragment.chat.group.ChatGroupFragment
import vn.techres.line.fragment.chat.personal.ChatPersonalFragment
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.OnMenuMoreClick
import vn.techres.line.interfaces.chat.ChatActivityHandler


class ChatActivity : BaseBindingActivity<ActivityChatBinding>(),
    QuickAction.OnActionItemClickListener, LifecycleHandler.Listener {
    private var group: Group? = null
    private var quickAction: QuickAction? = null
    private var onMenuMoreClick: OnMenuMoreClick? = null
    private var chatActivityHandler: ChatActivityHandler? = null

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var init = false
    override val bindingInflater: (LayoutInflater) -> ActivityChatBinding
        get() = ActivityChatBinding::inflate

    companion object {
        @JvmStatic
        val LIMIT_MESSAGE = 30
        lateinit var chatActivity: ChatActivity
        val codeVn = arrayOf(
            "a",
            "á",
            "à",
            "ả",
            "ã",
            "ạ",
            "ă",
            "ắ",
            "ằ",
            "ẳ",
            "ẵ",
            "ặ",
            "â",
            "ấ",
            "ầ",
            "ẩ",
            "ẫ",
            "ậ",
            "b",
            "c",
            "d",
            "đ",
            "e",
            "é",
            "è",
            "ẻ",
            "ẽ",
            "ẹ",
            "ê",
            "ế",
            "ề",
            "ể",
            "ễ",
            "ệ",
            "f",
            "g",
            "h",
            "i",
            "í",
            "ì",
            "ỉ",
            "ĩ",
            "ị",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "ó",
            "ò",
            "ỏ",
            "õ",
            "ọ",
            "ô",
            "ố",
            "ồ",
            "ổ",
            "ỗ",
            "ộ",
            "ơ",
            "ớ",
            "ờ",
            "ở",
            "ỡ",
            "ợ",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "ú",
            "ù",
            "ủ",
            "ũ",
            "ụ",
            "ư",
            "ứ",
            "ừ",
            "ử",
            "ữ",
            "ự",
            "v",
            "x",
            "y",
            "z",
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            ".",
            "_",
            "-"
        )
    }

    @SuppressLint("WrongConstant")
    override fun onSetBodyView() {
        chatActivity = this
        setDefaultFragmentBackground(android.R.color.white)
        mSocket = application.getSocketInstance(this)
//        SocketCallManager(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        user = CurrentUser.getCurrentUser(this)
        LifecycleHandler.instance.addListener(this)
        intent?.let {
            if (!it.getStringExtra(TechresEnum.CREATE_GROUP_CHAT.toString()).isNullOrBlank()) {
                val  bundleMember = Bundle()
                bundleMember.putInt(TechResEnumChat.NUM_MEMBER.toString(), 0)
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<AddMemberGroupFragment>(R.id.frameContainerChat, args = bundleMember)
                    addToBackStack(AddMemberGroupFragment().tag)
                }
            } else if (!it.getStringExtra(TechResEnumVideoCall.NOTIFY_DATA.toString())
                    .isNullOrBlank()
            ) {
                val notifyVideoCall = Gson().fromJson<NotifyBubbleChat>(
                    it.getStringExtra(TechResEnumVideoCall.NOTIFY_DATA.toString()), object :
                        TypeToken<NotifyBubbleChat>() {}.type
                )
                val bundle = Bundle()
                bundle.putString(
                    TechResEnumVideoCall.NOTIFY_DATA.toString(),
                    Gson().toJson(notifyVideoCall)
                )
                this.supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    if ((notifyVideoCall?.conversation_type
                            ?: "") == TechResEnumChat.GROUP.toString()
                    ) {
                        add<ChatGroupFragment>(R.id.frameContainerChat, args = bundle)
                        addToBackStack(ChatGroupFragment().tag)
                    } else {
                        add<ChatPersonalFragment>(R.id.frameContainerChat, args = bundle)
                        addToBackStack(ChatPersonalFragment().tag)
                    }
                }
            } else {
                group = Gson().fromJson(
                    it.getStringExtra(
                        TechresEnum.GROUP_CHAT.toString()
                    ), object :
                        TypeToken<Group>() {}.type
                )
                val bundle = Bundle()
                bundle.putString(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group))
                cacheManager.put(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group))
                this.supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    if ((group?.conversation_type ?: "") == TechResEnumChat.GROUP.toString()) {
                        add<ChatGroupFragment>(R.id.frameContainerChat, args = bundle)
                        addToBackStack(ChatGroupFragment().tag)
                    } else {
                        add<ChatPersonalFragment>(R.id.frameContainerChat, args = bundle)
                        addToBackStack(ChatPersonalFragment().tag)
                    }
                }
            }
        }

        quickAction?.setOnActionItemClickListener(this)
        init = true
    }

    fun addOnceFragment(fromFragment: Fragment, toFragment: Fragment) {
        val isInBackStack =
            supportFragmentManager.findFragmentByTag(toFragment.javaClass.simpleName)
        if (isInBackStack == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                .add(R.id.frameContainerChat, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    fun setOnMenuMoreClick(onMenuMoreClick: OnMenuMoreClick) {
        this.onMenuMoreClick = onMenuMoreClick
    }

    fun setChatActivityHandler(chatActivityHandler: ChatActivityHandler) {
        this.chatActivityHandler = chatActivityHandler
    }

    fun toProfile(id: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), id)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onItemClick(item: ActionItem?) {
        val idClick = item?.actionId ?: 0
        onMenuMoreClick?.onMenuMore(idClick)
    }

    override fun onBecameForeground(activity: Activity) {
//        if(group!=null)
//        {
//            if (group?.conversation_type ?: "" == TechResEnumChat.GROUP.toString()) {
//                group?.let { joinGroup(it, user) }
//            } else {
//                group?.let { joinPersonal(it, user) }
//            }
//        }
    }

    override fun onBecameBackground(activity: Activity) {
//        if (group != null)
//        {
//            if (group?.conversation_type ?: "" == TechResEnumChat.GROUP.toString()) {
//                group?.let { leaveGroup(it, user) }
//            } else {
//                group?.let { leavePersonal(it, user) }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        if (CurrentUser.isLogin(this) && mSocket != null && init) {
            connectRoom()
        }
        chatActivityHandler?.onResumeChat()
    }

    override fun onPause() {
        super.onPause()
        if (CurrentUser.isLogin(this) && mSocket != null && init) {
            disconnectRoom()
        }
        chatActivityHandler?.onPauseChat()
    }

    override fun onStop() {
        super.onStop()
        chatActivityHandler?.onStopChat()
    }

    override fun onBackPressed() {
        if (onBackClickFragments[onBackClickFragments.size - 1].onBack()) {
            if (supportFragmentManager.backStackEntryCount == 1) {
                finish()
                overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
                cacheManager.remove(TechresEnum.GROUP_CHAT.toString())
            } else {
                super.onBackPressed()
            }
        } else {
            onBackClickFragments[onBackClickFragments.size - 1].onBack()
        }
    }

    //group
    private fun joinGroup(group: Group, user: User) {
        val joinGroup = JoinChatRequest()
        joinGroup.group_id = group._id
        joinGroup.member_id = user.id
        joinGroup.avatar = user.avatar_three_image.original
        joinGroup.full_name = user.name ?: ""
        joinGroup.os_name = "android"
        try {
            val jsonObject = JSONObject(Gson().toJson(joinGroup))
            WriteLog.d("JOIN_GROUP_PARTY", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.JOIN_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

//    private fun leaveGroup(group: Group, user: User) {
//        val leaveChatRequest = LeaveChatRequest()
//        leaveChatRequest.group_id = group._id
//        leaveChatRequest.member_id = user.id
//        leaveChatRequest.full_name = user.name
//        leaveChatRequest.avatar = user.avatar_three_image.original
//        try {
//            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
//            if (group?.conversation_type ?: "" == TechResEnumChat.GROUP.toString()) {
//                mSocket?.emit(TechResEnumChat.LEAVE_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
//                WriteLog.d("LEAVE_ROOM_GROUP_ALO_LINE", jsonObject.toString())
//            } else {
//                mSocket?.emit(TechResEnumChat.LEAVE_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
//                WriteLog.d("LEAVE_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }

    //personal
//    private fun joinPersonal(group: Group, user: User) {
//        val joinGroup = JoinChatRequest()
//        joinGroup.group_id = group._id
//        joinGroup.member_id = user.id
//        joinGroup.full_name = user.name ?: ""
//        joinGroup.avatar = user.avatar_three_image.original
//        joinGroup.os_name = "android"
//        try {
//            val jsonObject = JSONObject(Gson().toJson(joinGroup))
//            mSocket?.emit(TechResEnumChat.JOIN_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
//            WriteLog.d("JOIN_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun leavePersonal(group: Group, user: User) {
//        val leaveChatRequest = LeaveChatRequest()
//        leaveChatRequest.group_id = group._id
//        leaveChatRequest.member_id = user.id
//        leaveChatRequest.full_name = user.name ?: ""
//        leaveChatRequest.avatar = user.avatar_three_image.original
//
//        try {
//            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
//            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
//            WriteLog.d("LEAVE_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }

    private fun disconnectRoom() {
        val user = CurrentUser.getCurrentUser(this)
        val disconnectNodeRequest = DisconnectRoomNodeRequest()
//        disconnectNodeRequest.avatar = user.avatar_three_image.original
//        disconnectNodeRequest.full_name = user.name
        disconnectNodeRequest.member_id = user.id
        disconnectNodeRequest.group_id = group?._id
        try {
            val jsonObject = JSONObject(Gson().toJson(disconnectNodeRequest))
            WriteLog.d("CLIENT_DISCONNECTION_ROOM_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CLIENT_DISCONNECTION_ROOM_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun connectRoom() {
        val user = CurrentUser.getCurrentUser(this)
        val connectNodeRequest = DisconnectRoomNodeRequest()
//        connectNodeRequest.avatar = user.avatar_three_image.original
//        connectNodeRequest.full_name = user.name
        connectNodeRequest.member_id = user.id
        connectNodeRequest.group_id = group?._id
        try {
            val jsonObject = JSONObject(Gson().toJson(connectNodeRequest))
            mSocket?.emit(TechResEnumChat.CLIENT_CONNECTION_ROOM_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CLIENT_CONNECTION_ROOM_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        startActivity(intent)
    }

    @Subscribe
    fun onRemoveGroupUserInGroup(event: EventBusRemoveGroupInGroup) {
        finish()
        Toast.makeText(application, "Nhóm đã bị giải tán", Toast.LENGTH_SHORT).show()
    }
}