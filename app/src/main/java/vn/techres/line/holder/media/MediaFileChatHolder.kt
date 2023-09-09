package vn.techres.line.holder.media

import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemSectionMediaFileChatBinding
import vn.techres.line.helper.FileDownloadingListener
import vn.techres.line.helper.TaskManagerFile
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.utils.invisible
import vn.techres.line.interfaces.chat.EventBusDownloadFileComplete
import vn.techres.line.interfaces.chat.MediaHandler
import java.io.File
import java.net.URLDecoder

class MediaFileChatHolder(private val binding: ItemSectionMediaFileChatBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var id: Int = 0
    lateinit var file: FileNodeJs
    fun bind(
        configNodeJs: ConfigNodeJs,
        dataSource: ArrayList<FileNodeJs>,
        position: Int,
        mediaHandler: MediaHandler,
    ) {
        file = dataSource[position]
        file.name_file = URLDecoder.decode(
            file.name_file, "UTF-8"
        )
        binding.tvNameFile.text = file.name_file
        binding.tvCapacityFile.text = Utils.setSizeFile(file.size ?: 0)
        binding.btnDownloadFile.tag = this
        /**
         * Check isFile
         * */
        val path =
            FileUtils.getStorageDir(file.name_file ?: "")

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
         * set TaskManagerFile
         * */
        TaskManagerFile().getImpl().createPath(path)
        if (TaskManagerFile().getImpl().isReady()) {
            val status: Int = TaskManagerFile().getImpl().getStatus(id, path)
            if (status == FileDownloadStatus.pending.toInt() || status == FileDownloadStatus.started.toInt() || status == FileDownloadStatus.connected.toInt()) {
                // start task, but file not created yet
                updateDownLoading(
                    status,
                    TaskManagerFile().getImpl().getSoFar(id),
                    TaskManagerFile().getImpl().getTotal(id)
                )
            } else if (!File(path ?: "").exists() &&
                !File(FileDownloadUtils.getTempPath(path ?: "")).exists()
            ) {
                // not exist file
                updateNotDownLoaded(0, 0)
            } else if (TaskManagerFile().getImpl().isDownloaded(status)) {
                // already downloaded and exist
                updateDownloaded()
            } else if (status == FileDownloadStatus.progress.toInt()) {
                // downloading
                updateDownLoading(
                    status,
                    TaskManagerFile().getImpl().getSoFar(id),
                    TaskManagerFile().getImpl().getTotal(id)
                )
            } else {
                // not start
                updateNotDownLoaded(
                    TaskManagerFile().getImpl().getSoFar(id),
                    TaskManagerFile().getImpl().getTotal(id)
                )
            }
        }
        /**
         * Set listener
         * */
        binding.btnOpenFile.setOnClickListener {
            mediaHandler.onOpenFile(path)
        }
        binding.btnDownloadFile.setOnClickListener {
            val model = TaskManagerFile().getImpl()[file.link_original]
            model?.link_original?.let { it1 -> WriteLog.d("model ", it1) }
            val url =
                String.format("%s%s", configNodeJs.api_ads, model?.link_original)
            WriteLog.d("url ", url)

            val task: BaseDownloadTask = FileDownloader.getImpl().create(url)
                .setPath(path ?: "")
                .setCallbackProgressTimes(100)
                .setListener(taskDownloadListener)

            TaskManagerFile().getImpl().addTaskForViewHolder(task)
            id = task.id
            TaskManagerFile().getImpl().updateViewHolder(id, it.tag as MediaFileChatHolder)

            task.start()
        }
        binding.imgPauseDownload.setOnClickListener {
            // to pause
            FileDownloader.getImpl().pause(id)
            val nameFile = file.name_file
            mediaHandler.onDeleteDownLoadFile(nameFile)
            updateNotDownLoaded(0, 0)
        }
        when (file.link_original?.let { FileUtils.getMimeType(it) }) {
            TechResEnumChat.TYPE_JPEG.toString(), TechResEnumChat.TYPE_JPG.toString(), TechResEnumChat.TYPE_SVG.toString(), TechResEnumChat.TYPE_PNG.toString()
            -> {
                Utils.getGlide(binding.imgFile, file.link_original, configNodeJs)
            }
            TechResEnumChat.TYPE_DOCX.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_word)
            }
            TechResEnumChat.TYPE_AI.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_ai)
            }
            TechResEnumChat.TYPE_DMG.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_dmg)
            }
            TechResEnumChat.TYPE_XLSX.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_excel)
            }
            TechResEnumChat.TYPE_HTML.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_html)
            }
            TechResEnumChat.TYPE_MP3.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_music)
            }
            TechResEnumChat.TYPE_PDF.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_pdf)
            }
            TechResEnumChat.TYPE_PPTX.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_powerpoint)
            }
            TechResEnumChat.TYPE_PSD.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_psd)
            }
            TechResEnumChat.TYPE_REC.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_record)
            }
            TechResEnumChat.TYPE_EXE.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_setup)
            }
            TechResEnumChat.TYPE_SKETCH.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_sketch)
            }
            TechResEnumChat.TYPE_TXT.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_txt)
            }
            TechResEnumChat.TYPE_MP4.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_video)
            }
            TechResEnumChat.TYPE_XML.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_xml)
            }
            TechResEnumChat.TYPE_ZIP.toString() -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_zip)
            }
            else -> {
                binding.imgFile.setImageResource(R.drawable.icon_file_attach)
            }
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

        private fun checkCurrentHolder(task: BaseDownloadTask): MediaFileChatHolder? {
            return if (task.tag is MediaFileChatHolder) {
                val tag = task.tag as MediaFileChatHolder
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
            TaskManagerFile().getImpl().removeTaskForViewHolder(id)
        }

        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateNotDownLoaded(task.largeFileSoFarBytes, task.largeFileTotalBytes)
            TaskManagerFile().getImpl().removeTaskForViewHolder(id)
        }

        override fun completed(task: BaseDownloadTask?) {
            super.completed(task)
            val tag = checkCurrentHolder(
                task!!
            ) ?: return
            tag.updateDownloaded()
            EventBus.getDefault().post(EventBusDownloadFileComplete(file.random_key))
            TaskManagerFile().getImpl().removeTaskForViewHolder(task.id)
        }
    }
}