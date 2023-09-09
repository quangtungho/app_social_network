package vn.techres.line.adapter.chat

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kohii.v1.core.Manager
import kohii.v1.exoplayer.Kohii
import vn.techres.line.adapter.account.LoadImageChatAdapter
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.*
import vn.techres.line.helper.utils.ChatUtils.checkSenderMessage
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.holder.message.NotificationMessageHolder
import vn.techres.line.holder.message.information.left.*
import vn.techres.line.holder.message.information.right.*
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class InformationMessageAdapter(var context: Context, var manager: Manager, var kohii: Kohii) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mediaPlayer: MediaPlayer? = null
    private var dataSource = ArrayList<MessagesByGroup>()
    private var receivedViewer = ArrayList<MessageViewer>()
    private var receivedViewerNotSeen = ArrayList<MessageViewer>()
    private var chatGroupHandler: ChatGroupHandler? = null
    private var adapter: LoadImageChatAdapter? = null
    private var imageMoreChatHandler: ImageMoreChatHandler? = null
    private var chooseNameUserHandler: ChooseNameUserHandler? = null
    private var viewerMessageHandler: ViewerMessageHandler? = null
    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setViewerMessageHandler(viewerMessageHandler: ViewerMessageHandler?) {
        this.viewerMessageHandler = viewerMessageHandler
    }

    fun setImageMoreChatHandler(imageMoreChatHandler: ImageMoreChatHandler) {
        this.imageMoreChatHandler = imageMoreChatHandler
    }

    fun setChatGroupHandler(chatGroupHandler: ChatGroupHandler) {
        this.chatGroupHandler = chatGroupHandler
    }

    fun setChooseTagNameHandler(chooseNameUserHandler: ChooseNameUserHandler) {
        this.chooseNameUserHandler = chooseNameUserHandler
    }

    fun setDataSource(
        dataSource: ArrayList<MessagesByGroup>,
        receivedViewer: ArrayList<MessageViewer>,
        receivedViewerNotSeen: ArrayList<MessageViewer>
    ) {
        val index = dataSource.size
        this.dataSource = dataSource
        this.receivedViewer = receivedViewer
        this.receivedViewerNotSeen = receivedViewerNotSeen
        notifyItemRangeInserted(index, dataSource.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT_MY -> {
                TextInformationRightHolder(
                    ItemTextInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_TEXT_THEIR -> {
                TextInformationLeftHolder(
                    ItemTextInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_IMAGE_MY -> {
                ImageInformationRightHolder(
                    ItemImageInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_IMAGE_THEIR -> {
                ImageInformationLeftHolder(
                    ItemImageInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_STICKER_MY -> {
                StickerInformationRightHolder(
                    ItemStickerInfomationRightBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            TYPE_STICKER_THEIR -> {
                StickerInformationLeftHolder(
                    ItemStickerInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_VIDEO_MY -> {
                VideoInformationRightHolder(
                    ItemVideoInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_VIDEO_THEIR -> {
                VideoInformationLeftHolder(
                    ItemVideoInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_REPLY_MY -> {
                ReplyInformationRightHolder(
                    ItemReplyInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_REPLY_THEIR -> {
                ReplyInformationLeftHolder(
                    ItemReplyInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_AUDIO_MY -> {
                AudioInformationRightHolder(
                    ItemAudioInformationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_AUDIO_THEIR -> {
                AudioInformationLeftHolder(
                    ItemAudioInformationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_LINK_MY -> {
                LinkInformationRightHolder(
                    ItemLinkInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_LINK_THEIR -> {
                LinkInformationLeftHolder(
                    ItemLinkInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_FILE_MY -> {
                FileInformationRightHolder(
                    ItemFileInfomationRightBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_FILE_THEIR -> {
                FileInformationLeftHolder(
                    ItemFileInfomationLeftBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            TYPE_BUSINESS_CARD_MY -> {
                BusinessCardInformationRightHolder(
                    ItemBusinessCardInfomationRightBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
            TYPE_BUSINESS_CARD_THEIR -> {
                BusinessCardInformationLeftHolder(
                    ItemBusinessCardInfomationLeftBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_TEXT_MY -> {
                (holder as TextInformationRightHolder).bind(
                    context,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler, receivedViewerNotSeen
                )
            }
            TYPE_TEXT_THEIR -> {
                (holder as TextInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    user,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_IMAGE_MY -> {
                (holder as ImageInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    adapter,
                    chatGroupHandler,
                    imageMoreChatHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_IMAGE_THEIR -> {
                (holder as ImageInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    adapter,
                    chatGroupHandler,
                    imageMoreChatHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_STICKER_MY -> {
                (holder as StickerInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_STICKER_THEIR -> {
                (holder as StickerInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_VIDEO_MY -> {
                (holder as VideoInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    imageMoreChatHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_VIDEO_THEIR -> {
                (holder as VideoInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    imageMoreChatHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_REPLY_MY -> {
                (holder as ReplyInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_REPLY_THEIR -> {
                (holder as ReplyInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_AUDIO_MY -> {
                mediaPlayer = MediaPlayer()
                (holder as AudioInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    mediaPlayer,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_AUDIO_THEIR -> {
                mediaPlayer = MediaPlayer()
                (holder as AudioInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    mediaPlayer,
                    dataSource,
                    receivedViewer,
                    viewerMessageHandler,
                    receivedViewerNotSeen
                )
            }
            TYPE_FILE_MY -> {
                (holder as FileInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_FILE_THEIR -> {
                (holder as FileInformationLeftHolder).bind(
                    context,
                    user,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_BUSINESS_CARD_MY -> {
                (holder as BusinessCardInformationRightHolder).bind(
                    context,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_BUSINESS_CARD_THEIR -> {
                (holder as BusinessCardInformationLeftHolder).bind(
                    context,
                    configNodeJs,
                    user,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_LINK_MY -> {
                (holder as LinkInformationRightHolder).bind(
                    context,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
            TYPE_LINK_THEIR -> {
                (holder as LinkInformationLeftHolder).bind(
                    context,
                    user,
                    configNodeJs,
                    dataSource,
                    receivedViewer,
                    chatGroupHandler,
                    chooseNameUserHandler,
                    viewerMessageHandler,receivedViewerNotSeen
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

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
            TechResEnumChat.TYPE_LINK.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_LINK_MY
                } else {
                    TYPE_LINK_THEIR
                }
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_FILE_MY
                } else {
                    TYPE_FILE_THEIR
                }
            }
            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                if (checkSenderMessage(message, user)) {
                    TYPE_BUSINESS_CARD_MY
                } else {
                    TYPE_BUSINESS_CARD_THEIR
                }
            }
            else -> {
                -1
            }
        }
    }

    fun stopPlaying() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        stopPlaying()
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
        private const val TYPE_AUDIO_MY = 11
        private const val TYPE_AUDIO_THEIR = 12
        private const val TYPE_LINK_MY = 13
        private const val TYPE_LINK_THEIR = 14
        private const val TYPE_FILE_MY = 15
        private const val TYPE_FILE_THEIR = 16
        private const val TYPE_BUSINESS_CARD_MY = 17
        private const val TYPE_BUSINESS_CARD_THEIR = 18
    }
}