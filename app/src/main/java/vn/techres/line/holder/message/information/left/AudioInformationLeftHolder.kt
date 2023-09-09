package vn.techres.line.holder.message.information.left

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
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
import vn.techres.line.databinding.ItemAudioInformationLeftBinding
import vn.techres.line.helper.FileDownloadingListener
import vn.techres.line.helper.TasksManager
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.interfaces.chat.EventBusDownloadSuccess
import vn.techres.line.interfaces.chat.ViewerMessageHandler
import java.io.File
import java.io.IOException

class AudioInformationLeftHolder(private val binding : ItemAudioInformationLeftBinding) : RecyclerView.ViewHolder(binding.root) {
    private var run: Runnable? = null
    private val seekHandler = Handler(Looper.myLooper()!!)
    var id: Int = 0
    var randomkey = ""
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        mediaPlayer: MediaPlayer?,
        dataSource: ArrayList<MessagesByGroup>,
        receivedViewer: ArrayList<MessageViewer>,
        viewerMessageHandler: ViewerMessageHandler?,
        receivedViewerNotSeen: ArrayList<MessageViewer>
    ){
        val message = dataSource[bindingAdapterPosition]
        binding.tvNameAudio.text = message.sender.full_name
        Utils.getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
        binding.tvTimeAudio.text = TimeFormatHelper.getDateFromStringDay(
            message.created_at
        )
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
                            .updateViewHolder(id, itemView.tag as AudioInformationLeftHolder)
                        task.start()
                        return@forEachIndexed
                    }
                }
            }
        }
        /**
         * visible and gone avatar user
         * */
        if (bindingAdapterPosition >= 0 && (bindingAdapterPosition + 1) < dataSource.size) {
            if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition + 1].sender.member_id) {
                binding.tvNameAudio.visibility = View.GONE
                binding.cvAudioUser.visibility = View.INVISIBLE
            } else {
                binding.tvNameAudio.visibility = View.VISIBLE
                binding.cvAudioUser.visibility = View.VISIBLE
            }
        }

        /**
         * time message
         * */
        if (message.today == 1) {
            binding.tvTimeHeader.visibility = View.VISIBLE
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.visibility = View.GONE
        /**
         * create and set time audio
         * */

//        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        try {
            val linkOriginal =
                FileUtils.getInternalStogeDir(message.files[0].name_file ?: "", context) ?: ""
            if (!File(linkOriginal).exists()) {
                mediaPlayer?.setDataSource(
                    ChatUtils.getUrl(
                        message.files[0].link_original,
                        configNodeJs
                    )
                )
            } else {
                mediaPlayer?.setDataSource(linkOriginal)
            }
            mediaPlayer?.prepare() // might take long for buffering.
        } catch (e: IOException) {
            e.printStackTrace()
        }
        binding.seekBarAudio.max = mediaPlayer?.duration ?: 0
        binding.seekBarAudio.tag = bindingAdapterPosition
        binding.seekBarAudio.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        binding.tvTimePlayAudio.text = TimeFormatHelper.calculateDuration(mediaPlayer?.duration ?: 0)
        binding.imgPlayAudio.setOnClickListener {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer.start()
                binding.imgPlayAudio.setImageResource(R.drawable.ic_pause_chat)
                run = Runnable {
                    // Updating SeekBar every 100 miliseconds
                    binding.seekBarAudio.progress = mediaPlayer.currentPosition
                    seekHandler.postDelayed(run!!, 100)
                    //For Showing time of audio(inside runnable)
                    if (mediaPlayer.currentPosition != 0) {
                        binding.tvTimePlayAudio.text = TimeFormatHelper.calculateDuration(mediaPlayer.currentPosition)
                    } else {
                        //Displaying total time if audio not playing
                        binding.tvTimePlayAudio.text = TimeFormatHelper.calculateDuration(mediaPlayer.duration)
                    }
                }
                run?.run()
            } else {
                mediaPlayer.pause()
                binding.imgPlayAudio.setImageResource(R.drawable.ic_play_button)
            }
        }
        mediaPlayer?.setOnCompletionListener {
            binding.imgPlayAudio.setImageResource(R.drawable.ic_play_button)
        }
        /**
         * Set reaction
         * */
//        if (message.reactions.reactions_count == 0) {
//            binding.reaction.lnReaction.visibility = View.GONE
//            binding.reaction.imgReactionPress.visibility = View.VISIBLE
//            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
//
//        binding.reaction.imgReactionPress.visibility = View.GONE


        /**
         * Message viewed
         * */
        binding.tvSeen.hide()
        binding.tvReceived.hide()
        binding.rcViewerSeen.hide()
        binding.rcViewerReceived.hide()
        if(bindingAdapterPosition == 0){
            binding.tvStatusView.text = context.getString(R.string.received)
            if(receivedViewerNotSeen.size > 0){
                binding.tvReceived.show()
                binding.rcViewerReceived.show()
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerReceived, receivedViewerNotSeen, viewerMessageHandler)
            }
            if (receivedViewer.size > 0){
                binding.tvSeen.show()
                binding.rcViewerSeen.show()
                binding.tvStatusView.text = context.getString(R.string.seen)
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerSeen, receivedViewer, viewerMessageHandler)
            }
        }
    }
    private fun updateDownloaded() {
        EventBus.getDefault().post(EventBusDownloadSuccess(randomkey))
    }

    private fun updateNotDownLoaded() {

    }

    private fun updateDownLoading() {
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

        private fun checkCurrentHolder(task: BaseDownloadTask): AudioInformationLeftHolder? {
            return if (task.tag is AudioInformationLeftHolder) {
                val tag = task.tag as AudioInformationLeftHolder
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