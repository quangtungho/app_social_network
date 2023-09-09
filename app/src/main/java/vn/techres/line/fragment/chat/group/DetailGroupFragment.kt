package vn.techres.line.fragment.chat.group

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import coil.load
import coil.request.CachePolicy
import coil.size.Scale
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.PinnedDetailActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.ImageVideo
import vn.techres.line.data.model.chat.Members
import vn.techres.line.data.model.chat.Sender
import vn.techres.line.data.model.chat.request.LeaveChatRequest
import vn.techres.line.data.model.chat.request.RemoveGroupParty
import vn.techres.line.data.model.chat.request.group.MemberLeaveGroupRequest
import vn.techres.line.data.model.chat.request.group.UpdateDetailGroupRequest
import vn.techres.line.data.model.chat.response.DetailGroupResponse
import vn.techres.line.data.model.chat.response.FileNodeJsResponse
import vn.techres.line.data.model.eventbus.EventBusChangeStatusVice
import vn.techres.line.data.model.eventbus.EventBusLeaveGroup
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.NotifyChatParams
import vn.techres.line.data.model.params.UpdateGroupParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDetailGroupBinding
import vn.techres.line.fragment.chat.AddMemberGroupFragment
import vn.techres.line.fragment.chat.MediaManagerFragment
import vn.techres.line.fragment.chat.MemberGroupFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.EventBusScrollMessPin
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File

open class DetailGroupFragment :
    BaseBindingChatFragment<FragmentDetailGroupBinding>(FragmentDetailGroupBinding::inflate, true) {
    private var members = ArrayList<Members>()
    private var group = Group()
    private var listImageVideo = ArrayList<ImageVideo>()

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var isCheckBackground = false
    private var avatarGroup = ""
    private var nameGroup = ""
    private var notify = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        mSocket?.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            group = Gson().fromJson(
                it.getString(
                    TechresEnum.GROUP_CHAT.toString()
                ), object :
                    TypeToken<Group>() {}.type
            )
            setData(group)
        }
        setListener()
    }

    private fun setData(group: Group) {
        getDetailGroup(group)
        if (avatarGroup.isNotEmpty()) {
            ChatUtils.getAvatarGroup(binding.imgGroupDetail, avatarGroup, configNodeJs)
        } else {
            ChatUtils.getAvatarGroup(binding.imgGroupDetail, group.avatar, configNodeJs)
        }
        if (nameGroup.isNotEmpty()) {
            binding.tvNameGroup.text = nameGroup
        } else {
            binding.tvNameGroup.text = group.name
        }
        val createdLastMessage = group.created_last_message
        binding.tvTimeOffline.hide()
        binding.tvTimeOffline.text =
            String.format("%s %s", createdLastMessage, resources.getString(R.string.before))
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            var list = TechResApplication.applicationContext().getMessageDao()
                ?.getImageAndVideoGroup(
                    group._id,
                    CurrentUser.getCurrentUser(TechResApplication.applicationContext()).id
                )
            if (list == null) {
                list = ArrayList()
            }
            list.forEachIndexed { _, messagesByGroup ->
                messagesByGroup.files.forEach {
                    if (listImageVideo.size == 4)
                        return@forEachIndexed
                    listImageVideo.add(
                        ImageVideo(
                            it.link_original ?: "",
                            messagesByGroup.message_type ?: ""
                        )
                    )
                }
            }
            setImageVideo(listImageVideo)
        }

    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPress()
            activityChat?.supportFragmentManager?.popBackStack()

        }
        binding.lnChangeBackground.setOnClickListener {
            isCheckBackground = true
            onBackPress()
            activityChat?.supportFragmentManager?.popBackStack()

        }

        binding.lnPinnedGroup.setOnClickListener {
//            val bundlePinned = Bundle()
//            bundlePinned.putString(
//                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
//            )
//            bundlePinned.putString(
//                TechresEnum.PINNED_DETAIL.toString(),
//                TechresEnum.CHAT_GROUP.toString()
//            )
//            val pinnedDetailFragment = PinnedDetailFragment()
//            pinnedDetailFragment.arguments = bundlePinned
//            activityChat?.addOnceFragment(this, pinnedDetailFragment)

            val intent = Intent(context, PinnedDetailActivity::class.java)
            intent.putExtra(
                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
            )
            intent.putExtra(
                TechresEnum.PINNED_DETAIL.toString(),
                TechresEnum.CHAT_GROUP.toString()
            )
            startActivity(intent)
//            mainActivity?.overridePendingTransition(
//                R.anim.translate_from_right,
//                R.anim.translate_to_right
//            )

        }

        binding.lnLeaveGroup.setOnClickListener {
            val member = members.stream().filter { it.member_id == user.id }.findFirst().get()
            if (member.permissions == TechResEnumChat.TYPE_MEMBER_ADMIN.toString()) {
                checkAdminLeaveGroup()
            } else {
                memberLeaveGroup(group)
                leaveGroup(group._id.toString())
                leaveGroup(group)
            }
        }

        binding.lnAddMemberGroup.setOnClickListener {
            val bundleMember = Bundle()
            try {
                cacheManager.put(
                    TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                        group
                    )
                )
                cacheManager.put(
                    TechresEnum.IS_CHECK_MEMBERS.toString(),
                    TechresEnum.IS_CHECK_MEMBERS.toString()
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            bundleMember.putInt(TechResEnumChat.NUM_MEMBER.toString(), members.size)
            val memberGroupFragment = AddMemberGroupFragment()
            memberGroupFragment.arguments = bundleMember
            activityChat?.addOnceFragment(this, memberGroupFragment)
        }

        binding.lnMediaChat.setOnClickListener {
            val mediaManagerFragment = MediaManagerFragment()
            activityChat?.addOnceFragment(this, mediaManagerFragment)
        }
        binding.lnMediaShowChat.setOnClickListener {
            val mediaManagerFragment = MediaManagerFragment()
            activityChat?.addOnceFragment(this, mediaManagerFragment)
        }
        binding.imgEditAvatarGroup.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isWithVideoImage(true)
                .maxSelectNum(1)
                .minSelectNum(0)
                .maxVideoSelectNum(0)
                .selectionMode(PictureConfig.MULTIPLE)
                .isSingleDirectReturn(false)
                .isPreviewImage(true)
                .isPreviewVideo(true)
                .isOpenClickSound(true)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }

        binding.lnMemberGroup.setOnClickListener {
            val bundleMember = Bundle()
            try {
                bundleMember.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            val memberGroupFragment = MemberGroupFragment()
            memberGroupFragment.arguments = bundleMember
            activityChat?.addOnceFragment(this, memberGroupFragment)
        }
        if (group.conversation_type.equals(TechResEnumChat.PERSONAL.toString())) {
            binding.lnEditGroupName.visibility = View.GONE
            binding.viewEditName.visibility = View.GONE
        } else {
            binding.lnEditGroupName.visibility = View.VISIBLE
            binding.viewEditName.visibility = View.VISIBLE
            binding.lnEditGroupName.setOnClickListener {
                showDialog()
            }
        }
        binding.lnNotificationGroup.setOnClickListener {
            binding.lnNotificationGroup.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                activity?.runOnUiThread {
                    binding.lnNotificationGroup.isEnabled = true
                }
            }, 2000)
            if (notify == 0) {
                notify = 1
                binding.imgBellGroup.setImageResource(R.drawable.ic_notification)
                binding.txtBellGroup.text =
                    requireActivity().getString(R.string.turn_off_notification_n)
                onOffNotify(notify, group)
            } else {
                notify = 0
                binding.imgBellGroup.setImageResource(R.drawable.ic_baseline_notifications_off_24)
                binding.txtBellGroup.text =
                    requireActivity().getString(R.string.turn_on_notification_n)
                onOffNotify(notify, group)
            }
        }
        binding.lnDissolutionGroup.setOnClickListener {
            checkDissolutionGroup()
        }
    }

    private fun memberLeaveGroup(group: Group) {
        val memberLeaveGroupRequest = MemberLeaveGroupRequest()
        memberLeaveGroupRequest.member_id = user.id
        memberLeaveGroupRequest.group_id = group._id
        memberLeaveGroupRequest.message_type = TechResEnumChat.TYPE_MEMBER_LEAVE_GROUP.toString()
        memberLeaveGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(memberLeaveGroupRequest))
            mSocket?.emit(TechResEnumChat.MEMBER_LEAVE_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("MEMBER_LEAVE_GROUP_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun updateAvatarGroup(avatar: String) {
        val updateDetailGroupRequest = UpdateDetailGroupRequest()
        updateDetailGroupRequest.avatar = avatar
        updateDetailGroupRequest.background = group.background
        updateDetailGroupRequest.group_id = group._id
        updateDetailGroupRequest.name = group.name
        updateDetailGroupRequest.member_id = user.id
        updateDetailGroupRequest.message_type = TechResEnumChat.TYPE_AVATAR.toString()
        updateDetailGroupRequest.key_message_error =
            getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(updateDetailGroupRequest))
            mSocket?.emit(TechResEnumChat.UPDATE_GROUP_AVATAR_ALO_LINE.toString(), jsonObject)
            WriteLog.d("UPDATE_GROUP_AVATAR_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        group.avatar = avatar
    }

    private fun updateNameGroup(group: Group, name: String) {
        val updateDetailGroupRequest = UpdateDetailGroupRequest()
        updateDetailGroupRequest.avatar = group.avatar
        updateDetailGroupRequest.background = group.background
        updateDetailGroupRequest.group_id = group._id
        updateDetailGroupRequest.name = name
        updateDetailGroupRequest.member_id = user.id
        updateDetailGroupRequest.message_type = TechResEnumChat.TYPE_GROUP_NAME.toString()
        updateDetailGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(updateDetailGroupRequest))
            mSocket?.emit(TechResEnumChat.UPDATE_GROUP_NAME_ALO_LINE.toString(), jsonObject)
            WriteLog.d("UPDATE_GROUP_NAME_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        group.name = name
    }

    private fun leaveGroup(groupId: String) {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = groupId
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name
        leaveChatRequest.avatar = user.avatar_three_image.original
        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_GROUP_ALO_LINE", jsonObject.toString())
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
            dissolutionGroup(group)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun checkDissolutionGroup() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_detail_group)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val btnBackDetailGroup = dialog.findViewById(R.id.btnBackDetailGroup) as Button
        val btnDissolutionGroup = dialog.findViewById(R.id.btnSaveDetailGroup) as Button
        val tvNoteDetailGroup = dialog.findViewById(R.id.tvNoteDetailGroup) as TextView
        tvNoteDetailGroup.text =
            requireActivity().getString(R.string.notification_dissolution_group)
        btnDissolutionGroup.text = requireActivity().getString(R.string.dissolution)
        btnDissolutionGroup.setOnClickListener {
            removeGroupParty(group)
            dialog.dismiss()
        }
        btnBackDetailGroup.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkAdminLeaveGroup() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_notification_food_point)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.attributes = lp
        val btnBack = dialog.findViewById(R.id.btnBack) as Button
        val tvNote = dialog.findViewById(R.id.tvNote) as TextView
        tvNote.text = resources.getString(R.string.check_authorize_admin)
        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkMedia(type: String, imageView: ImageView?) {
        if (type == requireActivity().getString(R.string.type_image)) {
            imageView!!.hide()
        } else {
            imageView!!.show()
        }
    }

    private fun setMemberGroup(members: ArrayList<Members>) {
        when {
            members.size == 1 -> {
                getImage(binding.imgMemberOne, members[0].avatar?.medium, configNodeJs)
                binding.rlMemberOne.show()
                binding.rlMemberTwo.hide()
                binding.rlMemberThree.hide()
                binding.rlMemberMore.hide()
            }
            members.size == 2 -> {
                getImage(binding.imgMemberOne, members[0].avatar?.medium, configNodeJs)
                getImage(binding.imgMemberTwo, members[1].avatar?.medium, configNodeJs)
                binding.rlMemberOne.show()
                binding.rlMemberTwo.show()
                binding.rlMemberThree.hide()
                binding.rlMemberMore.hide()
            }
            members.size == 3 -> {
                getImage(binding.imgMemberOne, members[0].avatar?.medium, configNodeJs)
                getImage(binding.imgMemberTwo, members[1].avatar?.medium, configNodeJs)
                getImage(binding.imgMemberThree, members[2].avatar?.medium, configNodeJs)
                binding.rlMemberOne.show()
                binding.rlMemberTwo.show()
                binding.rlMemberThree.show()
                binding.rlMemberMore.hide()
            }
            members.size >= 4 -> {
                getImage(binding.imgMemberOne, members[0].avatar?.medium, configNodeJs)
                getImage(binding.imgMemberTwo, members[1].avatar?.medium, configNodeJs)
                getImage(binding.imgMemberThree, members[2].avatar?.medium, configNodeJs)
                binding.rlMemberOne.show()
                binding.rlMemberTwo.show()
                binding.rlMemberThree.show()
                binding.rlMemberMore.show()
                if (members.size == 4) {
                    binding.txtNumMember.text = String.format("+%s", 1)
                } else if (members.size > 4) {
                    val numMember = (members.size - 3).toString()
                    binding.txtNumMember.text = String.format("+%s", numMember)
                }
            }
        }
    }

    private fun setImageVideo(data: ArrayList<ImageVideo>) {
        when (data.size) {
            0 -> {
                binding.lnNullMedia.show()
                binding.lnMediaContainer.hide()
            }
            1 -> {
                binding.lnNullMedia.hide()
                binding.lnMediaContainer.show()
                binding.rlMediaOne.show()
                binding.rlMediaTwo.hide()
                binding.rlMediaThree.hide()
                binding.rlMediaFour.hide()
                binding.lnMediaChatNext.show()
                checkMedia(data[0].message_type, binding.imgPlayOne)
                val path =
                    FileUtils.getInternalStogeDir(data[0].link_original, requireActivity())
                        ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
            }
            2 -> {
                binding.lnNullMedia.hide()
                binding.lnMediaContainer.show()
                binding.imgMediaOne.show()
                binding.imgMediaTwo.show()
                binding.imgMediaThree.hide()
                binding.imgMediaFour.hide()
                binding.lnMediaChatNext.show()
                checkMedia(data[0].message_type, binding.imgPlayOne)
                checkMedia(data[1].message_type, binding.imgPlayTwo)
                val path =
                    FileUtils.getInternalStogeDir(data[0].link_original, requireActivity())
                        ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 =
                    FileUtils.getInternalStogeDir(data[1].link_original, requireActivity())
                        ?: ""
                if (!File(path1).exists()) {
                    getGlide(binding.imgMediaTwo, data[1].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaTwo, path1)
                }
            }
            3 -> {
                binding.lnNullMedia.hide()
                binding.lnMediaContainer.show()
                binding.imgMediaOne.show()
                binding.imgMediaTwo.show()
                binding.imgMediaThree.show()
                binding.imgMediaFour.hide()
                binding.lnMediaChatNext.show()
                checkMedia(data[0].message_type, binding.imgPlayOne)
                checkMedia(data[1].message_type, binding.imgPlayTwo)
                checkMedia(data[2].message_type, binding.imgPlayThree)
                val path =
                    FileUtils.getInternalStogeDir(data[0].link_original, requireActivity())
                        ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 =
                    FileUtils.getInternalStogeDir(data[1].link_original, requireActivity())
                        ?: ""
                if (!File(path1).exists()) {
                    getGlide(binding.imgMediaTwo, data[1].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaTwo, path1)
                }
                val path2 =
                    FileUtils.getInternalStogeDir(data[2].link_original, requireActivity())
                        ?: ""
                if (!File(path2).exists()) {
                    getGlide(binding.imgMediaThree, data[2].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaThree, path2)
                }
            }
            4 -> {
                binding.lnNullMedia.hide()
                binding.lnMediaContainer.show()
                binding.imgMediaOne.show()
                binding.imgMediaTwo.show()
                binding.imgMediaThree.show()
                binding.imgMediaFour.show()
                binding.lnMediaChatNext.show()
                checkMedia(data[0].message_type, binding.imgPlayOne)
                checkMedia(data[1].message_type, binding.imgPlayTwo)
                checkMedia(data[2].message_type, binding.imgPlayThree)
                checkMedia(data[3].message_type, binding.imgPlayFour)
                val path =
                    FileUtils.getInternalStogeDir(data[0].link_original, requireActivity())
                        ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 =
                    FileUtils.getInternalStogeDir(data[1].link_original, requireActivity())
                        ?: ""
                if (!File(path1).exists()) {
                    getGlide(binding.imgMediaTwo, data[1].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaTwo, path1)
                }
                val path2 =
                    FileUtils.getInternalStogeDir(data[2].link_original, requireActivity())
                        ?: ""
                if (!File(path2).exists()) {
                    getGlide(binding.imgMediaThree, data[2].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaThree, path2)
                }
                val path3 =
                    FileUtils.getInternalStogeDir(data[3].link_original, requireActivity())
                        ?: ""
                if (!File(path3).exists()) {
                    getGlide(binding.imgMediaFour, data[3].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaFour, path3)
                }
            }
        }
    }

    private fun showDialog() {
        val dialog = this.requireActivity().let { Dialog(it) }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.edit_name_group_chat_popup)
        val edtNameGroup = dialog.findViewById<EditText>(R.id.edtNameGroup)
        val txtCancel = dialog.findViewById<TextView>(R.id.txtCancel)
        val txtSave = dialog.findViewById<TextView>(R.id.txtSave)
//        val imgSticker = dialog?.findViewById(R.id.imgSticker) as ImageView

        edtNameGroup?.setText(group.name)

        txtCancel?.setOnClickListener {
            dialog.dismiss()
        }

        txtSave?.setOnClickListener {
            txtSave.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                txtSave.isEnabled = true
            }, 1500)
            if (edtNameGroup?.text.toString() == "") {
                Toast.makeText(requireActivity(), "Tên nhóm không được để rỗng", Toast.LENGTH_LONG)
                    .show()
            } else {
                binding.tvNameGroup.text = edtNameGroup?.text.toString()
                nameGroup = edtNameGroup?.text.toString()
                updateNameGroup(group, nameGroup)
                dialog.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

    }

    private fun uploadFile(file: File) {
//        mainActivity!!.setLoading(true)
        val serverUrlString =
            String.format(
                "%s/api-upload/upload-file-by-group/%s/%s",
                configNodeJs.api_ads,
                TechResEnumChat.TYPE_GROUP_FILE.toString(),
                file.name
            )

        val paramNameString = resources.getString(R.string.send_file)
        try {
            MultipartUploadRequest(
                requireActivity(),
                serverUrlString
            )
                .setMethod("POST")
                .addFileToUpload(file.path, paramNameString)
                .addHeader(
                    requireActivity().getString(R.string.Authorization),
                    user.nodeAccessToken
                )
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        context,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .subscribe(requireActivity(), this, object : RequestObserverDelegate {
                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                    }

                    override fun onCompletedWhileNotObserving() {

                    }

                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                        FancyToast.makeText(
                            requireActivity(),
                            requireActivity().getString(R.string.sever_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }

                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {

                    }

                    override fun onSuccess(
                        context: Context,
                        uploadInfo: UploadInfo,
                        serverResponse: ServerResponse
                    ) {
                    }
                })
            // these are the different exceptions that may be thrown
        } catch (exc: Exception) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }

    }

    private fun getDetailGroup(group: Group) {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format("%s%s%s", "api/groups/", group._id, "/get-detail")
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getDetailGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DetailGroupResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: DetailGroupResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        if (response.data != null) {
                            val data = response.data
                            context?.let {
                                if (data != null && data.members.size != 0) {
                                    if (members.containsAll(data.members)) {
                                        setMemberGroup(members)
                                    } else {
                                        members = data.members
                                        setMemberGroup(members)
                                    }
                                    val member =
                                        data.members.find { item -> item.member_id == user.id }
                                    if (member?.permissions == TechResEnumChat.TYPE_MEMBER_ADMIN.toString()) {
                                        binding.lnDissolutionGroup.show()
                                    } else {
                                        binding.lnDissolutionGroup.hide()
                                    }
                                }
                                notify = data?.is_notification ?: 0
                                if (notify == 1) {
                                    binding.imgBellGroup.setImageResource(R.drawable.ic_notification)
                                    binding.txtBellGroup.text =
                                        requireActivity().getString(R.string.turn_off_notification_n)
                                } else {
                                    binding.imgBellGroup.setImageResource(R.drawable.ic_baseline_notifications_off_24)
                                    binding.txtBellGroup.text =
                                        requireActivity().getString(R.string.turn_on_notification_n)
                                }
//                                data?.list_image?.stream()?.filter { it.message_type.contains(".mp4") == false ||  }
//                                    ?.collect(
//                                        Collectors.toList()
//                                    ) as ArrayList<String>?
//                                data?.list_image?.let { setImageVideo(it) }
                            }
                        }
                    }
                }
            })
    }

    private fun onOffNotify(status: Int, group: Group) {

        val params = NotifyChatParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/on-off-notify-chat"
        params.project_id = AppConfig.getProjectChat()


        params.params.group_id = group._id
        params.params.status = status
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .onOffNotifyChat(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
//                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
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
//                    mainActivity?.setLoading(false)
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        context?.let {
                            FancyToast.makeText(
                                it,
                                requireActivity().getString(R.string.dissolution_group_success),
                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                false
                            ).show()
                        }

                        cacheManager.put(TechresEnum.CURRENT_PAGE.toString(), 3.toString())
                        activityChat?.finish()
                        activityChat?.overridePendingTransition(
                            R.anim.translate_from_right,
                            R.anim.translate_to_right
                        )
                    } else {
                        context?.let {
                            Toast.makeText(it, response.message, Toast.LENGTH_LONG).show()
                        }

                    }
                }
            })

    }

    private fun leaveGroup(group: Group) {
        val params = BaseParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s%s", "/api/groups/", group._id, "/leave")
        params.project_id = AppConfig.getProjectChat()
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .leaveGroup(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
//                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        EventBus.getDefault().post(group._id?.let {
                            EventBusLeaveGroup(
                                it
                            )
                        })
                        cacheManager.put(TechresEnum.CURRENT_PAGE.toString(), 3.toString())
                        activityChat?.finish()
                        activityChat?.overridePendingTransition(
                            R.anim.translate_from_right,
                            R.anim.translate_to_right
                        )
                    }
                })
        }
    }

    private fun getAvatarGroup(nameFile: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api-upload/get-link-file-by-group?group_id=",
            group._id,
            "&type=",
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            "&name_file=",
            nameFile,
            "&width=",
            0,
            "&height=",
            0
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getAvatarGroup(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FileNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
//                    mainActivity?.setLoading(false)
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: FileNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        avatarGroup = response.data.link_original ?: ""
                        updateAvatarGroup(avatarGroup)
                    }
                }
            })
    }

    private fun updateGroup() {
        val params = UpdateGroupParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s", "/groups/", group._id)
        params.project_id = AppConfig.getProjectChat()
        params.params.avatar = group.avatar
        params.params.background = group.background
        params.params.name = group.name
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .updateGroup(params)
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
                        nameGroup = ""
                        avatarGroup = ""
                    }
                }
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                val selectList = PictureSelector.obtainMultipleResult(data)
                val file = File(selectList[0].realPath)
                binding.imgGroupDetail.load(file) {
                    crossfade(true)
                    scale(Scale.FIT)
                    memoryCachePolicy(CachePolicy.DISABLED)
                    placeholder(R.drawable.logo_aloline_placeholder)
                    error(R.drawable.logo_aloline_placeholder)
                }
                getAvatarGroup(file.name)
                uploadFile(file)
            }
        }
    }

    override fun onBackPress(): Boolean {
        if (nameGroup.isNotEmpty() ||
            avatarGroup.isNotEmpty()
        ) {
            updateGroup()
        }

        val bundle = Bundle()
        if (isCheckBackground) {
            bundle.putString(
                TechresEnum.LINK_BACKGROUND.toString(),
                TechresEnum.LINK_BACKGROUND.toString()
            )
        }
        bundle.putString(TechresEnum.AVATAR_GROUP.toString(), group.avatar)
        bundle.putString(TechresEnum.NAME_GROUP.toString(), group.name)

        activityChat?.getOnRefreshFragment()?.onCallBack(bundle)
        return true
    }

    @Subscribe
    fun onSrollMessagePin(event: EventBusScrollMessPin) {
        onBackPress()
        activityChat?.supportFragmentManager?.popBackStack()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onChangeStatusVice(event: EventBusChangeStatusVice) {
        if (event.status == 1)
            getDetailGroup(group)
    }
}