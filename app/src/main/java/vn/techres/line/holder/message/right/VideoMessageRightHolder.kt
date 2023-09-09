package vn.techres.line.holder.message.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import kohii.v1.core.Playback
import kohii.v1.core.Scope
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemVideoChatRightBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.EventBusDownloadSuccess
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import java.io.File

class VideoMessageRightHolder(val binding: ItemVideoChatRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var context: Context? = null
    var playback: Playback? = null
    var volumeInfo: VolumeInfo? = null
    private val cacheManager: CacheManager = CacheManager.getInstance()
    var id: Int = 0
    var randomkey = ""
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        imageMoreChatHandler: ImageMoreChatHandler?
    ) {
        this.context = context
        val message = dataSource[position]
        itemView.tag = this
        randomkey = message.random_key ?: ""
        var path = ""
        if (message.is_stroke == 1) {
            binding.rlVideo.isSelected = true
            binding.rlVideo.setPadding(4,4,4,4)
        } else {
            binding.rlVideo.isSelected = false
            binding.rlVideo.setPadding(0,0,0,0)
        }
        /**
         * set TasksManager
         * */
        message.files.forEach {
            val model = TasksManager().getImpl()[message.random_key]
            path = FileUtils.getInternalStogeDir(it.name_file ?: "", context) ?: ""
            if (!File(path).exists()) {
                TasksManager().getImpl().createPath(path)
                if (TasksManager().getImpl().isReady()) {
                    val status: Int = TasksManager().getImpl().getStatus(id, path)
                    if (status == FileDownloadStatus.pending.toInt() || status == FileDownloadStatus.started.toInt() || status == FileDownloadStatus.connected.toInt()) {
                        // start task, but file not created yet
                        updateDownLoading(
                            status,
                            TasksManager().getImpl().getSoFar(id),
                            TasksManager().getImpl().getTotal(id)
                        )
                    } else if (!File(path ?: "").exists() &&
                        !File(FileDownloadUtils.getTempPath(path ?: "")).exists()
                    ) {
                        // not exist file
                        updateNotDownLoaded(0, 0)
                    } else if (TasksManager().getImpl().isDownloaded(status)) {
                        // already downloaded and exist
                        updateDownloaded()
                    } else if (status == FileDownloadStatus.progress.toInt()) {
                        // downloading
                        updateDownLoading(
                            status,
                            TasksManager().getImpl().getSoFar(id),
                            TasksManager().getImpl().getTotal(id)
                        )
                    } else {
                        // not start
                        updateNotDownLoaded(
                            TasksManager().getImpl().getSoFar(id),
                            TasksManager().getImpl().getTotal(id)
                        )
                    }
                }
                model?.files?.forEachIndexed { index, fileNodeJs ->
                    if (fileNodeJs.link_original.equals(it.link_original)) {
                        val url =
                            String.format("%s%s", configNodeJs.api_ads, it.link_original)
                        val task: BaseDownloadTask = FileDownloader.getImpl().create(url)
                            .setPath(path)
                            .setCallbackProgressTimes(100)
                            .setListener(taskDownloadListener)

                        TasksManager().getImpl().addTaskForViewHolder(task)
                        id = task.id
                        TasksManager().getImpl()
                            .updateViewHolder(id, itemView.tag as VideoMessageRightHolder)
                        task.start()
                        return@forEachIndexed
                    }
                }
            }
        }
//        binding.tvTimeVideo.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeVideo, position, dataSource)
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
//        if (message.files[0].height != 0 && message.files[0].width != 0) {
//            ChatUtils.setLayoutParams(
//                binding.playerViewMessage,
//                message.files[0].height ?: 0,
//                message.files[0].width ?: 0
//            )
//            ChatUtils.setLayoutParams(
//                binding.imgPlayerView,
//                message.files[0].height ?: 0,
//                message.files[0].width ?: 0
//            )
//        }
        if (message.local == 1) {
            binding.reaction.imgReactionPress.isEnabled = false
            binding.pbLoadingVideo.show()
            binding.pbLoadingVideo.progress = message.files[0].process
        } else {
            binding.reaction.imgReactionPress.isEnabled = true
            binding.pbLoadingVideo.hide()
        }
        val linkOriginal = FileUtils.getInternalStogeDir(message.files[0].name_file ?: "", context) ?: ""
        val kohii = Kohii[context]
        Utils.resizeVideo(
            context,
            linkOriginal ?: "",
            binding.playerViewMessage,
            binding.imgPlayerView
        )
        kohii.setUp(
            linkOriginal
        )
            .bind(binding.playerViewMessage)
            {
                playback = it
                volumeInfo = it.volumeInfo
                it.addStateListener(object : Playback.StateListener {
                    override fun onPlaying(playback: Playback) {
                        super.onPlaying(playback)
                        binding.imgPlayerView.hide()
                        if (cacheManager.get(TechresEnum.CHECK_VOLUME_MUTE.toString()) == "true") {
                            volumeInfo = VolumeInfo(true, 0f)
                            kohii.applyVolumeInfo(
                                volumeInfo!!,
                                playback,
                                Scope.GLOBAL
                            )
                            binding.imgVolume.setImageResource(R.drawable.ic_volume_off)
                        } else {
                            volumeInfo = VolumeInfo(false, 1f)
                            kohii.applyVolumeInfo(
                                volumeInfo!!,
                                playback,
                                Scope.GLOBAL
                            )
                            binding.imgVolume.setImageResource(R.drawable.ic_volume_on)
                        }
                    }

                    override fun onPaused(playback: Playback) {
                        super.onPaused(playback)
                        binding.imgPlayerView.show()
                    }

                    override fun onEnded(playback: Playback) {
                        super.onEnded(playback)
                        binding.imgPlayerView.show()
                    }

                })

                binding.imgVolume.setOnClickListener {
                    if (cacheManager.get(TechresEnum.CHECK_VOLUME_MUTE.toString()) == "true") {
                        cacheManager.put(TechresEnum.CHECK_VOLUME_MUTE.toString(), "false")
                        volumeInfo = VolumeInfo(false, 1f)
                        binding.imgVolume.setImageResource(R.drawable.ic_volume_on)
                    } else {
                        cacheManager.put(
                            TechresEnum.CHECK_VOLUME_MUTE.toString(),
                            "true"
                        )
                        volumeInfo = VolumeInfo(true, 0f)
                        binding.imgVolume.setImageResource(R.drawable.ic_volume_off)
                    }
                    kohii.applyVolumeInfo(volumeInfo!!, playback!!, Scope.GLOBAL)
                }
            }

        binding.imgVolume.setOnClickListener {
            if (cacheManager.get(TechresEnum.CHECK_VOLUME_MUTE.toString()) == "true") {
                cacheManager.put(TechresEnum.CHECK_VOLUME_MUTE.toString(), "false")
                volumeInfo = VolumeInfo(false, 1f)
                binding.imgVolume.setImageResource(R.drawable.ic_volume_on)
            } else {
                cacheManager.put(
                    TechresEnum.CHECK_VOLUME_MUTE.toString(),
                    "true"
                )
                volumeInfo = VolumeInfo(true, 0f)
                binding.imgVolume.setImageResource(R.drawable.ic_volume_off)
            }
            kohii.applyVolumeInfo(volumeInfo!!, playback!!, Scope.GLOBAL)
        }
        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.hide()
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            binding.reaction.imgReactionPress.show()
        } else {
            ChatUtils.setReaction(
                binding.lnReactionContainer,
                binding.reaction,
                message.reactions
            )
        }
        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.rlVideo)

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        binding.cvVideoContainer.setOnClickListener {
            imageMoreChatHandler?.onVideoMore(message.files, 0)
        }

        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }
        /**
         * Message viewed
         * */
        if (bindingAdapterPosition == 0) {
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed?.let {
                ChatUtils.setMessageViewer(context, binding.rcViewer, it)
                binding.rcViewer.show()
                binding.tvStatusView.hide()
            }
        } else {
            binding.rcViewer.hide()
            binding.tvStatusView.hide()
        }
    }
    private fun updateDownloaded() {
        EventBus.getDefault().post(EventBusDownloadSuccess(randomkey))
    }

    private fun updateNotDownLoaded(soFar: Long, total: Long) {

    }

    private fun updateDownLoading(status: Int, soFar: Long, total: Long) {
//        val percent = soFar / total.toFloat()
//        binding.pbDownload.max = 100
//        binding.pbDownload.progress = (percent * 100).toInt()
//        when (status) {
//            FileDownloadStatus.started.toInt() -> {
//                binding.rlDownloadFile.show()
//                binding.btnOpenFile.hide()
//                binding.btnDownloadFile.hide()
//            }
//            FileDownloadStatus.pending.toInt() -> {
//                binding.rlDownloadFile.show()
//                binding.btnOpenFile.hide()
//                binding.btnDownloadFile.hide()
//            }
//            FileDownloadStatus.progress.toInt() -> {
//                binding.rlDownloadFile.show()
//                binding.btnOpenFile.hide()
//                binding.btnDownloadFile.hide()
//            }
//            FileDownloadStatus.connected.toInt() -> {
//                binding.rlDownloadFile.show()
//                binding.btnOpenFile.hide()
//                binding.btnDownloadFile.hide()
//            }
//        }
    }

    private val taskDownloadListener = object : FileDownloadingListener() {

        private fun checkCurrentHolder(task: BaseDownloadTask): VideoMessageRightHolder? {
            return if (task.tag is VideoMessageRightHolder) {
                val tag = task.tag as VideoMessageRightHolder
                if (tag.id != task.id) {
                    return null
                }
                tag
            } else {
                null
            }
        }

        override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.pending(task, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownLoading(
                FileDownloadStatus.pending.toInt(),
                soFarBytes.toLong(),
                totalBytes.toLong()
            )
        }

        override fun started(task: BaseDownloadTask?) {
            super.started(task)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
        }

        override fun connected(
            task: BaseDownloadTask?,
            etag: String?,
            isContinue: Boolean,
            soFarBytes: Int,
            totalBytes: Int
        ) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownLoading(
                FileDownloadStatus.connected.toInt(),
                soFarBytes.toLong(),
                totalBytes.toLong()
            )
        }

        override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.progress(task, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownLoading(
                FileDownloadStatus.progress.toInt(),
                soFarBytes.toLong(),
                totalBytes.toLong()
            )
        }

        override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.paused(task, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded(soFarBytes.toLong(), totalBytes.toLong())
            TasksManager().getImpl().removeTaskForViewHolder(id)
        }

        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded(task.largeFileSoFarBytes, task.largeFileTotalBytes)
            TasksManager().getImpl().removeTaskForViewHolder(id)
        }

        override fun completed(task: BaseDownloadTask?) {
            super.completed(task)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownloaded()
            TasksManager().getImpl().removeTaskForViewHolder(task.id)
        }
    }
}