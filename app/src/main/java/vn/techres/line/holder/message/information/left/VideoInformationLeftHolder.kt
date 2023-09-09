package vn.techres.line.holder.message.information.left

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemVideoInfomationLeftBinding
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
import vn.techres.line.interfaces.chat.ViewerMessageHandler
import java.io.File

class VideoInformationLeftHolder(private val binding: ItemVideoInfomationLeftBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var context: Context? = null
    var id: Int = 0
    var randomkey = ""
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        dataSource: ArrayList<MessagesByGroup>,
        receivedViewer: ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        imageMoreChatHandler: ImageMoreChatHandler?,
        viewerMessageHandler: ViewerMessageHandler?,
        receivedViewerNotSeen : ArrayList<MessageViewer>
    ) {
        this.context = context
        val message = dataSource[bindingAdapterPosition]
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
                            .updateViewHolder(id, itemView.tag as VideoInformationLeftHolder)
                        task.start()
                        return@forEachIndexed
                    }
                }
            }
        }

        binding.tvTimeVideo.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        binding.tvNameVideo.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        if (bindingAdapterPosition >= 0 && (bindingAdapterPosition + 1) < dataSource.size) {
            if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition + 1].sender.member_id) {
                binding.tvNameVideo.hide()
                binding.cvVideo.visibility = View.INVISIBLE
            } else {
                binding.tvNameVideo.show()
                binding.cvVideo.show()
            }
        }

        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()

        val linkOriginal =
            FileUtils.getInternalStogeDir(message.files[0].name_file ?: "", context) ?: ""
//        if(!File(linkOriginal).exists()) {
//            Utils.resizeVideo(
//                context,
//                message.files[0].link_original ?: "",
//                binding.playerViewMessage,
//                binding.imgPlayerView
//            )
//        } else {
        Utils.resizeVideo(
            context,
            linkOriginal,
            binding.playerViewMessage,
            binding.imgPlayerView
        )
//        }

//        if (message.reactions.reactions_count == 0) {
//            binding.reaction.lnReaction.hide()
//            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
//            binding.reaction.imgReactionPress.show()
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
//        binding.reaction.imgReactionPress.visibility = View.GONE


        binding.cvVideoContainer.setOnClickListener {
            imageMoreChatHandler!!.onVideoMore(message.files, 0)
        }

        /**
         * Message viewed
         * */
        binding.tvSeen.hide()
        binding.tvReceived.hide()
        binding.rcViewerSeen.hide()
        binding.rcViewerReceived.hide()
        if (bindingAdapterPosition == 0) {
            binding.tvStatusView.text = context.getString(R.string.received)
            if (receivedViewerNotSeen.size > 0) {
                binding.tvReceived.show()
                binding.rcViewerReceived.show()
                ChatUtils.setMessageViewerSeen(
                    context,
                    binding.rcViewerReceived,
                    receivedViewerNotSeen,
                    viewerMessageHandler
                )
            }
            if (receivedViewer.size > 0) {
                binding.tvSeen.show()
                binding.rcViewerSeen.show()
                binding.tvStatusView.text = context.getString(R.string.seen)
                ChatUtils.setMessageViewerSeen(
                    context,
                    binding.rcViewerSeen,
                    receivedViewer,
                    viewerMessageHandler
                )
            }
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

        private fun checkCurrentHolder(task: BaseDownloadTask): VideoInformationLeftHolder? {
            return if (task.tag is VideoInformationLeftHolder) {
                val tag = task.tag as VideoInformationLeftHolder
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