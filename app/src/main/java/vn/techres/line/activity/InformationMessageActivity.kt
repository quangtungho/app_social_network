package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liulishuo.filedownloader.FileDownloader
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kohii.v1.exoplayer.Kohii
import org.greenrobot.eventbus.Subscribe
import vn.techres.line.R
import vn.techres.line.adapter.chat.InformationMessageAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.response.MessageViewedDataRespone
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivityInformationMessageBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.TasksManager
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.*
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File

class InformationMessageActivity : BaseBindingActivity<ActivityInformationMessageBinding>(),
    ChatGroupHandler, ImageMoreChatHandler, ChooseNameUserHandler, ViewerMessageHandler {
    val activityChat: ChatActivity?
        get() = this as ChatActivity?
    private var message = MessagesByGroup()
    private var listMessage = ArrayList<MessagesByGroup>()
    private var receivedViewer = ArrayList<MessageViewer>()
    private var receivedViewerNotSeen = ArrayList<MessageViewer>()
    private var adapter: InformationMessageAdapter? = null
    private var configNodeJs = ConfigNodeJs()

    override val bindingInflater: (LayoutInflater) -> ActivityInformationMessageBinding
        get() = ActivityInformationMessageBinding::inflate
    override fun onStop() {
        super.onStop()
        adapter?.stopPlaying()
    }

    override fun onPause() {
        super.onPause()
        adapter?.stopPlaying()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.stopPlaying()
        TasksManager().getImpl().onDestroy()
        FileDownloader.getImpl().pauseAll()
    }

    override fun onSetBodyView() {
      //  TasksManager().getImpl().onCreate(WeakReference(this))
        binding.rcvInformationMessage.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val koHi = Kohii[this]
        val manager = koHi.register(this).addBucket(binding.rcvInformationMessage)
        adapter = InformationMessageAdapter(this, manager, koHi)
        adapter?.setChatGroupHandler(this)
        adapter?.setImageMoreChatHandler(this)
        adapter?.setChooseTagNameHandler(this)
        adapter?.setViewerMessageHandler(this)

        binding.rcvInformationMessage.adapter = adapter

        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        intent?.let {
            message = Gson().fromJson(
                it.getStringExtra(TechResEnumChat.MESSAGE_INFORMATION.toString()), object :
                    TypeToken<MessagesByGroup>() {}.type
            )
            val group = Gson().fromJson<Group>(
                cacheManager.get(TechresEnum.GROUP_CHAT.toString()), object :
                    TypeToken<Group>() {}.type
            )
            listMessage.add(message)
            if (message.message_type == TechResEnumChat.TYPE_FILE.toString() || message.message_type == TechResEnumChat.TYPE_IMAGE.toString() || message.message_type == TechResEnumChat.TYPE_VIDEO.toString() || message.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                TasksManager().getImpl().addMessage(message)
            }
            if (group.conversation_type == TechResEnumChat.GROUP.toString()) {
                message.random_key?.let { it1 -> group._id?.let { it2 ->
                    getMessageViewed(it1,
                        it2
                    )
                } }
//                val member = Gson().fromJson<ArrayList<Members>>(
//                    it.getString(TechResEnumChat.MEMBER_INFORMATION.toString()), object :
//                        TypeToken<ArrayList<Members>>() {}.type
//                )
//                member.removeAt(0)
//                loop@ for (i in member.indices) {
//                    for (j in (message.message_viewed ?: ArrayList()).indices) {
//                        if (member[i].member_id != message.message_viewed.get(j).member_id) {
//                            receivedViewer.add(
//                                MessageViewer(
//                                    member[i].avatar,
//                                    member[i].full_name,
//                                    member[i].member_id
//                                )
//                            )
//                            continue@loop
//                        }
//                    }
//                }
            } else {
                receivedViewer = ArrayList()
                receivedViewerNotSeen = ArrayList()
                adapter?.setDataSource(listMessage, receivedViewer, receivedViewerNotSeen)
            }
        }
        binding.imgBack.setOnClickListener {
           onBackPressed()
        }
    }


    override fun onPressReaction(messagesByGroup: MessagesByGroup, view: View) {

    }

    override fun onClickViewReaction(
        messagesByGroup: MessagesByGroup,
        reactionItems: ArrayList<ReactionItem>
    ) {
        TODO("Not yet implemented")
    }

    override fun onOpenFile(path: String?) {
        path?.let {
            FileUtils.openDocument(this, it)
        }
    }

    override fun onDeleteDownLoadFile(nameFile: String?) {
        if (nameFile != null) {
            FileUtils.getStorageDir(nameFile)?.let {
                File(it).delete()
            }
        }
    }

    override fun onAddMember() {

    }

    override fun onChooseAvatarBusinessCard(userId: Int) {
       activityChat!!.toProfile(userId)
    }

    override fun onChatBusinessCard(phoneMessage: PhoneMessage) {

    }

    override fun onCallBusinessCard(phoneMessage: PhoneMessage) {
    }

    override fun onChooseBusinessCard(phoneMessage: PhoneMessage) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + phoneMessage.phone)
        startActivity(dialIntent)
    }

    override fun onChooseCallVideo(messagesByGroup: MessagesByGroup) {
    }

    override fun onScrollMessage(messagesByGroup: MessagesByGroup) {
    }

    override fun onShareMessage(messagesByGroup: MessagesByGroup) {

    }

    override fun onIntentSendBirthDayCard(messagesByGroup: MessagesByGroup) {

    }

    override fun onReviewInviteCard(messagesByGroup: MessagesByGroup) {

    }

    override fun onClickTextPhone(messagesByGroup: MessagesByGroup) {
    }

    override fun onClickHideKeyboard() {
    }

    override fun onCLickImageMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        imgList.forEach {
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", this) ?: ""
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if(File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onVideoMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        imgList.forEach {
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", this) ?: ""
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if(File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onRevokeImageMore(messagesByGroup: MessagesByGroup, view: View) {
    }

    override fun onChooseTagName(tagName: TagName) {
        activityChat!!.toProfile(tagName.member_id ?: 0)
    }

    override fun onChooseNameUser(sender: Sender) {
       activityChat!!.toProfile(sender.member_id ?: 0)
    }

    override fun onChooseViewer(messageViewer: MessageViewer) {
        activityChat!!.toProfile(messageViewer.member_id ?: 0)
    }

    fun onBackPress(): Boolean {
        return true
    }

    @Subscribe
    fun onDownloadFileSuccess(event: EventBusDownloadSuccess) {
        listMessage.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key == event.randomKey) {
                if (adapter != null)
                    adapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    private fun getMessageViewed(randomkey:String, groupId:String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "/api/messages/message-viewed-detail?randomKey=${randomkey}&groupId=${groupId}"
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getMessageViewed(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessageViewedDataRespone> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: MessageViewedDataRespone) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        receivedViewer = data.list_user_seen
                        receivedViewerNotSeen = data.list_user_not_seen
                        adapter?.setDataSource(listMessage, receivedViewer, receivedViewerNotSeen)

                    } else Toast.makeText(
                        this@InformationMessageActivity,
                       resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    )
                }
            })


    }
}

