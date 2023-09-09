package vn.techres.line.fragment.chat.personal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.techres.line.R
import vn.techres.line.activity.PinnedDetailActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.ImageVideo
import vn.techres.line.data.model.chat.response.DetailGroupResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.NotifyChatParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDetailChatPersonalBinding
import vn.techres.line.fragment.chat.MediaManagerFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.EventBusScrollMessPin
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.util.*

class DetailPersonalFragment :
    BaseBindingChatFragment<FragmentDetailChatPersonalBinding>(FragmentDetailChatPersonalBinding::inflate) {
    private var group = Group()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()

    private var isCheckBackground = false

    //socket
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    private var notify = 0
    private var listImageVideo = ArrayList<ImageVideo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        mSocket?.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitleHomeHeader.text =
            requireActivity().baseContext.getString(R.string.detail).uppercase(
                Locale.getDefault()
            )
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
        val member = group.member
        getImage(binding.imgGroupDetailPersonal, member.avatar?.original, configNodeJs)
        binding.tvNameGroupPersonal.text = member.full_name
        getDetailGroup(group)
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
        binding.imgGroupDetailPersonal.setOnClickListener {
            activityChat?.toProfile(group.member.member_id ?: 0)
        }
        binding.lnPinnedPersonal.setOnClickListener {
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
//            val bundle = Bundle()
//            bundle.putString(
//                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
//            )
//            bundle.putString(
//                TechresEnum.PINNED_DETAIL.toString(),
//                TechresEnum.CHAT_PERSONAL.toString()
//            )
//            val pinnedDetailFragment = PinnedDetailFragment()
//            pinnedDetailFragment.arguments = bundle
//            activityChat?.addOnceFragment(this, pinnedDetailFragment)
        }
        binding.lnNotificationPersonal.setOnClickListener {
            binding.lnNotificationPersonal.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.lnNotificationPersonal.isEnabled = true
            }, 2000)
            if (notify == 0) {
                notify = 1
                binding.imgNotificationPersonal.setImageResource(R.drawable.ic_baseline_notifications_off_24)
                binding.tvBellPersonal.text =
                    requireActivity().baseContext.getString(R.string.turn_on_notification_n)
                onOffNotify(notify, group)
            } else {
                notify = 0
                binding.imgNotificationPersonal.setImageResource(R.drawable.ic_notification)
                binding.tvBellPersonal.text = getString(R.string.turn_off_notification_n)
                onOffNotify(notify, group)
            }
        }
        binding.lnChangeBackgroundPersonal.setOnClickListener {
            isCheckBackground = true
            onBackPress()
            activityChat?.supportFragmentManager?.popBackStack()

        }
        binding.lnMediaChat.setOnClickListener {
//            val bundle = Bundle()
//            try {
//                bundle.putString(
//                    TechresEnum.GROUP_CHAT.toString(),
//                    Gson().toJson(group)
//                )
//                bundle.putString(
//                    TechresEnum.MEDIA_CHAT.toString(),
//                    TechresEnum.CHAT_PERSONAL.toString()
//                )
//            } catch (e: JsonProcessingException) {
//                e.printStackTrace()
//            }
//                val mediaChatFragment = MediaChatFragment()
//                mediaChatFragment.arguments = bundle
//                activityChat?.addOnceFragment(DetailChatPersonalFragment(), mediaChatFragment)

            val mediaManagerFragment = MediaManagerFragment()
            activityChat?.addOnceFragment(this, mediaManagerFragment)
        }

        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
            onBackPress()
        }
    }


    private fun checkMedia(type: String, imageView: ImageView?) {
        if (type == requireActivity().baseContext.getString(R.string.type_image)) {
            imageView!!.visibility = View.GONE

        } else {
            imageView!!.visibility = View.VISIBLE
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
                checkMedia(data[0].message_type.toString(), binding.imgPlayOne)
                val path = FileUtils.getInternalStogeDir(data[0].link_original ?: "", requireActivity()) ?: ""
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
                checkMedia(data[0].message_type.toString(), binding.imgPlayOne)
                checkMedia(data[1].message_type.toString(), binding.imgPlayTwo)
                val path = FileUtils.getInternalStogeDir(data[0].link_original ?: "", requireActivity()) ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 = FileUtils.getInternalStogeDir(data[1].link_original ?: "", requireActivity()) ?: ""
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
                checkMedia(data[0].message_type.toString(), binding.imgPlayOne)
                checkMedia(data[1].message_type.toString(), binding.imgPlayTwo)
                checkMedia(data[2].message_type.toString(), binding.imgPlayThree)
                val path = FileUtils.getInternalStogeDir(data[0].link_original ?: "", requireActivity()) ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 = FileUtils.getInternalStogeDir(data[1].link_original ?: "", requireActivity()) ?: ""
                if (!File(path1).exists()) {
                    getGlide(binding.imgMediaTwo, data[1].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaTwo, path1)
                }
                val path2 = FileUtils.getInternalStogeDir(data[2].link_original ?: "", requireActivity()) ?: ""
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
                checkMedia(data[0].message_type.toString(), binding.imgPlayOne)
                checkMedia(data[1].message_type.toString(), binding.imgPlayTwo)
                checkMedia(data[2].message_type.toString(), binding.imgPlayThree)
                checkMedia(data[3].message_type.toString(), binding.imgPlayFour)
                val path = FileUtils.getInternalStogeDir(data[0].link_original ?: "", requireActivity()) ?: ""
                if (!File(path).exists()) {
                    getGlide(binding.imgMediaOne, data[0].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaOne, path)
                }
                val path1 = FileUtils.getInternalStogeDir(data[1].link_original ?: "", requireActivity()) ?: ""
                if (!File(path1).exists()) {
                    getGlide(binding.imgMediaTwo, data[1].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaTwo, path1)
                }
                val path2 = FileUtils.getInternalStogeDir(data[2].link_original ?: "", requireActivity()) ?: ""
                if (!File(path2).exists()) {
                    getGlide(binding.imgMediaThree, data[2].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaThree, path2)
                }
                val path3 = FileUtils.getInternalStogeDir(data[3].link_original ?: "", requireActivity()) ?: ""
                if (!File(path3).exists()) {
                    getGlide(binding.imgMediaFour, data[3].link_original, configNodeJs)
                } else {
                    Utils.showImage(binding.imgMediaFour, path3)
                }
            }
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
                        val data = response.data
                        notify = data?.is_notification!!
                        if (notify != 0) {
                            binding.imgNotificationPersonal.setImageResource(R.drawable.ic_baseline_notifications_off_24)
                            binding.tvBellPersonal.text =
                                requireActivity().baseContext.getString(R.string.turn_on_notification_n)
                        } else {
                            binding.imgNotificationPersonal.setImageResource(R.drawable.ic_notification)
                            binding.tvBellPersonal.text =
                                requireActivity().baseContext.getString(R.string.turn_off_notification_n)
                        }
//                        setImageVideo(data.list_image)
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
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                }
            })
    }

    override fun onBackPress(): Boolean {
        val bundle = Bundle()
        if (isCheckBackground) {
            bundle.putString(
                TechresEnum.LINK_BACKGROUND.toString(),
                TechresEnum.LINK_BACKGROUND.toString()
            )
        }
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
}