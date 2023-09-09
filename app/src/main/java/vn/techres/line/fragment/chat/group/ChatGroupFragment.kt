package vn.techres.line.fragment.chat.group

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.*
import com.aghajari.emojiview.listener.PopupListener
import com.alexbbb.uploadservice.BuildConfig
import com.fasterxml.jackson.core.JsonProcessingException
import com.giphy.sdk.core.models.Media
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.adapter.chat.MessageChatAdapter
import vn.techres.line.adapter.chat.TagNameUserAdapter
import vn.techres.line.adapter.chat.UserReactionAdapter
import vn.techres.line.adapter.librarydevice.SuggestFileAdapter
import vn.techres.line.base.BaseBindingBubbleChatFragment
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.request.*
import vn.techres.line.data.model.chat.request.group.*
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.*
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.eventbus.EventBusGroup
import vn.techres.line.data.model.eventbus.EventBusLeaveGroup
import vn.techres.line.data.model.eventbus.EventBusLeaveRoom
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.params.UpdateGroupParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.*
import vn.techres.line.databinding.ChatRevokeMessageBottomBinding
import vn.techres.line.databinding.FragmentChatGroupBinding
import vn.techres.line.fragment.chat.MemberGroupFragment
import vn.techres.line.fragment.chat.MessageImportantFragment
import vn.techres.line.fragment.chat.ShareMessageFragment
import vn.techres.line.fragment.chat.vote.CreateVoteMessageFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.PrefUtils.savePreferences
import vn.techres.line.helper.Utils.downLoadFile
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getBitmapRotationImage
import vn.techres.line.helper.Utils.getBitmapRotationVideo
import vn.techres.line.helper.Utils.getMediaGlide
import vn.techres.line.helper.Utils.getNameFileToPath
import vn.techres.line.helper.Utils.getRandomString
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.ifShow
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.Utils.showImage
import vn.techres.line.helper.Utils.stopMedia
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.screenshot.ScreenshotDetectionDelegate
import vn.techres.line.helper.screenshot.ScreenshotDetectionDelegate.getFilePathClipFromContentResolver
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.topbottombehavior.TopSheetBehavior
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.flyEmoji
import vn.techres.line.helper.utils.ChatUtils.getAvatarGroup
import vn.techres.line.helper.utils.ChatUtils.setReactionClick
import vn.techres.line.helper.utils.ChatUtils.setTagName
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.utils.FileUtils.getMimeType
import vn.techres.line.helper.utils.FileUtils.getStorageDir
import vn.techres.line.helper.utils.FileUtils.openDocument
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.GiPhyHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.SelectBackgroundHandler
import vn.techres.line.interfaces.SwipeItemHandler
import vn.techres.line.interfaces.chat.*
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.interfaces.util.SuggestFileHandler
import vn.techres.line.roomdatabase.sync.SyncDataUpdateMessage
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.widget.NumberComparator
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors


@SuppressLint("UseRequireInsteadOfGet")
open class ChatGroupFragment : BaseBindingBubbleChatFragment<FragmentChatGroupBinding>(
    FragmentChatGroupBinding::inflate
),
    RevokeMessageHandler, OnRefreshFragment,
    ChatGroupHandler, ImageMoreChatHandler, GetDataEmojiChildToParent, TagNameUserHandler,
    ChooseNameUserHandler, GiPhyHandler,
    ScreenshotDetectionDelegate.ScreenshotDetectionListener, SuggestFileHandler,
    UtilitiesChatHandler, PopupListener, SelectBackgroundHandler, ChatActivityHandler {
    private var useragent = ""
    private val limit = 50
    private var totalMessage = 0f
    private var totalPage = 0
    private var seenMessage = 0
    private var isTying = 0
    private var page = 1
    private var positionTyping = 0
    private var numberFile = 0
    private var randomKeyFile = ""
    private var idPinnedMessage = ""
    private var randomKeyTyping = ""
    private var isCheckReaction = true
    private var scrollPosition = true
    private var isReplyMessage = false
    private var isCheckPage = true
    private var syncData = false
    private lateinit var layoutManager: LinearLayoutManager

    private var handler = Handler(Looper.myLooper()!!)

    private var messageDBSync = ArrayList<MessagesByGroup>()
    private var listMessages = ArrayList<MessagesByGroup>()
    private var listTypingUser = ArrayList<TypingUser>()
    private var chatAdapter: MessageChatAdapter? = null

    private var nameListTag = ArrayList<String>()

    private var myClipboard: ClipboardManager? = null

    private var selectList: List<LocalMedia> = ArrayList()
    private var imageList = ArrayList<FileNodeJs>()
    private lateinit var toolsChat: ToolsChat
    private var group = Group()
    private var replyMessage = Reply()

    //typing
    private val typingHandler = Handler(Looper.getMainLooper())

    //audio
    private var timer: Timer? = null
    private var audioFile: File? = null
    private var recorder: MediaRecorder? = null
    private var startHTime = 0L
    private val audioTimeHandler = Handler(Looper.getMainLooper())
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L

    private val requiredPermissionFile = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val requiredPermissionsFileCode = 101

    private val requiredPermissionAudio = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private val requiredPermissionAudioCode = 102


    //tag name
    private var indexLast = 0

    private var listTagName = ArrayList<TagName>()
    private var listMembers = ArrayList<Members>()
    private var tagNameUserAdapter: TagNameUserAdapter? = null

    //swipe reply
    private var itemTouchHelper: ItemTouchHelper? = null
    private var vibrator: Vibrator? = null

    //link
    private lateinit var clipboardManager: ClipboardManager
    private var contentPaste = ""
    private var authorLink = ""
    private var titleLink = ""
    private var desLink = ""
    private var imageLink = ""
    private var urlLink = ""
    private var isCheckLink = false

    //screen shot
    private var listScreenShot: ArrayList<String>? = null
    private var adapterSuggestFile: SuggestFileAdapter? = null
    private val screenshotDetectionDelegate by lazy {
        ScreenshotDetectionDelegate(requireActivity(), this)
    }

    //keyboard
    private var onViewHeightChanged = 0
    private var isShowingKeyBoard = false
    private var isKeyBoard = false

    //background
    private var urlBackground = ""

    //socket
    private val application = TechResApplication()
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var bindingReactionClick: ChatRevokeMessageBottomBinding? = null
    private var userReactionAdapter: UserReactionAdapter? = null
    private var listReactionAll: ArrayList<UserReactionChat> = ArrayList()
    private var listReactionSwap: ArrayList<UserReactionChat> = ArrayList()
    private var pre_cursor = "-1";
    private var next_cursor = "-1";
    private var is_oldest_message = true
    private var is_latest_message = true
    private var isLoading = false
    private lateinit var linkPreviewClip: LinkPreviewCallback
    private lateinit var linkPreview: LinkPreviewCallback
    private var isScrollData = false
    private var isChooseImage = false
    private var listImageSuggestCache = ArrayList<String>()
    private var listStringImageSuggestCache = ""
    private var init = 0
    private var positionStroke = -1
    private var stroke = false
    private var firstPosition = 0
    private var lastPosition = 0
    private var messageSearch = ArrayList<MessagesByGroup>()
    private var currentSearch = 0
    private var isStrokeSearch = 0
    private var isClick = false
    var delay: Long = 1000 // 1 seconds after user stops typing

    var last_text_edit: Long = 0

    companion object {
        private const val fileSelectCode = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        onSocket()
    }

    override fun onResume() {
        super.onResume()
        if (init == 1) {
            getDetailGroup(group._id ?: "")
        }
        chatAdapter?.startVideo()
        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onPause() {
        super.onPause()
        typingOff(group)
        stopMedia()
        chatAdapter?.stopPlayer()
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
        stopMedia()
        chatAdapter?.stopPlayer()
        if (listMessages.size > 0) {
            chatAdapter?.stopVideo()
        }
        if (recorder != null) {
            stopDrawing()
        }
//        if(chatAdapter != null){
//            AudioMessageRightHolder().closeAudio()
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
        supportBinding: FragmentChatGroupBinding?,
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
        useragent = resources.getString(R.string.useragent) + BuildConfig.VERSION_NAME
        myClipboard = Objects.requireNonNull(requireActivity())
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        toolsChat = ToolsChat()
        toolsChat.initView(
            binding.layoutMain
        )
        vibrator =
            requireActivity().baseContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val koHi = Kohii[this]
        val manager = koHi.register(this).addBucket(toolsChat.binding.rcvChat)
        chatAdapter = MessageChatAdapter(requireActivity(), manager, koHi)
        adapterSuggestFile = SuggestFileAdapter()
        adapterSuggestFile?.setSuggestFileHandler(this)
        chatAdapter?.setChatGroupHandler(this)
        chatAdapter?.setRevokeMessageHandler(this)
        chatAdapter?.setImageMoreChatHandler(this)
        chatAdapter?.setChooseTagNameHandler(this)
        chatAdapter?.setUtilitiesChatHandler(this)
        ChatUtils.configRecyclerView(toolsChat.binding.rcvChat, chatAdapter)
//        layoutManager =
//            LinearLayoutManager(requireActivity().baseContext, RecyclerView.VERTICAL, true)
//        toolsChat.binding.rcvChat.layoutManager = layoutManager
        toolsChat.binding.rcScreenShot.layoutManager =
            CenterLayoutManager(requireActivity().baseContext, RecyclerView.HORIZONTAL, true)
        toolsChat.binding.rcvChat.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(toolsChat.binding.rcvChat.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        toolsChat.binding.rcScreenShot.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(toolsChat.binding.rcScreenShot.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
//        toolsChat.binding.rcvChat.adapter = chatAdapter
        tagNameUserAdapter = TagNameUserAdapter(requireActivity().baseContext)
        tagNameUserAdapter?.setTagNameUserHandler(this)
        toolsChat.binding.utilities.rcTagUserChat.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        toolsChat.binding.utilities.rcTagUserChat.adapter = tagNameUserAdapter
        toolsChat.binding.rcScreenShot.adapter = adapterSuggestFile
        //swipe item
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper?.attachToRecyclerView(toolsChat.binding.rcvChat)
        //cache copy
        clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //keyboard
        toolsChat.getUtilitiesKeyBoard().setUtilitiesChatHandler(this)
        toolsChat.binding.layoutKeyboard.setPopupListener(this)
        UIView.setUtilitiesChatHandler(this)
        //background
        toolsChat.adapter?.setSelectBackgroundHandler(this)
        toolsChat.binding.tvLoadingMessage.show()
        //set Data
        arguments?.let {
            try {
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
                    getAvatarGroup(toolsChat.binding.imgAvatarChat, group.avatar, configNodeJs)
                    Utils.getGlide(
                        toolsChat.binding.imgBackgroundChat,
                        group.background,
                        configNodeJs
                    )
                    toolsChat.binding.tvTitleChat.text = group.name
                    toolsChat.binding.edtMessageChat.addTextChangedListener(textWatcher)
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
//                    group._id?.let { it1 -> getMessage(it1, "-1", "-1", "-1") }
                }
                init = 1
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }

        }
        setListener()
    }

    //set
    private fun setChat(group: Group) {
        joinGroup(group._id ?: "")
        if (listMessages.size == 0) {
            getFileSuggest()
            getClipboardManager()
        } else {
            chatAdapter?.startVideo()
            chatAdapter?.setDataSource(listMessages)
        }
        getPinned()
        getMember(group._id ?: "")
        toolsChat.binding.tvLoadingMessage.hide()
        getAvatarGroup(toolsChat.binding.imgAvatarChat, group.avatar, configNodeJs)
        Utils.getGlide(toolsChat.binding.imgBackgroundChat, group.background, configNodeJs)
        toolsChat.binding.tvTitleChat.text = group.name
        toolsChat.binding.edtMessageChat.addTextChangedListener(textWatcher)
    }

    //set controller
    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {
        toolsChat.binding.lnStatusCallVideo.hide()
        toolsChat.binding.imgCallChat.hide()
        toolsChat.binding.imgCallVideoChat.hide()

        toolsChat.binding.imgSent.setOnClickListener {
            toolsChat.binding.imgSent.isEnabled = false
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
                    toolsChat.binding.edtMessageChat.text =
                        Editable.Factory.getInstance().newEditable(
                            ""
                        )
                }
            }
            isCheckLink = false
            isReplyMessage = false
            toolsChat.binding.imgSent.isEnabled = true
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
                updateGroup(urlBackground)
            }
            toolsChat.bottomSheet.state = TopSheetBehavior.STATE_COLLAPSED
        }
        //reply
        toolsChat.binding.utilities.reply.imgReplyClose.setOnClickListener {
            isReplyMessage = false
            toolsChat.binding.utilities.ctlReplyMessage.hide()
        }

        toolsChat.binding.imgEmoji.setOnClickListener {
            if (isShowingKeyBoard && toolsChat.binding.layoutKeyboard.isCheckView == 1) {
                toolsChat.binding.layoutKeyboard.openKeyboard()
            } else {
                toolsChat.binding.layoutKeyboard.setView(1)
                toolsChat.binding.imgEmoji.setImageResource(R.drawable.ic_keyboard_chat_message)
                if (toolsChat.binding.layoutKeyboard.isCheckView != 2) {
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
                isKeyBoard = false
                toolsChat.binding.layoutKeyboard.onBackPressed()
                closeKeyboard(toolsChat.binding.edtMessageChat)
                activityChat?.finish()
                leaveGroup()
            }
        }

        toolsChat.binding.edtMessageChat.setOnClickListener {
            toolsChat.binding.layoutKeyboard.openKeyboard()
            toolsChat.binding.edtMessageChat.isCursorVisible = true
        }

        toolsChat.binding.imgCamera.setOnClickListener {
            isChooseImage = true
            toolsChat.binding.imgCamera.isEnabled = false
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
            toolsChat.binding.imgCamera.isEnabled = true
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
            toolsChat.binding.pinned.tvListPinned.isEnabled = false
//            val bundle = Bundle()
//            bundle.putString(
//                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
//            )
//            bundle.putString(
//                TechresEnum.PINNED_DETAIL.toString(),
//                TechresEnum.CHAT_GROUP.toString()
//            )
//            val pinnedDetailFragment = PinnedDetailFragment()
//            pinnedDetailFragment.arguments = bundle
//            activityChat?.addOnceFragment(
//                this,
//                pinnedDetailFragment
//            )

            val intent = Intent(context, PinnedDetailActivity::class.java)
            intent.putExtra(
                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group)
            )
            intent.putExtra(
                TechresEnum.PINNED_DETAIL.toString(),
                TechresEnum.CHAT_GROUP.toString()
            )
            startActivity(intent)

            toolsChat.binding.pinned.tvListPinned.isEnabled = true
        }
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

        toolsChat.binding.imgAudio.setOnClickListener {
            closeKeyboard(toolsChat.binding.edtMessageChat)
            requestMultiplePermissionAudio()
        }

        toolsChat.binding.imgRecycleAudio.setOnClickListener {
            toolsChat.binding.imgEmoji.setImageResource(R.drawable.ic_sticker_tab)
            toolsChat.showController()
            stopDrawing()
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
            val detailGroupFragment = DetailGroupFragment()
            detailGroupFragment.arguments = bundle
            activityChat?.addOnceFragment(this, detailGroupFragment)
            toolsChat.binding.imgMoreActionChat.isEnabled = true
        }

        toolsChat.binding.tvTitleChat.setOnClickListener {
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
            val detailGroupFragment = DetailGroupFragment()
            detailGroupFragment.arguments = bundle
            activityChat?.addOnceFragment(this, detailGroupFragment)
        }

        toolsChat.binding.imgAvatarChat.setOnClickListener {
            val bundle = Bundle()
            try {
                bundle.putString(
                    TechresEnum.GROUP_CHAT.toString(),
                    Gson().toJson(group)
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }

            val detailGroupFragment = DetailGroupFragment()
            detailGroupFragment.arguments = bundle
            activityChat?.addOnceFragment(this, detailGroupFragment)
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
            toolsChat.binding.rlListScreenShot.hide()
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
            toolsChat.binding.rlListScreenShot.hide()
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
                if (stroke == true) {
                    listMessages.forEachIndexed { index, messagesByGroup ->
                        if (messagesByGroup.is_stroke == 1) {
                            positionStroke = index
                            return@forEachIndexed
                        }
                    }
                    if (positionStroke != -1 && (positionStroke - 2 <= firstPosition || positionStroke + 2 >= lastPosition)) {
                        listMessages[positionStroke].is_stroke = 0
                        chatAdapter?.notifyItemChanged(positionStroke)
                        stroke = false
                        positionStroke = -1
                    }
                }
                if (!isLoading) {
                    if (lastPosition >= listMessages.size - 2) {
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
                    if (firstPosition <= 2) {
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
                    toolsChat.binding.messCountSearch.setText("Kết quả thứ $currentSearch / ${messageSearch.size}")
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
     * Add data position first list
     * */
    private fun addItem(item: MessagesByGroup, index: Int) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            TechResApplication.applicationContext().getMessageDao()?.insertOneMessage(item)
        }
        listMessages.set(index, item)
//        chatAdapter?.notifyItemInserted(index)
        if (listMessages.size > 1) {
//            if ((listMessages[1 + index].message_type == TechResEnumChat.TYPE_TEXT.toString()
//                        || listMessages[1 + index].message_type == TechResEnumChat.TYPE_REPLY.toString())
//            ) {
//                chatAdapter?.notifyItemChanged(1 + index)
//            }
//            chatAdapter?.notifyItemChanged(1 + index)
            chatAdapter?.notifyItemChanged(index)
        }
    }

    /**
     * Action Reply Quickly
     * */
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
            replyAction(messagesByGroup)
        }

        override fun onVibrator(position: Int) {
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
    })

    /**
     * Check Tag Name, Link, Typing
     * */
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s!!.isNotEmpty()) {
                toolsChat.binding.imgSent.visibility = View.VISIBLE
                linkAction(s)
                toolsChat.binding.utilities.ctlTagUserChat.hide()
                val string = s.toString()
                val arrString = string.split("\\s".toRegex())
                if (arrString.isNotEmpty()) {
                    val position = arrString.size - 1
                    if (arrString[position].isNotEmpty()) {
                        toolsChat.binding.utilities.ctlTagUserChat.hide()
                        if (arrString[position].contains('@')) {
                            tagNameUserAdapter?.setDataSource(checkMemberTag(string, listMembers))
                            toolsChat.binding.utilities.ctlTagUserChat.show()
                            val indexFirst = arrString[position].indexOfFirst { it == '@' }
                            indexLast = arrString[position].indexOfLast { it == '@' }
                            if ((arrString[position].length - 1) > indexLast) {
                                val nameShot = arrString[position].substring(indexLast + 1)
                                tagNameUserAdapter?.searchFullText(nameShot)
                            }
                            if (indexLast - indexFirst > 2) {
                                toolsChat.binding.utilities.ctlTagUserChat.hide()
                            }
                        } else {
                            if (arrString[position].length > 1 && tagNameUserAdapter?.searchFullText(
                                    arrString[position]
                                ) == true
                            ) {
                                indexLast = arrString[position].length
                                tagNameUserAdapter?.searchFullText(arrString[position])
                                toolsChat.binding.utilities.ctlTagUserChat.show()
                            }
                        }
                    } else {
                        toolsChat.binding.utilities.ctlTagUserChat.hide()
                    }
                }
                typingOn(group)
            } else {
                toolsChat.binding.imgSent.visibility = View.GONE
                if (listMembers.isNotEmpty()) {
                    listTagName = ArrayList()
                    val members = Members()
                    members.member_id = -1
                    members.full_name = getString(R.string.tag_all_chat)
                    if (!listMembers.any { it.member_id == members.member_id }) {
                        listMembers.add(0, members)
                    }
                    tagNameUserAdapter?.setDataSource(listMembers)
                }
                toolsChat.binding.utilities.ctlTagUserChat.hide()
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

    private fun checkMemberTag(
        string: String,
        listMembers: ArrayList<Members>
    ): ArrayList<Members> {
        if (listTagName.isNotEmpty()) {
            val tagNameTemp = ArrayList<TagName>()
            val list = listMembers
            listTagName.forEach { tagName ->
                val nameTemp = String.format("%s%s", "@", tagName.full_name)
                if (string.contains(nameTemp)) {
                    list.removeIf { it.member_id == tagName.member_id }
                    tagNameTemp.add(tagName)
                }
            }
            listTagName = tagNameTemp
            return list
        }
        return listMembers
    }


    /**
     * Audio
     * */

    private fun startRecording() {
        toolsChat.binding.lnAudio.show()

        try {
            val contextWrapper = ContextWrapper(requireActivity().baseContext)
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
            prepare()
            start()
        }
        startDrawing()
    }

    private val runnable = Runnable { startRecording() }

    private fun startDrawing() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    toolsChat.binding.audioRecordView.update(recorder?.maxAmplitude ?: 0)
                } catch (e: IOException) {
                }
            }
        }, 0, 100)
        toolsChat.binding.tvTimePlayAudio.text =
            requireActivity().baseContext?.resources?.getString(
                R.string.time_default
            ) ?: ""
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
        toolsChat.binding.imgSent.hide()
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }

    private fun stopRecording() {
        try {
            stopDrawing()
            randomKeyFile = getRandomString(12)
            val nameFile = String.format(
                "%s.%s",
                getRandomString(24),
                "mp3"
            )
            val size = ((audioFile?.length() ?: 0) / 1024).toInt()
            audioFile?.let { uploadFileAudio(it, nameFile, 1, 0, 0, size, updatedTime.toInt()) }

        } catch (e: java.lang.Exception) {
        }
    }

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
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_micro),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_micro_step_two),
                        R.drawable.btn_chat_input_voice, "", 0, object : DialogPermissionHandler {
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

            })
    }

    /**
     * Link copy
     * */
    private fun getClipboardManager() {
        val primaryClipData: ClipData? = clipboardManager.primaryClip
        if (primaryClipData != null && primaryClipData.itemCount > 0) {
            primaryClipData.getItemAt(0).text?.let {
                contentPaste = it.toString()
                if ((toolsChat.getCacheCopy(requireContext()) ?: "") != contentPaste) {
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
                                                requireContext()
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

    /**
     * Suggest File and Screen Shot time
     * */
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
                val json = SuggestFile(
                    list,
                    TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
                )
                savePreferences(
                    TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                        json
                    )
                )
                toolsChat.binding.imgOne.show()
                toolsChat.binding.imgTwo.hide()
                getMediaGlide(toolsChat.binding.imgOne, list[0])

            }
            else -> {
                val json = SuggestFile(
                    list,
                    TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
                )
                savePreferences(
                    TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                        json
                    )
                )
                toolsChat.binding.imgOne.show()
                toolsChat.binding.imgTwo.show()
                getMediaGlide(toolsChat.binding.imgOne, list[0])
                getMediaGlide(toolsChat.binding.imgTwo, list[1])
                if (list.size > 2) {
                    adapterSuggestFile?.setDataSource(list)
                }
            }
        }
    }

    private fun uploadScreenShot() {
        listScreenShot?.let {
            val json = SuggestFile(
                it,
                TimeFormatHelper.getTimeNow(Calendar.getInstance().time) ?: ""
            )
            savePreferences(
                TechResEnumChat.CACHE_SUGGEST_FILE.toString(), Gson().toJson(
                    json
                )
            )
            numberFile = it.size
            randomKeyFile = getRandomString(12)
            uploadImageLocal(selectList, it, randomKeyFile, 1)
            it.forEach { url ->
                val file = File(url)
                val size = (file.length() / 1024).toInt()

                uploadFile(
                    file,
                    file.name,
                    1,
                    getBitmapRotationImage(file)?.height ?: 0,
                    getBitmapRotationImage(file)?.width ?: 0,
                    size
                )
            }
            listScreenShot = ArrayList()
        }

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

    /**
     * Set data Reply Quickly
     * */
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
        replyMessage.list_tag_name = messagesByGroup.list_tag_name
        replyMessage._id = messagesByGroup._id
        toolsChat.binding.utilities.reply.tvReplyName.text =
            messagesByGroup.sender.full_name
        when (messagesByGroup.message_type) {
            TechResEnumChat.TYPE_TEXT.toString(),
            TechResEnumChat.TYPE_REPLY.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.hide()
                var messageContent = messagesByGroup.message
                messagesByGroup.list_tag_name.let {
                    it.forEach { tagName ->
                        val name = String.format(
                            "%s%s%s",
                            "<font color='#198be3' >",
                            "@" + tagName.full_name,
                            "</font>"
                        )
                        messageContent = messageContent!!.replace(
                            tagName.key_tag_name.toString(),
                            name
                        )
                    }
                }
                toolsChat.binding.utilities.reply.tvReplyContent.text = Html.fromHtml(
                    messageContent,
                    Html.FROM_HTML_MODE_LEGACY
                )

            }
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.show()
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.hide()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    getString(R.string.pinned_image)

                Utils.getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
                    configNodeJs
                )
            }
            TechResEnumChat.TYPE_STICKER.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.show()
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.hide()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_sticker)

                Utils.getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
                    configNodeJs
                )
            }
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.show()
                toolsChat.binding.utilities.reply.imgReplyPlay.show()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.hide()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    requireActivity().resources.getString(R.string.pinned_video)

                Utils.getGlide(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.files[0].link_original,
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
                showImage(
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
                val mineType = messagesByGroup.files[0].link_original?.let { getMimeType(it) }
                ChatUtils.setImageFile(toolsChat.binding.utilities.reply.imgFile, mineType)
            }
            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                toolsChat.binding.utilities.reply.rlReplyThumbContainer.show()
                toolsChat.binding.utilities.reply.cvMediaReply.show()
                toolsChat.binding.utilities.reply.imgReplyPlay.hide()
                toolsChat.binding.utilities.reply.imgReplyAudio.hide()
                toolsChat.binding.utilities.reply.imgFile.hide()
                toolsChat.binding.utilities.reply.tvReplyContent.text =
                    String.format(
                        "%s %s",
                        getString(R.string.pinned_business_card),
                        messagesByGroup.message_phone?.full_name ?: ""
                    )
                getImage(
                    toolsChat.binding.utilities.reply.imgReply,
                    messagesByGroup.message_phone?.avatar?.medium,
                    configNodeJs
                )
            }
        }
        showKeyboard(toolsChat.binding.edtMessageChat)
        toolsChat.binding.layoutKeyboard.openKeyboard()
    }

    /**
     * Bottom Sheet Utilities
     *      Note : Revoke, Reaction, Copy, Download, Information, Share
     * */
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
        val lnMessageImportantReaction =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.lnMessageImportantReaction)
        val llDownloadReaction =
            bottomSheetDialog.findViewById<LinearLayout>(R.id.llDownloadReaction)
        val llRevokeReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.llRevokeReaction)
        val itemLike = bottomSheetDialog.findViewById<ImageView>(R.id.like_item)
        val itemLove = bottomSheetDialog.findViewById<ImageView>(R.id.love_item)
        val itemSmile = bottomSheetDialog.findViewById<ImageView>(R.id.smile_item)
        val itemWow = bottomSheetDialog.findViewById<ImageView>(R.id.wow_item)
        val itemAngry = bottomSheetDialog.findViewById<ImageView>(R.id.angry_item)
        val itemSad = bottomSheetDialog.findViewById<ImageView>(R.id.sad_item)
        val revokeReaction = bottomSheetDialog.findViewById<LinearLayout>(R.id.revokeReaction)

        if (messagesByGroup.sender.member_id ?: 0 != user.id) {
            llRevokeReaction?.hide()
        } else {
            llRevokeReaction?.show()
        }

        if (messagesByGroup.reactions.my_reaction!! > 0) {
            revokeReaction?.visibility = View.VISIBLE
        } else {
            revokeReaction?.visibility = View.GONE
        }
        revokeReaction?.setOnClickListener {
            revokeReactionMessage(messagesByGroup, group)
            bottomSheetDialog.dismiss()
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

        //Reaction
        itemLike?.setOnClickListener {
            binding.fragmentRootView.stopDropping()
            binding.fragmentRootView.clearEmojis()
            binding.fragmentRootView.addEmoji(R.drawable.ic_like)
            binding.fragmentRootView.startDropping()

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
            revokeMessage(messagesByGroup)
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
                downLoadFile(
                    requireActivity(),
                    it1, configNodeJs
                )
            }
            bottomSheetDialog.dismiss()
        }
        llCopyReaction?.setOnClickListener {
            when (messagesByGroup.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString(), TechResEnumChat.TYPE_LINK.toString() -> {
                    var messageHtml = messagesByGroup.message?.replace("\n", "<br>") ?: ""

                    messagesByGroup.list_tag_name.forEach { tagName ->
                        val name = String.format(
                            "%s%s", "@", tagName.full_name
                        )
                        messageHtml = messageHtml.replace(tagName.key_tag_name ?: "", name)
                    }

                    val clipData = ClipData.newPlainText(
                        TechResEnumChat.TYPE_TEXT.toString(),
                        Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_LEGACY)
                    )
                    myClipboard?.setPrimaryClip(clipData)
                    FancyToast.makeText(
                        context,
                        requireActivity().resources.getString(R.string.copied),
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                        false
                    ).show()
                }
                TechResEnumChat.BUSINESS_CARD_ALO_LINE.toString() -> {
                    val clipData = ClipData.newPlainText(
                        TechResEnumChat.TYPE_TEXT.toString(),
                        messagesByGroup.message_phone?.phone ?: ""
                    )
                    myClipboard?.setPrimaryClip(clipData)
                    FancyToast.makeText(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.copied),
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                        false
                    ).show()
                }
            }
            bottomSheetDialog.dismiss()
        }

        llReplyReaction?.setOnClickListener {
            bottomSheetDialog.dismiss()
            Handler(Looper.getMainLooper()).postDelayed({
                replyAction(messagesByGroup)
            }, 500)
        }

        llPinnedReaction?.setOnClickListener {
            pinned(messagesByGroup)
            bottomSheetDialog.dismiss()
        }

        llInformationReaction?.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString(
//                TechResEnumChat.MESSAGE_INFORMATION.toString(), Gson().toJson(
//                    messagesByGroup
//                )
//            )
//            bundle.putString(
//                TechResEnumChat.MEMBER_INFORMATION.toString(), Gson().toJson(
//                    listMembers
//                )
//            )
//
//            val informationMessageFragment = InformationMessageFragment()
//            informationMessageFragment.arguments = bundle
//            activityChat?.addOnceFragment(this, informationMessageFragment)
            val intent = Intent(context, InformationMessageActivity::class.java)
            intent.putExtra(
                TechResEnumChat.MESSAGE_INFORMATION.toString(), Gson().toJson(
                    messagesByGroup
                )
            )
            intent.putExtra(
                TechResEnumChat.MEMBER_INFORMATION.toString(), Gson().toJson(
                    listMembers
                )
            )

            startActivity(intent)
            bottomSheetDialog.dismiss()
        }
        llShareReaction?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(TechresEnum.SHARE_MESSAGE.toString(), Gson().toJson(messagesByGroup))
            bundle.putString(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group))
            val shareMessageGroupFragment = ShareMessageFragment()

            shareMessageGroupFragment.arguments = bundle
            activityChat?.addOnceFragment(this, shareMessageGroupFragment)
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    /**
     * Set data Pinned
     * */
    @SuppressLint("SetTextI18n")
    private fun setPinned(messagesByGroup: MessagesByGroup) {
        idPinnedMessage = messagesByGroup.message_pinned?.random_key ?: ""
        when (messagesByGroup.message_pinned?.message_type) {
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                toolsChat.binding.pinned.rlThumbContainer.show()
                toolsChat.binding.pinned.imgPinnedPlay.hide()
                toolsChat.binding.pinned.cvMedia.show()
                toolsChat.binding.pinned.imgFile.hide()
                toolsChat.binding.pinned.imgAudio.hide()
                Utils.getGlide(
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
                    Utils.getGlide(
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
                Utils.getGlide(
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
                    requireActivity().resources.getString(R.string.pinned_file) + URLDecoder.decode(
                        messagesByGroup.message_pinned?.files?.get(
                            0
                        )?.name_file, "UTF-8"
                    )
                val mineType = messagesByGroup.message_pinned?.files?.get(0)?.link_original?.let {
                    getMimeType(
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
                showImage(
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
        toolsChat.binding.pinned.tvUserPinned.text =
            messagesByGroup.message_pinned?.sender?.full_name ?: ""
//        toolsChat.binding.pinned.tvUserPinnedList.text = messagesByGroup.sender.full_name
//        toolsChat.binding.pinned.tvTimePinned.text = TimeFormatHelper.getDateFromStringDayMonthYear(
//            messagesByGroup.created_at
//        )
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

    /**
     * Set data Typing
     * */
    private fun setTypingTextReply(messagesByGroup: MessagesByGroup) {
//        if (messagesByGroup.sender.member_id != user.id) {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            messagesByGroup.user_id = user.id
            TechResApplication.applicationContext().getMessageDao()
                ?.insertOneMessage(messagesByGroup)
        }
        if (is_latest_message) {
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

    }

    /**
     * Socket On
     * */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun onSocket() {
        /**
         * Text
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_TEXT_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_TEXT", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTypingTextReply(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Typing on
         * */
        mSocket?.on(TechResEnumChat.RES_TYPING_ON_GROUP_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_USER_IS_TYPING_PARTY", it[0].toString())
                    val typingOnResponse = Gson().fromJson<TypingUser>(it[0].toString(), object :
                        TypeToken<TypingUser>() {}.type)
                    this.randomKeyTyping = typingOnResponse.random_key ?: ""
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
                            listTypingUser.add(0, typingOnResponse)
                            listMessages.add(0, messagesByGroup)
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
        /**
         * Typing off
         * */
        mSocket?.on(TechResEnumChat.RES_TYPING_OFF_GROUP_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_TYPING_OFF_GROUP_ALO_LINE", it[0].toString())
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
         * Sticker
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_STICKER_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_STICKER_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        /**
         * Image
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_IMAGE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_IMAGE_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        /**
         * Audio
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_AUDIO_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_AUDIO_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {

                }
            }
        }
        /**
         * Video
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_VIDEO_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_VIDEO_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {

                }
            }
        }
        /**
         * Update Background
         * */
        mSocket?.on(TechResEnumChat.RES_UPDATE_GROUP_BACKGROUND_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_UPDATE_GROUP_BACKGROUND_ALO_LINE", args[0].toString())
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
        /**
         * Member Leave
         * */
        mSocket?.on(TechResEnumChat.RES_MEMBER_LEAVE_GROUP_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_MEMBER_LEAVE_GROUP_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    if (messagesByGroup.sender.member_id == user.id)
                        EventBus.getDefault().post(messagesByGroup.receiver_id?.let {
                            EventBusLeaveGroup(
                                it
                            )
                        })
                    setTyping(messagesByGroup)
                } catch (e: IOException) {

                }
            }
        }
        /**
         * Remove member
         * */
        mSocket?.on(TechResEnumChat.RES_REMOVE_USER_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REMOVE_USER_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                    if (messagesByGroup.list_member[0].member_id == user.id) {
                        cacheManager.put(TechresEnum.CURRENT_PAGE.toString(), 3.toString())
                        isKeyBoard = false
                        toolsChat.binding.layoutKeyboard.onBackPressed()
                        closeKeyboard(toolsChat.binding.edtMessageChat)
                        activityChat?.finish()
                        leaveGroup()
                    }
                } catch (e: IOException) {

                }
            }
        }
        /**
         * Add Member
         * */
        mSocket?.on(TechResEnumChat.RES_ADD_NEW_USER_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_ADD_NEW_USER_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Authorize
         * */
        mSocket?.on(TechResEnumChat.RES_ADMIN_AUTHORIZE_MEMBER_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_ADMIN_AUTHORIZE_MEMBER_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)

                } catch (e: IOException) {

                }
            }
        }
        mSocket?.on(TechResEnumChat.RES_PROMOTE_GROUP_VICE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_PROMOTE_GROUP_VICE_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)

                } catch (e: IOException) {

                }
            }
        }
        /**
         * Update Name
         * */
        mSocket?.on(TechResEnumChat.RES_UPDATE_GROUP_NAME_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_UPDATE_GROUP_NAME_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    toolsChat.binding.tvTitleChat.text = messagesByGroup.group_name
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Reaction
         * */
        mSocket?.on(TechResEnumChat.RES_REACTION_MESSAGE_ALO_LINE.toString() + "/${user.id}") { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REACTION_MESSAGE_ALO_LINE", args[0].toString())
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
                                    if (it.reactions.is_revoke == 0) {
                                        binding.fragmentRootView.clearEmojis()
                                        binding.fragmentRootView.addEmoji(
                                            setReactionClick(
                                                messagesByGroup
                                            )
                                        )
                                        binding.fragmentRootView.startDropping()
                                        isCheckReaction = false
                                    }
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
         * Pinned
         * */
        mSocket?.on(TechResEnumChat.RES_PINNED_MESSAGE_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_PINNED_MESSAGE_ALO_LINE", it[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(it[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setPinned(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }

        mSocket?.on(TechResEnumChat.RES_PINNED_MESSAGE_TEXT_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_PINNED_MESSAGE_TEXT_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Update Avatar
         * */
        mSocket?.on(TechResEnumChat.RES_UPDATE_GROUP_AVATAR_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_UPDATE_AVATAR_GROUP_PARTY", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Revoke Pinned
         * */
        mSocket?.on(TechResEnumChat.RES_REVOKE_PINNED_MESSAGE_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_REVOKE_PINNED_MESSAGE_ALO_LINE", it[0].toString())
                toolsChat.binding.pinned.lnPinned.hide()
            }
        }

        mSocket?.on(TechResEnumChat.RES_REVOKE_PINNED_MESSAGE_TEXT_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_PINNED_MESSAGE_TEXT_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTyping(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Revoke Message
         * */
        mSocket?.on(TechResEnumChat.RES_REVOKE_MESSAGE_REPLY_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_MESSAGE_REPLY_ALO_LINE", args[0].toString())
                    val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                        args[0].toString(),
                        object :
                            TypeToken<MessagesByGroup>() {}.type
                    )
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

        mSocket?.on(TechResEnumChat.RES_REVOKE_MESSAGE_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_REVOKE_MESSAGE_ALO_LINE", args[0].toString())
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
                            return@forEachIndexed
                        }
                    }
                } catch (e: IOException) {
                }
            }
        }
        /**
         * Reply
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_REPLY_ALO_LINE.toString()) { args ->
            activityChat?.runOnUiThread {
                try {
                    WriteLog.d("RES_CHAT_REPLY_ALO_LINE", args[0].toString())
                    val messagesByGroup =
                        Gson().fromJson<MessagesByGroup>(args[0].toString(), object :
                            TypeToken<MessagesByGroup>() {}.type)
                    setTypingTextReply(messagesByGroup)
                } catch (e: IOException) {
                }
            }
        }
        /**
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
         * File
         * */
        mSocket?.on(
            TechResEnumChat.RES_CHAT_FILE_ALO_LINE.toString()
        ) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_FILE_ALO_LINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        /**
         * Business Card
         * */
        mSocket?.on(TechResEnumChat.RES_BUSINESS_CARD_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_BUSINESS_CARD_ALO_LINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        /**
         * Vote
         * */
        mSocket?.on(TechResEnumChat.RES_CREATE_VOTE_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CREATE_VOTE_ALO_LINE", it[0].toString())
                val messagesByGroup = Gson().fromJson<MessagesByGroup>(
                    it[0].toString(),
                    object :
                        TypeToken<MessagesByGroup>() {}.type
                )
                setTyping(messagesByGroup)
            }
        }
        /**
         * Message Viewer
         * */
        mSocket?.on(TechResEnumChat.RES_MESSAGE_VIEWED_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_MESSAGE_VIEWED_ALO_LINE", it[0].toString())
                val messageViewed = Gson().fromJson<MessageViewer>(
                    it[0].toString(),
                    object :
                        TypeToken<MessageViewer>() {}.type
                )
                if (listMessages.size > 1 && listMessages[0].message_viewed == null) {
                    listMessages[0].message_viewed = ArrayList()
                    listMessages[0].message_viewed?.add(messageViewed)
                    chatAdapter?.notifyItemChanged(1)
                    chatAdapter?.notifyItemChanged(0)
                }
            }
        }

        mSocket?.on(TechResEnumChat.RES_LIST_MESSAGE_VIEWED_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_LIST_MESSAGE_VIEWED_ALO_LINE", it[0].toString())
                val listMessageViewed = Gson().fromJson<ArrayList<MessageViewer>>(
                    it[0].toString(),
                    object :
                        TypeToken<ArrayList<MessageViewer>>() {}.type
                )
                if (listMessages.size > 1 && listMessages[0].message_viewed == null) {
                    listMessages[0].message_viewed = listMessageViewed
                    chatAdapter?.notifyItemChanged(1)
                    chatAdapter?.notifyItemChanged(0)
                }
            }
        }
        /**
         * Link
         * */
        mSocket?.on(TechResEnumChat.RES_CHAT_LINK_ALO_LINE.toString()) {
            activityChat?.runOnUiThread {
                WriteLog.d("RES_CHAT_LINK_ALO_LINE", it[0].toString())
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
     * Socket Emit
     * */
    private fun joinGroup(groupId: String) {
        val joinGroup = JoinChatRequest()
        joinGroup.group_id = groupId
        joinGroup.member_id = user.id
        joinGroup.avatar = user.avatar_three_image.original
        joinGroup.full_name = user.name
        joinGroup.os_name = "android"
        try {
            val jsonObject = JSONObject(Gson().toJson(joinGroup))
            mSocket?.emit(TechResEnumChat.JOIN_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("JOIN_ROOM_GROUP_ALO_LINE", jsonObject.toString())

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
                mSocket?.emit(TechResEnumChat.TYPING_ON_GROUP_ALO_LINE.toString(), jsonObject)
                WriteLog.d("TYPING_ON_GROUP_ALO_LINE", jsonObject.toString())

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun typingOff(group: Group) {
        val typingOffRequest = TypingOffRequest()
        typingOffRequest.group_id = group._id
        typingOffRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(typingOffRequest))
            isTying = 0
            mSocket?.emit(TechResEnumChat.TYPING_OFF_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("TYPING_OFF_GROUP_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatImage(file: ArrayList<FileNodeJs>) {
        val chatImageGroupRequest = ChatImageGroupRequest()
        chatImageGroupRequest.member_id = user.id
        chatImageGroupRequest.message_type = TechResEnumChat.TYPE_IMAGE.toString()
        chatImageGroupRequest.group_id = group._id
        chatImageGroupRequest.files = file
        chatImageGroupRequest.key_message_error = getRandomString(10)
        chatImageGroupRequest.key_message = randomKeyFile

        try {
            val jsonObject = JSONObject(Gson().toJson(chatImageGroupRequest))
            WriteLog.d("CHAT_IMAGE_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_IMAGE_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatAudio(file: ArrayList<FileNodeJs>) {
        val chatAudioGroupRequest = ChatAudioGroupRequest()
        chatAudioGroupRequest.member_id = user.id
        chatAudioGroupRequest.message_type = TechResEnumChat.TYPE_AUDIO.toString()
        chatAudioGroupRequest.group_id = group._id
        chatAudioGroupRequest.files = file
        chatAudioGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatAudioGroupRequest))
            WriteLog.d("CHAT_AUDIO_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_AUDIO_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatVideo(file: ArrayList<FileNodeJs>) {
        val chatVideoGroupRequest = ChatVideoGroupRequest()
        chatVideoGroupRequest.member_id = user.id
        chatVideoGroupRequest.message_type = TechResEnumChat.TYPE_VIDEO.toString()
        chatVideoGroupRequest.group_id = group._id
        chatVideoGroupRequest.files = file
        chatVideoGroupRequest.key_message_error = getRandomString(10)
        chatVideoGroupRequest.key_message = randomKeyFile
        try {
            val jsonObject = JSONObject(Gson().toJson(chatVideoGroupRequest))
            WriteLog.d("CHAT_VIDEO_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_VIDEO_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun chatLink() {
        val linkMessageRequest = LinkMessageRequest()
        var textMessage = toolsChat.binding.edtMessageChat.text.toString()
        linkMessageRequest.member_id = user.id
        linkMessageRequest.message_type = TechResEnumChat.TYPE_LINK.toString()
        linkMessageRequest.group_id = group._id
        if (listTagName.isNotEmpty()) {
            listTagName.forEach {
                textMessage = textMessage.replace(
                    "@" + it.full_name.toString(),
                    it.key_tag_name.toString()
                )
            }
        }
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
            mSocket?.emit(TechResEnumChat.CHAT_LINK_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable("")
        nameListTag = ArrayList()
        toolsChat.hideLink()
        isCheckLink = false
    }

    private fun chatText(group: Group) {
        val s = toolsChat.binding.edtMessageChat.text.toString()
        val m: Matcher = Pattern.compile("(.{1,2000}(\\W|$))").matcher(s)
        try {
            while (m.find()) {
                val chatTextRequest = ChatTextGroupRequest()
                var textMessage = m.group()
                chatTextRequest.member_id = user.id
                chatTextRequest.message_type = TechResEnumChat.TYPE_TEXT.toString()
                chatTextRequest.group_id = group._id.toString()
                if (listTagName.isNotEmpty()) {
                    listTagName.forEach {
                        textMessage = textMessage.replace(
                            "@" + it.full_name.toString(),
                            it.key_tag_name.toString()
                        )
                    }
                }
                chatTextRequest.message = textMessage
                chatTextRequest.list_tag_name = listTagName
                chatTextRequest.key_message_error = getRandomString(10)
                listTagName = ArrayList()
                try {
                    val jsonObject = JSONObject(Gson().toJson(chatTextRequest))
                    mSocket?.emit(TechResEnumChat.CHAT_TEXT_ALO_LINE.toString(), jsonObject)
                    WriteLog.d("CHAT_TEXT_ALO_LINE", jsonObject.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable("")
        nameListTag = ArrayList()
    }

    private fun chatFile(group: Group, file: ArrayList<FileNodeJs>) {
        val chatFileGroupRequest = ChatFileGroupRequest()
        chatFileGroupRequest.member_id = user.id
        chatFileGroupRequest.files = file
        chatFileGroupRequest.group_id = group._id
        chatFileGroupRequest.message_type = TechResEnumChat.TYPE_FILE.toString()
        chatFileGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatFileGroupRequest))
            mSocket?.emit(TechResEnumChat.CHAT_FILE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("CHAT_FILE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun reactionMessage(
        messagesByGroup: MessagesByGroup,
        group: Group,
        idReaction: Int
    ) {
        val reactionMessageRequest = ReactionMessageRequest()
        reactionMessageRequest.group_id = group._id
        reactionMessageRequest.random_key = messagesByGroup.random_key
        if (idReaction > 0) {
            reactionMessageRequest.last_reactions = idReaction
        } else {
            reactionMessageRequest.last_reactions =
                TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt()
        }
        reactionMessageRequest.member_id = user.id
        reactionMessageRequest.last_reactions_id = user.id
        reactionMessageRequest.key_message_error = getRandomString(
            10
        )
        try {
            val jsonObject = JSONObject(Gson().toJson(reactionMessageRequest))
            WriteLog.d("REACTION_MESSAGE_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.REACTION_MESSAGE_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun revokeReactionMessage(
        messagesByGroup: MessagesByGroup,
        group: Group
    ) {
        val revokeReactionMessageRequest = RevokeReactionMessageRequest()
        revokeReactionMessageRequest.group_id = group._id
        revokeReactionMessageRequest.random_key = messagesByGroup.random_key
        revokeReactionMessageRequest.member_id = user.id
        revokeReactionMessageRequest.key_message_error = getRandomString(
            10
        )
        try {
            val jsonObject = JSONObject(Gson().toJson(revokeReactionMessageRequest))
            WriteLog.d("REVOKE_REACTION_MESSAGE_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.REVOKE_REACTION_MESSAGE_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun revokeMessage(messagesByGroup: MessagesByGroup) {
        val revokeChatRequest = RevokeChatRequest()
        revokeChatRequest.random_key = messagesByGroup.random_key
        revokeChatRequest.group_id = group._id
        revokeChatRequest.member_id = user.id
        revokeChatRequest.message_type = TechResEnumChat.TYPE_REVOKE.toString()
        revokeChatRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(revokeChatRequest))
            mSocket?.emit(TechResEnumChat.REVOKE_MESSAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REVOKE_MESSAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun leaveGroup() {
        val leaveChatRequest = LeaveChatRequest()
        leaveChatRequest.group_id = group._id
        leaveChatRequest.member_id = user.id
        leaveChatRequest.full_name = user.name
        leaveChatRequest.avatar = user.avatar_three_image.original
        try {
            val jsonObject = JSONObject(Gson().toJson(leaveChatRequest))
            mSocket?.emit(TechResEnumChat.LEAVE_ROOM_GROUP_ALO_LINE.toString(), jsonObject)
            WriteLog.d("LEAVE_ROOM_GROUP_ALO_LINE", jsonObject.toString())
            EventBus.getDefault().post(group._id?.let { EventBusLeaveRoom(it) })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun pinned(messagesByGroup: MessagesByGroup) {
        val pinnedRequest = PinnedRequest()
        pinnedRequest.random_key = messagesByGroup.random_key
        pinnedRequest.member_id = user.id
        pinnedRequest.group_id = group._id
        pinnedRequest.message_type = TechResEnumChat.TYPE_PINNED.toString()
        pinnedRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(pinnedRequest))
            mSocket?.emit(TechResEnumChat.PINNED_MESSAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("PINNED_MESSAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
            WriteLog.d("Error", e.message.toString())
        }
    }

    private fun revokePinned() {
        toolsChat.binding.pinned.lnPinned.hide()
        val revokePinnedChatRequest = RevokePinnedChatRequest()
        revokePinnedChatRequest.random_key = idPinnedMessage
        revokePinnedChatRequest.group_id = group._id
        revokePinnedChatRequest.member_id = user.id
        revokePinnedChatRequest.message_type = TechResEnumChat.TYPE_REMOVE_PINNED.toString()
        revokePinnedChatRequest.key_message_error = getRandomString(
            10
        )
        try {
            val jsonObject = JSONObject(Gson().toJson(revokePinnedChatRequest))
            mSocket?.emit(TechResEnumChat.REVOKE_PINNED_MESSAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("REVOKE_PINNED_MESSAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun reply(group: Group, reply: Reply) {
        val replyRequest = ReplyGroupRequest()
        var textMessage = toolsChat.binding.edtMessageChat.text.toString()
        replyRequest.message_type = TechResEnumChat.TYPE_REPLY.toString()
        replyRequest.group_id = group._id
        replyRequest.list_tag_name = listTagName
        replyRequest.member_id = user.id
        replyRequest.random_key = reply.random_key
        replyRequest.key_message_error = getRandomString(10)
        if (listTagName.isNotEmpty()) {
            listTagName.forEach {
                textMessage = textMessage.replace(
                    "@" + it.full_name.toString(),
                    it.key_tag_name.toString()
                )
            }
            replyRequest.message = textMessage
        } else {
            replyRequest.message = textMessage
        }
        try {
            val jsonObject = JSONObject(Gson().toJson(replyRequest))
            mSocket?.emit(TechResEnumChat.CHAT_REPLY_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        isReplyMessage = false
        toolsChat.binding.utilities.ctlReplyMessage.hide()
        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable("")
    }

    private fun chatSticker(sticker: Sticker) {
        val chatStickerGroupRequest = ChatStickerGroupRequest()
        chatStickerGroupRequest.member_id = user.id
        chatStickerGroupRequest.group_id = group._id
        chatStickerGroupRequest.sticker_id = sticker._id
        chatStickerGroupRequest.message_type = TechResEnumChat.TYPE_STICKER.toString()
        chatStickerGroupRequest.key_message_error = getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(chatStickerGroupRequest))
            WriteLog.d("CHAT_STICKER_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.CHAT_STICKER_ALO_LINE.toString(), jsonObject)
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
            WriteLog.d("BUSINESS_CARD_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.BUSINESS_CARD_ALO_LINE.toString(), jsonObject)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun changeBackground(url: String) {
        val updateDetailGroupRequest = UpdateDetailGroupRequest()
        updateDetailGroupRequest.avatar = group.avatar
        updateDetailGroupRequest.background = url
        updateDetailGroupRequest.group_id = group._id
        updateDetailGroupRequest.name = group.name
        updateDetailGroupRequest.member_id = user.id
        updateDetailGroupRequest.message_type = TechResEnumChat.TYPE_BACKGROUND.toString()
        try {
            val jsonObject = JSONObject(Gson().toJson(updateDetailGroupRequest))
            mSocket?.emit(TechResEnumChat.UPDATE_GROUP_BACKGROUND_ALO_LINE.toString(), jsonObject)
            WriteLog.d("UPDATE_GROUP_BACKGROUND_ALO_LINE", jsonObject.toString())
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
            mSocket?.emit(TechResEnumChat.STAR_MESSAGE_ALO_LINE.toString(), jsonObject)
            WriteLog.d("STAR_MESSAGE_ALO_LINE", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * Upload Local
     * */
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
                fileNodeJs.height = getBitmapRotationImage(File(it.realPath))?.height ?: 0
                fileNodeJs.width = getBitmapRotationImage(File(it.realPath))?.width ?: 0
                file.add(fileNodeJs)
            }
        } else {
            listScreenShot.forEach {
                val fileNodeJs = FileNodeJs()
                fileNodeJs.link_original = it
                fileNodeJs.height = getBitmapRotationImage(File(it))?.height ?: 0
                fileNodeJs.width = getBitmapRotationImage(File(it))?.width ?: 0
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
//        toolsChat.binding.lnNoMessage.hide()
//        listMessages.add(0, message)
//        chatAdapter?.notifyItemInserted(0)
//        toolsChat.binding.rcvChat.scrollToPosition(0)
    }

    @SuppressLint("SimpleDateFormat")
    private fun uploadVideoLocal(selectList: LocalMedia, randomKey: String) {
        val sender = Sender()
        val message = MessagesByGroup()
        val file = ArrayList<FileNodeJs>()
        val today = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        val fileNodeJs = FileNodeJs()
        fileNodeJs.link_original = selectList.realPath
        fileNodeJs.height = getBitmapRotationVideo(File(selectList.realPath))?.height ?: 0
        fileNodeJs.width = getBitmapRotationVideo(File(selectList.realPath))?.width ?: 0
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
//        toolsChat.binding.lnNoMessage.hide()
//        listMessages.add(0, message)
//        chatAdapter?.notifyItemInserted(0)
//        toolsChat.binding.rcvChat.scrollToPosition(0)
    }

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
        toolsChat.binding.rcvChat.scrollToPosition(0)
    }

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
                            group.name = data?.group_name
                            group.avatar = data?.avatar
                            group.background = data?.background
                            group.activities_status = data?.activities_status.toString()
                            data?.members?.forEachIndexed { index, members ->
                                if (user.id != members.member_id) {
                                    group.member = members
                                }
                            }
                            setChat(group)
                        }
                    }
                }
            })
    }

    private fun getFile(
        nameFile: String,
        type: Int,
        height: Int,
        width: Int,
        size: Int,
        time: Int = 0
    ) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api-upload/get-link-server-chat?group_id=",
            group._id,
            "&type=",
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            "&name_file=",
            nameFile,
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
                        WriteLog.d("uploading...", "success")
                        if (type == 1) {
                            if (nameFile.contains("mp4")) {
                                val arrayList = ArrayList<FileNodeJs>()
                                response.data.height = height
                                response.data.width = width
                                arrayList.add(response.data)
                                chatVideo(arrayList)
                                randomKeyFile = ""
                            } else if (nameFile.contains("mp3")) {
                                val arrayList = ArrayList<FileNodeJs>()
                                response.data.time = time
                                arrayList.add(response.data)
                                chatAudio(arrayList)
                                randomKeyFile = ""
                            } else {
                                response.data.height = height
                                response.data.width = width
                                imageList.add(response.data)
                                if (imageList.size == numberFile) {
                                    chatImage(imageList)
                                    imageList.clear()
                                    numberFile = 0
                                    randomKeyFile = ""
                                }
                            }
                        } else {
                            val arrayList = ArrayList<FileNodeJs>()
                            response.data.size = size
                            arrayList.add(response.data)
                            chatFile(group, arrayList)
                        }

                    } else {
                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                            .show()
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
//        var nameMain = URLEncoder.encode(nameFile, "UTF-8")
        var nameMain = nameFile.replace(" ", "%20")
//        nameFile.forEachIndexed { index, c ->
//            if (ChatActivity.codeVn.contains(c.toString().lowercase())) {
//                nameMain += c
//            }
//        }
        val serverUrlString = String.format(
            "%s/api-upload/upload-file-server-chat/%s/%s/%s",
            configNodeJs.api_ads,
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
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

    private fun uploadFileAudio(
        file: File,
        name: String,
        type: Int,
        height: Int,
        width: Int,
        size: Int,
        time: Int = 0
    ) {
        val serverUrlString = String.format(
            "%s/api-upload/upload-file-server-chat/%s/%s/%s",
            configNodeJs.api_ads,
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            group._id,
            name
        )
        WriteLog.d("Upload audio :", serverUrlString)
        val paramNameString = resources.getString(R.string.send_file)
        try {
            MultipartUploadRequest(
                requireActivity(),
                serverUrlString
            )
                .setMethod("POST")
                .addFileToUpload(file.absolutePath, paramNameString)
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
                            getFile(name, type, height, width, size, time)
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
                        if (uploadInfo.files[0].path.contains("mp4")) {
                            val position =
                                listMessages.indexOfFirst { it.key_message == randomKeyFile }
                            listMessages[position].files[0].process = uploadInfo.progressPercent
                            chatAdapter?.notifyItemChanged(position)
                        }
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

    private fun getPinned() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url =
            String.format("%s%s", "api/pinned-messages/get-one?group_id=", group._id)

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
                        val pinned = response.data
                        context?.let {
                            pinned?.let { pinned ->
                                setPinned(pinned)
                            }
                        }
                    }
                }
            })
    }

    private fun getMember(idGroup: String) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            String.format("%s%s%s", "/api/groups/", idGroup, "/get-list-tag-name")
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getMemberGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MemberGroupResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "SetTextI18n")
                override fun onNext(response: MemberGroupResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listMembers = response.data.members
                        if (listMembers.isNotEmpty() && context != null) {
                            tagNameUserAdapter?.setDataSource(listMembers)
                            toolsChat.binding.utilities.ctlTagUserChat.hide()
                            toolsChat.binding.tvStatusHeader.text =
                                (listMembers.size - 1).toString() + " Thành viên"
                            toolsChat.binding.tvStatusHeader.show()
                        } else {
                            toolsChat.binding.tvStatusHeader.hide()
                        }
                    }
                }
            })
    }

    private fun updateGroup(url: String) {
        val params = UpdateGroupParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s", "/groups/", group._id)
        params.project_id = AppConfig.getProjectChat()
        params.params.avatar = group.avatar
        params.params.background = url
        group.background = url
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
                }
            })
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
        isChooseImage = false
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                /**
                 * Image / Video
                 * */
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
//                            val size = (file.length() / 1024).toInt()
                            if (it.mimeType == "video/mp4") {
                                uploadFile(
                                    file,
                                    file.name,
                                    1,
                                    getBitmapRotationVideo(file)?.height ?: 0,
                                    getBitmapRotationVideo(file)?.width ?: 0,
                                    0
                                )
                            } else {
                                uploadFile(
                                    file,
                                    file.name,
                                    1,
                                    getBitmapRotationImage(file)?.height ?: 0,
                                    getBitmapRotationImage(file)?.width ?: 0,
                                    0
                                )
                            }
                        }
                    }
                    numberFile = selectList.size
                }
                /**
                 * File
                 * */
                FilePickerManager.REQUEST_CODE -> {
                    if (null != data!!.clipData) {
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
                        requireActivity().resources.getString(R.string.title_permission_contact),
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
                        requireActivity().resources.getString(R.string.title_permission_contact),
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
    }

    override fun onRevoke(messagesByGroup: MessagesByGroup, view: View) {
        bottomSheetReaction(messagesByGroup)
    }

    override fun onPressReaction(messagesByGroup: MessagesByGroup, view: View) {
        val originalPos = IntArray(2)
        view.getLocationInWindow(originalPos)
        val x = originalPos[0]
        val y = originalPos[1]
        flyEmoji(
            setReactionClick(messagesByGroup),
            requireActivity(),
            toolsChat.binding.lnContainerChat,
            x.toFloat(),
            (y - dpToPx(60)).toFloat()

        )
        reactionMessage(messagesByGroup, group, messagesByGroup.reactions.last_reactions!!)
    }

    override fun onClickViewReaction(
        messagesByGroup: MessagesByGroup,
        reactionItems: ArrayList<ReactionItem>
    ) {
        setClickRevokeEmoji(messagesByGroup, reactionItems)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    fun setClickRevokeEmoji(
        messages: MessagesByGroup,
        reactionItems: ArrayList<ReactionItem>,
    ) {
        var reactionItems = reactionItems
        if (messages.status != 0) {
//            if (positionPlayer !== -1) {
//                listMessages[positionPlayer].files[0].setStop(true)
//                chatAdapter!!.notifyItemChanged(positionPlayer)
//            }
//            hideKeyboard()
            listReactionAll = ArrayList()
            listReactionSwap = ArrayList()

            reactionItems = reactionItems.stream()
                .filter { x: ReactionItem -> 0 != x.number }
                .collect(Collectors.toList()) as ArrayList<ReactionItem>
            reactionItems.sortWith(NumberComparator())

            bindingReactionClick = ChatRevokeMessageBottomBinding.inflate(layoutInflater)
            val dialog = activityChat?.let { BottomSheetDialog(it, R.style.SheetDialog) }
            //            Dialog dialog = new Dialog(this, R.style.SheetDialog);
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            bindingReactionClick?.root?.let { dialog?.setContentView(it) }
            //            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
//            wmlp.gravity = Gravity.TOP | Gravity.CENTER;
//            wmlp.y = yItem;   //y position
            dialog?.show()
            //            WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(this);
//            bindingReactionClick.rclEmojiRevoke.setLayoutManager(linearLayoutManager);
            userReactionAdapter = activityChat?.let { UserReactionAdapter(it) }
            bindingReactionClick?.rclEmojiRevoke?.layoutManager = LinearLayoutManager(
                requireActivity(),
                RecyclerView.VERTICAL,
                false
            )
            bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
            if (messages.reactions.my_reaction!! > 0) {
                bindingReactionClick?.revokeReaction?.setVisibility(View.VISIBLE)
            } else {
                bindingReactionClick?.revokeReaction?.setVisibility(View.GONE)
            }
            bindingReactionClick?.revokeReaction?.setOnClickListener {
                revokeReactionMessage(messages, group)
                dialog?.dismiss()
            }
            messages.reactions.list_reaction_all?.list_user?.forEachIndexed { index, userReaction ->
                listReactionAll.add(UserReactionChat(userReaction, 0))
            }
            userReactionAdapter!!.setDataSource(listReactionAll)
            //            setMessageChatClickReactionBottom(messages, dialog);
            bindingReactionClick?.txtUserReaction?.setText("${messages.reactions.list_reaction_all?.list_user?.size} đã bày tỏ cảm xúc")
            bindingReactionClick?.txtCountAll?.setText(messages.reactions.reactions_count.toString() + "")
            if (reactionItems.size == 0) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 1) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.lnEmoji2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 2) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.imgEmoji2?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[1]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.txtCount2?.setText(reactionItems[1].number.toString() + "")
                bindingReactionClick?.lnEmoji3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 3) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.imgEmoji2?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[1]
                    )
                }
                bindingReactionClick?.imgEmoji3?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[2]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.txtCount2?.setText(reactionItems[1].number.toString() + "")
                bindingReactionClick?.txtCount3?.setText(reactionItems[2].number.toString() + "")
                bindingReactionClick?.lnEmoji4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 4) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.imgEmoji2?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[1]
                    )
                }
                bindingReactionClick?.imgEmoji3?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[2]
                    )
                }
                bindingReactionClick?.imgEmoji4?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[3]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.txtCount2?.setText(reactionItems[1].number.toString() + "")
                bindingReactionClick?.txtCount3?.setText(reactionItems[2].number.toString() + "")
                bindingReactionClick?.txtCount4?.setText(reactionItems[3].number.toString() + "")
                bindingReactionClick?.lnEmoji5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 5) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.imgEmoji2?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[1]
                    )
                }
                bindingReactionClick?.imgEmoji3?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[2]
                    )
                }
                bindingReactionClick?.imgEmoji4?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[3]
                    )
                }
                bindingReactionClick?.imgEmoji5?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[4]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.txtCount2?.setText(reactionItems[1].number.toString() + "")
                bindingReactionClick?.txtCount3?.setText(reactionItems[2].number.toString() + "")
                bindingReactionClick?.txtCount4?.setText(reactionItems[3].number.toString() + "")
                bindingReactionClick?.txtCount5?.setText(reactionItems[4].number.toString() + "")
                bindingReactionClick?.lnEmoji6?.setVisibility(View.INVISIBLE)
            } else if (reactionItems.size == 6) {
                bindingReactionClick?.lnEmoji1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji3?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji4?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji5?.setVisibility(View.VISIBLE)
                bindingReactionClick?.lnEmoji6?.setVisibility(View.VISIBLE)
                bindingReactionClick?.imgEmoji1?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[0]
                    )
                }
                bindingReactionClick?.imgEmoji2?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[1]
                    )
                }
                bindingReactionClick?.imgEmoji3?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[2]
                    )
                }
                bindingReactionClick?.imgEmoji4?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[3]
                    )
                }
                bindingReactionClick?.imgEmoji5?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[4]
                    )
                }
                bindingReactionClick?.imgEmoji6?.let {
                    ChatUtils.setImageReaction(
                        it,
                        reactionItems[5]
                    )
                }
                bindingReactionClick?.txtCount1?.setText(reactionItems[0].number.toString() + "")
                bindingReactionClick?.txtCount2?.setText(reactionItems[1].number.toString() + "")
                bindingReactionClick?.txtCount3?.setText(reactionItems[2].number.toString() + "")
                bindingReactionClick?.txtCount4?.setText(reactionItems[3].number.toString() + "")
                bindingReactionClick?.txtCount5?.setText(reactionItems[4].number.toString() + "")
                bindingReactionClick?.txtCount6?.setText(reactionItems[5].number.toString() + "")
            }
            bindingReactionClick?.lnAlL?.setOnClickListener { v ->
                bindingReactionClick?.viewAll?.setVisibility(View.VISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
                userReactionAdapter?.setDataSource(listReactionAll)
//                bindingReactionClick?.rclEmojiRevoke.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji1?.setOnClickListener { v ->
                bindingReactionClick?.view1?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
//                bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[0].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji2?.setOnClickListener { v ->
                bindingReactionClick?.view2?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[1].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji3?.setOnClickListener { v ->
                bindingReactionClick?.view3?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[2].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji4?.setOnClickListener { v ->
                bindingReactionClick?.view4?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[3].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji5?.setOnClickListener { v ->
                bindingReactionClick?.view5?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view6?.setVisibility(View.INVISIBLE)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[4].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                bindingReactionClick?.rclEmojiRevoke?.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.lnEmoji6?.setOnClickListener { v ->
                bindingReactionClick?.view6?.setVisibility(View.VISIBLE)
                bindingReactionClick?.viewAll?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view1?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view3?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view4?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view5?.setVisibility(View.INVISIBLE)
                bindingReactionClick?.view2?.setVisibility(View.INVISIBLE)
                listReactionSwap.clear()
                listReactionSwap =
                    reactionItems[5].name?.let { setListUserReaction(it, messages) }!!
                userReactionAdapter?.setDataSource(listReactionSwap)
//                bindingReactionClick?.rclEmojiRevoke.setAdapter(userReactionAdapter)
//                userReactionAdapter.notifyDataSetChanged()
            }
            bindingReactionClick?.likeItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt()
                )
                dialog?.dismiss()
            }
            bindingReactionClick?.loveItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt()
                )
                dialog?.dismiss()
            }
            bindingReactionClick?.angryItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt()
                )
                dialog?.dismiss()
            }
            bindingReactionClick?.smileItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt()
                )
                dialog?.dismiss()
            }
            bindingReactionClick?.wowItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_WOW.toString().toInt()
                )
                dialog?.dismiss()
            }
            bindingReactionClick?.sadItem?.setOnClickListener {
                reactionMessage(
                    messages,
                    group,
                    TechResEnumChat.TYPE_REACTION_SAD.toString().toInt()
                )
                dialog?.dismiss()
            }
        }
    }

    fun setListUserReaction(type: String, message: MessagesByGroup): ArrayList<UserReactionChat> {
        var listReactionAll = ArrayList<UserReactionChat>()
        when (type) {
            TechResEnumChat.TYPE_REACTION_LOVE.toString() -> {
                message.reactions.list_reaction_love?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt()
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_LIKE.toString() -> {
                message.reactions.list_reaction_like?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt()
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_SAD.toString() -> {
                message.reactions.list_reaction_sad?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_SAD.toString().toInt()
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_SMILE.toString() -> {
                message.reactions.list_reaction_smile?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt()
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_ANGRY.toString() -> {
                message.reactions.list_reaction_angry?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt()
                        )
                    )
                }
            }
            TechResEnumChat.TYPE_REACTION_WOW.toString() -> {
                message.reactions.list_reaction_wow?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(
                        UserReactionChat(
                            userReaction,
                            TechResEnumChat.TYPE_REACTION_WOW.toString().toInt()
                        )
                    )
                }
            }
            else -> {
                message.reactions.list_reaction_all?.list_user?.forEachIndexed { index, userReaction ->
                    listReactionAll.add(UserReactionChat(userReaction, 0))
                }
            }
        }
        return listReactionAll
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

    override fun onOpenFile(path: String?) {
        path?.let {
            openDocument(activityChat, it)
        }
    }

    override fun onDeleteDownLoadFile(nameFile: String?) {
        if (nameFile != null) {
            getStorageDir(nameFile)?.let {
                File(it).delete()
            }
        }
    }

    override fun onAddMember() {
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

    override fun onChooseAvatarBusinessCard(userId: Int) {
        activityChat?.toProfile(userId)
    }

    override fun onChatBusinessCard(phoneMessage: PhoneMessage) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(phoneMessage.member_id ?: 0)
        createGroupPersonal(memberList, phoneMessage.member_id ?: 0, 0)
    }

    override fun onCallBusinessCard(phoneMessage: PhoneMessage) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(phoneMessage.member_id ?: 0)
        createGroupPersonal(memberList, phoneMessage.member_id ?: 0, 1)
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
        isKeyBoard = false
        toolsChat.binding.layoutKeyboard.onBackPressed()
        closeKeyboard(toolsChat.binding.edtMessageChat)
    }

    override fun onChooseCallVideo(messagesByGroup: MessagesByGroup) {

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
                }
            }
        }
    }

    override fun onShareMessage(messagesByGroup: MessagesByGroup) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.SHARE_MESSAGE.toString(), Gson().toJson(messagesByGroup))
        bundle.putString(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(group))
        val shareMessageGroupFragment = ShareMessageFragment()
        shareMessageGroupFragment.arguments = bundle
        activityChat?.addOnceFragment(this, shareMessageGroupFragment)
    }

    override fun onIntentSendBirthDayCard(messagesByGroup: MessagesByGroup) {

    }

    override fun onReviewInviteCard(messagesByGroup: MessagesByGroup) {

    }

    override fun onCallBack(bundle: Bundle) {
        val isCheckBackground = bundle.getString(TechresEnum.LINK_BACKGROUND.toString())
        val avatar = bundle.getString(TechresEnum.AVATAR_GROUP.toString())
        val nameGroup = bundle.getString(TechresEnum.NAME_GROUP.toString())
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
        if (avatar != null) {
            getAvatarGroup(toolsChat.binding.imgAvatarChat, avatar, configNodeJs)
            group.avatar = avatar
        }
        if (nameGroup != null) {
            toolsChat.binding.tvTitleChat.text = nameGroup
            group.name = nameGroup
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
        val intent = Intent(requireActivity(), MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onVideoMore(imgList: ArrayList<FileNodeJs>, position: Int) {
        var url = ArrayList<String>()
        closeKeyboard(toolsChat.binding.inputSearch)
        imgList.forEach {
            val linkOriginal =
                FileUtils.getInternalStogeDir(it.name_file ?: "", requireActivity()) ?: ""
            if (File(linkOriginal).exists())
                url.add(linkOriginal)
        }
        var intent = Intent(requireActivity(), MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        intent.putExtra("TYPE", "chat")
        startActivity(intent)
    }

    override fun onRevokeImageMore(messagesByGroup: MessagesByGroup, view: View) {
        bottomSheetReaction(messagesByGroup)
    }

    override fun getDataEmoji(code: String) {
        if (code.isNotEmpty()) {
            toolsChat.binding.edtMessageChat.append(code)
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onTagName(member: Members) {
        // add tag name
        val tagName = TagName()
        val base64 = Base64.encodeToString(
            (member.member_id.toString() + member.full_name).toByteArray(charset("UTF-8")),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
        tagName.avatar = member.avatar?.medium
        tagName.full_name = member.full_name
        tagName.member_id = member.member_id
        tagName.key_tag_name = "@$base64"
        listTagName.add(tagName)
        //find tag name and replace before add tag name
        val lengthTagName = toolsChat.binding.edtMessageChat.text.toString().length
        val arrayString = toolsChat.binding.edtMessageChat.text.toString().split("\\s".toRegex())

        val index = arrayString[arrayString.size - 1].indexOfLast { it == '@' }
        val indexEnd = if (index == -1) {
            arrayString[arrayString.size - 1].length
        } else {
            arrayString[arrayString.size - 1].length - index
        }
        var textMessage =
            toolsChat.binding.edtMessageChat.text.toString().substring(0, lengthTagName - indexEnd)

        //add tag name
        val nameTagName = String.format(
            "%s%s", "@", tagName.full_name ?: ""
        )

        textMessage = if (arrayString.size == 1) {
            if (index == -1) {
                textMessage = ""
            }
            String.format("%s%s ", textMessage, nameTagName)
        } else {
            String.format("%s %s ", textMessage, nameTagName)
        }
        WriteLog.d("setTagName", Gson().toJson(listTagName) + "")
        toolsChat.binding.edtMessageChat.text =
            Editable.Factory.getInstance().newEditable(textMessage)
        toolsChat.binding.edtMessageChat.setTagName(listTagName, R.color.blue_tag_name)
        toolsChat.binding.edtMessageChat.setSelection(toolsChat.binding.edtMessageChat.text.toString().length)
        toolsChat.binding.utilities.ctlTagUserChat.hide()
    }

    override fun onChooseTagName(tagName: TagName) {
        activityChat?.toProfile(tagName.member_id ?: 0)
    }

    override fun onChooseNameUser(sender: Sender) {
        activityChat?.toProfile(sender.member_id ?: 0)
    }

    override fun onGiPhy(media: Media) {

    }

    override fun closeGiPhy() {
        toolsChat.binding.imgEmoji.setImageResource(R.drawable.ic_sticker_tab)
    }

    override fun onBackPressGiPhy() {
        onBackPress()
    }

    override fun onScreenCaptured(path: String?) {
        if (!isChooseImage) {
            if (listScreenShot == null) {
                listScreenShot = ArrayList<String>()
            }
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

    override fun onDestroy() {
        super.onDestroy()
//        chatAdapter = null
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
        activityChat?.addOnceFragment(this, messageImportantFragment)
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
        Utils.getGlide(toolsChat.binding.imgBackgroundChat, urlBackground, configNodeJs)
    }

    override fun onResumeChat() {
//        chatAdapter?.startVideo()
//        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onPauseChat() {
//        typingOff(group)
//        stopMedia()
//        chatAdapter?.stopPlayer()
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
//        stopMedia()
//        chatAdapter?.stopPlayer()
//        toolsChat.binding.layoutKeyboard.onBackPressed()
//        closeKeyboard(toolsChat.binding.edtMessageChat)
//        if (listMessages.size > 0) {
//            chatAdapter?.stopVideo()
//        }
//        if (recorder != null) {
//            stopDrawing()
//        }
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
            leaveGroup()
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
            "/api/messages/pagination?group_id=",
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
            .getMessages(baseRequest)
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
                        if (listMessages.size > 0) {
                            listMessages.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }
                        isLoading = false
                    }
                }
            })
    }

    private fun getMessagePre(
        group_id: String,
        pre_curcors: String,
        random_key: String
    ) {
        isLoading = true
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages/pagination?group_id=",
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
            .getMessages(baseRequest)
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
                        var index = listMessages.size + 1
                        if (!is_oldest_message)
                            is_oldest_message = response.data.is_oldest_message
                        pre_cursor = response.data.pagination.pre_cursor
                        listMessages.addAll(data)
//                        chatAdapter?.setDataSource(listMessages)
                        chatAdapter?.notifyItemRangeInserted(index, listMessages.size)
                    }
                    isLoading = false
                }
            })
    }

    private fun getMessageNext(
        group_id: String,
        next_cursors: String,
        random_key: String
    ) {
        isLoading = true
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api/messages/pagination?group_id=",
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
            .getMessages(baseRequest)
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
                        if (!is_latest_message)
                            is_latest_message = response.data.is_latest_message
                        next_cursor = response.data.pagination.next_cursor
                        listMessages.addAll(0, data)
//                        chatAdapter?.setDataSource(listMessages)
                        chatAdapter?.notifyItemRangeInserted(0, data.size)
                    }
                    isLoading = false
                }
            })
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
                            requireContext().resources.getString(R.string.title_permission),
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
                            requireContext(),
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
            "/api/messages/pagination?group_id=",
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
            .getMessages(baseRequest)
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
                        isScrollData = true;
                        is_latest_message = response.data.is_latest_message
                        is_oldest_message = response.data.is_oldest_message
                        pre_cursor = response.data.pagination.pre_cursor
                        next_cursor = response.data.pagination.next_cursor
                        listMessages.clear()
                        listMessages.addAll(response.data.list)
                        chatAdapter?.notifyDataSetChanged()
                        if (listMessages.size > 0) {
                            toolsChat.binding.lnNoMessage.hide()
                        } else {
                            toolsChat.binding.lnNoMessage.show()
                        }
                        listMessages.forEachIndexed { index, messagesByGroup ->
                            if (messagesByGroup.random_key?.contains(random_key) == true) {
                                toolsChat.binding.rcvChat.smoothScrollToPosition(index)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    listMessages[index].is_stroke = 1
                                    chatAdapter?.notifyItemChanged(index)
                                    stroke = true
                                }, 500)
                                return@forEachIndexed
                            }
                        }
                        toolsChat.binding.tvLoadingMessage.hide()
                        if (listMessages.size > 0) {
                            listMessages.forEach {
                                if (it.message_type == TechResEnumChat.TYPE_FILE.toString()) {
                                    TasksManager().getImpl().addMessage(it)
                                }
                            }
                        }
                    }
                    isLoading = false
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
            TechResApplication.messageDao?.getLastMessage(groupId, user.id) ?: MessagesByGroup()
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
        if (idPinnedMessage != "") {
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
        toolsChat.binding.inputSearch.isFocusable = false
        toolsChat.binding.inputSearch.requestFocus()
        showInputMethod(toolsChat.binding.inputSearch)
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