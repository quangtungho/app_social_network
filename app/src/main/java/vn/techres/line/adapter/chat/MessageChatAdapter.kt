package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import kohii.v1.core.Manager
import kohii.v1.exoplayer.Kohii
import org.jetbrains.annotations.NotNull
import vn.techres.line.R
import vn.techres.line.adapter.account.LoadImageChatAdapter
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.*
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.checkSenderMessage
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.holder.message.*
import vn.techres.line.holder.message.left.*
import vn.techres.line.holder.message.right.*
import vn.techres.line.interfaces.ChooseOrderCustomerHandler
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import vn.techres.line.interfaces.gift.ChatGiftHandler
import java.io.File
import java.io.IOException

class MessageChatAdapter(
    var context: Context,
    var manager: Manager,
    var kohii: Kohii
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Handler.Callback,
    SeekBar.OnSeekBarChangeListener {
    private var mediaPlayer: MediaPlayer? = null
    private var seekHandler = Handler(this)
    var playingPosition = -1
    private var playingHolder: RecyclerView.ViewHolder? = null
    private var dataSource = ArrayList<MessagesByGroup>()
    private var revokeMessageHandler: RevokeMessageHandler? = null
    private var chatGroupHandler: ChatGroupHandler? = null
    private var adapter: LoadImageChatAdapter? = null
    private var imageMoreChatHandler: ImageMoreChatHandler? = null
    private var chooseNameUserHandler: ChooseNameUserHandler? = null
    private var utilitiesChatHandler: UtilitiesChatHandler? = null
    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var chooseOrderDetail: ChooseOrderCustomerHandler? = null
    private var chatGiftHandler: ChatGiftHandler? = null
    var positionStroke = -1
    fun setChooseGiftNotification(chatGiftHandler: ChatGiftHandler) {
        this.chatGiftHandler = chatGiftHandler
    }

    fun setChooseOrderDetail(chooseOrderDetail: ChooseOrderCustomerHandler) {
        this.chooseOrderDetail = chooseOrderDetail
    }

    fun setUtilitiesChatHandler(utilitiesChatHandler: UtilitiesChatHandler?) {
        this.utilitiesChatHandler = utilitiesChatHandler
    }

    fun setImageMoreChatHandler(imageMoreChatHandler: ImageMoreChatHandler) {
        this.imageMoreChatHandler = imageMoreChatHandler
    }

    fun setChatGroupHandler(chatGroupHandler: ChatGroupHandler) {
        this.chatGroupHandler = chatGroupHandler
    }

    fun setRevokeMessageHandler(revokeMessageHandler: RevokeMessageHandler) {
        this.revokeMessageHandler = revokeMessageHandler
    }

    fun setChooseTagNameHandler(chooseNameUserHandler: ChooseNameUserHandler) {
        this.chooseNameUserHandler = chooseNameUserHandler
    }


    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(newDataSource: ArrayList<MessagesByGroup>) {
//        val diffCallback = MessageDiffCallback(this.dataSource, newDataSource)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        diffResult.dispatchUpdatesTo(this)
//        dataSource.clear()
//        this.dataSource.addAll(newDataSource)
        this.dataSource = newDataSource
        notifyDataSetChanged()
    }

    fun startVideo() {
        kohii.unlockManager(manager)
        notifyDataSetChanged()
    }

    fun stopVideo() {
        kohii.lockManager(manager)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSource[position].random_key.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_MY -> {
                TextMessageRightHolder(
                    ItemTextChatRightBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_TEXT_THEIR -> {
                TextMessageLeftHolder(
                    ItemTextChatLeftBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_IMAGE_MY -> {
                ImageMessageRightHolder(
                    ItemImageChatRightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_IMAGE_THEIR -> {
                ImageMessageLeftHolder(
                    ItemImageChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_STICKER_MY -> {
                StickerMessageRightHolder(
                    ItemStickerChatRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_STICKER_THEIR -> {
                StickerMessageLeftHolder(
                    ItemStickerChatLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_VIDEO_MY -> {
                VideoMessageRightHolder(
                    ItemVideoChatRightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_VIDEO_THEIR -> {
                VideoMessageLeftHolder(
                    ItemVideoChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_REPLY_MY -> {
                ReplyMessageRightHolder(
                    ItemReplyChatRightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_REPLY_THEIR -> {
                ReplyMessageLeftHolder(
                    ItemReplyChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_AUDIO_MY -> {
                AudioMessageRightHolder(
                    ItemAudioRightChatBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_AUDIO_THEIR -> {
                AudioMessageLeftHolder(
                    ItemAudioLeftChatBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_REVOKE_MY -> {
                RevokeMessageRightHolder(
                    ItemRevokeChatRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_REVOKE_THEIR -> {
                RevokeMessageLeftHolder(
                    ItemRevokeChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_NOTIFICATION -> {
                NotificationMessageHolder(
                    ItemNotificationChatBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            TYPE_CREATE_GROUP -> {
                CreateGroupMessageHolder(
                    ItemCreateGroupMessageBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            TYPE_CREATE_GROUP_PERSONAL -> {
                CreateGroupPersonalMessageHolder(
                    ItemCreatePersonalMessageBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_FILE_MY -> {
                FileMessageRightHolder(
                    ItemFileChatRightBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_FILE_THEIR -> {
                FileMessageLeftHolder(
                    ItemFileChatLeftBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_ADD_MEMBER -> {
                AddMemberMessageHolder(
                    ItemAddMemberMessageBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            TYPE_PINNED -> {
                PinnedMessageHolder(
                    ItemPinnedMessageBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_UTILS -> {
                UtilsMessageHolder(
                    ItemUtilsMessageBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_MEMBER -> {
                MemberMessageHolder(
                    ItemAuthorizeMessageBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            TYPE_GI_PHY_MY -> {
                GiPhyMessageRightHolder(
                    ItemGiphyChatRightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_GI_PHY_THEIR -> {
                GiPhyMessageLeftHolder(
                    ItemGiphyChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_BUSINESS_CARD_MY -> {
                BusinessCardRightHolder(
                    ItemBusinessCardMessageRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_BUSINESS_CARD_THEIR -> {
                BusinessCardLeftHolder(
                    ItemBusinessCardMessageLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_LINK_MY -> {
                LinkMessageRightHolder(
                    ItemLinkChatRightBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_LINK_THEIR -> {
                LinkMessageLeftHolder(
                    ItemLinkChatLeftBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_PROMOTED_VICE_GROUP -> {
//                PromotedViceGroupMessageHolder(
//                    ItemPromotedViceGroupChatBinding.inflate(
//                        LayoutInflater.from(parent.context), parent, false
//                    )
//                )
                NotificationMessageHolder(
                    ItemNotificationChatBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            TYPE_DEMOTED_VICE_GROUP -> {
//                DemotedViceGroupMessageHolder(
//                    ItemDemotedViceGroupChatBinding.inflate(
//                        LayoutInflater.from(
//                            parent.context
//                        ), parent, false
//                    )
//                )
                NotificationMessageHolder(
                    ItemNotificationChatBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            TYPE_VIDEO_CALL_MY -> {
                VideoCallMessageRightHolder(
                    ItemVideoCallChatRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_VIDEO_CALL_THEIR -> {
                VideoCallMessageLeftHolder(
                    ItemVideoCallChatLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_ADVERTISEMENT_MY -> {
                AdvertisementMessageRight(
                    ItemAdvertisementMessageRightBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            TYPE_ADVERTISEMENT_THEIR -> {
                AdvertisementMessageLeft(
                    ItemAdvertisementMessageLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_NOTIFICATION_BIRTH_DAY -> {
                NotificationHappyBirthDayMessageHolder(
                    ItemNotificationHappyBirthdayBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_INVITE_EVENT_MY -> {
                InviteEventMessageRightHolder(
                    ItemInviteEventCardRightChatBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_INVITE_EVENT_THEIR -> {
                InviteEventMessageLeftHolder(
                    ItemInviteEventCardLeftChatBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_COMPLETE_BILL -> {
                NotificationCompleteBillHolder(
                    ItemNotificationCompleteBillBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_GIFT_NOTIFICATION -> {
                ChatGiftNotificationHolder(
                    ItemChatGiftNotificationBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_GETTINGS -> {
                GettingChatHolder(
                    ItemGettingChatBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_MESSAGE_INFORMATION -> {
                ChatGiftNotificationHolder(
                    ItemChatGiftNotificationBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            else -> {
                NotificationMessageHolder(
                    ItemNotificationChatBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(@NotNull holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            chatGroupHandler!!.onClickHideKeyboard()
        }
        when (getItemViewType(position)) {
            TYPE_TEXT_MY -> {
                (holder as TextMessageRightHolder).bind(
                    context,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_TEXT_THEIR -> {
                (holder as TextMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    user,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_IMAGE_MY -> {
                (holder as ImageMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    adapter,
                    chatGroupHandler,
                    revokeMessageHandler,
                    imageMoreChatHandler
                )
            }
            TYPE_IMAGE_THEIR -> {
                (holder as ImageMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    adapter,
                    chatGroupHandler,
                    revokeMessageHandler,
                    imageMoreChatHandler
                )
            }
            TYPE_STICKER_MY -> {
                (holder as StickerMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_STICKER_THEIR -> {
                (holder as StickerMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_VIDEO_MY -> {
                (holder as VideoMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    imageMoreChatHandler
                )
            }
            TYPE_VIDEO_THEIR -> {
                (holder as VideoMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    imageMoreChatHandler
                )
            }
            TYPE_REPLY_MY -> {
                (holder as ReplyMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_REPLY_THEIR -> {
                (holder as ReplyMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_AUDIO_MY -> {
                (holder as AudioMessageRightHolder).bind(
                    context,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler,
                    configNodeJs
                )
                holder.binding.seekBarAudio.setOnSeekBarChangeListener(this)
                if (position != playingPosition) {
                    updateNonPlayingView(holder)
                } else {
                    updatePlayingView()
                }
                holder.binding.imgPlayAudio.setOnClickListener {
                    if (holder.bindingAdapterPosition == playingPosition) {
                        if (mediaPlayer?.isPlaying == true) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                        updatePlayingView()
                    } else {
                        holder.binding.pbLoading.show()
                        holder.binding.imgPlayAudio.isEnabled = false
                        if (mediaPlayer != null && playingPosition != -1) {
                            playingHolder?.let {
                                updateNonPlayingView(it, dataSource[playingPosition])
                            }
                            mediaPlayer?.release()
                        }
                        playingPosition = holder.bindingAdapterPosition
                        playingHolder = holder
                        startMediaPlayer(dataSource[playingPosition])
                    }
                }
            }
            TYPE_AUDIO_THEIR -> {
                (holder as AudioMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
                holder.binding.seekBarAudio.setOnSeekBarChangeListener(this)
                if (position != playingPosition) {
                    updateNonPlayingView(holder)
                } else {
                    updatePlayingView()
                }
                holder.binding.imgPlayAudio.setOnClickListener {
                    if (holder.bindingAdapterPosition == playingPosition && mediaPlayer != null) {
                        if (mediaPlayer?.isPlaying == true) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                        updatePlayingView()
                    } else {
                        holder.binding.pbLoading.show()
                        holder.binding.imgPlayAudio.isEnabled = false
                        if (mediaPlayer != null && playingPosition != -1) {
                            playingHolder?.let {
                                if (dataSource[playingPosition].message_type.equals(TechResEnumChat.TYPE_AUDIO.toString()))
                                    updateNonPlayingView(it, dataSource[playingPosition])
                            }
                            mediaPlayer?.release()
                        }
                        playingPosition = holder.bindingAdapterPosition
                        playingHolder = holder
                        startMediaPlayer(dataSource[position])
                    }
                }
            }
            TYPE_REVOKE_MY -> {
                (holder as RevokeMessageRightHolder).bind(
                    context,
                    holder.bindingAdapterPosition,
                    dataSource
                )
            }
            TYPE_REVOKE_THEIR -> {
                (holder as RevokeMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource
                )
            }
            TYPE_NOTIFICATION -> {
                (holder as NotificationMessageHolder).bin(
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chooseNameUserHandler
                )
            }
            TYPE_CREATE_GROUP -> {
                (holder as CreateGroupMessageHolder).bind(
                    context,
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition],
                    chatGroupHandler,
                    utilitiesChatHandler
                )
            }
            TYPE_CREATE_GROUP_PERSONAL -> {
                (holder as CreateGroupPersonalMessageHolder).bind(
                    context,
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition],
                    utilitiesChatHandler
                )
            }
            TYPE_FILE_MY -> {
                (holder as FileMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    holder.bindingAdapterPosition,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_FILE_THEIR -> {
                (holder as FileMessageLeftHolder).bind(
                    context,
                    user,
                    configNodeJs,
                    dataSource,
                    holder.bindingAdapterPosition,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_PINNED -> {
                (holder as PinnedMessageHolder).bind(
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition],
                    chooseNameUserHandler
                )
            }
            TYPE_ADD_MEMBER -> {
                (holder as AddMemberMessageHolder).bind(
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition],
                    chooseNameUserHandler
                )
            }
            TYPE_UTILS -> {
                (holder as UtilsMessageHolder).bind(configNodeJs, dataSource[position])
            }
            TYPE_MEMBER -> {
                (holder as MemberMessageHolder).bind(
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition],
                    chooseNameUserHandler
                )
            }
            TYPE_GI_PHY_MY -> {
                (holder as GiPhyMessageRightHolder).bind(
                    dataSource[holder.bindingAdapterPosition],
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_GI_PHY_THEIR -> {
                (holder as GiPhyMessageLeftHolder).bind(
                    configNodeJs,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_BUSINESS_CARD_MY -> {
                (holder as BusinessCardRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    position,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_BUSINESS_CARD_THEIR -> {
                (holder as BusinessCardLeftHolder).bind(
                    context,
                    configNodeJs,
                    user,
                    dataSource,
                    position,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_LINK_MY -> {
                (holder as LinkMessageRightHolder).bind(
                    context,
                    dataSource,
                    position,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_LINK_THEIR -> {
                (holder as LinkMessageLeftHolder).bind(
                    context,
                    user,
                    configNodeJs,
                    dataSource,
                    position,
                    chatGroupHandler,
                    revokeMessageHandler,
                    chooseNameUserHandler
                )
            }
            TYPE_PROMOTED_VICE_GROUP -> {
//                (holder as PromotedViceGroupMessageHolder)
                (holder as NotificationMessageHolder).bin(
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chooseNameUserHandler
                )
            }
            TYPE_DEMOTED_VICE_GROUP -> {
//                (holder as DemotedViceGroupMessageHolder)
                (holder as NotificationMessageHolder).bin(
                    configNodeJs,
                    holder.bindingAdapterPosition,
                    dataSource,
                    chooseNameUserHandler
                )
            }
            TYPE_VIDEO_CALL_MY -> {
                (holder as VideoCallMessageRightHolder).bind(
                    context,
                    user,
                    dataSource,
                    chatGroupHandler
                )
            }
            TYPE_VIDEO_CALL_THEIR -> {
                (holder as VideoCallMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    user,
                    dataSource,
                    chatGroupHandler
                )
            }
            TYPE_ADVERTISEMENT_MY -> {
                (holder as AdvertisementMessageRight).bind(
                    context,
                    position,
                    dataSource,
                    chatGroupHandler
                )
            }
            TYPE_ADVERTISEMENT_THEIR -> {
                (holder as AdvertisementMessageLeft).bind(
                    context,
                    position,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_NOTIFICATION_BIRTH_DAY -> {
                chatGroupHandler?.let {
                    (holder as NotificationHappyBirthDayMessageHolder).bind(
                        context,
                        dataSource,
                        position,
                        configNodeJs,
                        it
                    )
                }
            }
            TYPE_INVITE_EVENT_MY -> {
                (holder as InviteEventMessageRightHolder).bind(
                    context,
                    configNodeJs,
                    position,
                    dataSource,
                    chatGroupHandler,
                    revokeMessageHandler
                )
            }
            TYPE_INVITE_EVENT_THEIR -> {
                (holder as InviteEventMessageLeftHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    position,
                    chatGroupHandler
                )
            }
            TYPE_COMPLETE_BILL -> {
                chooseOrderDetail?.let {
                    (holder as NotificationCompleteBillHolder).bind(
                        configNodeJs,
                        dataSource[holder.bindingAdapterPosition],
                        it
                    )
                }
            }
            TYPE_GIFT_NOTIFICATION -> {
                chatGiftHandler?.let {
                    (holder as ChatGiftNotificationHolder).bind(
                        configNodeJs,
                        dataSource[holder.bindingAdapterPosition],
                        it,
                        TechResEnumChat.TYPE_GIFT_NOTIFICATION.toString()
                    )
                }
            }
            TYPE_GETTINGS -> {
                (holder as GettingChatHolder).bind(
                    configNodeJs,
                    dataSource[holder.bindingAdapterPosition]
                )
            }
            TYPE_MESSAGE_INFORMATION -> {
                chatGiftHandler?.let {
                    (holder as ChatGiftNotificationHolder).bind(
                        configNodeJs,
                        dataSource[holder.bindingAdapterPosition],
                        it,
                        TechResEnumChat.TYPE_MESSAGE_INFORMATION.toString()
                    )
                }
            }
        }
    }

//    override fun onBindViewHolder(
//        holder: RecyclerView.ViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if(payloads.isEmpty()){
//            super.onBindViewHolder(holder, position, payloads)
//        }else{
//            val set = payloads.firstOrNull() as Set<MessagesByGroup>
//            set.forEach {
//                when(it.message_type){
//                    TechresEnumChat.TYPE_TEXT.toString() ->{
//                        if (checkSenderMessage(it, user)) {
//                            (holder as TextMessageRightHolder).bind(
//                                context,
//                                configNodeJs,
//                                holder.bindingAdapterPosition,
//                                dataSource,
//                                chatGroupHandler,
//                                revokeMessageHandler,
//                                chooseNameUserHandler
//                            )
//                        } else {
//                            (holder as TextMessageLeftHolder).bind(
//                                context,
//                                configNodeJs,
//                                user,
//                                holder.bindingAdapterPosition,
//                                dataSource,
//                                chatGroupHandler,
//                                revokeMessageHandler,
//                                chooseNameUserHandler
//                            )
//                        }
//                    }
//                    TechresEnumChat.TYPE_IMAGE.toString() ->{
//                        if (checkSenderMessage(it, user)) {
//                            (holder as ImageMessageRightHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        } else {
//                            (holder as ImageMessageLeftHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        }
//                    }
//                    TechresEnumChat.TYPE_VIDEO.toString() ->{
//                        if (checkSenderMessage(it, user)) {
//                            (holder as VideoMessageRightHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        } else {
//                            (holder as VideoMessageLeftHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        }
//                    }
//                    TechresEnumChat.TYPE_AUDIO.toString() ->{
//                        if (checkSenderMessage(it, user)) {
//                            (holder as AudioMessageRightHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        } else {
//                            (holder as AudioMessageLeftHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        }
//                    }
//                    TechresEnumChat.TYPE_REPLY.toString() ->{
//                        if (checkSenderMessage(it, user)) {
//                            (holder as ReplyMessageRightHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        } else {
//                            (holder as ReplyMessageLeftHolder)
//                            setReaction(holder.binding.lnReactionContainer, holder.binding.reaction, it)
//                        }
//                    }
//                }
//            }
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        val message = dataSource[position]
        return when (message.message_type) {
            TechResEnumChat.TYPE_TEXT.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_TEXT_MY
                } else {
                    TYPE_TEXT_THEIR
                }
            }
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_IMAGE_MY
                } else {
                    TYPE_IMAGE_THEIR
                }
            }
            TechResEnumChat.TYPE_STICKER.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_STICKER_MY
                } else {
                    TYPE_STICKER_THEIR
                }
            }
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_VIDEO_MY
                } else {
                    TYPE_VIDEO_THEIR
                }
            }
            TechResEnumChat.TYPE_REPLY.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_REPLY_MY
                } else {
                    TYPE_REPLY_THEIR
                }
            }
            TechResEnumChat.TYPE_AUDIO.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_AUDIO_MY
                } else {
                    TYPE_AUDIO_THEIR
                }
            }
            TechResEnumChat.TYPE_REVOKE.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_REVOKE_MY
                } else {
                    TYPE_REVOKE_THEIR
                }
            }
            TechResEnumChat.TYPE_BACKGROUND.toString(), TechResEnumChat.TYPE_MEMBER_LEAVE_GROUP.toString(),
            TechResEnumChat.TYPE_AVATAR.toString(), TechResEnumChat.TYPE_GROUP_NAME.toString()
            -> {
                TYPE_NOTIFICATION
            }
            TechResEnumChat.TYPE_CREATE_GROUP.toString() -> {
                TYPE_CREATE_GROUP
            }
            TechResEnumChat.TYPE_CREATE_GROUP_PERSONAL.toString() -> {
                TYPE_CREATE_GROUP_PERSONAL
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_FILE_MY
                } else {
                    TYPE_FILE_THEIR
                }
            }
            TechResEnumChat.TYPE_GI_PHY.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_GI_PHY_MY
                } else {
                    TYPE_GI_PHY_THEIR
                }
            }
            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_BUSINESS_CARD_MY
                } else {
                    TYPE_BUSINESS_CARD_THEIR
                }
            }
            TechResEnumChat.TYPE_REMOVE_PINNED.toString(), TechResEnumChat.TYPE_PINNED.toString() -> {
                TYPE_PINNED
            }
            TechResEnumChat.TYPE_ADD_NEW_USER.toString() -> {
                TYPE_ADD_MEMBER
            }
            TechResEnumChat.TYPE_AUTHORIZE.toString(), TechResEnumChat.TYPE_KICK_MEMBER.toString() -> {
                TYPE_MEMBER
            }
            TechResEnumChat.TYPE_TYPING.toString(), TechResEnumChat.TYPE_LOAD_PAGE.toString() -> {
                TYPE_UTILS
            }
            TechResEnumChat.TYPE_LINK.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_LINK_MY
                } else {
                    TYPE_LINK_THEIR
                }
            }
            TechResEnumChat.TYPE_PROMOTE_VICE.toString() -> {
                TYPE_PROMOTED_VICE_GROUP
            }
            TechResEnumChat.TYPE_DEMOTED_VICE.toString() -> {
                TYPE_DEMOTED_VICE_GROUP
            }
            TechResEnumChat.TYPE_ADVERTISEMENT.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_ADVERTISEMENT_MY
                } else {
                    TYPE_ADVERTISEMENT_THEIR
                }
            }
            TechResEnumVideoCall.TYPE_CALL_PHONE.toString(), TechResEnumVideoCall.TYPE_CALL_VIDEO.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_VIDEO_CALL_MY
                } else {
                    TYPE_VIDEO_CALL_THEIR
                }
            }
            TechResEnumChat.TYPE_NOTIFICATION_BIRTHDAY.toString() -> {
                TYPE_NOTIFICATION_BIRTH_DAY
            }
            TechResEnumChat.TYPE_INVITE_EVENT.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_INVITE_EVENT_MY
                } else {
                    TYPE_INVITE_EVENT_THEIR
                }
            }
            TechResEnumChat.TYPE_COMPLETE_BILL.toString() -> {
                TYPE_COMPLETE_BILL
            }

            TechResEnumChat.TYPE_GIFT_NOTIFICATION.toString() -> {
                TYPE_GIFT_NOTIFICATION
            }

            TechResEnumChat.TYPE_GETTINGS.toString() -> {
                TYPE_GETTINGS
            }

            TechResEnumChat.TYPE_MESSAGE_INFORMATION.toString() -> {
                TYPE_MESSAGE_INFORMATION
            }

            TechResEnumChat.TYPE_SHARE.toString() -> {
                when (dataSource[position].message_share?.message_type) {
                    TechResEnumChat.TYPE_TEXT.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_TEXT_MY
                        } else {
                            TYPE_TEXT_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_IMAGE.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_IMAGE_MY
                        } else {
                            TYPE_IMAGE_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_STICKER.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_STICKER_MY
                        } else {
                            TYPE_STICKER_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_VIDEO.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_VIDEO_MY
                        } else {
                            TYPE_VIDEO_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_REPLY.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_REPLY_MY
                        } else {
                            TYPE_REPLY_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_AUDIO.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_AUDIO_MY
                        } else {
                            TYPE_AUDIO_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_LINK.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_LINK_MY
                        } else {
                            TYPE_LINK_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_BUSINESS_CARD_MY
                        } else {
                            TYPE_BUSINESS_CARD_THEIR
                        }
                    }
                    TechResEnumChat.TYPE_FILE.toString() -> {
                        if (checkSenderMessage(message, user)) {
                            TYPE_FILE_MY
                        } else {
                            TYPE_FILE_THEIR
                        }
                    }
                    else -> {
                        -1
                    }
                }
            }
            else -> {
                -1
            }
        }
    }

    private fun startMediaPlayer(message: MessagesByGroup) {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        try {
            val path =
                FileUtils.getInternalStogeDir(message.files[0].name_file ?: "", context) ?: ""
            if (!File(path).exists()) {
                mediaPlayer?.setDataSource(
                    ChatUtils.getUrl(
                        message.files[0].link_original,
                        configNodeJs
                    )
                )
            } else {
                mediaPlayer?.setDataSource(path)
            }
            mediaPlayer?.prepare()
            mediaPlayer?.setOnPreparedListener {
                it.start()
                updatePlayingView()
            }

            mediaPlayer?.setOnCompletionListener {
                releaseMediaPlayer()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updatePlayingView() {
        when (playingHolder) {
            is AudioMessageRightHolder -> {
                (playingHolder as AudioMessageRightHolder).binding.seekBarAudio.max =
                    mediaPlayer?.duration ?: 0
                (playingHolder as AudioMessageRightHolder).binding.seekBarAudio.progress =
                    mediaPlayer?.currentPosition ?: 0
                (playingHolder as AudioMessageRightHolder).binding.seekBarAudio.isEnabled = true

                if (mediaPlayer?.isPlaying == true) {
                    (playingHolder as AudioMessageRightHolder).binding.pbLoading.hide()
                    (playingHolder as AudioMessageRightHolder).binding.imgPlayAudio.isEnabled = true
                    seekHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                    (playingHolder as AudioMessageRightHolder).binding.imgPlayAudio.setImageResource(
                        R.drawable.ic_pause_chat
                    )
                } else {
                    (playingHolder as AudioMessageRightHolder).binding.imgPlayAudio.setImageResource(
                        R.drawable.ic_play_button
                    )
                }
            }
            is AudioMessageLeftHolder -> {
                (playingHolder as AudioMessageLeftHolder).binding.seekBarAudio.max =
                    mediaPlayer?.duration ?: 0
                (playingHolder as AudioMessageLeftHolder).binding.seekBarAudio.progress =
                    mediaPlayer?.currentPosition ?: 0
                (playingHolder as AudioMessageLeftHolder).binding.seekBarAudio.isEnabled = true
                if (mediaPlayer?.isPlaying == true) {
                    (playingHolder as AudioMessageLeftHolder).binding.pbLoading.hide()
                    (playingHolder as AudioMessageLeftHolder).binding.imgPlayAudio.isEnabled = true
                    seekHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                    (playingHolder as AudioMessageLeftHolder).binding.imgPlayAudio.setImageResource(
                        R.drawable.ic_pause_chat
                    )
                } else {
                    (playingHolder as AudioMessageLeftHolder).binding.imgPlayAudio.setImageResource(
                        R.drawable.ic_play_button
                    )
                }
            }
        }
    }

    private fun updateNonPlayingView(
        holder: RecyclerView.ViewHolder,
        message: MessagesByGroup? = null
    ) {
        when (holder) {
            is AudioMessageRightHolder -> {
                if (holder == playingHolder) {
                    seekHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
                }
                holder.binding.seekBarAudio.isEnabled = false
                holder.binding.seekBarAudio.progress = 0
                holder.binding.imgPlayAudio.setImageResource(R.drawable.ic_play_button)
                message?.let {
                    holder.binding.tvTimePlayAudio.text =
                        if (it.message_share != null && !StringUtils.isNullOrEmpty(it.message_share?._id)) {
                            TimeFormatHelper.calculateDuration(
                                it.message_share?.files?.get(0)?.time ?: 0
                            )
                        } else {
                            TimeFormatHelper.calculateDuration(it.files[0].time ?: 0)
                        }
                }
            }
            is AudioMessageLeftHolder -> {
                if (holder == playingHolder) {
                    seekHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
                }
                holder.binding.seekBarAudio.isEnabled = false
                holder.binding.seekBarAudio.progress = 0
                holder.binding.imgPlayAudio.setImageResource(R.drawable.ic_play_button)
                message?.let {
                    holder.binding.tvTimePlayAudio.text =
                        if (it.message_share != null && !StringUtils.isNullOrEmpty(it.message_share?._id)) {
                            TimeFormatHelper.calculateDuration(
                                it.message_share?.files?.get(0)?.time ?: 0
                            )
                        } else {
                            TimeFormatHelper.calculateDuration(it.files[0].time ?: 0)
                        }
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (playingPosition == holder.bindingAdapterPosition) {
            updateNonPlayingView(holder)
            playingHolder = null
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        return if (msg.what == MSG_UPDATE_SEEK_BAR) {
            when (playingHolder) {
                is AudioMessageRightHolder -> {
                    (playingHolder as AudioMessageRightHolder).binding.seekBarAudio.progress =
                        mediaPlayer?.currentPosition ?: 0
                    seekHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                    if (mediaPlayer?.currentPosition != 0) {
                        (playingHolder as AudioMessageRightHolder).binding.tvTimePlayAudio.text =
                            TimeFormatHelper.calculateDuration(
                                mediaPlayer?.currentPosition ?: 0
                            )
                    } else {
                        //Displaying total time if audio not playing
                        (playingHolder as AudioMessageRightHolder).binding.tvTimePlayAudio.text =
                            TimeFormatHelper.calculateDuration(
                                mediaPlayer?.duration ?: 0
                            )
                    }
                }
                is AudioMessageLeftHolder -> {
                    (playingHolder as AudioMessageLeftHolder).binding.seekBarAudio.progress =
                        mediaPlayer?.currentPosition ?: 0
                    seekHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                    if (mediaPlayer?.currentPosition != 0) {
                        (playingHolder as AudioMessageLeftHolder).binding.tvTimePlayAudio.text =
                            TimeFormatHelper.calculateDuration(
                                mediaPlayer?.currentPosition ?: 0
                            )
                    } else {
                        //Displaying total time if audio not playing
                        (playingHolder as AudioMessageLeftHolder).binding.tvTimePlayAudio.text =
                            TimeFormatHelper.calculateDuration(
                                mediaPlayer?.duration ?: 0
                            )
                    }
                }
            }
            true
        } else {
            false
        }
    }

    fun stopPlayer() {
        if (mediaPlayer != null) {
            releaseMediaPlayer()
        }
    }

    private fun releaseMediaPlayer() {
        playingHolder?.let { updateNonPlayingView(it) }
        mediaPlayer?.release()
        mediaPlayer = null
        playingPosition = -1
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mediaPlayer?.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    companion object {
        private const val TYPE_TEXT_MY = 0
        private const val TYPE_TEXT_THEIR = 1
        private const val TYPE_IMAGE_MY = 2
        private const val TYPE_IMAGE_THEIR = 3
        private const val TYPE_STICKER_MY = 4
        private const val TYPE_STICKER_THEIR = 5
        private const val TYPE_VIDEO_MY = 6
        private const val TYPE_VIDEO_THEIR = 7
        private const val TYPE_REPLY_MY = 8
        private const val TYPE_REPLY_THEIR = 9
        private const val TYPE_NOTIFICATION = 10
        private const val TYPE_AUDIO_MY = 11
        private const val TYPE_AUDIO_THEIR = 12
        private const val TYPE_REVOKE_MY = 13
        private const val TYPE_REVOKE_THEIR = 14
        private const val TYPE_CREATE_GROUP = 15
        private const val TYPE_CREATE_GROUP_PERSONAL = 16
        private const val TYPE_FILE_MY = 17
        private const val TYPE_FILE_THEIR = 18
        private const val TYPE_PINNED = 19
        private const val TYPE_ADD_MEMBER = 20
        private const val TYPE_UTILS = 21
        private const val TYPE_MEMBER = 22
        private const val TYPE_GI_PHY_MY = 23
        private const val TYPE_GI_PHY_THEIR = 24
        private const val TYPE_BUSINESS_CARD_MY = 25
        private const val TYPE_BUSINESS_CARD_THEIR = 26
        private const val TYPE_LINK_MY = 27
        private const val TYPE_LINK_THEIR = 28
        private const val TYPE_PROMOTED_VICE_GROUP = 29
        private const val TYPE_DEMOTED_VICE_GROUP = 30
        private const val TYPE_COMPLETE_BILL = 31
        private const val TYPE_VIDEO_CALL_MY = 32
        private const val TYPE_VIDEO_CALL_THEIR = 33
        private const val TYPE_ADVERTISEMENT_MY = 34
        private const val TYPE_ADVERTISEMENT_THEIR = 35
        private const val MSG_UPDATE_SEEK_BAR = 1845
        private const val TYPE_NOTIFICATION_BIRTH_DAY = 36
        private const val TYPE_INVITE_EVENT_MY = 37
        private const val TYPE_INVITE_EVENT_THEIR = 38
        private const val TYPE_GIFT_NOTIFICATION = 39
        private const val TYPE_GETTINGS = 40
        private const val TYPE_MESSAGE_INFORMATION = 41
    }

}