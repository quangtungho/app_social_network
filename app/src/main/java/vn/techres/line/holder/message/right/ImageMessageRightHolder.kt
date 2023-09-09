package vn.techres.line.holder.message.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.adapter.account.LoadImageChatAdapter
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemImageChatRightBinding
import vn.techres.line.helper.FileDownloadingListener
import vn.techres.line.helper.TasksManager
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.EventBusDownloadSuccess
import vn.techres.line.interfaces.chat.ImageMoreChatHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import java.io.File
import java.util.*


class ImageMessageRightHolder(val binding: ItemImageChatRightBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    private var adapter: LoadImageChatAdapter? = null
    var id: Int = 0
    var randomkey = ""
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        adapter: LoadImageChatAdapter?,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        imageMoreChatHandler: ImageMoreChatHandler?
    ) {
        this.adapter = adapter
        val message = dataSource[position]
        if (message.is_stroke == 1) {
            binding.imageGr.isSelected = true
            binding.imageGr.setPadding(4,4,4,4)
        } else {
            binding.imageGr.isSelected = false
            binding.imageGr.setPadding(0,0,0,0)
        }
        if(message.files.size > 1) {
            binding.imageGr.background = null
        } else {
            binding.imageGr.background = itemView.context.resources.getDrawable(R.drawable.state_message_right_media, null)
        }
        val imageList = message.files
        itemView.tag = this
        randomkey = message.random_key ?: ""
        var path = ""
//        binding.tvTimeImage.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
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
                            .updateViewHolder(id, itemView.tag as ImageMessageRightHolder)
                        task.start()
                        return@forEachIndexed
                    }
                }
            }
        }
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeImage, position, dataSource)
        if (imageList.size == 1) {
            binding.cvImageMore.hide()
            binding.cvImageOne.show()

            val linkOriginal =
                FileUtils.getInternalStogeDir(message.files[0].name_file ?: "", context) ?: ""
//            if (!File(linkOriginal).exists()) {
//                message.files[0].link_original?.let {
//                    Utils.resizeImage(
//                        context,
//                        it, binding.imgOne, binding.cvImageOne
//                    )
//                }
//            } else {
                linkOriginal.let {
                    Utils.resizeImage(
                        context,
                        it, binding.imgOne, binding.cvImageOne
                    )
                }
//            }
            binding.reaction.imgReactionPress.isEnabled = true

        } else {
            binding.cvImageMore.show()
            binding.cvImageOne.hide()
            setAdapterImageVideo(context, message, imageMoreChatHandler)
        }

        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()

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

        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.imageGr)

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        itemView.setOnLongClickListener {
            revokeMessageHandler?.onRevoke(
                message,
                it
            )
            true
        }

        binding.imgOne.setOnClickListener {
            imageMoreChatHandler?.onCLickImageMore(message.files, 0)
        }
        /**
         * Message viewed
         * */
        if (bindingAdapterPosition == 0) {
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            if (message.local == 1) {
                binding.tvStatusView.text = context.getString(R.string.sent)
            } else {
                binding.tvStatusView.text = context.getString(R.string.received)
            }
            message.message_viewed.let {
                ChatUtils.setMessageViewer(context, binding.rcViewer, it)
                binding.rcViewer.show()
                binding.tvStatusView.hide()
            }
        } else {
            binding.rcViewer.hide()
            binding.tvStatusView.hide()
        }
    }

    private fun setAdapterImageVideo(
        context: Context,
        message: MessagesByGroup,
        imageMoreChatHandler: ImageMoreChatHandler?
    ) {
        adapter = LoadImageChatAdapter(context)
        adapter?.setImageMoreChatHandler(imageMoreChatHandler)
        val layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_END
            alignItems = AlignItems.CENTER

        }
        binding.rcImgMore.layoutManager = layoutManager
        binding.rcImgMore.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcImgMore.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        var mess = MessagesByGroup()
        mess.files.addAll(message.files)
        mess.files.forEach {
            val linkOriginal = FileUtils.getInternalStogeDir(it.name_file ?: "", context) ?: ""
//            if(File(linkOriginal).exists()) {
            it.link_original = linkOriginal
//            }
        }
        adapter?.setDataSource(message.files, 1, mess)
        binding.rcImgMore.adapter = adapter
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

        private fun checkCurrentHolder(task: BaseDownloadTask): ImageMessageRightHolder? {
            return if (task.tag is ImageMessageRightHolder) {
                val tag = task.tag as ImageMessageRightHolder
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



