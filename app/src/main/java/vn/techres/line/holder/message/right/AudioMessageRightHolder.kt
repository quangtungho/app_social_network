package vn.techres.line.holder.message.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemAudioRightChatBinding
import vn.techres.line.helper.FileDownloadingListener
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TasksManager
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.EventBusDownloadSuccess
import vn.techres.line.interfaces.chat.RevokeMessageHandler
import java.io.File

class AudioMessageRightHolder(
    val binding: ItemAudioRightChatBinding
) : RecyclerView.ViewHolder(binding.root) {
    var id: Int = 0
    var randomkey = ""
    fun bind(
        context: Context,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        revokeMessageHandler: RevokeMessageHandler?,
        configNodeJs: ConfigNodeJs
    ) {
        val message = dataSource[position]
        if(message.is_stroke == 1) {
            binding.rltAudio.isSelected = true
            binding.rltAudio.setPadding(2,2,2,2)
        } else {
            binding.rltAudio.isSelected = false
            binding.rltAudio.setPadding(0,0,0,0)
        }
//        binding.tvTimeAudio.text = TimeFormatHelper.getDateFromStringDay(
//            message.created_at
//        )
        itemView.tag = this
        randomkey = message.random_key ?: ""
        var path = ""
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
                        updateDownLoading()
                    } else if (!File(path ?: "").exists() &&
                        !File(FileDownloadUtils.getTempPath(path ?: "")).exists()
                    ) {
                        // not exist file
                        updateNotDownLoaded()
                    } else if (TasksManager().getImpl().isDownloaded(status)) {
                        // already downloaded and exist
                        updateDownloaded()
                    } else if (status == FileDownloadStatus.progress.toInt()) {
                        // downloading
                        updateDownLoading()
                    } else {
                        // not start
                        updateNotDownLoaded()
                    }
                }
                model?.files?.forEachIndexed { _, fileNodeJs ->
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
                            .updateViewHolder(id, itemView.tag as AudioMessageRightHolder)
                        task.start()
                        return@forEachIndexed
                    }
                }
            }
        }
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeAudio, position, dataSource)
        if (message.is_stroke == 1) {
            binding.cvAudio.isSelected = true
        } else {
            binding.cvAudio.isSelected = false
        }
        /**
         * time message
         * */
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
        /**
         * create and set time audio
         * */
        binding.tvTimePlayAudio.text =
            TimeFormatHelper.calculateDuration(message.files[0].time ?: 0)

        /**
         * Set reaction
         * */
        if (message.reactions.reactions_count == 0) {
            binding.reaction.lnReaction.hide()
            binding.reaction.imgReactionPress.show()
            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            if (position > 0) {
                if (dataSource[position].sender.member_id == dataSource[position - 1].sender.member_id) {
                    binding.lnReactionContainer.hide()
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

        ChatUtils.setViewTimeRight(binding.reaction.lnReaction, binding.cvAudio)

        binding.reaction.lnReactionPress.setOnClickListener {
            chatGroupHandler?.onPressReaction(message, it)
        }
        binding.reaction.lnReaction.setOnClickListener {
            val reactionList = ChatUtils.getReactionItem(message.reactions)
            reactionList.sortByDescending { it.number }
            chatGroupHandler?.onClickViewReaction(message, reactionList)
        }

        /**
         * revoke message
         * */
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

    private fun updateNotDownLoaded() {

    }

    private fun updateDownLoading() {
    }

    private val taskDownloadListener = object : FileDownloadingListener() {

        private fun checkCurrentHolder(task: BaseDownloadTask): AudioMessageRightHolder? {
            return if (task.tag is AudioMessageRightHolder) {
                val tag = task.tag as AudioMessageRightHolder
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
            tag.updateDownLoading()
        }

        override fun started(task: BaseDownloadTask?) {
            super.started(task)
            checkCurrentHolder(
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
            tag.updateDownLoading()
        }

        override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.progress(task, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownLoading()
        }

        override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.paused(task, soFarBytes, totalBytes)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded()
            TasksManager().getImpl().removeTaskForViewHolder(id)
        }

        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded()
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