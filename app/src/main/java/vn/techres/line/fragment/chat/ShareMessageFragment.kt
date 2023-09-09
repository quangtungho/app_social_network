package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import vn.techres.line.adapter.chat.GroupSelectedAdapter
import vn.techres.line.adapter.chat.ShareMessageAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.request.LinkMessageRequest
import vn.techres.line.data.model.chat.request.group.*
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.GroupsShareResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentShareMessageBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.Utils.showImage
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils.getMimeType
import vn.techres.line.interfaces.chat.RemoveGroupHandler
import vn.techres.line.interfaces.chat.ShareMessageHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.net.URLDecoder
import java.util.*
import java.util.stream.Collectors

@SuppressLint("UseRequireInsteadOfGet")

class ShareMessageFragment : BaseBindingChatFragment<FragmentShareMessageBinding>(
    FragmentShareMessageBinding::inflate,
    true
), ShareMessageHandler, RemoveGroupHandler {
    //group
    private var messages = MessagesByGroup()
    private var groups = ArrayList<Group>()
    private var groupsPersonal = ArrayList<Group>()
    private var groupsSends = ArrayList<Group>()
    private var group = Group()
    private var adapterGroup: ShareMessageAdapter? = null
    private var adapterGroupPersonal: ShareMessageAdapter? = null
    private var groupSelectedAdapter: GroupSelectedAdapter? = null

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var numberGroup = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Adapter group
        adapterGroup = ShareMessageAdapter(requireActivity().baseContext)
        adapterGroup?.setShareMessageHandler(this)
        binding.recyclerViewGroup.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewGroup.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.recyclerViewGroup.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        binding.recyclerViewGroup.adapter = adapterGroup

        //Adapter group personal
        adapterGroupPersonal = ShareMessageAdapter(requireActivity().baseContext)
        adapterGroupPersonal?.setShareMessageHandler(this)
        binding.recyclerViewGroupPersonal.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewGroupPersonal.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.recyclerViewGroupPersonal.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        binding.recyclerViewGroupPersonal.adapter = adapterGroupPersonal

        //Adapter selected group
        groupSelectedAdapter = GroupSelectedAdapter(requireActivity().baseContext)
        groupSelectedAdapter?.setRemoveGroupHandler(this)
        binding.recyclerViewChoose.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewChoose.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.recyclerViewChoose.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        binding.recyclerViewChoose.adapter = groupSelectedAdapter

        //set Data
        val bundle = arguments

        if (bundle != null) {
            group = Gson().fromJson(
                bundle.getString(
                    TechresEnum.GROUP_CHAT.toString()
                ), object : TypeToken<Group>() {}.type
            )
            messages = Gson().fromJson(
                bundle.getString(
                    TechresEnum.SHARE_MESSAGE.toString()
                ), object : TypeToken<MessagesByGroup>() {}.type
            )
            setData(messages)
        }
        setListener()
    }

    private fun setData(messagesByGroup: MessagesByGroup) {
        getListData()
        groupSelectedAdapter?.setDataSource(groupsSends)

        binding.svShareMessage.clearFocus()

        val listFile = messagesByGroup.files
        messagesByGroup.interval_of_time = ""

        when (messagesByGroup.message_type) {
            TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString() -> {
                binding.rlReplyThumbContainer.hide()
                binding.tvReplyContent.hide()


                var messageHtml = messagesByGroup.message
                val links = ArrayList<Pair<String, View.OnClickListener>>()
                if (messagesByGroup.list_tag_name.isNotEmpty()) {
                    messagesByGroup.list_tag_name.forEach { tagName ->
                        val name = String.format(
                            "%s%s", "@", tagName.full_name
                        )
                        messageHtml = messageHtml!!.replace(tagName.key_tag_name ?: "", name)
                        val pair = Pair(
                            "@" + tagName.full_name, View.OnClickListener {
                            }
                        )
                        links.add(pair)
                    }
                }

                binding.tvReplyName.text = messageHtml
            }

            TechResEnumChat.TYPE_IMAGE.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgReply.show()
                binding.tvReplyContent.hide()
                getGlide(binding.imgReply, listFile[0].link_original, configNodeJs)
                binding.tvReplyName.text = "Hình ảnh"
            }

            TechResEnumChat.TYPE_VIDEO.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgReply.show()
                binding.imgReplyPlay.show()
                binding.tvReplyContent.hide()
                getGlide(binding.imgReply, listFile[0].link_original, configNodeJs)
                binding.tvReplyName.text = "Video"
            }

            TechResEnumChat.TYPE_AUDIO.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgAudio.show()
                binding.tvReplyContent.hide()
                binding.tvReplyName.text = "Ghi âm"
            }

            TechResEnumChat.TYPE_STICKER.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgReply.show()
                binding.tvReplyContent.hide()
                getGlide(binding.imgReply, messagesByGroup.files[0].link_thumb, configNodeJs)
                binding.tvReplyName.text = "Sticker"
            }

            TechResEnumChat.TYPE_LINK.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgReply.show()
                binding.tvReplyContent.hide()
                showImage(binding.imgReply, messagesByGroup.message_link!!.media_thumb)
                binding.tvReplyName.text = messagesByGroup.message_link!!.cannonical_url
            }

            TechResEnumChat.TYPE_FILE.toString() -> {
                binding.rlReplyThumbContainer.show()
                binding.imgFile.show()
                binding.tvReplyContent.hide()
                binding.tvReplyName.text = URLDecoder.decode(messagesByGroup.files[0].name_file, "UTF-8")
                val mineType = messagesByGroup.files[0].link_original?.let { getMimeType(it) }
                ChatUtils.setImageFile(binding.imgFile, mineType)

            }

            else -> {
//                binding.file.lnImageVideoShare.hide()
//                binding.reply.lnReplyShare.hide()
            }
        }
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
        }
        binding.imageButton.setOnClickListener {

            val listReceiverId = ArrayList<String>()
            var numberCheck = groupsSends.stream()
                .filter { x: Group -> x.isCheck == true }
                .collect(Collectors.toList()) as ArrayList<Group>
            groupsSends.forEach {
                if (it.isCheck == true) {
                    numberGroup++
                    val typeGroup = it.conversation_type
                    when (messages.message_type) {
                        TechResEnumChat.TYPE_IMAGE.toString() -> {
                            it._id?.let { it1 -> chatImage(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_STICKER.toString() -> {
                            it._id?.let { it1 -> chatSticker(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_VIDEO.toString() -> {
                            it._id?.let { it1 -> chatVideo(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_AUDIO.toString() -> {
                            it._id?.let { it1 -> chatAudio(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_LINK.toString() -> {
                            it._id?.let { it1 -> chatLink(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_TEXT.toString() -> {
                            it._id?.let { it1 -> chatText(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_REPLY.toString() -> {
                            it._id?.let { it1 -> chatText(it1, messages, typeGroup?:"") }
                        }
                        TechResEnumChat.TYPE_FILE.toString() -> {
                            it._id?.let { it1 -> chatFile(it1, messages, typeGroup?:"") }
                        }
                    }

                    if (numberGroup == numberCheck.size) {
                        activityChat?.supportFragmentManager?.popBackStack()
                    }
                }
                    listReceiverId.add(it._id ?: "")
            }
        }

        binding.svShareMessage.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (groups.size >= 5){
                        adapterGroup!!.setDataSource(groups, 0)
                        binding.txtSeeMore.visibility = View.GONE
                    }
                    adapterGroup?.searchFullText(newText)
                    adapterGroupPersonal?.searchFullText(newText)
                }
                return true
            }
        })

        binding.txtSeeMore.setOnClickListener {
            adapterGroup!!.setDataSource(groups, 0)
            binding.txtSeeMore.visibility = View.GONE
        }
    }


    private fun getListData() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/groups-for-share?page=1&limit=-1"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getGroupsShare(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GroupsShareResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GroupsShareResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        groups = response.data.groups
                        groupsPersonal = response.data.group_personals

                        adapterGroupPersonal!!.setDataSource(groupsPersonal, 1)
                        if (groups.size > 5){
                            binding.txtSeeMore.visibility = View.VISIBLE
                            val dataTemp = groups.take(5) as ArrayList
                            adapterGroup!!.setDataSource(dataTemp, 0)
                        }else{
                            binding.txtSeeMore.visibility = View.GONE
                            adapterGroup!!.setDataSource(groups, 0)
                        }
                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onChooseGroup(data: Group, type: Int) {
        if (type == 0){
            val positionGroup = groups.indexOfFirst { it._id == data._id }
            if (data.isCheck == true) {
                groupsSends.removeAt(groupsSends.indexOfFirst { it._id == data._id })
                groups[positionGroup].isCheck = false
            } else {
                groupsSends.add(data)
                groups[positionGroup].isCheck = true
            }
            adapterGroup!!.notifyItemChanged(positionGroup)
        }else{
            val positionGroupPermission = groupsPersonal.indexOfFirst { it._id == data._id }
            if (data.isCheck == true) {
                groupsSends.removeAt(groupsSends.indexOfFirst { it._id == data._id })
                groupsPersonal[positionGroupPermission].isCheck = false
            } else {
                groupsSends.add(data)
                groupsPersonal[positionGroupPermission].isCheck = true
            }
            adapterGroupPersonal!!.notifyItemChanged(positionGroupPermission)
        }

        groupSelectedAdapter!!.notifyDataSetChanged()

        if (groupsSends.size > 0){
            binding.clSend.visibility = View.VISIBLE
            binding.recyclerViewChoose.visibility = View.VISIBLE
        }else{
            binding.clSend.visibility = View.GONE
            binding.recyclerViewChoose.visibility = View.GONE
        }

        binding.txtQuantity.text = "Đã chọn: " + groupsSends.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun removeGroup(data: Group) {
        val positionGroup = groups.indexOfFirst { it._id == data._id }
        val positionGroupPermission = groupsPersonal.indexOfFirst { it._id == data._id }
        val positionGroupSends = groupsSends.indexOfFirst { it._id == data._id }

        groupsSends.removeAt(positionGroupSends)

        if (data.conversation_type == context!!.resources.getString(R.string.two_personal)){
            groupsPersonal[positionGroupPermission].isCheck = false
            adapterGroupPersonal!!.notifyItemChanged(positionGroupPermission)
        }else{
            groups[positionGroup].isCheck = false
            adapterGroup!!.notifyItemChanged(positionGroup)
        }

        groupSelectedAdapter!!.notifyDataSetChanged()

        if (groupsSends.size > 0){
            binding.clSend.visibility = View.VISIBLE
            binding.recyclerViewChoose.visibility = View.VISIBLE
        }else{
            binding.clSend.visibility = View.GONE
            binding.recyclerViewChoose.visibility = View.GONE
        }

        binding.txtQuantity.text = "Đã chọn: " + groupsSends.size
    }

    override fun onBackPress(): Boolean {
        return true
    }

    private fun chatText(groupID: String, message: MessagesByGroup, typeGroup: String) {
        val chatTextRequest = ChatTextGroupRequest()
        var textMessage = message.message
        chatTextRequest.member_id = user.id
        chatTextRequest.message_type = TechResEnumChat.TYPE_TEXT.toString()
        chatTextRequest.group_id = groupID
        chatTextRequest.message = textMessage
        chatTextRequest.list_tag_name = message.list_tag_name
        chatTextRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatTextRequest))
            WriteLog.d("CHAT_TEXT_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_TEXT_PERSONAL_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_TEXT_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatImage(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val chatImageGroupRequest = ChatImageGroupRequest()
        chatImageGroupRequest.member_id = user.id
        chatImageGroupRequest.message_type = TechResEnumChat.TYPE_IMAGE.toString()
        chatImageGroupRequest.group_id = groupId
        chatImageGroupRequest.files = message.files
        chatImageGroupRequest.key_message_error = getRandomString(10)
        chatImageGroupRequest.key_message = message.key_message

        try {
            val jsonObject = JSONObject(Gson().toJson(chatImageGroupRequest))
            WriteLog.d("CHAT_IMAGE_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_PERSONAL_IMAGE_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_IMAGE_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatVideo(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val chatVideoGroupRequest = ChatVideoGroupRequest()
        chatVideoGroupRequest.member_id = user.id
        chatVideoGroupRequest.message_type = TechResEnumChat.TYPE_VIDEO.toString()
        chatVideoGroupRequest.group_id = groupId
        chatVideoGroupRequest.files = message.files
        chatVideoGroupRequest.key_message_error = getRandomString(10)
        chatVideoGroupRequest.key_message = message.key_message
        try {
            val jsonObject = JSONObject(Gson().toJson(chatVideoGroupRequest))
            WriteLog.d("CHAT_VIDEO_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_PERSONAL_VIDEO_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_VIDEO_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatSticker(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val chatImageGroupRequest = ChatImageGroupRequest()
        chatImageGroupRequest.member_id = user.id
        chatImageGroupRequest.message_type = TechResEnumChat.CHAT_STICKER_ALO_LINE.toString()
        chatImageGroupRequest.group_id = groupId
        chatImageGroupRequest.files = message.files
        chatImageGroupRequest.key_message_error = getRandomString(10)
        chatImageGroupRequest.key_message = message.key_message

        try {
            val jsonObject = JSONObject(Gson().toJson(chatImageGroupRequest))
            WriteLog.d("CHAT_STICKER_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_STICKER_PERSONAL_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_STICKER_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatAudio(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val chatAudioGroupRequest = ChatAudioGroupRequest()
        chatAudioGroupRequest.member_id = user.id
        chatAudioGroupRequest.message_type = TechResEnumChat.TYPE_AUDIO.toString()
        chatAudioGroupRequest.group_id = groupId
        chatAudioGroupRequest.files = message.files
        chatAudioGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatAudioGroupRequest))
            WriteLog.d("CHAT_AUDIO_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_AUDIO_PERSONAL_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_AUDIO_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatLink(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val linkMessageRequest = LinkMessageRequest()
        var textMessage = message.message
        linkMessageRequest.member_id = user.id
        linkMessageRequest.message_type = TechResEnumChat.TYPE_LINK.toString()
        linkMessageRequest.group_id = groupId
        linkMessageRequest.message = textMessage
        linkMessageRequest.message_link.media_thumb = message.message_link?.media_thumb
        linkMessageRequest.message_link.favicon = message.message_link?.favicon
        linkMessageRequest.message_link.title = message.message_link?.title
        linkMessageRequest.message_link.description = message.message_link?.description
        linkMessageRequest.message_link.cannonical_url = message.message_link?.cannonical_url
        linkMessageRequest.message_link.author = message.message_link?.author
        linkMessageRequest.message_link.url = message.message_link?.url

        linkMessageRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(linkMessageRequest))
            WriteLog.d("CHAT_LINK_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_LINK_PERSONAL_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_LINK_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatFile(groupId: String, message: MessagesByGroup, typeGroup: String) {
        val chatFileGroupRequest = ChatFileGroupRequest()
        chatFileGroupRequest.member_id = user.id
        chatFileGroupRequest.files = message.files
        chatFileGroupRequest.group_id = groupId
        chatFileGroupRequest.message_type = TechResEnumChat.TYPE_FILE.toString()
        chatFileGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatFileGroupRequest))
            WriteLog.d("CHAT_FILE_ALO_LINE", jsonObject.toString())
            if (typeGroup == context!!.resources.getString(R.string.two_personal)){
                mSocket?.emit(TechResEnumChat.CHAT_FILE_PERSONAL_ALO_LINE.toString(), jsonObject)
            }else{
                mSocket?.emit(TechResEnumChat.CHAT_FILE_ALO_LINE.toString(), jsonObject)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}