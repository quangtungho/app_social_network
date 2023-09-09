package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liulishuo.filedownloader.FileDownloader
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.Subscribe
import vn.techres.line.R
import vn.techres.line.activity.MediaSliderActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.MessageImportantAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.response.MessagesResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMessageImportantBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.EventBusDownloadSuccess
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.MessageImportantHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.ceil

class MessageImportantFragment : BaseBindingChatFragment<FragmentMessageImportantBinding>(FragmentMessageImportantBinding::inflate), MessageImportantHandler,
    ImageMoreChatHandler, ChooseNameUserHandler {

    private var adapter : MessageImportantAdapter? = null
    private var listMessage = ArrayList<MessagesByGroup>()
    private var group = Group()
    private var limit = 20
    private var page = 1
    private var totalMessage = 0F
    //socket
    private val application = TechResApplication()
    private var user = User()
    private var mSocket: Socket? = null
    private var configNodeJs = ConfigNodeJs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        mSocket?.connect()
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
        listMessage = ArrayList()
        TasksManager().getImpl().onDestroy()
        FileDownloader.getImpl().pauseAll()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TasksManager().getImpl().onCreate(WeakReference(this))
        binding.rcMessageImportant.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        adapter = MessageImportantAdapter(requireActivity())
        adapter?.setImageMoreChatHandler(this)
        adapter?.setMessageImportantHandler(this)
        adapter?.setChooseTagNameHandler(this)
        binding.rcMessageImportant.adapter = adapter
        arguments?.let {
            try {
                group = Gson().fromJson(
                    it.getString(
                        TechresEnum.GROUP_CHAT.toString()
                    ), object :
                        TypeToken<Group>() {}.type
                )
                getMessageImportant(page)
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
        }

        binding.rcMessageImportant.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y <= 0) {
                        if (page < ceil((totalMessage / limit).toDouble())) {
                            page++
                            getMessageImportant(page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
        }
    }

    private fun getMessageImportant(page : Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url =
            String.format("%s%s", "/api/messages-starred?group_id=", group._id, "&limit=", limit, "&page=", page)

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessageImportant(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        if(listMessage.size == 0){
                            listMessage = data
                            listMessage.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }else{
                            listMessage.addAll(listMessage.size, data)
                            data.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }
                        adapter?.setDataSource(listMessage)
                    }
                }
            })
    }

    private fun removeMessageImportant(randomKey : String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url =
            String.format("%s%s", "/api/messages-starred/un-star?random_key=", randomKey)

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removeMessageImportant(
                baseRequest
            )

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
                        FancyToast.makeText(
                            requireActivity(),
                            requireActivity().resources.getString(R.string.unmarked),
                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                            false
                        ).show()
                        if(adapter != null){
                            val position = listMessage.indexOfFirst { it.random_key == randomKey }
                            listMessage.removeAt(position)
                            adapter?.notifyItemRemoved(position)
                        }
                    }
                }
            })
    }

    override fun onCLickImageMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        imgList.forEach {
            val linkOriginal = FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""
//            if(File(linkOriginal).exists()) {
//            }
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if(File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        val intent = Intent(activityChat, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onVideoMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        imgList.forEach {
            val linkOriginal = FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if(File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        val intent = Intent(activityChat, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onRevokeImageMore(messagesByGroup: MessagesByGroup, view: View) {
    }

    override fun onChooseBusinessCard(phoneMessage: PhoneMessage) {

        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + phoneMessage.phone)
        startActivity(dialIntent)
    }

    override fun onChooseLink(messagesByGroup: MessagesByGroup) {
    }

    override fun onRemoveMessage(messagesByGroup: MessagesByGroup) {
        removeMessageImportant(messagesByGroup.random_key ?: "")
    }

    override fun onChooseAvatarBusinessCard(userId : Int) {
        activityChat?.toProfile(userId)
    }

    override fun onOpenFile(path: String?) {
        path?.let {
            FileUtils.openDocument(activityChat, it)
        }
    }

    override fun onDeleteDownLoadFile(nameFile: String?) {
        if (nameFile != null) {
            FileUtils.getStorageDir(nameFile)?.let {
                File(it).delete()
            }
        }
    }

    override fun onCopyMessage(messagesByGroup: MessagesByGroup) {

    }

    override fun onChooseSender(sender: Sender) {
        sender.member_id?.let {
            activityChat?.toProfile(it)
        }
    }

    override fun onChooseTagName(tagName: TagName) {
        tagName.member_id?.let {
            activityChat?.toProfile(it)
        }
    }

    override fun onChooseNameUser(sender: Sender) {

    }

    override fun onBackPress() : Boolean{
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
}