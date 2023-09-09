package vn.techres.line.fragment.chat.personal

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.content.*
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.*
import cn.jzvd.JZVideoPlayerStandard
import com.aghajari.emojiview.listener.PopupListener
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leocardz.link.preview.library.LinkPreviewCallback
import com.leocardz.link.preview.library.SourceContent
import com.leocardz.link.preview.library.TextCrawler
import com.liulishuo.filedownloader.FileDownloader
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import kohii.v1.exoplayer.Kohii
import me.rosuh.filepicker.config.FilePickerManager
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import nl.dionsegijn.konfetti.xml.KonfettiView
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.activity.TechResApplication.Companion.messageDao
import vn.techres.line.adapter.chat.MessageChatAdapter
import vn.techres.line.adapter.librarydevice.SuggestFileAdapter
import vn.techres.line.base.BaseBindingBubbleChatFragment
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.request.*
import vn.techres.line.data.model.chat.request.group.StartMessageRequest
import vn.techres.line.data.model.chat.request.personal.*
import vn.techres.line.data.model.chat.response.*
import vn.techres.line.data.model.chat.videocall.NotifyVideoCall
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.eventbus.EventBusGroup
import vn.techres.line.data.model.eventbus.EventBusLeaveRoom
import vn.techres.line.data.model.eventbus.EventBusRefresh
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.FriendAcceptParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.*
import vn.techres.line.databinding.FragmentChatPersonalBinding
import vn.techres.line.fragment.chat.MessageImportantFragment
import vn.techres.line.fragment.chat.ShareMessageFragment
import vn.techres.line.fragment.chat.vote.CreateVoteMessageFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getFileNameFromURL
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.getNameFileToPath
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.hideKeyboard
import vn.techres.line.helper.Utils.ifShow
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.Utils.stopMedia
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.screenshot.ScreenshotDetectionDelegate
import vn.techres.line.helper.screenshot.ScreenshotDetectionDelegate.getFilePathClipFromContentResolver
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.topbottombehavior.TopSheetBehavior
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.setReactionClick
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.ChooseOrderCustomerHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.SelectBackgroundHandler
import vn.techres.line.interfaces.SwipeItemHandler
import vn.techres.line.interfaces.chat.*
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.gift.ChatGiftHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.interfaces.util.SuggestFileHandler
import vn.techres.line.roomdatabase.sync.SyncDataUpdateMessage
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.videocall.SocketCallOnDataEnum
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors


open class ChatPersonalFragment :
    BaseBindingBubbleChatFragment<FragmentChatPersonalBinding>(FragmentChatPersonalBinding::inflate),
    ChooseNameUserHandler,
    RevokeMessageHandler, ImageMoreChatHandler, ChatGroupHandler, OnRefreshFragment,
    GetDataEmojiChildToParent,
    ScreenshotDetectionDelegate.ScreenshotDetectionListener, SuggestFileHandler,
    UtilitiesChatHandler, PopupListener, SelectBackgroundHandler, ChatActivityHandler,
    ChooseOrderCustomerHandler, ChatGiftHandler {
    private val currentLimit = 50
    private var totalMessage = 0f
    private var page = 1
    private var totalPage = 1
    private var resultPage = 1
    private var seenMessage = 0
    private var isTying = 0
    private var numberFile = 0
    private var idPinnedMessage = ""
    private var randomKeyTyping = ""
    private var randomKeyFile = ""

    private var isReplyMessage = false
    private var scrollPosition = true
    private var isCheckUtilities = true
    private var isCheckPage = true
    private var isCheckScroll = false
    private var isCheckReaction = true
    private var syncData = false
    private lateinit var layoutManager: LinearLayoutManager
    private var group = Group()

    private var replyMessage = Reply()
    private var messageDBSync = ArrayList<MessagesByGroup>()
    private var listMessages = ArrayList<MessagesByGroup>()
    private var imageList = ArrayList<FileNodeJs>()
    private var chatAdapter: MessageChatAdapter? = null
    private var myClipboard: ClipboardManager? = null

    //call
    private var notifyVideoCall = NotifyVideoCall()

    //typing
    private var listTypingUser = ArrayList<TypingUser>()
    private var positionTyping = 0
    private lateinit var toolsChat: ToolsChat

    //Picture
    private var selectList: List<LocalMedia> = ArrayList()

    //audio
    private var handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var audioFile: File? = null
    private var recorder: MediaRecorder? = null
    private var startHTime = 0L
    private val audioTimeHandler = Handler(Looper.getMainLooper())
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L

    //swipe
    private var vibrator: Vibrator? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    //link
    private lateinit var clipboardManager: ClipboardManager
    private var contentPaste = ""
    private var authorLink = ""
    private var titleLink = ""
    private var desLink = ""
    private var imageLink = ""
    private var urlLink = ""
    private var isCheckLink = false

    //keyboard
    private var onViewHeightChanged = 0
    private var isShowingKeyBoard = false
    private var isKeyBoard = false

    //background
    private var urlBackground = ""

    //screen shot
    private var listScreenShot: ArrayList<String>? = null
    private var adapterSuggestFile: SuggestFileAdapter? = null
    private val screenshotDetectionDelegate by lazy {
        ScreenshotDetectionDelegate(activityChat, this)
    }

    //permission
    private val requiredPermissionFile =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    private val requiredPermissionImage =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    private val requiredPermissionsFileCode = 101
    private val PICK_FROM_GALLERY = 131

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

    private val requiredPermissionsVideoCallCode = 103

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var pre_cursor = "-1";
    private var next_cursor = "-1";
    private var is_oldest_message = true
    private var is_latest_message = true
    private var isLoading = true
    private var isChooseImage = false
    private lateinit var konfetti: KonfettiView
    private lateinit var linkPreviewClip: LinkPreviewCallback
    private lateinit var linkPreview: LinkPreviewCallback
    private var listImageSuggestCache = ArrayList<String>()
    private var listStringImageSuggestCache = ""
    private var init = 0
    private var positionStroke = -1;
    private var stroke = false
    private var firstPosition = 0
    private var lastPosition = 0
    private var messageSearch = ArrayList<MessagesByGroup>()
    private var currentSearch = 0
    private var isStrokeSearch = 0
    private var isClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity())
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        user = CurrentUser.getCurrentUser(requireActivity())
        onSocket()
    }

    override fun onResume() {
        super.onResume()
        if (init == 1)
            joinGroup(group._id ?: "")
        screenshotDetectionDelegate.startScreenshotDetection()
        getCalling()
        chatAdapter?.startVideo()
    }

    override fun onPause() {
        super.onPause()
        typingOff(group)
        stopMedia()
        chatAdapter?.stopPlayer()
        isCheckUtilities = true
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
        if (listMessages.size > 0) {
            chatAdapter?.stopVideo()
        }
        if (recorder != null) {
            stopDrawing()
        }
    }

    override fun onStop() {
        super.onStop()
        typingOff(group)
        JZVideoPlayerStandard.releaseAllVideos()
        hideKeyboard(binding.layoutMainPersonal)
        toolsChat.binding.layoutKeyboard.onBackPressed()
        stopMedia()
        if (recorder != null) {
            stopDrawing()
        }
    }

    override fun onResumeChat() {
//        screenshotDetectionDelegate.startScreenshotDetection()
//        getCalling()
//        chatAdapter?.startVideo()

    }

    override fun onPauseChat() {
//        typingOff(group)
//        stopMedia()
//        chatAdapter?.stopPlayer()
//        isCheckUtilities = true
//        toolsChat.binding.layoutKeyboard.onBackPressed()
//        closeKeyboard(toolsChat.binding.edtMessageChat)
//        if (listMessages.size > 0) {
//            chatAdapter?.stopVideo()
//        }
//        if (recorder != null) {
//            stopDrawing()
//        }
    }

    override fun onStopChat() {
//        typingOff(group)
//        JZVideoPlayerStandard.releaseAllVideos()
//        hideKeyboard(binding.layoutMainPersonal)
//        toolsChat.binding.layoutKeyboard.onBackPressed()
//        closeKeyboard(toolsChat.binding.edtMessageChat)
//        stopMedia()
//        if (recorder != null) {
//            stopDrawing()
//            recorder?.stop()
//            recorder?.reset()
//            recorder?.release()
//            recorder = null
//        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        TasksManager().getImpl().onDestroy()
        FileDownloader.getImpl().pauseAll()
        screenshotDetectionDelegate.stopScreenshotDetection()
        EventBus.getDefault().unregister(this)
        super.onDetach()
    }

    override fun onSetBodyView(
        supportBinding: FragmentChatPersonalBinding?,
        savedInstanceState: Bundle?
    ) {
        listStringImageSuggestCache =
            cacheManager.get(TechResEnumChat.LIST_IMAGE_SUGGET.name).toString()
        if (!listStringImageSuggestCache.equals("") && !listStringImageSuggestCache.equals("null")) {
            val itemType = object : TypeToken<ArrayList<String>>() {}.type
            listImageSuggestCache =
                Gson().fromJson(listStringImageSuggestCache, itemType)
        }
        activityChat?.setChatActivityHandler(this)
        activityChat?.setOnRefreshFragment(this)
        TasksManager().getImpl().onCreate(WeakReference(this))
        myClipboard = Objects.requireNonNull(TechResApplication.applicationContext())
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager =
            TechResApplication.applicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        toolsChat = ToolsChat()
        toolsChat.initView(
            supportBinding?.layoutMainPersonal
        )
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val koHi = Kohii[this]
        val manager = koHi.register(this).addBucket(toolsChat.binding.rcvChat)
        adapterSuggestFile = SuggestFileAdapter()
        adapterSuggestFile?.setSuggestFileHandler(this)
        chatAdapter = MessageChatAdapter(requireActivity(), manager, koHi)
        chatAdapter?.setHasStableIds(true)
        chatAdapter?.setRevokeMessageHandler(this)
        chatAdapter?.setChatGroupHandler(this)
        chatAdapter?.setImageMoreChatHandler(this)
        chatAdapter?.setChooseTagNameHandler(this)
        chatAdapter?.setUtilitiesChatHandler(this)
        chatAdapter?.setChooseOrderDetail(this)
        chatAdapter?.setChooseGiftNotification(this)
        ChatUtils.configRecyclerView(toolsChat.binding.rcvChat, chatAdapter)
        toolsChat.binding.rcvChat.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(toolsChat.binding.rcvChat.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        toolsChat.binding.rcScreenShot.adapter = adapterSuggestFile
        //swipe item
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper?.attachToRecyclerView(toolsChat.binding.rcvChat)
        //keyboard
        toolsChat.getUtilitiesKeyBoard().setUtilitiesChatHandler(this)
        toolsChat.binding.layoutKeyboard.setPopupListener(this)
        UIView.setUtilitiesChatHandler(this)
        //background
        toolsChat.adapter?.setSelectBackgroundHandler(this)
        toolsChat.binding.tvLoadingMessage.show()
        arguments?.let {
            if (it.getString(TechResEnumVideoCall.NOTIFY_DATA.toString()) != null) {
                val notifyVideoCall = Gson().fromJson<NotifyBubbleChat>(
                    it.getString(TechResEnumVideoCall.NOTIFY_DATA.toString()), object :
                        TypeToken<NotifyBubbleChat>() {}.type
                )
                val avatar = Gson().fromJson<Avatar>(
                    notifyVideoCall.avatar,
                    object : TypeToken<Avatar>() {}.type
                )
                getImage(toolsChat.binding.imgAvatarChat, avatar?.original, configNodeJs)
                toolsChat.binding.tvTitleChat.text = notifyVideoCall.full_name
                getDetailGroup(notifyVideoCall.value)
            } else {
                group = Gson().fromJson(
                    it.getString(
                        TechresEnum.GROUP_CHAT.toString()
                    ), object :
                        TypeToken<Group>() {}.type
                )
//                setData(group)
                val members = group.member
                getImage(toolsChat.binding.imgAvatarChat, members.avatar?.original, configNodeJs)
                Utils.getGlide(
                    toolsChat.binding.imgBackgroundChat,
                    group.background,
                    configNodeJs
                )
                toolsChat.binding.tvTitleChat.text = members.full_name
                group._id?.let { it1 -> getDetailGroup(it1) }
                group._id?.let { it1 ->
                    group.conversation_type?.let { it2 ->
                        setupDataSyncLocal(
                            it1,
                            it2
                        )
                    }
                    getMessageDB(it1)
                }
//                group._id?.let { it1 -> getMessage(it1, "-1", "-1", "-1") }
            }
            init = 1
        }
        setListener()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setData(group: Group) {
        joinGroup(group._id!!.toString())
//        getMessageDB(1)
//        setupDataSyncLocal()
        val members = group.member
        getImage(toolsChat.binding.imgAvatarChat, members.avatar?.original, configNodeJs)
        Utils.getGlide(
            toolsChat.binding.imgBackgroundChat,
            group.background,
            configNodeJs
        )
        toolsChat.binding.tvTitleChat.text = members.full_name

        toolsChat.binding.edtMessageChat.addTextChangedListener(textWatcher)
        if (listMessages.size == 0) {
            toolsChat.binding.lnNoMessage.show()
            getClipboardManager()
            Handler(Looper.getMainLooper()).postDelayed({
                requestMultiplePermissionMedia()
            }, 100)
        } else {
            toolsChat.binding.lnNoMessage.hide()
            chatAdapter?.setDataSource(listMessages)
            chatAdapter?.startVideo()
        }
        toolsChat.binding.tvLoadingMessage.hide()
        setStranger()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun setListener() {
        toolsChat.binding.imgSent.setOnClickListener {
            if (toolsChat.binding.lnAudio.visibility == View.VISIBLE) {
                stopRecording()
            } else {
                if (isCheckLink) {
                    chatLink()
                } else {
                    if ((toolsChat.binding.edtMessageChat.text ?: "").trim { it <= ' ' }
                            .isNotEmpty()) {
                        if (!isReplyMessage) {
                            chatText(group)
                        } else {
                            reply(group, replyMessage)
                        }
                    }
                }
            }
            isCheckLink = false
            isReplyMessage = false
            toolsChat.binding.imgSent.visibility = View.GONE
        }
        //make friend
        toolsChat.binding.lnStatusMember.setOnClickListener {
            when (group.member.status) {
                4 -> {
                    group.member.status = 2
                    makeFriendSocket()
                    makeFriend()
                    setStranger()
                    EventBus.getDefault().post(EventBusRefresh())
                }
            }
        }
        toolsChat.binding.btnAcceptFriend.setOnClickListener {
            toolsChat.binding.lnStatusMember.hide()
            toolsChat.binding.btnAcceptFriend.hide()
            group.member.status = 1
            acceptFriend()
            acceptFriend(group.member.member_id)
            setStranger()
            EventBus.getDefault().post(EventBusRefresh())
        }
        toolsChat.binding.btnAcceptFriend2.setOnClickListener {
            toolsChat.binding.lnStatusMember.hide()
            toolsChat.binding.btnAcceptFriend.hide()
            group.member.status = 1
            acceptFriend()
            acceptFriend(group.member.member_id)
            setStranger()
            EventBus.getDefault().post(EventBusRefresh())
        }
        //background
        toolsChat.binding.tvCloseBackground.setOnClickListener {
            toolsChat.binding.rlHeaderChat.show()
            toolsChat.binding.rlHeaderBackground.hide()
            toolsChat.bottomSheet.state = TopSheetBehavior.STATE_COLLAPSED
            Utils.getGlide(
                toolsChat.binding.imgBackgroundChat,
                group.background,
                configNodeJs
            )
        }
        toolsChat.binding.tvDoneBackground.setOnClickListener {
            toolsChat.binding.rlHeaderChat.show()
            toolsChat.binding.rlHeaderBackground.hide()
            if (urlBackground.isNotEmpty()) {
                changeBackground(urlBackground)
            }
            toolsChat.bottomSheet.state = TopSheetBehavior.STATE_COLLAPSED
        }
        toolsChat.binding.imgAvatarChat.setOnClickListener {
            activityChat?.toProfile(group.member.member_id ?: 0)
        }
        toolsChat.binding.imgMoreActionChat.setOnClickListener {
            toolsChat.binding.imgMoreActionChat.isEnabled = false
            toolsChat.binding.layoutKeyboard.onBackPressed()
            closeKeyboard(toolsChat.binding.edtMessageChat)
            val bundle = Bundle()
            try {
                bundle.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            val detailChatPersonalFragment = DetailPersonalFragment()
            detailChatPersonalFragment.arguments = bundle
            activityChat?.addOnceFragment(this, detailChatPersonalFragment)
            toolsChat.binding.imgMoreActionChat.isEnabled = true
        }

        toolsChat.binding.imgMore.setOnClickListener {
            if (isShowingKeyBoard && toolsChat.binding.layoutKeyboard.isCheckView == 2) {
                toolsChat.binding.layoutKeyboard.openKeyboard()
            } else {
                toolsChat.binding.imgEmoji.setImageResource(R.drawable.btn_chat_input_emoticon)
                toolsChat.binding.layoutKeyboard.setView(2)
                if (toolsChat.binding.layoutKeyboard.isCheckView != 1) {
                    toolsChat.binding.layoutKeyboard.show()
                }
            }
        }

        toolsChat.binding.imgBackChat.setOnClickListener {
            if (toolsChat.binding.lnAudio.visibility == View.VISIBLE) {
                stopDrawing()
            }
            if (!toolsChat.binding.layoutKeyboard.onBackPressed() && !isKeyBoard) {
                activityChat?.onBackPressed()
            } else {
                toolsChat.binding.layoutKeyboard.onBackPressed()
                closeKeyboard(toolsChat.binding.edtMessageChat)
                activityChat?.finish()
                leavePersonal()
//                onBackPress()
            }
        }

        toolsChat.binding.tvTitleChat.setOnClickListener {
            toolsChat.binding.tvTitleChat.isEnabled = false
            val bundle = Bundle()
            try {
                bundle.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            val detailChatPersonalFragment = DetailPersonalFragment()
            detailChatPersonalFragment.arguments = bundle
            activityChat?.addOnceFragment(this, detailChatPersonalFragment)
            toolsChat.binding.tvTitleChat.isEnabled = true
        }

        toolsChat.binding.utilities.reply.imgReplyClose.setOnClickListener {
            isReplyMessage = false
            toolsChat.binding.utilities.ctlReplyMessage.hide()
        }

        toolsChat.binding.imgCamera.setOnClickListener {
            toolsChat.binding.imgCamera.isEnabled = false
            isChooseImage = true
            closeKeyboard(toolsChat.binding.edtMessageChat)
            val animationMode = AnimationType.DEFAULT_ANIMATION
            val language = 7
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isWithVideoImage(false)
                .maxSelectNum(30)
                .minSelectNum(0)
                .maxVideoSelectNum(1)
                .selectionMode(PictureConfig.MULTIPLE)
                .isSingleDirectReturn(false)
                .isPreviewImage(true)
                .isPreviewVideo(true)
                .isOpenClickSound(true)
                .selectionData(ArrayList())
                .forResult(PictureConfig.CHOOSE_REQUEST)
//            requestMultiplePermissionImage()
            toolsChat.binding.imgCamera.isEnabled = true
        }

        toolsChat.binding.pinned.tvRemovePinned.setOnClickListener {
            revokePinned()
        }

        toolsChat.binding.pinned.imgShowMorePinned.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            if (toolsChat.binding.pinned.lnShowPinned.visibility == View.GONE) {
                toolsChat.binding.pinned.lnShowPinned.show()
            } else {
                toolsChat.binding.pinned.lnShowPinned.hide()
            }
        }

        toolsChat.binding.pinned.tvListPinned.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            toolsChat.binding.pinned.tvListPinned.isEnabled = false
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
            toolsChat.binding.pinned.tvListPinned.isEnabled = true
        }

        toolsChat.binding.rcvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                firstPosition =
                    (Objects.requireNonNull(toolsChat.binding.rcvChat.getLayoutManager()) as LinearLayoutManager).findFirstVisibleItemPosition()
                lastPosition =
                    (Objects.requireNonNull(toolsChat.binding.rcvChat.getLayoutManager()) as LinearLayoutManager).findLastVisibleItemPosition()
                WriteLog.e("firstPosition %s", "$firstPosition")
                WriteLog.e("lastPosition %s", "$lastPosition")
                WriteLog.e("List item %s", "${listMessages.size}")
                if (firstPosition >= 10) {
                    toolsChat.binding.tvMoreMessage.show()
                } else {
                    toolsChat.binding.tvMoreMessage.hide()
                }
                if (stroke == true && isStrokeSearch == 0) {
                    listMessages.forEachIndexed { index, messagesByGroup ->
                        if (messagesByGroup.is_stroke == 1) {
                            positionStroke = index
                            return@forEachIndexed
                        }
                    }
                    if (positionStroke != -1 && (positionStroke - 5 <= firstPosition || positionStroke + 5 >= lastPosition)) {
                        listMessages[positionStroke].is_stroke = 0
                        chatAdapter?.notifyItemChanged(positionStroke)
                        stroke = false
                        positionStroke = -1
                    }
                }
                if (!isLoading) {
                    if (lastPosition >= listMessages.size - 5) {
                        if (!is_oldest_message) {
//                            group._id?.let { getMessagePre(it, pre_cursor, "-1") }
                            group._id?.let {
                                getMessagePreDB(
                                    it,
                                    java.lang.Long.parseLong(pre_cursor)
                                )
                            }
                        }
                    }
                    if (firstPosition <= 5) {
                        if (!is_latest_message) {
//                            group._id?.let { getMessageNext(it, next_cursor, "-1") }
                            group._id?.let {
                                getMessageNextDB(
                                    it,
                                    java.lang.Long.parseLong(next_cursor)
                                )
                            }
                        }
                    }
                }
            }
        })

        toolsChat.binding.tvMoreMessage.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            toolsChat.binding.rcvChat.scrollToPosition(0)
            toolsChat.binding.tvMoreMessage.hide()
        }
        toolsChat.binding.tvNewMessage.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            group._id?.let { it1 -> getMessageDB(it1) }
            toolsChat.binding.tvNewMessage.hide()
        }
        //call video
        toolsChat.binding.lnStatusCallVideo.setOnClickListener {
            ChatUtils.connectToRoom(
                requireActivity(),
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
        toolsChat.binding.imgCallChat.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            requestMultiplePermissionAudio(true)
        }
        toolsChat.binding.imgCallVideoChat.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            requestMultiplePermissionVideoCall()
        }

        toolsChat.binding.imgAudio.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            requestMultiplePermissionAudio(false)
        }

        toolsChat.binding.imgRecycleAudio.setOnClickListener {
            toolsChat.binding.imgEmoji.setImageResource(R.drawable.btn_chat_input_emoticon)
            toolsChat.showController()
            stopDrawing()
        }

        toolsChat.binding.imgEmoji.setOnClickListener {
            if (isShowingKeyBoard && toolsChat.binding.layoutKeyboard.isCheckView == 1) {
                toolsChat.binding.layoutKeyboard.openKeyboard()
            } else {
                toolsChat.binding.imgEmoji.setImageResource(R.drawable.ic_keyboard_chat_message)
                toolsChat.binding.layoutKeyboard.setView(1)
                if (toolsChat.binding.layoutKeyboard.isCheckView != 2) {
                    toolsChat.binding.layoutKeyboard.show()
                }
            }
        }
        //link
        toolsChat.binding.utilities.imgCloseLink.setOnClickListener {
            toolsChat.hideLink()
            isCheckLink = false
        }
        toolsChat.binding.imgCloseLinkSuggest.setOnClickListener {
            toolsChat.binding.cvLinkSuggest.hide()
            isCheckLink = false
        }
        toolsChat.binding.tvSendLinkSuggest.setOnClickListener {
            chatLink()
            isCheckLink = false
            toolsChat.binding.cvLinkSuggest.hide()
        }
        //close screen shot suggest
        toolsChat.binding.imgCloseScreenShot.setOnClickListener {
            listScreenShot?.let { it1 -> listImageSuggestCache.addAll(it1) }
            listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
            cacheManager.put(TechResEnumChat.LIST_IMAGE_SUGGET.name, listStringImageSuggestCache)
            toolsChat.binding.rlScreenShot.hide()
            listScreenShot = ArrayList()
        }
        toolsChat.binding.btnSendScreenShotMore.setOnClickListener {
            listScreenShot?.let { it1 -> listImageSuggestCache.addAll(it1) }
            listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
            cacheManager.put(TechResEnumChat.LIST_IMAGE_SUGGET.name, listStringImageSuggestCache)
            toolsChat.binding.rlScreenShot.hide()
            toolsChat.binding.rlListScreenShot.hide()
            uploadScreenShot()
        }
        toolsChat.binding.btnSendScreenShot.setOnClickListener {
            listScreenShot?.let { it1 -> listImageSuggestCache.addAll(it1) }
            listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
            cacheManager.put(TechResEnumChat.LIST_IMAGE_SUGGET.name, listStringImageSuggestCache)
            toolsChat.binding.rlScreenShot.hide()
            uploadScreenShot()
        }

        toolsChat.binding.btnCloseListScreenShot.setOnClickListener {
            listScreenShot?.let { it1 -> listImageSuggestCache.addAll(it1) }
            listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
            cacheManager.put(TechResEnumChat.LIST_IMAGE_SUGGET.name, listStringImageSuggestCache)
            listScreenShot = ArrayList()
            toolsChat.binding.rlScreenShot.hide()
            toolsChat.binding.rlListScreenShot.hide()
        }
        toolsChat.binding.imgOpenMoreScreenShot.setOnClickListener {
            toolsChat.binding.rlScreenShot.hide()
            toolsChat.binding.rlListScreenShot.show()
        }
        toolsChat.binding.imgCloseMoreScreenShot.setOnClickListener {
            toolsChat.binding.rlScreenShot.show()
            toolsChat.binding.rlListScreenShot.hide()
        }

        toolsChat.binding.edtMessageChat.setOnClickListener {
            toolsChat.binding.layoutKeyboard.openKeyboard()
            toolsChat.binding.edtMessageChat.isCursorVisible = true
        }
        toolsChat.binding.imgSearch.setOnClickListener {
            ShowSearch()
        }
        toolsChat.binding.backSearch.setOnClickListener {
            HideSearch()
        }

        toolsChat.binding.countUp.setOnClickListener {
            if (currentSearch > 1) {
                if (!isClick) {
                    isClick = true
                    currentSearch--
                    toolsChat.binding.messCountSearch.text =
                        "Kết quả thứ $currentSearch / ${messageSearch.size}"
                    isStrokeSearch = 1
                    var check = 0
                    group._id?.let { it1 ->
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (currentSearch > 0) {
                                if (messagesByGroup.random_key?.equals(messageSearch[currentSearch - 1].random_key) == true) {
                                    if (index < ChatActivity.LIMIT_MESSAGE / 2) {
                                        toolsChat.binding.rcvChat.smoothScrollToPosition(
                                            index
                                        )
                                        check = 1
                                        Handler(Looper.getMainLooper()).postDelayed(
                                            {
                                                setStrokeFalse()
                                                listMessages.forEachIndexed { index, mess ->
                                                    if (messagesByGroup.random_key.equals(mess.random_key)) {
                                                        mess.is_stroke = 1
                                                        chatAdapter?.notifyItemChanged(index)
                                                        stroke = true
                                                        isClick = false
                                                        return@forEachIndexed
                                                    }
                                                }
                                            }, 500
                                        )
                                        return@let
                                    }
                                }
                            }
                        }
                        if (check == 0)
                            if (!isLoading) {
                                getMessageScrollDB(
                                    it1,
                                    java.lang.Long.parseLong(messageSearch[currentSearch - 1].random_key)
                                )
//                        getMessageScroll(it1, "-1", "-1", it2)
                            }

                    }
                }
            }
        }
        toolsChat.binding.countDown.setOnClickListener {
            if (currentSearch < messageSearch.size && currentSearch != 0) {
                if (!isClick) {
                    isClick = true
                    currentSearch++
                    toolsChat.binding.messCountSearch.setText("Kết quả thứ " + currentSearch + "/" + messageSearch.size)
                    isStrokeSearch = 1
                    var check = 0
                    group._id?.let { it1 ->
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (currentSearch > 0) {
                                if (messagesByGroup.random_key?.equals(messageSearch[currentSearch - 1].random_key) == true) {
                                    if (index < ChatActivity.LIMIT_MESSAGE / 2) {
                                        toolsChat.binding.rcvChat.smoothScrollToPosition(
                                            index
                                        )
                                        check = 1
                                        isClick = false
                                        Handler(Looper.getMainLooper()).postDelayed(
                                            {
                                                setStrokeFalse()
                                                listMessages.forEachIndexed { index, mess ->
                                                    if (messagesByGroup.random_key.equals(mess.random_key)) {
                                                        mess.is_stroke = 1
                                                        chatAdapter?.notifyItemChanged(index)
                                                        stroke = true
                                                        return@forEachIndexed
                                                    }
                                                }
                                            }, 500
                                        )
                                        return@let
                                    }
                                }
                            }
                        }
                        if (check == 0)
                            if (!isLoading) {
                                getMessageScrollDB(
                                    it1,
                                    java.lang.Long.parseLong(messageSearch[currentSearch - 1].random_key)
                                )
//                        getMessageScroll(it1, "-1", "-1", it2)
                            }
                    }
                }
            }
        }
        toolsChat.binding.inputSearch.setOnQueryTextFocusChangeListener({ view, hasFocus ->
            if (hasFocus) {
                view.postDelayed({
                    showInputMethod(view.findFocus())
                }, 200)
            }
        })
        toolsChat.binding.inputSearch.setOnQueryTextListener(object : DelayedOnQueryTextListener() {
            override fun onDelayerQueryTextChange(s: String?) {
                runOnUiThread {
                    if (s.toString() == "") {
                        toolsChat.binding.messCountSearch.setText("")
                        currentSearch = 0
                        toolsChat.binding.countUp.setEnabled(false)
                        toolsChat.binding.countDown.setEnabled(false)
                        setStrokeFalse()
                        isStrokeSearch = 0
                    } else {
                        TechResApplication.applicationContext().getAppDatabase()
                            ?.runInTransaction {
                                messageSearch =
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.getListMessageSearch(
                                            group._id,
                                            user.id,
                                            s.toString()
                                        ) as ArrayList<MessagesByGroup>
                            }
                        if (messageSearch.size > 0) {
                            currentSearch = messageSearch.size
                            toolsChat.binding.messCountSearch.setText("Kết quả thứ " + currentSearch + "/" + messageSearch.size)

                            toolsChat.binding.countUp.setEnabled(true)
                            toolsChat.binding.countDown.setEnabled(true)
                            isStrokeSearch = 1
                            var check = 0
                            group._id?.let { it1 ->
                                listMessages.forEachIndexed { index, messagesByGroup ->
                                    if (currentSearch > 0) {
                                        if (messagesByGroup.random_key?.equals(messageSearch[currentSearch - 1].random_key) == true) {
                                            if (index < ChatActivity.LIMIT_MESSAGE / 2) {
                                                toolsChat.binding.rcvChat.smoothScrollToPosition(
                                                    index
                                                )
                                                check = 1
                                                Handler(Looper.getMainLooper()).postDelayed(
                                                    {
                                                        setStrokeFalse()
                                                        listMessages.forEachIndexed { index, mess ->
                                                            if (messagesByGroup.random_key.equals(
                                                                    mess.random_key
                                                                )
                                                            ) {
                                                                mess.is_stroke = 1
                                                                chatAdapter?.notifyItemChanged(index)
                                                                stroke = true
                                                                return@forEachIndexed
                                                            }
                                                        }
                                                    }, 500
                                                )
                                                return@let
                                            }
                                        }
                                    }
                                }
                                if (check == 0)
                                    if (!isLoading) {
                                        if (currentSearch > 0)
                                            getMessageScrollDB(
                                                it1,
                                                java.lang.Long.parseLong(messageSearch[currentSearch - 1].random_key)
                                            )
                                        //                        getMessageScroll(it1, "-1", "-1", it2)
                                    }
                            }
                        } else {
                            currentSearch = 0
                            isStrokeSearch = 0
                            setStrokeFalse()
                            toolsChat.binding.messCountSearch.setText("Không tìm thấy tin nhắn")
                            toolsChat.binding.countUp.setEnabled(false)
                            toolsChat.binding.countDown.setEnabled(false)
                        }
                    }
                }

            }
        })
    }

    private fun setStranger() {
        toolsChat.binding.tvStatusMember.text = when (group.member.status) {
            2 -> {
                toolsChat.binding.tvStatusHeader.show()
                toolsChat.binding.lnStatusMember.show()
                toolsChat.binding.imgStatusMember.show()
                toolsChat.binding.imgCallChat.hide()
                toolsChat.binding.imgCallVideoChat.hide()
                toolsChat.binding.imgStatusMember.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_accept_friend)
                requireActivity().resources.getString(R.string.status_member_2)
            }
            3 -> {
                toolsChat.binding.imgCallChat.hide()
                toolsChat.binding.imgCallVideoChat.hide()
                toolsChat.binding.tvStatusHeader.show()
                toolsChat.binding.lnStatusMember.show()
                toolsChat.binding.btnAcceptFriend.show()
                toolsChat.binding.imgStatusMember.hide()
                requireActivity().resources.getString(R.string.status_member_3)
            }
            4 -> {
                toolsChat.binding.imgCallChat.hide()
                toolsChat.binding.imgCallVideoChat.hide()
                toolsChat.binding.tvStatusHeader.show()
                toolsChat.binding.lnStatusMember.show()
                toolsChat.binding.imgStatusMember.show()
                toolsChat.binding.imgStatusMember.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_add_user)
                requireActivity().resources.getString(R.string.make_friend)
            }
            0 -> {
                toolsChat.binding.tvStatusHeader.hide()
                toolsChat.binding.lnStatusMember.hide()
                toolsChat.binding.imgStatusMember.hide()
                toolsChat.binding.imgCallChat.show()
                toolsChat.binding.imgCallVideoChat.show()
//                    requireActivity().resources.getString(R.string.wait_for_your_reply_friend)
                ""
            }
            else -> {
                toolsChat.binding.imgCallChat.show()
                toolsChat.binding.imgCallVideoChat.show()
                toolsChat.binding.tvStatusHeader.show()
//                toolsChat.binding.tvStatusHeader.text = requireActivity().resources.getString(R.string.status_member_1)
                toolsChat.binding.tvStatusHeader.text = group.activities_status
                ""
            }
        }
//        if (group.call_phone == 1) {
//        toolsChat.binding.imgCallChat.show()
//        } else {
//            toolsChat.binding.imgCallChat.hide()
//        }
//        if (group.call_video == 1) {
//        toolsChat.binding.imgCallVideoChat.show()
//        } else {
//            toolsChat.binding.imgCallVideoChat.hide()
//        }
    }

    private fun getCalling() {
        if (PrefUtils.readPreferences(
                TechResEnumVideoCall.CACHE_CALL.toString(),
                TechResEnumVideoCall.CACHE_CALL.toString()
            ) != TechResEnumVideoCall.CACHE_CALL.toString()
        ) {
            toolsChat.binding.lnStatusCallVideo.show()
            notifyVideoCall = Gson().fromJson(
                PrefUtils.readPreferences(
                    TechResEnumVideoCall.CACHE_CALL.toString(),
                    TechResEnumVideoCall.CACHE_CALL.toString()
                ),
                object : TypeToken<NotifyVideoCall>() {}.type
            )
            when (notifyVideoCall.conversation_type) {
                TechResEnumVideoCall.CACHE_CALL_PHONE.toString() -> {
                    toolsChat.binding.imgStatusCallVideo.setImageResource(R.drawable.ic_phone_filled)
                    toolsChat.binding.tvStatusCall.text =
                        requireActivity().resources.getString(R.string.press_to_call_phone)
                }
                TechResEnumVideoCall.CACHE_CALL_VIDEO.toString() -> {
                    toolsChat.binding.imgStatusCallVideo.setImageResource(R.drawable.ic_wfilled_video_call_banner)
                    toolsChat.binding.tvStatusCall.text =
                        requireActivity().resources.getString(R.string.press_to_call_video)
                }
            }
        } else {
            toolsChat.binding.lnStatusCallVideo.hide()
        }

    }

//    /**
//     * Event Bus
//     * Note : Network connect
//     * */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onNetworkConnect(data: EventBusNetworkChange) {
//        WriteLog.d("nhanpro", "ahihihihihihihihihihihihihihihi")
//    }

    /**
     * Event Bus
     * Note : Bussiness Card
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBusinessCard(list: ArrayList<ContactDevice>) {
        list.forEach {
            sendBusinessCard(it.phone ?: "")
        }
    }

    /**
     * Event Bus
     * Note : End Call
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEndCall(event: EventBusEndCall) {
        if (event.isCall) {
            getCalling()
        }
    }

    /**
     * Status Keyboard
     * */
    private fun updateButton(emo: Boolean) {
        if (isShowingKeyBoard == emo) return
        isShowingKeyBoard = emo
        if (emo) {
            toolsChat.binding.imgEmoji.setImageResource(R.drawable.ic_keyboard_chat_message)
        } else {
            toolsChat.binding.imgEmoji.setImageResource(R.drawable.btn_chat_input_emoticon)

        }
    }

    /**
     *
     * */
    open fun postNotifyDataChanged() {
        if (chatAdapter != null) {
            activity?.runOnUiThread {
                if (chatAdapter != null) {
                    chatAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * Permission
     * */

    private fun requestMultiplePermissionMedia() {
        requestPermissions(requiredPermissionFile, requiredPermissionsFileCode, object :
            RequestPermissionListener {
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
                    requireActivity().resources.getString(R.string.title_permission_media),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.title_permission_media_step_two),
                    R.drawable.ic_gallery, "", 0, object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                        }

                    }
                )
            }


            override fun onPermissionGranted() {
                getFileSuggest()
            }

        })
    }

    private fun requestMultiplePermissionImage() {
        requestPermissions(requiredPermissionImage, PICK_FROM_GALLERY, object :
            RequestPermissionListener {
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
                    requireActivity().resources.getString(R.string.title_permission_media),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.title_permission_media_step_two),
                    R.drawable.ic_gallery, "", 0, object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                        }

                    }
                )
            }


            override fun onPermissionGranted() {
                val animationMode = AnimationType.DEFAULT_ANIMATION
                val language = 7
                PictureSelector.create(requireActivity())
                    .openGallery(PictureMimeType.ofAll())
                    .theme(R.style.picture_WeChat_style)
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isCamera(true)
                    .isWithVideoImage(false)
                    .maxSelectNum(30)
                    .minSelectNum(0)
                    .maxVideoSelectNum(1)
                    .selectionMode(PictureConfig.MULTIPLE)
                    .isSingleDirectReturn(false)
                    .isPreviewImage(true)
                    .isPreviewVideo(true)
                    .isOpenClickSound(true)
                    .selectionData(ArrayList())
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }

        })
    }

    private fun requestMultiplePermissionAudio(typeCall: Boolean) {
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
                    if (typeCall) {
//                        val roomId = getRandomString(20)
//                        ChatUtils.connectToRoomVideoCall(
//                            requireActivity(),
//                            group,
//                            roomId,
//                            commandLineRun = false,
//                            loopback = false,
//                            runTimeMs = 0,
//                            option = 0,
//                            type = TechResEnumChat.PERSONAL.toString(),
//                            typeCall = TechResEnumVideoCall.TYPE_CALL_PHONE.toString(),
//                            memberCreate = user.id
//                        )
//                        connectVideoCallPersonal(
//                            roomId,
//                            TechResEnumVideoCall.TYPE_CALL_PHONE.toString()
//                        )
                        ChatUtils.connectToRoom(
                            requireActivity(),
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
                    } else {
                        handler.postDelayed(runnable, 100)
                        toolsChat.binding.imgSent.show()
                        toolsChat.binding.lnAudio.show()
                        toolsChat.binding.lnComment.hide()
                        toolsChat.binding.cvLinkSuggest.hide()
                        toolsChat.binding.rlListScreenShot.hide()
                        toolsChat.binding.rlScreenShot.hide()
                        toolsChat.binding.layoutKeyboard.onBackPressed()
                        closeKeyboard(toolsChat.binding.edtMessageChat)
                    }
                }

            })
    }

    private fun requestMultiplePermissionAudio(typeCall: Boolean, groupCall: Group) {
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
                    if (typeCall) {
                        ChatUtils.connectToRoom(
                            requireActivity(),
                            groupCall._id,
                            groupCall.member.member_id ?: 0,
                            groupCall.member.full_name,
                            groupCall.member.avatar!!.original,
                            user.id,
                            1,
                            "call_audio",
                            "aloline",
                            Utils.getRandomString(20)
                        )
                    } else {
                        handler.postDelayed(runnable, 100)
                        toolsChat.binding.imgSent.show()
                        toolsChat.binding.lnAudio.show()
                        toolsChat.binding.lnComment.hide()
                        toolsChat.binding.cvLinkSuggest.hide()
                        toolsChat.binding.rlListScreenShot.hide()
                        toolsChat.binding.rlScreenShot.hide()
                        toolsChat.binding.layoutKeyboard.onBackPressed()
                        closeKeyboard(toolsChat.binding.edtMessageChat)
                    }
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
                    ChatUtils.connectToRoom(
                        requireActivity(),
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

    private val simpleItemTouchCallback = SimpleItemTouchHelperCallback(object : SwipeItemHandler {
        override fun onReboundOffsetChanged(
            currentSwipePercentage: Float,
            swipeThreshold: Float,
            currentTargetHasMetThresholdOnce: Boolean,
            actionState: Int,
            viewHolder: RecyclerView.ViewHolder
        ) {
        }

        override fun onRebounded(viewHolder: RecyclerView.ViewHolder) {
            val messagesByGroup = listMessages[viewHolder.bindingAdapterPosition]
            when (messagesByGroup.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_IMAGE.toString(), TechResEnumChat.TYPE_STICKER.toString(),
                TechResEnumChat.TYPE_VIDEO.toString(), TechResEnumChat.TYPE_REPLY.toString(), TechResEnumChat.TYPE_AUDIO.toString() -> {
                    replyAction(messagesByGroup)
                }
            }
        }

        override fun onVibrator(position: Int) {
            val messagesByGroup = listMessages[position]
            when (messagesByGroup.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_IMAGE.toString(), TechResEnumChat.TYPE_STICKER.toString(),
                TechResEnumChat.TYPE_VIDEO.toString(), TechResEnumChat.TYPE_REPLY.toString(), TechResEnumChat.TYPE_AUDIO.toString() -> {
                    vibrator?.let {
                        if (Build.VERSION.SDK_INT >= 26) {
                            it.vibrate(
                                VibrationEffect.createOneShot(
                                    50,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            it.vibrate(50)
                        }
                    }
                }
            }
        }
    })

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s!!.isNotEmpty()) {
                linkAction(s)
                typingOn(group)
            } else {
                typingOff(group)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val content = s.toString()
            if (content != "") {
                toolsChat.binding.txtCountCharacter.show()
                if (content.length >= 10000) {
                    toolsChat.binding.edtMessageChat.removeTextChangedListener(this)
                    toolsChat.binding.lnComment.setBackgroundResource(R.drawable.bg_characters_red_radius12)
                    toolsChat.binding.txtCountCharacter.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.red
                        )
                    )
                    toolsChat.binding.edtMessageChat.setText(content.substring(0, 10000))
                    toolsChat.binding.edtMessageChat.setSelection(toolsChat.binding.edtMessageChat.text.length)
                    toolsChat.binding.edtMessageChat.addTextChangedListener(this)
                } else {
                    toolsChat.binding.lnComment.setBackgroundResource(R.drawable.bg_round_light_gray_12)
                    toolsChat.binding.txtCountCharacter.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.gray_text_title
                        )
                    )
                }
                toolsChat.binding.txtCountCharacter.text =
                    String.format("%s/10000", toolsChat.binding.edtMessageChat.text.length)
            } else {
                toolsChat.binding.txtCountCharacter.hide()
                toolsChat.binding.lnComment.setBackgroundResource(R.drawable.bg_round_light_gray_12)
            }
        }
    }

    private fun startRecording() {
        toolsChat.binding.lnAudio.show()
        try {
            val contextWrapper = ContextWrapper(requireActivity())
            audioFile = File(
                contextWrapper.externalCacheDir,
                String.format(
                    "%s%s",
                    getRandomString(12),
                    "audio.mp3"
                )
            )
        } catch (e: IOException) {
            return
        }
        recorder = MediaRecorder()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(48000)
        }
        recorder?.prepare()
        recorder?.start()
        startDrawing()
    }

    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    if (currentMaxAmplitude != null) {
                        toolsChat.binding.audioRecordView.update(currentMaxAmplitude)
                    } else {
                        toolsChat.binding.audioRecordView.update(0)
                    }
                } catch (e: IOException) {
                }
            }
        }, 0, 100)
        toolsChat.binding.tvTimePlayAudio.text =
            requireActivity().resources?.getString(R.string.time_default) ?: ""
        startHTime = SystemClock.uptimeMillis()
        audioTimeHandler.postDelayed(updateTimerThread, 0)
    }

    private val updateTimerThread: Runnable by lazy {
        object : Runnable {
            override fun run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startHTime
                updatedTime = timeSwapBuff + timeInMilliseconds
                var secs = (updatedTime / 1000).toInt()
                val min = secs / 60
                secs %= 60
                toolsChat.binding.tvTimePlayAudio.text = String.format("%s:%02d", min, secs)
                audioTimeHandler.postDelayed(this, 0)
            }
        }
    }

    private fun stopDrawing() {
        timer?.cancel()
        timeSwapBuff = 0L
        audioTimeHandler.removeCallbacks(updateTimerThread)
        toolsChat.binding.audioRecordView.recreate()
        toolsChat.binding.lnComment.show()
        toolsChat.binding.lnAudio.hide()
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }

    private val runnable = Runnable { startRecording() }

    private fun stopRecording() {
        try {
            timer?.cancel()
            timeSwapBuff = 0L
            audioTimeHandler.removeCallbacks(updateTimerThread)
            toolsChat.binding.audioRecordView.recreate()
            toolsChat.binding.lnComment.show()
            toolsChat.binding.lnAudio.hide()
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null

            randomKeyFile = getRandomString(12)
            val nameFile = String.format(
                "%s.%s",
                getRandomString(24),
                "mp3"
            )
            val size = (audioFile?.length() ?: 0 / 1024).toInt()
            uploadFile(audioFile!!, nameFile, 1, 0, 0, size, updatedTime.toInt())
        } catch (e: java.lang.Exception) {
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPinned(messagesByGroup: MessagesByGroup) {
        idPinnedMessage = messagesByGroup.random_key!!
        when (messagesByGroup.message_pinned?.message_type) {
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.imgPinnedPlay.hide()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                getGlide(
                    toolsChat.binding.pinned.imgLinkThumb,
                    messagesByGroup.message_pinned!!.files[0].link_original,
                    configNodeJs
                )
                toolsChat.binding.pinned.tvContentPinned.text =
                    requireActivity().resources.getString(R.string.pinned_image)
            }
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgPinnedPlay.show()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                try {
                    getGlide(
                        toolsChat.binding.pinned.imgLinkThumb,
                        messagesByGroup.message_pinned?.files?.get(0)?.link_original,
                        configNodeJs
                    )
                } catch (e: IOException) {

                }
                toolsChat.binding.pinned.tvContentPinned.text =
                    requireActivity().resources.getString(R.string.pinned_video)
            }
            TechResEnumChat.TYPE_STICKER.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                toolsChat.binding.pinned.imgPinnedPlay.hide()
                getGlide(
                    toolsChat.binding.pinned.imgLinkThumb,
                    messagesByGroup.message_pinned?.files?.get(0)?.link_original,
                    configNodeJs
                )
                toolsChat.binding.pinned.tvContentPinned.text =
                    requireActivity().resources.getString(R.string.pinned_sticker)
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.imgFile.show()
                toolsChat.binding.pinned.cvMedia.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                toolsChat.binding.pinned.tvContentPinned.text =
                    requireActivity().resources.getString(R.string.pinned_file) + " " + URLDecoder.decode(
                        messagesByGroup.message_pinned?.files?.get(
                            0
                        )?.name_file, "UTF-8"
                    )
                val mineType = messagesByGroup.message_pinned?.files?.get(0)?.link_original?.let {
                    FileUtils.getMimeType(
                        it
                    )
                }
                ChatUtils.setImageFile(toolsChat.binding.pinned.imgFile, mineType)
            }
            TechResEnumChat.TYPE_LINK.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgPinnedPlay.hide()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                toolsChat.binding.pinned.tvContentPinned.text = String.format(
                    "%s %s",
                    requireActivity().resources.getString(R.string.pinned_link),
                    messagesByGroup.message_pinned?.message_link?.url ?: ""
                )
                Utils.showImage(
                    toolsChat.binding.pinned.imgLinkThumb,
                    messagesByGroup.message_pinned?.message_link?.media_thumb
                )
            }
            TechResEnumChat.TYPE_AUDIO.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.imgAudio.show()
                toolsChat.binding.pinned.cvMedia.hide()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.tvContentPinned.text =
                    requireActivity().resources.getString(R.string.pinned_audio)
            }
            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.imgPinnedPlay.hide()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                getImage(
                    toolsChat.binding.pinned.imgLinkThumb,
                    messagesByGroup.message_pinned?.message_phone?.avatar?.medium,
                    configNodeJs
                )
                toolsChat.binding.pinned.tvContentPinned.text =
                    String.format(
                        "%s %s",
                        requireActivity().resources.getString(R.string.pinned_business_card),
                        messagesByGroup.message_pinned?.message_phone?.full_name ?: ""
                    )
            }
            else -> {
                toolsChat.binding.pinned.rlThumbContainer.hide()

                var messageHtml = messagesByGroup.message_pinned?.message ?: ""
                messagesByGroup.message_pinned?.list_tag_name?.let {
                    it.forEach { tagName ->
                        val name = String.format(
                            "%s%s%s",
                            "<font color='#198be3' >",
                            "@" + tagName.full_name,
                            "</font>"
                        )
                        messageHtml =
                            messageHtml.replace(tagName.key_tag_name.toString(), name)
                    }
                }
                toolsChat.binding.pinned.tvContentPinned.text =
                    Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_LEGACY)
            }
        }
        toolsChat.binding.pinned.lnPinned.show()
        toolsChat.binding.pinned.tvUserPinned.text = "Tin nhắn từ " +
                messagesByGroup.message_pinned?.sender?.full_name
//        toolsChat.binding.pinned.tvUserPinnedList.text = messagesByGroup.sender.full_name
//        toolsChat.binding.pinned.tvTimePinned.text =
//            TimeFormatHelper.getDateFromStringDayMonthYear(messagesByGroup.created_at)
        toolsChat.binding.pinned.lnPinned.setOnClickListener {
            var position = -1
            group._id?.let { it1 ->
                messagesByGroup.message_pinned?.random_key?.let { it2 ->
                    listMessages.forEachIndexed { index, messagesByGroup ->
                        if (messagesByGroup.random_key?.contains(it2) == true) {
                            position = index
                            return@forEachIndexed
                        }
                    }
                    if (position != -1 && position < ChatActivity.LIMIT_MESSAGE) {
                        toolsChat.binding.rcvChat.smoothScrollToPosition(position)
                        Handler(Looper.getMainLooper()).postDelayed({
                            listMessages.forEachIndexed { index, messagesByGroup ->
                                if (messagesByGroup.random_key?.contains(it2) == true) {
                                    listMessages[index].is_stroke = 1
                                    chatAdapter?.notifyItemChanged(index)
                                    stroke = true
                                    return@forEachIndexed
                                }
                            }
                        }, 500)
                        return@let
                    }
                    if (!isLoading) {
                        getMessageScrollDB(it1, java.lang.Long.parseLong(it2))
//                        getMessageScroll(it1, "-1", "-1", it2)
                    }
                }
            }
        }
    }

    private fun getClipboardManager() {
        val primaryClipData: ClipData? = clipboardManager.primaryClip
        if (primaryClipData != null && primaryClipData.itemCount > 0) {
            primaryClipData.getItemAt(0).text?.let {
                contentPaste = it.toString()
                if ((toolsChat.getCacheCopy(requireActivity()) ?: "") != contentPaste) {
                    if (Patterns.WEB_URL.matcher(contentPaste).matches()) {
                        if (contentPaste.toHttpUrlOrNull() != null) {
                            //clip coppy
                            object : LinkPreviewCallback {
                                override fun onPre() {}
                                override fun onPos(sourceContent: SourceContent, isNull: Boolean) {
                                    if (isNull || sourceContent.finalUrl == "") {
                                        activityChat?.runOnUiThread {
                                            isCheckLink = false
                                            toolsChat.binding.imgLinkSuggest.hide()
                                        }
                                    } else {
                                        activityChat?.runOnUiThread {
                                            isCheckLink = true
                                            toolsChat.saveCacheCopy(
                                                contentPaste,
                                                requireActivity()
                                            )
                                            toolsChat.binding.cvLinkSuggest.show()
                                            if (sourceContent?.images == null) {
                                                toolsChat.binding.imgLinkSuggest.hide()
                                            } else if (sourceContent?.images!!.size > 0) {
                                                toolsChat.binding.imgLinkSuggest.show()
                                                Utils.getMediaGlide(
                                                    toolsChat.binding.imgLinkSuggest,
                                                    sourceContent.images.get(0)
                                                )
                                                imageLink = sourceContent.images.get(0) ?: ""

                                            }
                                            if (sourceContent?.cannonicalUrl != null) {
                                                toolsChat.binding.tvAuthorLinkSuggest.show()
                                                toolsChat.binding.tvAuthorLinkSuggest.text =
                                                    sourceContent?.cannonicalUrl ?: ""
                                                authorLink =
                                                    sourceContent?.cannonicalUrl ?: ""
                                            } else {
                                                toolsChat.binding.tvAuthorLinkSuggest.hide()
                                            }

                                            if (sourceContent?.title != null) {
                                                toolsChat.binding.tvTitleLinkSuggest.show()
                                                toolsChat.binding.tvTitleLinkSuggest.text =
                                                    sourceContent.title ?: ""
                                                titleLink = sourceContent.title ?: ""
                                            } else {
                                                toolsChat.binding.tvTitleLinkSuggest.hide()
                                            }

                                            if (sourceContent?.description != null) {
                                                toolsChat.binding.tvDescriptionLinkSuggest.show()
                                                toolsChat.binding.tvDescriptionLinkSuggest.text =
                                                    sourceContent.description ?: ""
                                                desLink = sourceContent.description ?: ""
                                            } else {
                                                toolsChat.binding.tvDescriptionLinkSuggest.hide()
                                            }

                                            if (sourceContent?.url != null) {
                                                urlLink = sourceContent.url
                                                toolsChat.binding.rlLinkSuggest.setOnClickListener {
                                                    val browserIntent =
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(sourceContent.url)
                                                        )
                                                    startActivity(browserIntent)
                                                }
                                            } else {
                                                urlLink = contentPaste
                                            }
                                        }
                                    }
                                }
                            }.also { linkPreviewClip = it }
                            var textCrawlerClip = TextCrawler()
                            textCrawlerClip.makePreview(linkPreviewClip, contentPaste)

                        }

                    }
                }
            }
        } else {
            toolsChat.binding.cvLinkSuggest.hide()
        }


    }

    private fun getFileSuggest() {
        WriteLog.d("List String Image Sugget : ", listStringImageSuggestCache)
        listScreenShot =
            getFilePathClipFromContentResolver(requireActivity()) as ArrayList<String>?
        listScreenShot = listScreenShot?.stream()
            ?.filter { x: String -> !listStringImageSuggestCache.contains(x) }
            ?.collect(Collectors.toList()) as ArrayList<String>
        if (listScreenShot != null && listScreenShot?.size ?: 0 > 0) {
            setListSuggestFile(listScreenShot)
        }
    }

    private fun setListSuggestFile(list: ArrayList<String>?) {
        if (list?.size ?: 0 > 1) {
            toolsChat.binding.btnSendScreenShot.text = String.format(
                "Gửi %s ảnh",
                list?.size ?: 0
            )
            toolsChat.binding.btnSendScreenShotMore.text = String.format(
                "Gửi %s ảnh",
                list?.size ?: 0
            )
        } else {
            toolsChat.binding.btnSendScreenShot.text =
                requireActivity().resources.getString(R.string.send_now)
        }

        toolsChat.binding.imgOpenMoreScreenShot.ifShow(list?.size ?: 0 > 2)
        toolsChat.binding.rlScreenShot.ifShow(list?.size ?: 0 > 0)

        when (list?.size) {
            null, 0 -> {
                toolsChat.binding.imgOne.hide()
                toolsChat.binding.imgTwo.hide()
            }
            1 -> {
                toolsChat.binding.imgOne.show()
                toolsChat.binding.imgTwo.hide()
                Utils.getMediaGlide(toolsChat.binding.imgOne, list[0])
                val json = SuggestFile(
                    list,
                    TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
                )
                PrefUtils.savePreferences(
                    TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                        json
                    )
                )
            }
            else -> {
                toolsChat.binding.imgOne.show()
                toolsChat.binding.imgTwo.show()
                Utils.getMediaGlide(toolsChat.binding.imgOne, list[0])
                Utils.getMediaGlide(toolsChat.binding.imgTwo, list[1])
                if (list.size > 2) {
                    adapterSuggestFile?.setDataSource(list)
                }
                val json = SuggestFile(
                    list,
                    TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
                )
                PrefUtils.savePreferences(
                    TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                        json
                    )
                )
            }
        }
    }

    private fun uploadScreenShot() {
        val json = SuggestFile(
            listScreenShot,
            TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
        )
        PrefUtils.savePreferences(
            TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                json
            )
        )
        numberFile = listScreenShot?.size ?: 0
        randomKeyFile = getRandomString(12)
        listScreenShot?.let { it1 -> uploadImageLocal(selectList, it1, randomKeyFile, 1) }
        listScreenShot?.forEach {
            val file = File(it)
            val size = (file.length() / 1024).toInt()
            uploadFile(
                file,
                file.name,
                1,
                Utils.getBitmapRotationImage(file)?.height ?: 0,
                Utils.getBitmapRotationImage(file)?.width ?: 0,
                size
            )
        }
        listScreenShot = ArrayList()
    }

    /**
     * Link Suggest
     * */
    private fun linkAction(string: Editable) {
        if (!isCheckLink) {
            if (Patterns.WEB_URL.matcher(string.toString()).matches()) {
                if (string.toString().toHttpUrlOrNull() != null) {
                    //clip coppy
                    object : LinkPreviewCallback {
                        override fun onPre() {}
                        override fun onPos(sourceContent: SourceContent, isNull: Boolean) {
                            if (isNull || sourceContent.finalUrl == "") {
                                activityChat?.runOnUiThread {
                                    isCheckLink = false
                                    toolsChat.hideLink()
                                }
                            } else {
                                activityChat?.runOnUiThread {
                                    isCheckLink = true
                                    toolsChat.showLink()
                                    toolsChat.binding.imgLinkSuggest.hide()
                                    if (sourceContent.images.size > 0) {
                                        if (sourceContent.images[0] == null) {
                                            toolsChat.binding.utilities.imgLink.hide()
                                        } else {
                                            toolsChat.binding.utilities.imgLink.show()
                                            Utils.getMediaGlide(
                                                toolsChat.binding.utilities.imgLink,
                                                sourceContent.images[0]
                                            )
                                            imageLink = sourceContent.images.get(0) ?: ""

                                        }
                                    }

                                    if (sourceContent.raw != null) {
                                        toolsChat.binding.utilities.tvAuthorLink.show()
                                        toolsChat.binding.utilities.tvAuthorLink.text =
                                            sourceContent.raw ?: ""
                                        authorLink = sourceContent.raw ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvAuthorLink.hide()
                                    }

                                    if (sourceContent.title != null) {
                                        toolsChat.binding.utilities.tvTitleLink.show()
                                        toolsChat.binding.utilities.tvTitleLink.text =
                                            sourceContent.title ?: ""
                                        titleLink = sourceContent.title ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvTitleLink.hide()
                                    }

                                    if (sourceContent.description != null) {
                                        toolsChat.binding.utilities.tvDescriptionLink.show()
                                        toolsChat.binding.utilities.tvDescriptionLink.text =
                                            sourceContent.description ?: ""
                                        desLink = sourceContent.description ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvDescriptionLink.hide()
                                    }

                                    if (sourceContent.url != null) {
                                        urlLink = sourceContent.url
                                        toolsChat.binding.utilities.rlLink.setOnClickListener {
                                            val browserIntent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(sourceContent.url)
                                                )
                                            startActivity(browserIntent)
                                        }
                                    } else {
                                        urlLink = string.toString()
                                    }
                                }
                            }
                        }
                    }.also { linkPreview = it }
                    var textCrawler = TextCrawler()
                    textCrawler.makePreview(linkPreview, string.toString())
                }
            }
        } else {
            if (Patterns.WEB_URL.matcher(string.toString()).matches()) {
                if (string.toString().toHttpUrlOrNull() != null) {
                    //clip coppy
                    object : LinkPreviewCallback {
                        override fun onPre() {}
                        override fun onPos(sourceContent: SourceContent, isNull: Boolean) {
                            if (isNull || sourceContent.finalUrl == "") {
                            } else {
                                activityChat?.runOnUiThread {
                                    isCheckLink = true
                                    toolsChat.showLink()
                                    toolsChat.binding.imgLinkSuggest.hide()
                                    if (sourceContent.images.size > 0) {
                                        if (sourceContent.images[0] == null) {
                                            toolsChat.binding.utilities.imgLink.hide()
                                        } else {
                                            toolsChat.binding.utilities.imgLink.show()
                                            Utils.getMediaGlide(
                                                toolsChat.binding.utilities.imgLink,
                                                sourceContent.images[0]
                                            )
                                            imageLink = sourceContent.images[0] ?: ""

                                        }
                                    }

                                    if (sourceContent.raw != null) {
                                        toolsChat.binding.utilities.tvAuthorLink.show()
                                        toolsChat.binding.utilities.tvAuthorLink.text =
                                            sourceContent.raw ?: ""
                                        authorLink = sourceContent.raw ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvAuthorLink.hide()
                                    }

                                    if (sourceContent.title != null) {
                                        toolsChat.binding.utilities.tvTitleLink.show()
                                        toolsChat.binding.utilities.tvTitleLink.text =
                                            sourceContent.title ?: ""
                                        titleLink = sourceContent.title ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvTitleLink.hide()
                                    }

                                    if (sourceContent.description != null) {
                                        toolsChat.binding.utilities.tvDescriptionLink.show()
                                        toolsChat.binding.utilities.tvDescriptionLink.text =
                                            sourceContent.description ?: ""
                                        desLink = sourceContent.description ?: ""
                                    } else {
                                        toolsChat.binding.utilities.tvDescriptionLink.hide()
                                    }

                                    if (sourceContent.url != null) {
                                        urlLink = sourceContent.url
                                        toolsChat.binding.utilities.rlLink.setOnClickListener {
                                            val browserIntent =
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(sourceContent.url)
                                                )
                                            startActivity(browserIntent)
                                        }
                                    } else {
                                        urlLink = string.toString()
                                    }
                                }
                            }
                        }
                    }.also { linkPreview = it }
                    var textCrawler = TextCrawler()
                    textCrawler.makePreview(linkPreview, string.toString())
                }

            }

        }
    }

    private fun replyAction(messagesByGroup: MessagesByGroup) {
//        closeKeyboard(toolsChat.binding.edtMessageChat)
        isReplyMessage = true
        toolsChat.binding.utilities.ctlReplyMessage.show()
        replyMessage.sender = messagesByGroup.sender
        replyMessage.files = messagesByGroup.files
        replyMessage.status = messagesByGroup.status
        replyMessage.message = messagesByGroup.message
        replyMessage.message_type = messagesByGroup.message_type
        replyMessage.random_key = messagesByGroup.random_key
        when (messagesByGroup.message_type) {
            TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.hide()
                toolsChat.binding.utilities.reply.tvReplyName.text =
                    messagesByGroup.sender.full_name
                val messageHtml = messagesByGroup.message!!.replace("\n", "<br>")
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_COMPACT)
//                    toolsChat.binding.reply.tvReplyContent.text = messagesByGroup.message
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
            }
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.tvReplyName.text =
                    messagesByGroup.sender.full_name
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_image)
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
                    configNodeJs
                )
            }
            TechResEnumChat.TYPE_STICKER.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.tvReplyName.text =
                    messagesByGroup.sender.full_name
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_sticker)
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
                    configNodeJs
                )
            }
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.tvReplyName.text =
                    messagesByGroup.sender.full_name
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_video)
                toolsChat.binding.utilities.reply.imgReplyPlay.show()
                getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
                    configNodeJs
                )
            }

            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                toolsChat.binding.utilities.reply.cvAvatar.show()
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.hide()
                toolsChat.binding.utilities.reply.tvReplyName.text =
                    messagesByGroup.sender.full_name
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_business_card) + " " + messagesByGroup.message_phone!!.full_name
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                getGlide(
                    toolsChat.binding.utilities.reply.imgAvatar,
                    messagesByGroup.message_phone?.avatar?.medium,
                    configNodeJs
                )
            }
            TechResEnumChat.TYPE_AUDIO.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.show()
                toolsChat.binding.utilities.reply.imgFile.hide()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_audio)

            }
            TechResEnumChat.TYPE_LINK.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.show()
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.hide()
                Utils.showImage(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.message_link?.media_thumb
                )
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    String.format(
                        "%s %s",
                        requireActivity().resources.getString(R.string.pinned_link),
                        messagesByGroup.message_link?.url ?: ""
                    )
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.show()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_file) + URLDecoder.decode(
                        messagesByGroup.files.get(
                            0
                        ).name_file, "UTF-8"
                    )
                val mineType = messagesByGroup.files[0].link_original?.let {
                    FileUtils.getMimeType(
                        it
                    )
                }
                ChatUtils.setImageFile(toolsChat.binding.utilities.reply.imgFile, mineType)
            }
        }

        showKeyboard(toolsChat.binding.edtMessageChat)
        toolsChat.binding.layoutKeyboard.openKeyboard()
        isCheckUtilities = true
    }

    private fun bottomSheetReaction(messagesByGroup: MessagesByGroup) {
        closeKeyboard(toolsChat.binding.edtMessageChat)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(50)
            }
        }
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_message_reaction)
        bottomSheetDialog.setCancelable(true)
        val llReplyReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llReplyReaction)
        val llShareReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llShareReaction)
        val llPinnedReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llPinnedReaction)
        val llInformationReaction =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.llInformationReaction)
        val llCopyReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llCopyReaction)
        val llRevokeReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llRevokeReaction)
        val lnMessageImportantReaction =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.lnMessageImportantReaction)
        val llDownloadReaction =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.llDownloadReaction)
        val itemLike = bottomSheetDialog.findViewById<ImageView>(R.id.like_item)
        val itemLove = bottomSheetDialog.findViewById<ImageView>(R.id.love_item)
        val itemSmile = bottomSheetDialog.findViewById<ImageView>(R.id.smile_item)
        val itemWow = bottomSheetDialog.findViewById<ImageView>(R.id.wow_item)
        val itemAngry = bottomSheetDialog.findViewById<ImageView>(R.id.angry_item)
        val itemSad = bottomSheetDialog.findViewById<ImageView>(R.id.sad_item)

        llInformationReaction?.hide()

        if ((messagesByGroup.sender.member_id ?: 0) != user.id) {
            llRevokeReaction?.hide()
        } else {
            llRevokeReaction?.show()
        }

        when (messagesByGroup.message_type) {
            TechResEnumChat.TYPE_IMAGE.toString(),
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                llDownloadReaction?.show()
                llCopyReaction?.hide()
            }

            TechResEnumChat.TYPE_STICKER.toString(),
            TechResEnumChat.TYPE_BUSINESS_CARD.toString(),
            TechResEnumChat.TYPE_PROMOTE_VICE.toString(),
            TechResEnumChat.TYPE_GI_PHY.toString(),
            TechResEnumVideoCall.TYPE_CALL_PHONE.toString(),
            TechResEnumVideoCall.TYPE_CALL_VIDEO.toString(),
            TechResEnumChat.TYPE_INVITE_EVENT.toString(),
            TechResEnumChat.TYPE_AUDIO.toString() -> {
                llCopyReaction?.hide()
                llDownloadReaction?.hide()
            }

            else -> {
                llDownloadReaction?.hide()
                llCopyReaction?.show()
            }
        }

        itemLike?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_like)
            binding.fragmentRootView.startDropping()
            /*if (isCheckReaction) {
                binding.fragmentRootView.stopDropping()
                binding.fragmentRootView.clearEmojis()
                binding.fragmentRootView.addEmoji(R.drawable.ic_like)
                binding.fragmentRootView.startDropping()
                isCheckReaction = false
            } else {
                isCheckReaction = false
                Handler(Looper.getMainLooper()).postDelayed({
                    isCheckReaction = true
                }, 4000)
            }*/

            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }
        itemLove?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_heart)
            binding.fragmentRootView.startDropping()
            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }
        itemAngry?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_angry)
            binding.fragmentRootView.startDropping()
            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }
        itemSmile?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_smile)
            binding.fragmentRootView.startDropping()
            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }
        itemWow?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_wow)
            binding.fragmentRootView.startDropping()
            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_WOW.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }
        itemSad?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_sad)
            binding.fragmentRootView.startDropping()
            reactionMessage(
                messagesByGroup,
                group,
                TechResEnumChat.TYPE_REACTION_SAD.toString().toInt()
            )
            bottomSheetDialog.dismiss()
        }

        llRevokeReaction?.setOnClickListener {
            revokeMessage(messagesByGroup.random_key!!)
            bottomSheetDialog.dismiss()
        }

        llReplyReaction?.setOnClickListener {
            bottomSheetDialog.dismiss()
            Handler(Looper.getMainLooper()).postDelayed({
                replyAction(messagesByGroup)
            }, 500)
        }

        llInformationReaction?.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString(
//                TechResEnumChat.MESSAGE_INFORMATION.toString(),
//                Gson().toJson(messagesByGroup)
//            )
//            val informationMessageFragment = InformationMessageFragment()
//            informationMessageFragment.arguments = bundle
//            activityChat?.addOnceFragment(this, informationMessageFragment)
            val intent = Intent(context, InformationMessageActivity::class.java)
            intent.putExtra(
                TechResEnumChat.MESSAGE_INFORMATION.toString(),
                Gson().toJson(messagesByGroup)
            )
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        llShareReaction?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(TechresEnum.SHARE_MESSAGE.toString(), Gson().toJson(messagesByGroup))
            bundle.putString(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group))
            val shareMessageFragment = ShareMessageFragment()
            shareMessageFragment.arguments = bundle
            activityChat?.addOnceFragment(this, shareMessageFragment)
            bottomSheetDialog.dismiss()
        }

        llCopyReaction?.setOnClickListener {
            when (messagesByGroup.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString(), TechResEnumChat.TYPE_LINK.toString() -> {
                    val messageHtml = messagesByGroup.message!!.replace("\n", "<br>")
                    val clipData = ClipData.newPlainText(
                        "text",
                        Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_COMPACT)
                    )
                    myClipboard!!.setPrimaryClip(clipData)
                    FancyToast.makeText(
                        requireActivity(),
                        resources.getString(R.string.copied),
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                        false
                    ).show()
                }
                TechResEnumChat.TYPE_IMAGE.toString(), TechResEnumChat.TYPE_VIDEO.toString() -> {
                    val downloadManager =
                        requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri = Uri.parse(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            messagesByGroup.files[0].link_original
                        )
                    )
                    val request = DownloadManager.Request(uri)
                    request.setTitle(
                        getFileNameFromURL(
                            String.format(
                                "%s%s",
                                configNodeJs.api_ads,
                                messagesByGroup.files[0].link_original
                            )
                        )
                    )
                    request.setDescription(requireActivity().resources.getString(R.string.download))
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        getFileNameFromURL(
                            String.format(
                                "%s%s",
                                configNodeJs.api_ads,
                                messagesByGroup.files[0].link_original
                            )
                        )
                    )
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    downloadManager.enqueue(request)
                    FancyToast.makeText(
                        requireActivity(),
                        resources.getString(R.string.downloading),
                        FancyToast.LENGTH_LONG,
                        FancyToast.INFO,
                        false
                    ).show()
                }
            }
            bottomSheetDialog.dismiss()
        }

        llPinnedReaction?.setOnClickListener {
            pinned(messagesByGroup)
            bottomSheetDialog.dismiss()
        }

        lnMessageImportantReaction?.setOnClickListener {
            starMessageImportant(messagesByGroup)
            FancyToast.makeText(
                requireActivity(),
                requireActivity().resources.getString(R.string.marked),
                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                false
            ).show()
            bottomSheetDialog.dismiss()
        }

        llDownloadReaction?.setOnClickListener {
            messagesByGroup.files[0].link_original?.let { it1 ->
                Utils.downLoadFile(
                    requireActivity(),
                    it1, configNodeJs
                )
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    /**
     * Set Typing
     * */

    private fun setTypingTextReply(messagesByGroup: MessagesByGroup) {
//        if (messagesByGroup.sender.member_id != user.id) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            messagesByGroup.user_id = user.id
            TechResApplication.applicationContext().getMessageDao()
                ?.insertOneMessage(messagesByGroup)
        }
        if (is_latest_message == true) {
            toolsChat.binding.tvNewMessage.hide()
            if (listTypingUser.any { it.random_key == messagesByGroup.typing_off.random_key }) {
                positionTyping =
                    listTypingUser.indexOfFirst { it.random_key == messagesByGroup.typing_off.random_key }
                listMessages.removeAt(positionTyping)
                chatAdapter?.notifyItemRemoved(positionTyping)
                listTypingUser.removeAt(positionTyping)
                chatAdapter?.notifyItemChanged(listTypingUser.size)
                listMessages.add(listTypingUser.size, messagesByGroup)
                chatAdapter?.notifyItemInserted(listTypingUser.size)
            } else {
                if (listTypingUser.size >= 1) {
                    chatAdapter?.notifyItemChanged(listTypingUser.size)
                    listMessages.add(listTypingUser.size, messagesByGroup)
                    chatAdapter?.notifyItemInserted(listTypingUser.size)
                } else {
                    chatAdapter?.notifyItemChanged(0)
                    listMessages.add(0, messagesByGroup)
                    chatAdapter?.notifyItemInserted(0)
                }
            }
            if (chatAdapter?.playingPosition != -1)
                chatAdapter?.playingPosition = chatAdapter?.playingPosition?.plus(1)!!
            if (messagesByGroup.sender.member_id == user.id) {
                toolsChat.binding.lnNoMessage.hide()
                toolsChat.binding.tvMoreMessage.hide()
                toolsChat.binding.tvNewMessage.hide()
                toolsChat.binding.rcvChat.scrollToPosition(0)
            } else if (firstPosition < 10) {
                toolsChat.binding.lnNoMessage.hide()
                toolsChat.binding.tvMoreMessage.hide()
                toolsChat.binding.tvNewMessage.hide()
                toolsChat.binding.rcvChat.scrollToPosition(0)
            } else if (firstPosition > 10) {
                toolsChat.binding.tvNewMessage.show()
            }
        } else {
            if (messagesByGroup.sender.member_id == user.id) {
                group._id?.let { getMessageDB(it) };
            } else {
                toolsChat.binding.tvNewMessage.show()
            }
        }
//        }
    }

    private fun setTyping(messagesByGroup: MessagesByGroup) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            messagesByGroup.user_id = user.id
            TechResApplication.applicationContext().getMessageDao()
                ?.insertOneMessage(messagesByGroup)
        }
        if (is_latest_message == true) {
            toolsChat.binding.tvNewMessage.hide()
            if (listTypingUser.size >= 1) {
                listMessages.add(listTypingUser.size, messagesByGroup)
                chatAdapter?.notifyItemInserted(listTypingUser.size)
            } else {
                listMessages.add(0, messagesByGroup)
                chatAdapter?.notifyItemInserted(0)
            }
            if (messagesByGroup.message_type == TechResEnumChat.TYPE_FILE.toString() || messagesByGroup.message_type == TechResEnumChat.TYPE_IMAGE.toString() || messagesByGroup.message_type == TechResEnumChat.TYPE_VIDEO.toString() || messagesByGroup.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                TasksManager().getImpl().addMessage(messagesByGroup)
            }
//        chatAdapter?.stopPlayer()
//        listMessages.forEachIndexed { index, messagesByGroup ->
//            if(messagesByGroup.message_type.equals(TechResEnumChat.TYPE_AUDIO.toString()))
//                chatAdapter?.notifyItemChanged(index)
//        }
            if (chatAdapter?.playingPosition != -1)
                chatAdapter?.playingPosition = chatAdapter?.playingPosition?.plus(1)!!
            if (messagesByGroup.sender.member_id == user.id) {
                toolsChat.binding.lnNoMessage.hide()
                toolsChat.binding.rcvChat.scrollToPosition(0)
            } else if (firstPosition < 10) {
                toolsChat.binding.lnNoMessage.hide()
                toolsChat.binding.rcvChat.scrollToPosition(0)
            } else if (firstPosition > 10) {
                toolsChat.binding.tvNewMessage.show()
            }
        } else {
            if (messagesByGroup.sender.member_id == user.id) {
                group._id?.let { getMessageDB(it) };
            } else {
                toolsChat.binding.tvNewMessage.show()
            }
        }

    }

    /**
     * Upload File Local
     * */
    @SuppressLint("SimpleDateFormat")
    private fun uploadAudioLocal(audio: String, randomKey: String) {
        val sender = Sender()
        val message = MessagesByGroup()
        val file = ArrayList<FileNodeJs>()
        val fileNodeJs = FileNodeJs()
        val today = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        fileNodeJs.link_original = audio
        file.add(fileNodeJs)
        sender.avatar?.medium = user.avatar_three_image.original
        sender.full_name = user.full_name
        sender.member_id = user.id
        message.sender = sender
        message.created_at = today.format(Date())
        message.local = 1
        message.status = 1
        message.progress_number = 0
        message.key_message = randomKey
        message.random_key = getRandomString(12)
        message.files = file
        message.message_type = resources.getString(R.string.type_audio)
        toolsChat.binding.lnNoMessage.hide()
        listMessages.add(0, message)
        chatAdapter?.notifyItemInserted(0)
        toolsChat.binding.rcvChat.smoothScrollToPosition(0)
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadImageLocal(
        selectList: List<LocalMedia>,
        listScreenShot: ArrayList<String>,
        randomKey: String,
        type: Int
    ) {
        val sender = Sender()
        val message = MessagesByGroup()
        val file = ArrayList<FileNodeJs>()
        val today = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        if (type == 0) {
            selectList.forEach {
                val fileNodeJs = FileNodeJs()
                fileNodeJs.link_original = it.realPath
                fileNodeJs.height = Utils.getBitmapRotationImage(File(it.realPath))?.height ?: 0
                fileNodeJs.width = Utils.getBitmapRotationImage(File(it.realPath))?.width ?: 0
                file.add(fileNodeJs)
            }
        } else {
            listScreenShot.forEach {
                val fileNodeJs = FileNodeJs()
                fileNodeJs.link_original = it
                fileNodeJs.height = Utils.getBitmapRotationImage(File(it))?.height ?: 0
                fileNodeJs.width = Utils.getBitmapRotationImage(File(it))?.width ?: 0
                file.add(fileNodeJs)
            }
        }

        sender.avatar?.medium = user.avatar_three_image.original
        sender.full_name = user.full_name
        sender.member_id = user.id
        message.sender = sender
        message.created_at = today.format(Date())
        message.local = 1
        message.status = 1
        message.progress_number = 0
        message.key_message = randomKey
        message.random_key = getRandomString(12)
        message.files = file
        message.message_type = TechResEnumChat.TYPE_IMAGE.toString()
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadVideoLocal(selectList: LocalMedia, randomKey: String) {
        val sender = Sender()
        val message = MessagesByGroup()
        val file = ArrayList<FileNodeJs>()
        val today = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        val fileNodeJs = FileNodeJs()
        fileNodeJs.link_original = selectList.realPath
        fileNodeJs.height = Utils.getBitmapRotationVideo(File(selectList.realPath))?.height ?: 0
        fileNodeJs.width = Utils.getBitmapRotationVideo(File(selectList.realPath))?.width ?: 0
        file.add(fileNodeJs)
        sender.avatar?.medium = user.avatar_three_image.original
        sender.full_name = user.full_name
        sender.member_id = user.id
        message.sender = sender
        message.created_at = today.format(Date())
        message.local = 1
        message.status = 1
        message.progress_number = 0
        message.key_message = randomKey
        message.random_key = getRandomString(12)
        message.files = file
        message.message_type = resources.getString(R.string.type_video)
    }

    /**
     * Socket Emit
     * */
    private fun joinGroup(groupId: String) {
        val joinChatRequest = JoinChatRequest()
        joinChatRequest.group_id = groupId
        joinChatRequest.member_id = user.id
        joinChatRequest.full_name = user.name
        joinChatRequest.avatar = user.avatar_three_image.original
        joinChatRequest.os_name = "android"
        try {
            val jsonObject = JSONObject(Gson().toJson(joinChatRequest))
            mSocket?.emit(TechResEnumChat.JOIN_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("JOIN_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun leavePersonal() {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = group._id ?: ""
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name
        leaveChatRequest.avatar = user.avatar_three_image.original

        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_PERSONAL_ALO_LINE", jsonObject.toString())
            EventBus.getDefault().post(group._id?.let { EventBusLeaveRoom(it) })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatText(group: Group) {
        val s = toolsChat.binding.edtMessageChat.text.toString()
        val m: Matcher = Pattern.compile("(.{1,2000}(\\W|$))").matcher(s)

        if (Utils.checkForInternet(context!!)) {
            try {
                while (m.find()) {
                    val chatTextPersonalRequest = ChatTextPersonalRequest()
                    chatTextPersonalRequest.message_type = TechResEnumChat.TYPE_TEXT.toString()
                    chatTextPersonalRequest.group_id = group._id.toString()
                    chatTextPersonalRequest.member_id = user.id
                    chatTextPersonalRequest.message = m.group()
                    chatTextPersonalRequest.key_message_error = getRandomString(10)
                    try {
                        val jsonObject = JSONObject(Gson().toJson(chatTextPersonalRequest))
                        mSocket?.emit(
                            TechResEnumChat.CHAT_TEXT_PERSONAL_ALO_LINE.toString(),
                            jsonObject
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            //Tin nhắn offline
//            try {
//                while (m.find()) {
//                    val messagesByGroup = MessagesByGroup()
//                    messagesByGroup.message_type = TechResEnumChat.TYPE_TEXT.toString()
//                    messagesByGroup.receiver_id = group._id.toString()
//                    messagesByGroup.member_id = user.id
//                    messagesByGroup.user_id = user.id
//                    messagesByGroup.message = m.group()
//                    messagesByGroup.key_message_error = getRandomString(10)
//                    messagesByGroup.sender.member_id = user.id
//                    messagesByGroup.created_at = Utils.getCurrentDateTime()
//                    messagesByGroup.random_key = Date().time.toString()
//                    messagesByGroup.offline = 1
//                    messagesByGroup.group_id = 1
//                    setTypingTextReply(messagesByGroup)
//                }
//            } catch (ex: Exception){
//                ex.printStackTrace()
//            }


        }


        toolsChat.binding.edtMessageChat.text = Editable.Factory.getInstance().newEditable("")
    }

    private fun chatImage(file: ArrayList<FileNodeJs>) {
        val chatImagePersonalRequest = ChatImagePersonalRequest()
        chatImagePersonalRequest.message_type = TechResEnumChat.TYPE_IMAGE.toString()
        chatImagePersonalRequest.group_id = group._id
        chatImagePersonalRequest.member_id = user.id
        chatImagePersonalRequest.files = file
        chatImagePersonalRequest.key_message_error = getRandomString(10)
        chatImagePersonalRequest.key_message = randomKeyFile

        try {
            val jsonObject = JSONObject(Gson().toJson(chatImagePersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_PERSONAL_IMAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_PERSONAL_IMAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatVideo(file: ArrayList<FileNodeJs>) {
        val chatVideoPersonalRequest = ChatVideoPersonalRequest()
        chatVideoPersonalRequest.message_type = TechResEnumChat.TYPE_VIDEO.toString()
        chatVideoPersonalRequest.group_id = group._id
        chatVideoPersonalRequest.member_id = user.id
        chatVideoPersonalRequest.files = file
        chatVideoPersonalRequest.key_message_error = getRandomString(10)
        chatVideoPersonalRequest.key_message = randomKeyFile
        try {
            val jsonObject = JSONObject(Gson().toJson(chatVideoPersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_PERSONAL_VIDEO_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_PERSONAL_VIDEO_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatFile(file: ArrayList<FileNodeJs>) {
        val chatFilePersonalRequest = ChatFilePersonalRequest()
        chatFilePersonalRequest.message_type = TechResEnumChat.TYPE_FILE.toString()
        chatFilePersonalRequest.group_id = group._id.toString()
        chatFilePersonalRequest.files = file
        chatFilePersonalRequest.member_id = user.id
        chatFilePersonalRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatFilePersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_FILE_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_FILE_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun reactionMessage(
        messagesByGroup: MessagesByGroup,
        group: Group,
        idReaction: Int
    ) {
        val reactionMessageRequest = ReactionPersonalRequest()
        reactionMessageRequest.last_reactions_id = user.id
        reactionMessageRequest.group_id = group._id
        reactionMessageRequest.random_key = messagesByGroup.random_key
        reactionMessageRequest.member_id = user.id
        reactionMessageRequest.key_message_error =
            getRandomString(10)
        if (idReaction > 0) {
            reactionMessageRequest.last_reactions = idReaction
        } else {
            reactionMessageRequest.last_reactions =
                TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt()
        }
        try {
            val jsonObject = JSONObject(Gson().toJson(reactionMessageRequest))
            WriteLog.d("REACTION_MESSAGE_PERSONAL_ALO_LINE", jsonObject.toString())
            mSocket?.emit(
                TechResEnumChat.REACTION_MESSAGE_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun revokeMessage(random_key: String) {
        val revokePersonalRequest = RevokeChatRequest()
        revokePersonalRequest.random_key = random_key
        revokePersonalRequest.group_id = group._id
        revokePersonalRequest.member_id = user.id
        revokePersonalRequest.message_type = TechResEnumChat.TYPE_REVOKE.toString()
        revokePersonalRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(revokePersonalRequest))
            mSocket?.emit(TechResEnumChat.REVOKE_MESSAGE_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REVOKE_MESSAGE_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun pinned(messagesByGroup: MessagesByGroup) {
        val pinnedPersonalRequest = PinnedRequest()
        pinnedPersonalRequest.random_key = messagesByGroup.random_key
        pinnedPersonalRequest.member_id = user.id
        pinnedPersonalRequest.group_id = group._id
        pinnedPersonalRequest.message_type = TechresEnum.TYPE_MESSAGE_CHAT_PINNED.toString()
        pinnedPersonalRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(pinnedPersonalRequest))
            mSocket?.emit(TechResEnumChat.PINNED_MESSAGE_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("PINNED_MESSAGE_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun revokePinned() {
        toolsChat.binding.pinned.lnPinned.hide()
        val revokePinnedChatRequest = RevokePinnedChatRequest()
        revokePinnedChatRequest.random_key = idPinnedMessage
        revokePinnedChatRequest.group_id = group._id
        revokePinnedChatRequest.member_id = user.id
        revokePinnedChatRequest.message_type = TechResEnumChat.TYPE_REMOVE_PINNED.toString()
        revokePinnedChatRequest.key_message_error =
            getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(revokePinnedChatRequest))
            mSocket?.emit(TechResEnumChat.REVOKE_PINNED_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REVOKE_PINNED_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun typingOn(group: Group) {
        val typingOnRequest = TypingOnRequest()
        typingOnRequest.group_id = group._id
        typingOnRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(typingOnRequest))
            if (isTying == 0) {
                isTying = 1
                mSocket?.emit(TechResEnumChat.TYPING_ON_PERSONAL_ALO_LINE.toString(), jsonObject)
                WriteLog.d("TYPING_ON_PERSONAL_ALO_LINE", jsonObject.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun typingOff(group: Group) {
        val typingOffRequest = TypingOffRequest()
        typingOffRequest.group_id = group._id.toString()
        typingOffRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(typingOffRequest))
            isTying = 0
            mSocket?.emit(TechResEnumChat.TYPING_OFF_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("TYPING_OFF_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun reply(group: Group, reply: Reply) {
        val replyPersonalRequest = ReplyPersonalRequest()
        replyPersonalRequest.member_id = user.id
        replyPersonalRequest.group_id = group._id ?: ""
        replyPersonalRequest.message = toolsChat.binding.edtMessageChat.text.toString()
        replyPersonalRequest.message_type = TechResEnumChat.TYPE_REPLY.toString()
        replyPersonalRequest.random_key = reply.random_key ?: ""
        replyPersonalRequest.key_message_error = getRandomString(10)

        try {
            val jsonObject = JSONObject(Gson().toJson(replyPersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_REPLY_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_REPLY_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        isReplyMessage = false
        toolsChat.binding.utilities.ctlReplyMessage.hide()
        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable("")
    }

    private fun chatAudio(file: ArrayList<FileNodeJs>) {
        val chatAudioPersonalRequest = ChatAudioPersonalRequest()
        chatAudioPersonalRequest.member_id = user.id
        chatAudioPersonalRequest.message_type = TechResEnumChat.TYPE_AUDIO.toString()
        chatAudioPersonalRequest.group_id = group._id
        chatAudioPersonalRequest.files = file
        chatAudioPersonalRequest.key_message_error =
            getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatAudioPersonalRequest))
            WriteLog.d("CHAT_PERSONAL_AUDIO_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_AUDIO_PERSONAL_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatSticker(sticker: Sticker) {
        val chatStickerPersonalRequest = ChatStickerPersonalRequest()
        chatStickerPersonalRequest.member_id = user.id
        chatStickerPersonalRequest.sticker_id = sticker._id
        chatStickerPersonalRequest.group_id = group._id
        chatStickerPersonalRequest.message_type = TechResEnumChat.TYPE_STICKER.toString()
        chatStickerPersonalRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatStickerPersonalRequest))
            mSocket?.emit(TechResEnumChat.CHAT_STICKER_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_STICKER_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun sendBusinessCard(phone: String) {
        val businessCardRequest = BusinessCardRequest()
        businessCardRequest.group_id = group._id
        businessCardRequest.member_id = user.id
        businessCardRequest.message_type = TechResEnumChat.TYPE_BUSINESS_CARD.toString()
        businessCardRequest.phone = phone
        businessCardRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(businessCardRequest))
            WriteLog.d("BUSINESS_CARD_PERSONAL_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.BUSINESS_CARD_PERSONAL_ALO_LINE.toString(), jsonObject)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun chatLink() {
        val linkMessageRequest = LinkMessageRequest()
        val textMessage = toolsChat.binding.edtMessageChat.text.toString()
        linkMessageRequest.member_id = user.id
        linkMessageRequest.message_type = TechResEnumChat.TYPE_LINK.toString()
        linkMessageRequest.group_id = group._id
        linkMessageRequest.message = textMessage
        linkMessageRequest.message_link.media_thumb = imageLink
        linkMessageRequest.message_link.favicon = ""
        linkMessageRequest.message_link.title = titleLink
        linkMessageRequest.message_link.description = desLink
        linkMessageRequest.message_link.cannonical_url = urlLink
        linkMessageRequest.message_link.author = authorLink
        linkMessageRequest.message_link.url = urlLink

        linkMessageRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(linkMessageRequest))
            WriteLog.d("CHAT_LINK_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_LINK_PERSONAL_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable("")
        toolsChat.hideLink()
    }

    private fun changeBackground(background: String) {
        val changeBackgroundRequest = ChangeBackgroundRequest()
        changeBackgroundRequest.name = user.name ?: ""
        changeBackgroundRequest.avatar = group.avatar ?: ""
        changeBackgroundRequest.background = background
        changeBackgroundRequest.group_id = group._id ?: ""
        changeBackgroundRequest.member_id = user.id
        changeBackgroundRequest.message_type = TechResEnumChat.TYPE_BACKGROUND.toString()
        changeBackgroundRequest.key_message_error =
            getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(changeBackgroundRequest))
            mSocket?.emit(
                TechResEnumChat.UPDATE_BACKGROUND_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
            WriteLog.d("UPDATE_BACKGROUND_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun starMessageImportant(messagesByGroup: MessagesByGroup) {
        val startMessageRequest = StartMessageRequest()
        startMessageRequest.group_id = group._id
        startMessageRequest.member_id = user.id
        startMessageRequest.random_key = messagesByGroup.random_key
        startMessageRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(startMessageRequest))
            mSocket?.emit(TechResEnumChat.STAR_MESSAGE_PERSONAL_ALO_LINE.toString(), jsonObject)
            WriteLog.d("STAR_MESSAGE_PERSONAL_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun makeFriendSocket() {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("request-to-contact", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun acceptFriend() {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = group.member.member_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("approve-to-contact", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * Socket On
     * */
    private fun onSocket() {
        /**
         * Text and Link.
         * Response: data message and add to message list.
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_TEXT_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_TEXT_PERSONAL_ALO_LINE", it[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(it[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTypingTextReply(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Media: Video, Image, File, Audio.
         * Response: data file and add to message list.
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_PERSONAL_IMAGE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_IMAGE_PERSONAL_PARTY", args[0].toString())

                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_PERSONAL_VIDEO_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_VIDEO_PERSONAL_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_AUDIO_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_AUDIO_PARTY_PERSONAL", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_FILE_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_FILE_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Sticker.
         * Response: data file and add to message list.
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_STICKER_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_STICKER_PERSONAL_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Link.
         * Response: data file and add to message list.
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_LINK_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_LINK_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Revoke Message.
         * Response: change status message in message list.
         * */
        mSocket?.on(TechResEnumChat.RES_REVOKE_MESSAGE_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_PERSONAL_CHAT", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    listMessages.forEachIndexed { index, messages ->
                        if (messages.random_key == messagesByGroup.random_key) {
                            messages.status = 0
                            messages.message_type = TechResEnumChat.TYPE_REVOKE.toString()
                            chatAdapter?.notifyItemChanged(index)
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataRevoke(
                                            group._id,
                                            messages.random_key,
                                            messages.status,
                                            user.id,
                                            ArrayList(),
                                            messages.message_type
                                                ?: TechResEnumChat.TYPE_REVOKE.toString()
                                        )
                                }
//                            return@forEachIndexed
                        }
                    }
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Reaction Message
         * Note : drop reaction on a message
         * */
        mSocket?.on(TechResEnumChat.RES_MESSAGE_PERSONAL_REACTION_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REACTION_MESSAGE_PERSONAL_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    listMessages.forEachIndexed { i, it ->
                        if (it.random_key.equals(messagesByGroup.random_key)) {
                            it.reactions = messagesByGroup.reactions
                            chatAdapter?.notifyItemChanged(i)
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataReaction(
                                            group._id,
                                            it.random_key,
                                            it.reactions,
                                            user.id
                                        )
                                }
                            if (it.reactions.last_reactions_id != user.id && it.reactions.last_reactions == TechResEnumChat.TYPE_REACTION_LOVE.toString()
                                    .toInt()
                            ) {
                                if (isCheckReaction) {
                                    binding.fragmentRootView.clearEmojis()
                                    binding.fragmentRootView.addEmoji(
                                        setReactionClick(
                                            messagesByGroup
                                        )
                                    )
                                    binding.fragmentRootView.startDropping()
                                    isCheckReaction = false
                                } else {
                                    isCheckReaction = false
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        isCheckReaction = true
                                    }, 4000)
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Pinned and Revoke Pinned
         * Response: data message and add to message list.
         * Note:
         *      - Pinned message on header chat.
         *      - Revoke Pinned to
         * */
        mSocket?.on(TechResEnumChat.RES_PINNED_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_PINNED_MESSAGE_PERSONAL_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setPinned(messagesByGroup)
                } catch (e: IOException) {
                }

            }
        }
        mSocket?.on(TechResEnumChat.RES_REVOKE_PINNED_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_REVOKE_PINNED_PERSONAL_ALO_LINE", it[0].toString())
                idPinnedMessage = ""
                toolsChat.binding.pinned.lnPinned.hide()
            }
        }
        mSocket?.on(TechResEnumChat.RES_REVOKE_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE", it[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(it[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Reply
         * */
        mSocket?.on(TechResEnumChat.RES_REVOKE_MESSAGE_REPLY_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_MESSAGE_REPLY_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    listMessages.forEachIndexed { index, messages ->
                        if (messages.message_reply?.random_key == messagesByGroup.random_key) {
                            messages.message_reply?.status = 0
                            messages.message_reply?.message_type =
                                TechresEnum.TYPE_MESSAGE_CHAT_REVOKE.toString()
                            chatAdapter?.notifyItemChanged(index)
                            TechResApplication.applicationContext().getAppDatabase()
                                ?.runInTransaction {
                                    TechResApplication.applicationContext().getMessageDao()
                                        ?.updateDataRevokeReply(
                                            group._id,
                                            messages.random_key,
                                            user.id,
                                            messages.message_reply
                                        )
                                }
                        }
                    }
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_REPLY_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_REPLY_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTypingTextReply(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Typing
         * */
        mSocket?.on(TechResEnumChat.RES_TYPING_ON_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_USER_IS_TYPING_PERSONAL_PARTY", it[0].toString())
                    val typingOnResponse = Gson().fromJson<TypingUser>(it[0].toString(), object :
                        TypeToken<TypingUser>() {}.type)
                    this.randomKeyTyping = typingOnResponse.random_key!!
                    if (typingOnResponse.member_id ?: 0 != user.id) {
                        val list = listTypingUser.stream()
                            .filter { x: TypingUser -> typingOnResponse.member_id == x.member_id }
                            .collect(Collectors.toList()) as ArrayList<TypingUser>
                        if (list.size == 0) {
                            val messagesByGroup = MessagesByGroup()
                            messagesByGroup.status = 1
                            messagesByGroup.message_type = TechResEnumChat.TYPE_TYPING.toString()
                            messagesByGroup.sender.full_name = typingOnResponse.full_name
                            messagesByGroup.sender.member_id = typingOnResponse.member_id
                            messagesByGroup.sender.avatar = typingOnResponse.avatar
                            messagesByGroup.random_key = typingOnResponse.random_key
                            listMessages.add(0, messagesByGroup)
                            listTypingUser.add(0, typingOnResponse)
                            chatAdapter?.notifyItemInserted(0)
                            if (firstPosition < 5) {
                                toolsChat.binding.rcvChat.scrollToPosition(0)
                            }
                        }
                    }
                } catch (e: IOException) {
                }
            }
        }

        mSocket?.on(TechResEnumChat.RES_TYPING_OFF_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_TYPING_OFF_PERSONAL_ALO_LINE", it[0].toString())
                    val typingOnResponse =
                        Gson().fromJson<TypingUser>(it[0].toString(), object :
                            TypeToken<TypingUser>() {}.type)
                    if (typingOnResponse.member_id != user.id) {
                        if (listTypingUser.any { it.random_key == typingOnResponse.random_key }) {
                            positionTyping =
                                listTypingUser.indexOfFirst { it.random_key == typingOnResponse.random_key }
                            listTypingUser.removeAt(positionTyping)
                            listMessages.removeAt(positionTyping)
                            chatAdapter?.notifyItemRemoved(positionTyping)
                        }
                    }
                } catch (e: IOException) {

                }
            }
        }
        /**
         * Update Background
         * */
        mSocket?.on(TechResEnumChat.RES_UPDATE_BACKGROUND_PERSONAL_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_UPDATE_BACKGROUND_PERSONAL_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                    Utils.getGlide(
                        toolsChat.binding.imgBackgroundChat,
                        messagesByGroup.background,
                        configNodeJs
                    )
                    group.background = messagesByGroup.background
                } catch (e: IOException) {
                }
            }
        }

        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.RES_CHAT_GIFT_PERSONAL_ALOLINE.toString(),
                user.id
            )
        ) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_GIFT", args[0].toString())
                    val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                        args[0].toString(),
                        object :
                            TypeToken<MessagesByGroup>() {}.type
                    )
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }

        //call
//        mSocket?.on(SocketCallOnDataEnum.RES_CALL_CONNECT()) {
//            activityChat?.runOnUiThread {
//                val intent = Intent(requireActivity(), AnswerCallChatActivity::class.java)
//                intent.putExtra("DATA_CALL", it[0].toString())
//                startActivity(intent)
//            }
//        }

        mSocket?.on(SocketCallOnDataEnum.RES_CHAT_VIDEO_CALL()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_VIDEO_CALL", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        mSocket?.on(SocketCallOnDataEnum.RES_CHAT_AUDIO_CALL()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_AUDIO_CALL", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }


        /**
         *
         * Share
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_SHARE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_SHARE_PARTY", args[0].toString())
                    val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                        args[0].toString(),
                        object :
                            TypeToken<MessagesByGroup>() {}.type
                    )
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_SHARE_NOTE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_NOTE_SHARE_PARTY", args[0].toString())
                    val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                        args[0].toString(),
                        object :
                            TypeToken<MessagesByGroup>() {}.type
                    )
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Business Card
         * */
        mSocket?.on(TechResEnumChat.RES_BUSINESS_CARD_PERSONAL_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_BUSINESS_CARD_PERSONAL_ALO_LINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_BIRTHDAY_ALOLINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_BIRTHDAY_ALOLINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        mSocket?.on(TechResEnumChat.RES_CHAT_INVITE_CARD_ALOLINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_INVITE_CARD_ALOLINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
    }

    /**
     * API
     * */
    private fun getDetailGroup(groupId: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format("%s%s%s", "api/groups/", groupId, "/get-detail")
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
//                            group = Group(
//                                data?.group_id ?: "",
//                                data?.group_name ?: "",
//                                data?.avatar ?: "",
//                                data?.background ?: ""
//                            )
                            group.name = data?.group_name
                            group.avatar = data?.avatar
                            group.background = data?.background
                            group.activities_status = data?.activities_status.toString()
                            data?.members?.forEachIndexed { index, members ->
                                if (user.id != members.member_id) {
                                    group.member = members
                                }
                            }
                            setData(group)
                        }
                    }
                }
            })

    }

    private fun getFile(
        name: String,
        type: Int,
        height: Int,
        width: Int,
        size: Int,
        time: Int = 0
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            String.format(
                "%s%s%s%s%s%s%s%s%s%s",
                "/api-upload/get-link-server-chat?group_id=",
                group._id,
                "&type=",
                TechResEnumChat.TYPE_PERSONAL_FILE.toString(),
                "&name_file=",
                name,
                "&width=",
                width,
                "&height=",
                height
            )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getImageChat(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FileNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FileNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        if (type == 1) {
                            if (name.contains("mp4")) {
                                val arrayList = ArrayList<FileNodeJs>()
                                response.data.height = height
                                response.data.width = width
                                arrayList.add(response.data)
                                chatVideo(arrayList)
                            } else if (name.contains("mp3")) {
                                val arrayList = ArrayList<FileNodeJs>()
                                response.data.time = time
                                arrayList.add(response.data)
                                chatAudio(arrayList)
                            } else {
                                response.data.height = height
                                response.data.width = width
                                imageList.add(response.data)
                                if (imageList.size == numberFile) {
                                    chatImage(imageList)
                                    numberFile = 0
                                    imageList.clear()
                                }
                            }
                        } else {
                            val arrayList = ArrayList<FileNodeJs>()
                            response.data.size = size
                            arrayList.add(response.data)
                            chatFile(arrayList)
                        }
                    }
                }
            })

    }

    private fun getPinned() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url =
            String.format("%s%s", "/api/pinned-messages/get-one?group_id=", group._id)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getOnePinned(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PinnedOneResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: PinnedOneResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val messagesByGroup = response.data
                        messagesByGroup?.let {
                            setPinned(it)
                        }
                    }
                }
            })
    }

    private fun uploadFile(
        file: File,
        name: String,
        type: Int,
        height: Int,
        width: Int,
        size: Int,
        time: Int = 0
    ) {
//        val nameFile = ChatUtils.getNameFileFormatTime(getNameFileToPath(name))
        val nameFile = getNameFileToPath(name)
        val nameMain = nameFile.replace(" ", "%20")
//        nameFile.forEachIndexed { index, c ->
//            if (ChatActivity.codeVn.contains(c.toString().lowercase())) {
//                nameMain += c
//            }
//        }
        val serverUrlString = String.format(
            "%s/api-upload/upload-file-server-chat/%s/%s/%s",
            configNodeJs.api_ads,
            TechResEnumChat.TYPE_PERSONAL_FILE.toString(),
            group._id,
            nameMain
        )
        WriteLog.d("Upload File : ", serverUrlString)
        val paramNameString = resources.getString(R.string.send_file)
        try {
            MultipartUploadRequest(
                requireActivity(),
                serverUrlString
            )
                .setMethod("POST")
                .addFileToUpload(file.path, paramNameString)
                .addHeader(getString(R.string.Authorization), user.nodeAccessToken)
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        context,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .subscribe(requireActivity(), requireActivity(), object : RequestObserverDelegate {
                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                        uploadInfo.files.forEach {
                            nameMain.let { it1 -> getFile(it1, type, height, width, size, time) }
                        }
                    }

                    override fun onCompletedWhileNotObserving() {

                    }

                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                        FancyToast.makeText(
                            requireActivity().baseContext,
                            requireActivity().resources.getString(R.string.sever_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }

                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
//                        if (uploadInfo.files[0].path.contains("mp4")) {
//                            val position =
//                                listMessages.indexOfFirst { it.key_message == randomKeyFile }
//                            listMessages[position].files[0].process = uploadInfo.progressPercent
//                            chatAdapter?.notifyItemChanged(position)
//                        }
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
            WriteLog.d("uploadFile", exc.message + "")
            Toast.makeText(requireActivity(), exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun makeFriend() {
        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = group.member.member_id
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
                        } else {
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
                        } else {
                        }
                    }
                })
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     * @deprecated use
     * {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}
     * with the appropriate {@link ActivityResultContract} and handling the result in the
     * {@link ActivityResultCallback#onActivityResult(Object) callback}.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null)
            return
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    selectList = ArrayList()
                    numberFile = 0
                    selectList = PictureSelector.obtainMultipleResult(data)
                    if (selectList.isNotEmpty()) {
                        randomKeyFile = getRandomString(12)
//                        if (selectList[0].mimeType == "video/mp4") {
//                            uploadVideoLocal(selectList[0], randomKeyFile)
//                        } else {
//                            uploadImageLocal(selectList, ArrayList(), randomKeyFile, 0)
//                        }
                        selectList.forEach {
                            val file = File(it.realPath)
                            listImageSuggestCache.add(it.realPath)
                            listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
                            cacheManager.put(
                                TechResEnumChat.LIST_IMAGE_SUGGET.name,
                                listStringImageSuggestCache
                            )
                            val size = (file.length() / 1024).toInt()
                            WriteLog.d("onActivityResult", it.duration.toString() + "")
                            if (it.mimeType == "video/mp4") {
                                uploadFile(
                                    file,
                                    file.name,
                                    1,
                                    Utils.getBitmapRotationVideo(file)?.height ?: 0,
                                    Utils.getBitmapRotationVideo(file)?.width ?: 0,
                                    it.duration.toInt()
                                )
                            } else {
                                uploadFile(
                                    file,
                                    file.name,
                                    1,
                                    Utils.getBitmapRotationImage(file)?.height ?: 0,
                                    Utils.getBitmapRotationImage(file)?.width ?: 0,
                                    it.duration.toInt()
                                )
                            }
                        }
                    }
                    isChooseImage = false
                    numberFile = selectList.size
                }
                FilePickerManager.REQUEST_CODE -> {
                    if (null != data.clipData) {
                        for (i in 0 until data.clipData!!.itemCount) {
                            val file = ChatUtils.fileFromContentUri(
                                context!!,
                                data.clipData!!.getItemAt(i).uri
                            )
                            val size = (file.length()).toInt()
                            uploadFile(
                                file,
                                ChatUtils.getFileNameFromUri(
                                    context!!,
                                    data.clipData!!.getItemAt(i).uri
                                ),
                                2,
                                0,
                                0,
                                size
                            )
                        }
                    } else {
                        val uri = data.data
                        val file = ChatUtils.fileFromContentUri(context!!, uri!!)
                        val size = (file.length()).toInt()
                        uploadFile(
                            file,
                            ChatUtils.getFileNameFromUri(context!!, uri),
                            2,
                            0,
                            0,
                            size
                        )
                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            PICK_FROM_GALLERY,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {

                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                }

                override fun onPermissionGranted() {
                    val animationMode = AnimationType.DEFAULT_ANIMATION
                    val language = 7
                    PictureSelector.create(requireActivity())
                        .openGallery(PictureMimeType.ofAll())
                        .theme(R.style.picture_WeChat_style)
                        .imageEngine(GlideEngine.createGlideEngine())
                        .isCamera(true)
                        .isWithVideoImage(false)
                        .maxSelectNum(30)
                        .minSelectNum(0)
                        .maxVideoSelectNum(1)
                        .selectionMode(PictureConfig.MULTIPLE)
                        .isSingleDirectReturn(false)
                        .isPreviewImage(true)
                        .isPreviewVideo(true)
                        .isOpenClickSound(true)
                        .selectionData(ArrayList())
                        .forResult(PictureConfig.CHOOSE_REQUEST)
                }

            }
        )
        handleOnRequestPermissionResult(
            requiredPermissionsFileCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
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
                        requireActivity().resources.getString(R.string.title_permission_media_step_two),
                        R.drawable.ic_gallery, "", 0, object : DialogPermissionHandler {
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
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name
                        ),
                        requireActivity().resources.getString(R.string.title_permission_media_step_two),
                        R.drawable.ic_gallery, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
                    getFileSuggest()
                }

            }
        )

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
                    handler.postDelayed(runnable, 100)
                    toolsChat.binding.imgSent.show()
                    toolsChat.binding.lnAudio.show()
                    toolsChat.binding.lnComment.hide()
                    toolsChat.binding.cvLinkSuggest.hide()
                    toolsChat.binding.rlListScreenShot.hide()
                    toolsChat.binding.rlScreenShot.hide()
                    toolsChat.binding.layoutKeyboard.onBackPressed()
                    closeKeyboard(toolsChat.binding.edtMessageChat)
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
//                    val roomId = getRandomString(20)
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
//                        roomId,
//                        TechResEnumVideoCall.TYPE_CALL_VIDEO.toString()
//                    )
                    ChatUtils.connectToRoom(
                        requireActivity(),
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

    override fun onRevoke(messagesByGroup: MessagesByGroup, view: View) {
        bottomSheetReaction(messagesByGroup)
    }

    override fun onClickViewReaction(
        messagesByGroup: MessagesByGroup,
        reactionItems: ArrayList<ReactionItem>
    ) {

    }

    override fun onPressReaction(messagesByGroup: MessagesByGroup, view: View) {
        val originalPos = IntArray(2)
        view.getLocationInWindow(originalPos)
        val x = originalPos[0]
        val y = originalPos[1]
//        ChatUtils.flyEmoji(
//            setReactionClick(messagesByGroup),
//            activityChat,
//            toolsChat.binding.lnContainerChat,
//            x.toFloat(),
//            (y - dpToPx(60)).toFloat()
//        )
        reactionMessage(messagesByGroup, group, messagesByGroup.reactions.last_reactions ?: 0)
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

    override fun onAddMember() {
    }

    override fun onChooseAvatarBusinessCard(userId: Int) {
        activityChat?.toProfile(userId)
    }

    override fun onChatBusinessCard(phoneMessage: PhoneMessage) {
        if (phoneMessage.member_id == group.member.member_id) {
            Toast.makeText(
                requireActivity(),
                "Bạn đang nhắn tin với ${group.member.full_name}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val memberList = ArrayList<Int>()
            memberList.add(user.id)
            memberList.add(phoneMessage.member_id ?: 0)
            createGroupPersonal(memberList, phoneMessage.member_id ?: 0, 0)
        }
    }

    override fun onCallBusinessCard(phoneMessage: PhoneMessage) {
        if (phoneMessage.member_id == group.member.member_id) {
            ChatUtils.connectToRoom(
                requireActivity(),
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
        } else {
            val memberList = ArrayList<Int>()
            memberList.add(user.id)
            memberList.add(phoneMessage.member_id ?: 0)
            createGroupPersonal(memberList, phoneMessage.member_id ?: 0, 1)
        }
    }

    override fun onChooseBusinessCard(phoneMessage: PhoneMessage) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + phoneMessage.phone)
        startActivity(dialIntent)
    }

    override fun onClickTextPhone(phoneMessage: MessagesByGroup) {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:" + phoneMessage.message)
        startActivity(dialIntent)
    }

    override fun onClickHideKeyboard() {
        hideKeyboard(binding.layoutMainPersonal)
        toolsChat.binding.layoutKeyboard.onBackPressed()
    }

    override fun onChooseCallVideo(messagesByGroup: MessagesByGroup) {
        if (messagesByGroup.message_type == TechResEnumVideoCall.TYPE_CALL_PHONE.toString()) {
            requestMultiplePermissionAudio(true)
        } else {
            requestMultiplePermissionVideoCall()
        }
    }

    override fun onScrollMessage(messagesByGroup: MessagesByGroup) {
        var position = -1
        group._id?.let { it1 ->
            messagesByGroup.message_reply?.random_key?.let { it2 ->
                listMessages.forEachIndexed { index, messagesByGroup ->
                    if (messagesByGroup.random_key?.contains(it2) == true) {
                        position = index
                        return@forEachIndexed
                    }
                }
                if (position != -1 && position < ChatActivity.LIMIT_MESSAGE) {
                    toolsChat.binding.rcvChat.smoothScrollToPosition(position)
                    Handler(Looper.getMainLooper()).postDelayed({
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (messagesByGroup.random_key?.contains(it2) == true) {
                                listMessages[index].is_stroke = 1
                                chatAdapter?.notifyItemChanged(index)
                                stroke = true
                                return@forEachIndexed
                            }
                        }
                    }, 500)
                    return
                }
                if (!isLoading) {
                    getMessageScrollDB(it1, java.lang.Long.parseLong(it2))
//                    getMessageScroll(it1, "-1", "-1", it2)
                }
            }
        }
    }

    override fun onShareMessage(messagesByGroup: MessagesByGroup) {

    }

    override fun onIntentSendBirthDayCard(messagesByGroup: MessagesByGroup) {
        val intent = Intent(requireActivity(), SendBirthDayCardActivity::class.java)
        intent.putExtra(TechResEnumChat.GROUP_ID.toString(), group._id)
        intent.putExtra(TechResEnumChat.MEMBER_ID.toString(), user.id)
        intent.putExtra(TechResEnumChat.USER_INVITE_ID.toString(), messagesByGroup.sender.member_id)
        startActivity(intent)
        requireActivity()?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onReviewInviteCard(messagesByGroup: MessagesByGroup) {
        val dialog = Dialog(requireActivity(), R.style.DialogCustomTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_review_invite_card)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val imageView = dialog.findViewById<ImageView>(R.id.background)
        Utils.getGlide(imageView, messagesByGroup.message_event.background, configNodeJs)
        val cardView = dialog.findViewById<CardView>(R.id.cardView)
        val message = dialog.findViewById<TextView>(R.id.message)
        val close = dialog.findViewById<ImageView>(R.id.close)
        val rltImage = dialog.findViewById<RelativeLayout>(R.id.rltImage)
        cardView.layoutParams.width = TechResApplication.widthView
        val ratio = resources.displayMetrics.densityDpi
        message?.layoutParams?.height =
            messagesByGroup.message_event.position.height?.div(ratio)
        message?.layoutParams?.width =
            messagesByGroup.message_event.position.width?.div(ratio)

        message?.x =
            (messagesByGroup.message_event.position.margin_left!! / ratio).toFloat() + (rltImage.width - imageView.width) / 2
        message?.y =
            (messagesByGroup.message_event.position.margin_top!! / ratio).toFloat() + (rltImage.height - imageView.height) / 2
        message.text = messagesByGroup.message_event.message
        close.setOnClickListener { dialog.dismiss() }
        dialog.show()

        konfetti = dialog.findViewById<KonfettiView>(R.id.viewKonfetti)

        konfetti.start(Presets.explode())

    }

    override fun onCLickImageMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        val url = ArrayList<String>()
        closeKeyboard(toolsChat.binding.inputSearch)
        imgList.forEach {
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""

//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
            if (File(linkOriginal).exists())
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
//        imgList.forEach {
//            url.add(String.format("%s%s", configNodeJs.api_ads, it.link_original))
//        }
        closeKeyboard(toolsChat.binding.inputSearch)

        imgList.forEach {
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""
            if (File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        var intent = Intent(activityChat, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onRevokeImageMore(messagesByGroup: MessagesByGroup, view: View) {
        bottomSheetReaction(messagesByGroup)
    }

    override fun onCallBack(bundle: Bundle) {
        val isCheckBackground = bundle.getString(TechresEnum.LINK_BACKGROUND.toString())
        val contactDevice = bundle.getString(TechResEnumChat.CONTACT_DEVICE_CHAT.toString())
        if (isCheckBackground != null) {
            toolsChat.bottomSheet.state = TopSheetBehavior.STATE_EXPANDED
            toolsChat.binding.rlHeaderChat.hide()
            toolsChat.binding.rlHeaderBackground.show()
            toolsChat.binding.cdlListMessage.show()
            toolsChat.binding.bottomSheet.rcBackground.scrollToPosition(0)
        } else {
            toolsChat.binding.cdlListMessage.hide()
            toolsChat.binding.rlHeaderBackground.hide()
            toolsChat.binding.rlHeaderChat.show()
        }

        if (contactDevice != null) {

            val contactList = Gson().fromJson<ArrayList<ContactDevice>>(
                contactDevice, object :
                    TypeToken<ArrayList<ContactDevice>>() {}.type
            )

            contactList.forEach {
                sendBusinessCard(it.phone ?: "")
            }
        }
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
    }

    override fun getDataEmoji(code: String) {
        toolsChat.binding.edtMessageChat.append(code)
    }

    override fun onChooseTagName(tagName: TagName) {

    }

    override fun onChooseNameUser(sender: Sender) {

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onScreenCaptured(path: String?) {
        if (!isChooseImage) {
            var status = 0
            if (listScreenShot?.size ?: 0 > 0) {
                listScreenShot?.forEach {
                    if (it.contains(path ?: "")) {
                        status = 1
                        return@forEach
                    }
                }
                if (status == 0) {
                    listScreenShot?.add(path ?: "")
                }
            } else {
                listScreenShot?.add(path ?: "")
            }
            WriteLog.d("List String Image Sugget : ", listStringImageSuggestCache)
            if (listScreenShot != null) {
                listScreenShot = listScreenShot?.stream()
                    ?.filter { x: String -> !listStringImageSuggestCache.contains(x) }
                    ?.collect(Collectors.toList()) as ArrayList<String>
                if (listScreenShot?.size!! > 0)
                    setListSuggestFile(listScreenShot)
            }

        }
    }

    override fun onScreenCapturedWithDeniedPermission() {
    }

    override fun onClose(string: String) {
        val position = listScreenShot?.indexOfFirst { it == string }
        position?.let {
            listScreenShot?.removeAt(it)
            adapterSuggestFile?.notifyItemRemoved(it)
        }
        toolsChat.binding.btnSendScreenShot.text = String.format(
            "Gửi %s ảnh",
            listScreenShot?.size ?: 0
        )
        toolsChat.binding.btnSendScreenShotMore.text = String.format(
            "Gửi %s ảnh",
            listScreenShot?.size ?: 0
        )
    }

    override fun onVote() {
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
        val bundle = Bundle()
        bundle.putString(
            TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
        )
        val createVoteMessageFragment = CreateVoteMessageFragment()
        createVoteMessageFragment.arguments = bundle
        activityChat?.addOnceFragment(this, createVoteMessageFragment)
    }

    @SuppressLint("ResourceType")
    override fun onChooseFile() {
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        try {
//
//            startActivityForResult(
//                Intent.createChooser(intent, "Select a File to Upload"),
//                fileSelectCode
//            )
//        } catch (ex: java.lang.Exception) {
//            println("browseClick :$ex")
//        }


//        FilePickerManager
//            .from(this)
//            .setText(
//                selectAllString = requireActivity().resources.getString(R.string.select_all),
//                unSelectAllString = requireActivity().resources.getString(R.string.cancel),
//                hadSelectedStrRes = R.string.selected_file_picker,
//                confirmText = requireActivity().resources.getString(R.string.done_),
//                maxSelectCountTipsStrRes = 10,
//                emptyListTips = requireActivity().resources.getString(R.string.empty_list_file_picker)
//            )
//            .setTheme(R.style.FilePickerThemeReply)
//            .maxSelectable(10)
//            .forResult(FilePickerManager.REQUEST_CODE)


        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "*/*"
        startActivityForResult(intent, FilePickerManager.REQUEST_CODE)
    }

    override fun onChooseBusinessCard() {
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
        val intent = Intent(activityChat, ContactDeviceActivity::class.java)
        activityChat?.startActivity(intent)
        activityChat?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
//        val bottomSheetDialog = ContactDeviceBottomSheetFragment()
//        bottomSheetDialog.show(childFragmentManager, bottomSheetDialog.tag)
    }

    override fun onChooseSticker(sticker: Sticker) {
        chatSticker(sticker)
    }

    override fun onImportantMessage() {
        val bundle = Bundle()
        try {
            bundle.putString(
                TechresEnum.GROUP_CHAT.toString(),
                Gson().toJson(group)
            )
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        val messageImportantFragment = MessageImportantFragment()
        messageImportantFragment.arguments = bundle
        activityChat?.addOnceFragment(ChatPersonalFragment(), messageImportantFragment)
    }

    override fun onDismiss() {
        updateButton(false)
    }

    override fun onShow() {
        updateButton(true)
    }

    override fun onKeyboardOpened(height: Int) {
        isKeyBoard = true
        updateButton(false)
    }

    override fun onKeyboardClosed() {
        isKeyBoard = false
        updateButton(toolsChat.binding.layoutKeyboard.isShowing)
    }

    override fun onViewHeightChanged(height: Int) {
        onViewHeightChanged = height
    }

    override fun onSelectBackground(background: Background) {
        urlBackground = background.link_original ?: ""
        Utils.getGlide(
            toolsChat.binding.imgBackgroundChat,
            urlBackground,
            configNodeJs
        )
    }

    override fun onBackPress(): Boolean {
        if (toolsChat.binding.lnAudio.visibility == View.VISIBLE) {
            stopDrawing()
        }
        if (listScreenShot != null) {
            if (listScreenShot?.size!! > 0) {
                listScreenShot?.let { it1 -> listImageSuggestCache.addAll(it1) }
                listStringImageSuggestCache = Gson().toJson(listImageSuggestCache)
                cacheManager.put(
                    TechResEnumChat.LIST_IMAGE_SUGGET.name,
                    listStringImageSuggestCache
                )
            }
        }
        return if (onViewHeightChanged == 0 && !isKeyBoard) {
            leavePersonal()
            true
        } else {
            toolsChat.binding.layoutKeyboard.onBackPressed()
            false
        }
    }

    private fun createGroupPersonal(
        memberList: ArrayList<Int>,
        memberId: Int,
        typeBusinessCard: Int
    ) {
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
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
                            createChatPersonal(memberId)
                            if (typeBusinessCard == 0) {
                                activityChat?.finish()
                                EventBus.getDefault().post(EventBusGroup(group))
                            } else {
                                requestMultiplePermissionAudio(true, group)
                            }
                        } else {
                            Toast.makeText(
                                activityChat,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                })
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

    private fun getMessage(
        group_id: String,
        next_cursors: String,
        pre_curcors: String,
        random_key: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages-personal/pagination?group_id=",
            group_id,
            "&limit=",
            ChatActivity.LIMIT_MESSAGE,
            "&random_key=",
            random_key,
            "&pre_cursor=",
            pre_curcors,
            "&next_cursor=",
            next_cursors
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesPersonal(baseRequest)
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
                        is_latest_message = response.data.is_latest_message
                        is_oldest_message = response.data.is_oldest_message
                        pre_cursor = response.data.pagination.pre_cursor
                        next_cursor = response.data.pagination.next_cursor
                        listMessages.clear()
                        listMessages.addAll(data)
                        chatAdapter?.setDataSource(listMessages)
                        if (listMessages.size > 0) {
                            toolsChat.binding.lnNoMessage.hide()
                        } else {
                            toolsChat.binding.lnNoMessage.show()
                        }
                        toolsChat.binding.tvLoadingMessage.hide()
                        toolsChat.binding.tvMoreMessage.hide()
                        toolsChat.binding.tvNewMessage.hide()
                        isLoading = false
                        if (listMessages.size > 0) {
                            listMessages.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun getMessagePre(
        group_id: String,
        pre_curcors: String,
        random_key: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages-personal/pagination?group_id=",
            group_id,
            "&limit=",
            ChatActivity.LIMIT_MESSAGE,
            "&random_key=",
            random_key,
            "&pre_cursor=",
            pre_curcors,
            "&next_cursor=",
            -1
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesPersonal(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    isLoading = false
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        if (!is_oldest_message)
                            is_oldest_message = response.data.is_oldest_message

                        pre_cursor = response.data.pagination.pre_cursor
                        listMessages.addAll(data)
                        chatAdapter?.setDataSource(listMessages)
                        isLoading = false
                    }
                }
            })
    }

    private fun getMessageNext(
        group_id: String,
        next_cursors: String,
        random_key: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages-personal/pagination?group_id=",
            group_id,
            "&limit=",
            ChatActivity.LIMIT_MESSAGE,
            "&random_key=",
            random_key,
            "&pre_cursor=",
            -1,
            "&next_cursor=",
            next_cursors
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesPersonal(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    isLoading = false
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        if (!is_latest_message)
                            is_latest_message = response.data.is_latest_message
                        next_cursor = response.data.pagination.next_cursor
                        listMessages.addAll(0, data)
                        chatAdapter?.setDataSource(listMessages)
                        isLoading = false
                    }
                }
            })
    }

    override fun chooseOrder(position: Int) {
        var intent = Intent(activityChat, BillActivity::class.java)
        intent.putExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), position)
        startActivity(intent)
    }

    override fun onGetGift(messageGiftNotification: MessageGiftNotification) {
        val intent = Intent(activityChat, GiftDetailActivity::class.java)
        intent.putExtra(
            TechresEnum.DATA_GIFT_DETAIL.toString(),
            Gson().toJson(messageGiftNotification)
        )
        startActivity(intent)
    }

    private fun getMessageScroll(
        group_id: String,
        next_cursors: String,
        pre_curcors: String,
        random_key: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages-personal/pagination?group_id=",
            group_id,
            "&limit=",
            ChatActivity.LIMIT_MESSAGE,
            "&random_key=",
            random_key,
            "&pre_cursor=",
            pre_curcors,
            "&next_cursor=",
            next_cursors
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesPersonal(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    isLoading = false
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        is_latest_message = response.data.is_latest_message
                        is_oldest_message = response.data.is_oldest_message
                        pre_cursor = response.data.pagination.pre_cursor
                        next_cursor = response.data.pagination.next_cursor
                        listMessages.clear()
                        listMessages.addAll(data)
                        chatAdapter?.notifyDataSetChanged()
                        isLoading = false
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (messagesByGroup.random_key?.contains(random_key) == true) {
                                toolsChat.binding.rcvChat.scrollToPosition(index)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    listMessages[index].is_stroke = 1
                                    chatAdapter?.notifyItemChanged(index)
                                    stroke = true
                                }, 500)
                                return@forEachIndexed
                            }
                        }

                        if (listMessages.size > 0) {
                            listMessages.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }
                    }
                }
            })
    }

    @Subscribe
    fun onSrollMessagePin(event: EventBusScrollMessPin) {
        val random_key = event.random_key
        var position = -1
        group._id?.let { it1 ->
            random_key.let { it2 ->
                listMessages.forEachIndexed { index, messagesByGroup ->
                    if (messagesByGroup.random_key?.contains(it2) == true) {
                        position = index
                        return@forEachIndexed
                    }
                }
                if (position != -1 && position < ChatActivity.LIMIT_MESSAGE) {
                    toolsChat.binding.rcvChat.smoothScrollToPosition(position)
                    Handler(Looper.getMainLooper()).postDelayed({
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (messagesByGroup.random_key?.contains(it2) == true) {
                                listMessages[index].is_stroke = 1
                                chatAdapter?.notifyItemChanged(index)
                                stroke = true
                                return@forEachIndexed
                            }
                        }
                    }, 500)
                    return@let
                }
                if (!isLoading) {
                    getMessageScrollDB(it1, java.lang.Long.parseLong(it2))
//                    getMessageScroll(it1, "-1", "-1", it2)
                }

            }
        }
    }

    @Subscribe
    fun onDownloadFileComplete(event: EventBusDownloadFileComplete) {
        val random_key = event.random_Key
        group._id?.let { it1 ->
            random_key.let { it2 ->
                listMessages.forEachIndexed { index, messagesByGroup ->
                    if (messagesByGroup.random_key?.contains(it2) == true) {
                        chatAdapter?.notifyItemChanged(index)
                        return
                    }
                }
            }
        }
    }

    @Subscribe
    fun onFinishSyncMessage(event: EventBusFinishSyncMessage) {
        if (event.finish) {
            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                val data = group._id?.let {
                    TechResApplication.applicationContext().getMessageDao()
                        ?.getMessage(it, user.id)
                }
                if (data != null) {
                    listMessages.clear()
                    listMessages.addAll(data)
                }
                if (listMessages.isEmpty()) {
                    pre_cursor = ""
                    next_cursor = ""
                } else {
                    pre_cursor = listMessages.last().random_key.toString()
                    next_cursor = listMessages.first().random_key.toString()
                }
                var firstMessage = group._id?.let {
                    TechResApplication.applicationContext().getMessageDao()
                        ?.getFirtMessage(it, user.id)
                }
                var lastMessage = group._id?.let {
                    TechResApplication.applicationContext().getMessageDao()
                        ?.getLastMessage(it, user.id)
                }
                if (firstMessage == null) firstMessage = MessagesByGroup()
                if (lastMessage == null) lastMessage = MessagesByGroup()
                if (pre_cursor.equals(firstMessage.random_key)) {
                    is_oldest_message = true
                } else {
                    is_oldest_message = false
                }
                if (next_cursor.equals(lastMessage.random_key)) {
                    is_latest_message = true
                } else {
                    is_latest_message = false
                }
                WriteLog.e("is_latest_message ", is_latest_message.toString())
                WriteLog.e("is_oldest_message ", is_oldest_message.toString())
            }
            chatAdapter?.setDataSource(listMessages)
            if (listMessages.size > 0) {
                toolsChat.binding.lnNoMessage.hide()
            } else {
                toolsChat.binding.lnNoMessage.show()
            }
            toolsChat.binding.tvLoadingMessage.hide()
            toolsChat.binding.tvMoreMessage.hide()
            toolsChat.binding.tvNewMessage.hide()
            isLoading = false
            if (listMessages.size > 0) {
                listMessages.forEach {
                    if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                        TasksManager().getImpl().addMessage(it)
                    }
                }
            }
        }
    }

    private fun setupDataSyncLocal(groupId: String, conversationType: String) {
        var lastMessage: MessagesByGroup
        lastMessage =
            messageDao?.getLastMessage(groupId, user.id) ?: MessagesByGroup()
        if (lastMessage.random_key.equals(""))
            lastMessage.random_key = "-1"
        lastMessage.random_key?.let { WriteLog.e("lastMessage ", it) }
        val builder = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
        val dataWorker = Data.Builder()
            .putString(
                TechResEnumChat.CONVERSATION_TYPE.toString(),
                conversationType
            ).putString(
                TechResEnumChat.GROUP_ID.toString(),
                groupId
            ).putString(
                TechResEnumChat.LAST_MESSAGE_GROUP.toString(),
                lastMessage.random_key
            )
            .build()
        val syncDataUpdateMessage: WorkRequest =
            OneTimeWorkRequestBuilder<SyncDataUpdateMessage>()
                .addTag("SyncDataUpdateMessage")
                .setConstraints(builder.build())
                .setInputData(dataWorker)
                .build()
        WorkManager
            .getInstance(TechResApplication.applicationContext())
            .enqueue(syncDataUpdateMessage)
    }

    fun getMessageDB(groupId: String) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val data = groupId.let {
                TechResApplication.applicationContext().getMessageDao()
                    ?.getMessage(it, user.id)
            }
            getPinned()
            listMessages.clear()
            if (data != null) {
                listMessages.addAll(data)
            }
            if (listMessages.isEmpty()) {
                pre_cursor = ""
                next_cursor = ""
            } else {
                pre_cursor = listMessages.last().random_key.toString()
                next_cursor = listMessages.first().random_key.toString()
            }
            var firstMessage = groupId.let {
                TechResApplication.applicationContext().getMessageDao()?.getFirtMessage(it, user.id)
            }
            if (firstMessage == null) firstMessage = MessagesByGroup()
            if (pre_cursor.equals(firstMessage.random_key)) {
                is_oldest_message = true
            } else {
                is_oldest_message = false
            }

            var lastMessage = groupId.let {
                TechResApplication.applicationContext().getMessageDao()?.getLastMessage(it, user.id)
            }
            if (lastMessage == null) lastMessage = MessagesByGroup()
            if (next_cursor.equals(lastMessage.random_key)) {
                is_latest_message = true
            } else {
                is_latest_message = false
            }
            WriteLog.e("is_latest_message ", is_latest_message.toString())
            WriteLog.e("is_oldest_message ", is_oldest_message.toString())
        }
        chatAdapter?.setDataSource(listMessages)
        if (listMessages.size > 0) {
            toolsChat.binding.lnNoMessage.hide()
        } else {
            toolsChat.binding.lnNoMessage.show()
        }
        toolsChat.binding.tvLoadingMessage.hide()
        toolsChat.binding.tvMoreMessage.hide()
        toolsChat.binding.tvNewMessage.hide()
        isLoading = false
        if (listMessages.size > 0) {
//
            listMessages.forEach {
                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                    TasksManager().getImpl().addMessage(it)
                }
            }
        }
    }

    fun getMessagePreDB(groupId: String, randomKey: Long) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val data = groupId.let {
                TechResApplication.applicationContext().getMessageDao()
                    ?.getMessagePre(it, randomKey, ChatActivity.LIMIT_MESSAGE, user.id)
            }
            if (data != null) {
                listMessages.addAll(data)
                chatAdapter?.notifyItemRangeInserted(listMessages.size - data.size, data.size)
            }
            if (listMessages.isEmpty()) {
                pre_cursor = ""
            } else {
                pre_cursor = listMessages.last().random_key.toString()
            }
            if (!is_oldest_message) {
                var firstMessage = groupId.let {
                    TechResApplication.applicationContext().getMessageDao()
                        ?.getFirtMessage(it, user.id)
                }
                if (firstMessage == null) firstMessage = MessagesByGroup()
                if (pre_cursor.equals(firstMessage.random_key)) {
                    is_oldest_message = true
                } else {
                    is_oldest_message = false
                }
            }
            isLoading = false
            if (listMessages.size > 0) {
                listMessages.forEach {
                    if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                        TasksManager().getImpl().addMessage(it)
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getMessageNextDB(groupId: String, randomKey: Long) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val data = groupId.let {
                TechResApplication.applicationContext().getMessageDao()
                    ?.getMessageNext(it, randomKey, ChatActivity.LIMIT_MESSAGE, user.id)
            }
            if (data != null) {
                listMessages.addAll(0, data)
                chatAdapter?.notifyItemRangeInserted(0, data.size)
//                chatAdapter?.notifyDataSetChanged()
            }
            if (listMessages.isEmpty()) {
                next_cursor = ""
            } else {
                next_cursor = listMessages.first().random_key.toString()
            }
            if (!is_latest_message) {
                var lastMessage = groupId.let {
                    TechResApplication.applicationContext().getMessageDao()
                        ?.getLastMessage(it, user.id)
                }
                if (lastMessage == null) lastMessage = MessagesByGroup()
                if (next_cursor.equals(lastMessage.random_key)) {
                    is_latest_message = true
                } else {
                    is_latest_message = false
                }
            }
            if (listMessages.size > 0) {
                listMessages.forEach {
                    if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                        TasksManager().getImpl().addMessage(it)
                    }
                }
            }
            isLoading = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getMessageScrollDB(groupId: String, randomKey: Long) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            val dataScrollPre = groupId.let {
                TechResApplication.applicationContext().getMessageDao()
                    ?.getMessageScrollPre(
                        it,
                        user.id,
                        randomKey,
                        (ChatActivity.LIMIT_MESSAGE / 2) + 1
                    )
            }
            var limitNext = ChatActivity.LIMIT_MESSAGE / 2
            if (dataScrollPre != null) {
                limitNext += ChatActivity.LIMIT_MESSAGE / 2 + 1 - dataScrollPre.size
            }
            var dataScrollNext = groupId.let {
                TechResApplication.applicationContext().getMessageDao()
                    ?.getMessageScrollNext(it, user.id, randomKey, limitNext)
            }
            listMessages.clear()
            if (dataScrollPre != null) {
                listMessages.addAll(dataScrollPre)
            }
            if (dataScrollNext != null) {
                listMessages.addAll(0, dataScrollNext.reversed())
            }
            if (listMessages.isEmpty()) {
                pre_cursor = ""
                next_cursor = ""
            } else {
                pre_cursor = listMessages.last().random_key.toString()
                next_cursor = listMessages.first().random_key.toString()
            }
            var firstMessage = groupId.let {
                TechResApplication.applicationContext().getMessageDao()?.getFirtMessage(it, user.id)
            }
            if (firstMessage == null) firstMessage = MessagesByGroup()
            if (pre_cursor.equals(firstMessage.random_key)) {
                is_oldest_message = true
            } else {
                is_oldest_message = false
            }
            var lastMessage = groupId.let {
                TechResApplication.applicationContext().getMessageDao()?.getLastMessage(it, user.id)
            }
            if (lastMessage == null) lastMessage = MessagesByGroup()
            if (next_cursor.equals(lastMessage.random_key)) {
                is_latest_message = true
            } else {
                is_latest_message = false
            }
        }
        chatAdapter?.notifyDataSetChanged()
        if (listMessages.size > 0) {
            toolsChat.binding.lnNoMessage.hide()
        } else {
            toolsChat.binding.lnNoMessage.show()
        }
        toolsChat.binding.tvLoadingMessage.hide()
        toolsChat.binding.tvMoreMessage.hide()
        toolsChat.binding.tvNewMessage.hide()


        isLoading = false
        listMessages.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key?.contains(randomKey.toString()) == true) {
                toolsChat.binding.rcvChat.smoothScrollToPosition(index)
                Handler(Looper.getMainLooper()).postDelayed({
                    listMessages.forEachIndexed { index, messagesByGroup ->
                        if (messagesByGroup.random_key?.contains(randomKey.toString()) == true) {
                            listMessages[index].is_stroke = 1
                            chatAdapter?.notifyItemChanged(index)
                            stroke = true
                            isClick = false
                            return@forEachIndexed
                        }
                    }
                }, 500)
                return@forEachIndexed
            }
        }

        if (listMessages.size > 0) {

            listMessages.forEach {
                if (it.message_type == TechResEnumChat.TYPE_FILE.toString() || it.message_type == TechResEnumChat.TYPE_IMAGE.toString() || it.message_type == TechResEnumChat.TYPE_VIDEO.toString() || it.message_type == TechResEnumChat.TYPE_AUDIO.toString()) {
                    TasksManager().getImpl().addMessage(it)
                }
            }
        }
    }

    @Subscribe
    fun onDownloadFileSuccess(event: EventBusDownloadSuccess) {
        listMessages.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key == event.randomKey) {
                if (chatAdapter != null)
                    chatAdapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }

    fun ShowSearch() {
        if (!idPinnedMessage.equals("")) {
            toolsChat.binding.pinned.lnPinned.hide()
        }
        messageSearch = ArrayList<MessagesByGroup>()
        toolsChat.binding.countMessSearch.show()
        toolsChat.binding.rlHeader.hide()
        toolsChat.binding.headerMessSearch.show()
        toolsChat.binding.rlContainerComment.hide()
        toolsChat.binding.inputSearch.setIconifiedByDefault(true)
        toolsChat.binding.inputSearch.isFocusable = true
        toolsChat.binding.inputSearch.isIconified = false
        toolsChat.binding.inputSearch.requestFocusFromTouch()
    }

    fun HideSearch() {
        toolsChat.binding.inputSearch.setQuery("", false)
        messageSearch = ArrayList<MessagesByGroup>()
        toolsChat.binding.rlHeader.show()
        toolsChat.binding.rlContainerComment.show()
        toolsChat.binding.headerMessSearch.visibility = View.GONE
        toolsChat.binding.countMessSearch.visibility = View.GONE
        toolsChat.binding.messCountSearch.text = ""
        currentSearch = 0
        toolsChat.binding.countUp.isEnabled = false
        toolsChat.binding.countDown.isEnabled = false
        isStrokeSearch = 0
        setStrokeFalse()
        if (idPinnedMessage != "") {
            toolsChat.binding.pinned.lnPinned.hide()
        }
        closeKeyboard(toolsChat.binding.inputSearch)
    }

    fun setStrokeFalse() {
        listMessages.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.is_stroke == 1) {
                messagesByGroup.is_stroke = 0
                chatAdapter?.notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }
}

