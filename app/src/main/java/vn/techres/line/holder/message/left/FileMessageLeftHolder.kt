package vn.techres.line.holder.message.left

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemFileChatLeftBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.utils.ChatUtils.setImageFile
import vn.techres.line.helper.utils.FileUtils.getMimeType
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.utils.invisible
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import java.io.File
import java.net.URLDecoder
import java.util.*

class FileMessageLeftHolder(private val binding: ItemFileChatLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var id: Int = 0
    fun bind(
        context: Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        dataSource: ArrayList<MessagesByGroup>,
        position: Int,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?
    ) {
        val message = dataSource[position]
        message.files[0].name_file = URLDecoder.decode(message.files[0].name_file, "UTF-8")
        if (message.is_stroke == 1) {
            binding.rlFileContainer.isSelected = true
        } else {
            binding.rlFileContainer.isSelected = false
        }
//        binding.tvTimeFile.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeFile, position, dataSource)

        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        binding.tvSenderNameFile.text = message.sender.full_name
        binding.btnDownloadFile.tag = this


        val mineType = message.files[0].link_original?.let { getMimeType(it) }
        setImageFile(binding.imgFile, mineType)
//        binding.tvNameFile.text = message.files[0].name_file?.replace("%20", " ")
        binding.tvNameFile.text = message.files[0].name_file

        binding.tvTypeFile.text = String.format(
            "%s %s %s",
            (mineType ?: "").uppercase(Locale.ROOT),
            context.resources.getString(R.string.dot),
            Utils.setSizeFile(message.files[0].size ?: 0)
        )

        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id && message.today != 1 && dataSource[position + 1].message_type.equals(
                        TechResEnumChat.TYPE_FILE.toString()
                    )
                ) {
                    binding.tvSenderNameFile.hide()
                    binding.cvFile.visibility = View.INVISIBLE
                } else {
                    binding.tvSenderNameFile.show()
                    binding.cvFile.show()
                }
            }
        } else {
            binding.cvFile.hide()
            binding.tvSenderNameFile.hide()
        }
        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.hide()
            binding.reaction.imgReactionPress.show()
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            if (position > 0) {
                if (dataSource[position].sender.member_id == dataSource[position - 1].sender.member_id) {
                    binding.lnReactionContainer.visibility = View.GONE
                } else {
                    binding.lnReactionContainer.show()
                }
            } else {
                binding.lnReactionContainer.show()
            }
        } else {
            ChatUtils.setReaction(
                binding.lnReactionContainer,
                binding.reaction,
                message.reactions
            )
        }
        ChatUtils.setViewTimeLeft(binding.reaction.lnReaction, binding.rlFileContainer)

        /**
         * Check isFile
         * */
        val path =
                FileUtils.getStorageDir(message.files[0].name_file ?: "")
        path?.let {
            if (File(it).exists()) {
                binding.btnOpenFile.show()
                binding.btnDownloadFile.hide()
            } else {
                binding.btnOpenFile.invisible()
                binding.btnDownloadFile.show()
            }
        }

        /**
         * set TasksManager
         * */
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
        /**
         * Set listener
         * */
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

        binding.btnOpenFile.setOnClickListener {
            chatGroupHandler?.onOpenFile(path)
        }
        binding.btnDownloadFile.setOnClickListener {
            val model = TasksManager().getImpl()[message.random_key]
            val url =
                String.format("%s%s", configNodeJs.api_ads, model?.files?.get(0)?.link_original)
            val task: BaseDownloadTask = FileDownloader.getImpl().create(url)
                .setPath(path ?: "")
                .setCallbackProgressTimes(100)
                .setListener(taskDownloadListener)

            TasksManager().getImpl().addTaskForViewHolder(task)
            id = task.id
            TasksManager().getImpl().updateViewHolder(id, it.tag as FileMessageLeftHolder)

            task.start()
        }
        binding.imgPauseDownload.setOnClickListener {
            // to pause
            FileDownloader.getImpl().pause(id)
            val nameFile =
                if (message.message_share != null && !StringUtils.isNullOrEmpty(message.message_share?.random_key)) {
                    message.message_share?.files?.get(0)?.name_file
                } else {
                    message.files[0].name_file
                }
            chatGroupHandler?.onDeleteDownLoadFile(nameFile)
            updateNotDownLoaded(0, 0)
        }
        binding.imgActionMore.setOnClickListener {
            chatGroupHandler?.onShareMessage(message)
        }
        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        /**
         * Message viewed
         * */
        if (bindingAdapterPosition == 0) {
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
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

    private fun updateDownloaded() {
        binding.pbDownload.max = 1
        binding.pbDownload.progress = 1
        binding.rlDownloadFile.hide()
        binding.btnOpenFile.show()
        binding.btnDownloadFile.hide()
    }

    private fun updateNotDownLoaded(soFar: Long, total: Long) {
        if (soFar > 0 && total > 0) {
            val percent = soFar / total.toFloat()
            binding.pbDownload.max = 100
            binding.pbDownload.progress = (percent * 100).toInt()
        } else {
            binding.pbDownload.max = 1
            binding.pbDownload.progress = 0
        }
        binding.rlDownloadFile.hide()
        binding.btnOpenFile.hide()
        binding.btnDownloadFile.show()

    }

    private fun updateDownLoading(status: Int, soFar: Long, total: Long) {
        val percent = soFar / total.toFloat()
        binding.pbDownload.max = 100
        binding.pbDownload.progress = (percent * 100).toInt()
        when (status) {
            FileDownloadStatus.started.toInt() -> {
                binding.rlDownloadFile.show()
                binding.btnOpenFile.hide()
                binding.btnDownloadFile.hide()
            }
            FileDownloadStatus.pending.toInt() -> {
                binding.rlDownloadFile.show()
                binding.btnOpenFile.hide()
                binding.btnDownloadFile.hide()
            }
            FileDownloadStatus.progress.toInt() -> {
                binding.rlDownloadFile.show()
                binding.btnOpenFile.hide()
                binding.btnDownloadFile.hide()
            }
            FileDownloadStatus.connected.toInt() -> {
                binding.rlDownloadFile.show()
                binding.btnOpenFile.hide()
                binding.btnDownloadFile.hide()
            }
        }
    }

    private val taskDownloadListener = object : FileDownloadingListener() {

        private fun checkCurrentHolder(task: BaseDownloadTask): FileMessageLeftHolder? {
            return if (task.tag is FileMessageLeftHolder) {
                val tag = task.tag as FileMessageLeftHolder
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
            checkCurrentHolder(
                task!!
            ) ?: return
            binding.rlDownloadFile.show()
            binding.btnOpenFile.hide()
            binding.btnDownloadFile.hide()
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