package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.liulishuo.filedownloader.FileDownloader
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MediaSliderActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.section.MediaFileSection
import vn.techres.line.adapter.chat.section.MediaImageSection
import vn.techres.line.adapter.chat.section.MediaLinkSection
import vn.techres.line.adapter.chat.section.MediaVideoSection
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.response.LinkMediaChatResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMediaChatBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.MediaHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration
import java.io.File
import java.lang.ref.WeakReference
import java.util.stream.Collectors

class MediaFragment :
    BaseBindingChatFragment<FragmentMediaChatBinding>(FragmentMediaChatBinding::inflate),
    MediaHandler {
    private lateinit var sectionedAdapter: SectionedRecyclerViewAdapter
    private var group = Group()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var page = 1
    private var limit = 30
    private var totalRecord = 0F
    private var type = ""
    private var listImage = ArrayList<MediaChat>()
    private var listVideo = ArrayList<MediaChat>()
    private var listFile = ArrayList<MediaChat>()
    private var listLink = ArrayList<LinkMediaChat>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
    }

    fun newInstance(type: String): MediaFragment {
        val args = Bundle()
        val fragment = MediaFragment()
        args.putString(TechResEnumChat.TYPE_MEDIA.toString(), type)
        fragment.arguments = args
        return fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sectionedAdapter = SectionedRecyclerViewAdapter()
        arguments?.let {
            val jsonGroup = cacheManager.get(TechresEnum.GROUP_CHAT.toString())
            group = Gson().fromJson(
                jsonGroup, object :
                    TypeToken<Group>() {}.type
            )
            type = it.getString(TechResEnumChat.TYPE_MEDIA.toString()) ?: ""

            when (type) {
                TechResEnumChat.TYPE_IMAGE.toString() -> {
                    val gridLayoutManager = GridLayoutManager(requireActivity().baseContext, 3)
                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            if (sectionedAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                                return 3
                            }
                            return 1
                        }
                    }

                    binding.rcMedia.layoutManager = gridLayoutManager
                    binding.rcMedia.addItemDecoration(GridSpacingDecoration(7, 3))
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        var list = TechResApplication.applicationContext().getMessageDao()
                            ?.getImageInGroup(
                                group._id,
                                CurrentUser.getCurrentUser(TechResApplication.applicationContext()).id
                            )
                        if (list == null)
                            list = ArrayList()
                        var listTime = ArrayList<String>()
                        list.forEach {
                            val time = it.created_at?.substring(0, it.created_at?.indexOf(" ") ?: 0)
                            listTime.add(time ?: "")
                        }
                        listTime.distinct().forEach {
                            val listMessage = list?.stream()?.filter { x ->
                                x.created_at?.substring(
                                    0,
                                    x.created_at?.indexOf(" ") ?: 0
                                ).equals(it)
                            }?.collect(Collectors.toList()) as java.util.ArrayList<MessagesByGroup>
                            var mediaChat = MediaChat()
                            mediaChat.time = it
                            listMessage.forEach {
                                mediaChat.list.addAll(it.files)
                            }
                            listImage.add(mediaChat)
                        }
                    }
                    listImage.forEach {
                        sectionedAdapter.addSection(
                            MediaImageSection(
                                configNodeJs,
                                it.time,
                                it.list,
                                this@MediaFragment,
                                requireActivity(),
                                binding.rcMedia
                            )
                        )
                        it.list.forEach {
                            TaskManagerFile().getImpl().addMessage(it)
                        }
                    }
//                    getMedia(page, type)
                    sectionedAdapter.notifyDataSetChanged()
                    binding.rcMedia.adapter = sectionedAdapter

                }
                TechResEnumChat.TYPE_VIDEO.toString() -> {
                    val gridLayoutManager = GridLayoutManager(requireActivity().baseContext, 3)
                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            if (sectionedAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                                return 3
                            }
                            return 1
                        }
                    }

                    binding.rcMedia.layoutManager = gridLayoutManager
                    binding.rcMedia.addItemDecoration(GridSpacingDecoration(7, 3))
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        var list = TechResApplication.applicationContext().getMessageDao()
                            ?.getVideoInGroup(
                                group._id,
                                CurrentUser.getCurrentUser(TechResApplication.applicationContext()).id
                            )
                        if (list == null)
                            list = ArrayList()
                        var listTime = ArrayList<String>()
                        list?.forEach {
                            val time = it.created_at?.substring(0, it.created_at?.indexOf(" ") ?: 0)
                            listTime.add(time ?: "")
                        }
                        listTime.distinct().forEach {
                            val listMessage = list?.stream()?.filter { x ->
                                x.created_at?.substring(
                                    0,
                                    x.created_at?.indexOf(" ") ?: 0
                                ).equals(it)
                            }?.collect(Collectors.toList()) as java.util.ArrayList<MessagesByGroup>
                            var mediaChat = MediaChat()
                            mediaChat.time = it
                            listMessage.forEach { mediaChat.list.addAll(it.files) }
                            listVideo.add(mediaChat)
                        }
                    }
                    listVideo.forEach {
                        sectionedAdapter.addSection(
                            MediaVideoSection(
                                configNodeJs,
                                it.time,
                                it.list,
                                this@MediaFragment,
                                requireActivity(),
                                binding.rcMedia
                            )
                        )
                        it.list.forEach {
                            TaskManagerFile().getImpl().addMessage(it)
                        }
                    }
//                    getMedia(page, type)
                    sectionedAdapter.notifyDataSetChanged()
                    binding.rcMedia.adapter = sectionedAdapter
                }
                TechResEnumChat.TYPE_FILE.toString() -> {
                    binding.rcMedia.layoutManager = LinearLayoutManager(
                        requireActivity().baseContext,
                        RecyclerView.VERTICAL,
                        false
                    )
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        var list = TechResApplication.applicationContext().getMessageDao()
                            ?.getFileInGroup(
                                group._id,
                                CurrentUser.getCurrentUser(TechResApplication.applicationContext()).id
                            )
                        if (list == null)
                            list = ArrayList()
                        var listTime = ArrayList<String>()
                        list?.forEach {
                            val time = it.created_at?.substring(0, it.created_at?.indexOf(" ") ?: 0)
                            listTime.add(time ?: "")
                        }
                        listTime.distinct().forEach {
                            val listMessage = list?.stream()?.filter { x ->
                                x.created_at?.substring(
                                    0,
                                    x.created_at?.indexOf(" ") ?: 0
                                ).equals(it)
                            }?.collect(Collectors.toList()) as java.util.ArrayList<MessagesByGroup>
                            var mediaChat = MediaChat()
                            mediaChat.time = it
                            listMessage.forEach {
                                mediaChat.list.addAll(it.files)
                            }
                            listFile.add(mediaChat)
                        }
                    }
                    listFile.forEach {
                        sectionedAdapter.addSection(
                            MediaFileSection(
                                configNodeJs,
                                it.time,
                                it.list,
                                this@MediaFragment,
                                requireActivity()
                            )
                        )
                        it.list.forEach {
                            TaskManagerFile().getImpl().addMessage(it)
                        }
                    }
                    sectionedAdapter.notifyDataSetChanged()
                    binding.rcMedia.adapter = sectionedAdapter
//                    getMedia(page, type)
                }
                TechResEnumChat.TYPE_LINK.toString() -> {
                    binding.rcMedia.layoutManager = LinearLayoutManager(
                        requireActivity().baseContext,
                        RecyclerView.VERTICAL,
                        false
                    )
                    TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                        var list = TechResApplication.applicationContext().getMessageDao()
                            ?.getLinkInGroup(
                                group._id,
                                CurrentUser.getCurrentUser(TechResApplication.applicationContext()).id
                            )
                        if (list == null)
                            list = ArrayList()
                        var listTime = ArrayList<String>()
                        list?.forEach {
                            val time = it.created_at?.substring(0, it.created_at?.indexOf(" ") ?: 0)
                            listTime.add(time ?: "")
                        }
                        listTime.distinct().forEach {
                            val listMessage = list?.stream()?.filter { x ->
                                x.created_at?.substring(
                                    0,
                                    x.created_at?.indexOf(" ") ?: 0
                                ).equals(it)
                            }?.collect(Collectors.toList()) as java.util.ArrayList<MessagesByGroup>
                            var linkMediaChat = LinkMediaChat()
                            linkMediaChat.time = it
                            listMessage.forEach {
                                linkMediaChat.list.add(it.message_link ?: LinkMessage())
                            }
                            listLink.add(linkMediaChat)
                        }
                        listLink.forEach {
                            sectionedAdapter.addSection(
                                MediaLinkSection(
                                    activityChat!!.baseContext,
                                    configNodeJs,
                                    it.time,
                                    it.list,
                                    this@MediaFragment
                                )
                            )
                        }
                        sectionedAdapter.notifyDataSetChanged()
                        binding.rcMedia.adapter = sectionedAdapter
                    }
//                    getLinkMedia(page, type)
                }
                else -> {}
            }
        }
        TaskManagerFile().getImpl().onCreate(WeakReference(this))
//        binding.rcMedia.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            var y = 0
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                y = dy
//            }
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
//                    if (y <= 0) {
//                        if (page <= ceil((totalRecord / limit).toDouble())) {
//                            page++
//                            if (type == TechResEnumChat.TYPE_LINK.toString()) {
//                                getLinkMedia(page, type)
//                            } else {
//                                getMedia(page, type)
//                            }
//                        }
//                    } else {
//                        y = 0
//                    }
//                }
//
//            }
//        })
    }

//    private fun getMedia(page: Int, type: String) {
//        val baseRequest = BaseParams()
//        baseRequest.http_method = AppConfig.GET
//        baseRequest.project_id = AppConfig.getProjectChat()
//        baseRequest.request_url = String.format(
//            "%s%s%s%s%s%s%s%s",
//            "/api/messages/get-message-file?limit=",
//            limit,
//            "&page=",
//            page,
//            "&group_id=",
//            group._id,
//            "&type=",
//            type
//        )
//        ServiceFactory.createRetrofitServiceNode(
//            TechResService::class.java
//        )
//            .getMediaChat(baseRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<MediaChatResponse> {
//                override fun onComplete() {}
//                override fun onError(e: Throwable) {
//                    WriteLog.d("ERROR", e.message.toString())
//                }
//
//
//                override fun onSubscribe(d: Disposable) {}
//
//
//                @SuppressLint("ShowToast", "NotifyDataSetChanged")
//                override fun onNext(response: MediaChatResponse) {
//                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        val data = response.data
//                        if (data != null) {
//                            data.list.forEach {
//                                when (type) {
//                                    TechResEnumChat.TYPE_FILE.toString() -> {
//                                        sectionedAdapter.addSection(
//                                            MediaFileSection(
//                                                configNodeJs,
//                                                it.time,
//                                                it.list,
//                                                this@MediaFragment,
//                                                requireActivity()
//                                            )
//                                        )
//
//                                    }
//                                    TechResEnumChat.TYPE_VIDEO.toString() -> {
//                                        sectionedAdapter.addSection(
//                                            MediaVideoSection(
//                                                configNodeJs,
//                                                it.time,
//                                                it.list,
//                                                this@MediaFragment,
//                                                requireActivity(),
//                                                binding.rcMedia
//                                            )
//                                        )
//
//                                    }
//                                    TechResEnumChat.TYPE_IMAGE.toString() -> {
//                                        sectionedAdapter.addSection(
//                                            MediaImageSection(
//                                                configNodeJs,
//                                                it.time,
//                                                it.list,
//                                                this@MediaFragment,
//                                                requireActivity(),
//                                                binding.rcMedia
//                                            )
//                                        )
//                                    }
//                                }
//                                it.list.forEach {
//                                    TaskManagerFile().getImpl().addMessage(it)
//                                }
//                            }
//                            sectionedAdapter.notifyDataSetChanged()
//                            totalRecord = (data.total_record ?: 0).toFloat()
//                            if (context != null) {
//                                binding.rcMedia.adapter = sectionedAdapter
//                            }
//                        }
//                    } else Toast.makeText(
//                        requireActivity(),
//                        requireActivity().resources.getString(R.string.api_error),
//                        Toast.LENGTH_LONG
//                    )
//                }
//            })
//    }

    private fun getLinkMedia(page: Int, type: String) {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s",
            "/api/messages/get-message-file?limit=",
            limit,
            "&page=",
            page,
            "&group_id=",
            group._id,
            "&type=",
            type
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getLinkMedia(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<LinkMediaChatResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: LinkMediaChatResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        if (data != null) {
                            data.list.forEach {
                                sectionedAdapter.addSection(
                                    MediaLinkSection(
                                        activityChat!!.baseContext,
                                        configNodeJs,
                                        it.time,
                                        it.list,
                                        this@MediaFragment
                                    )
                                )
                            }
                            sectionedAdapter.notifyDataSetChanged()
                            totalRecord = (data.total_record ?: 0).toFloat()
                            binding.rcMedia.adapter = sectionedAdapter
                        }
                    } else Toast.makeText(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    )
                }
            })


    }

    override fun onClickMedia(list: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        list.forEach {
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""

//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if (!File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        val intent = Intent(activityChat, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onReSendLink(linkMessage: LinkMessage) {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(linkMessage.cannonical_url ?: ""))
        startActivity(browserIntent)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onSuccessDownload() {
        if (sectionedAdapter != null) {
            requireActivity().runOnUiThread {
                sectionedAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onBackPress(): Boolean {
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun postNotifyDataChanged() {
        if (sectionedAdapter != null) {
            requireActivity().runOnUiThread {
                sectionedAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        TaskManagerFile().getImpl().onDestroy()
        FileDownloader.getImpl().pauseAll()
    }
}