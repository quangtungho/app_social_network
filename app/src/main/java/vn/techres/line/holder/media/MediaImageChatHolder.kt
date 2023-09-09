package vn.techres.line.holder.media

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemSectionMediaImageChatBinding
import vn.techres.line.helper.FileDownloadingListener
import vn.techres.line.helper.TaskManagerFile
import vn.techres.line.helper.Utils
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.MediaHandler
import java.io.File

class MediaImageChatHolder(private val binding: ItemSectionMediaImageChatBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var id: Int = 0
    lateinit var handler: MediaHandler

    lateinit var file: FileNodeJs
    fun bind(
        configNodeJs: ConfigNodeJs,
        dataSource: ArrayList<FileNodeJs>,
        position: Int,
        mediaHandler: MediaHandler,
        view: View,
        context: Context
    ) {
        file = dataSource[position]
        handler = mediaHandler
        itemView.tag = this
        view.post {
            binding.imgMedia.layoutParams.width = view.width / 3
            binding.imgMedia.layoutParams.height = view.width / 3
        }
        val linkOriginal =
            FileUtils.getInternalStogeDir(file.name_file ?: "", context) ?: ""
        Utils.getMediaGlide(
            binding.imgMedia,
            linkOriginal
        )
        itemView.setOnClickListener {
            mediaHandler.onClickMedia(dataSource, position)
        }
        /**
         * Check isFile
         * */
        val model = TaskManagerFile().getImpl()[file.link_original]
        val path =
            FileUtils.getInternalStogeDir(file.name_file ?: "", context)
        if (!File(path).exists()) {
            TaskManagerFile().getImpl().createPath(path)
            if (TaskManagerFile().getImpl().isReady()) {
                val status: Int = TaskManagerFile().getImpl().getStatus(id, path)
                if (status == FileDownloadStatus.pending.toInt() || status == FileDownloadStatus.started.toInt() || status == FileDownloadStatus.connected.toInt()) {
                    // start task, but file not created yet
                    updateDownLoading()
                } else if (!File(path ?: "").exists() &&
                    !File(FileDownloadUtils.getTempPath(path ?: "")).exists()
                ) {
                    // not exist file
                    updateNotDownLoaded()
                } else if (TaskManagerFile().getImpl().isDownloaded(status)) {
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
            val url =
                String.format("%s%s", configNodeJs.api_ads, model?.link_original)
            val task: BaseDownloadTask = FileDownloader.getImpl().create(url)
                .setPath(path)
                .setCallbackProgressTimes(100)
                .setListener(taskDownloadListener)

            TaskManagerFile().getImpl().addTaskForViewHolder(task)
            id = task.id
            TaskManagerFile().getImpl()
                .updateViewHolder(id, itemView.tag as MediaImageChatHolder)
            task.start()
        }


    }

    private fun updateDownloaded() {
        handler.onSuccessDownload()
    }

    private fun updateNotDownLoaded() {

    }

    private fun updateDownLoading() {
    }

    private val taskDownloadListener = object : FileDownloadingListener() {

        private fun checkCurrentHolder(task: BaseDownloadTask): MediaImageChatHolder? {
            return if (task.tag is MediaImageChatHolder) {
                val tag = task.tag as MediaImageChatHolder
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
            TaskManagerFile().getImpl().removeTaskForViewHolder(id)
        }

        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded()
            TaskManagerFile().getImpl().removeTaskForViewHolder(id)
        }

        override fun completed(task: BaseDownloadTask?) {
            super.completed(task)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownloaded()
            TaskManagerFile().getImpl().removeTaskForViewHolder(task.id)
        }
    }
}