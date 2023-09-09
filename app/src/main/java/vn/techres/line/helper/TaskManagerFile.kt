package vn.techres.line.helper

import android.text.TextUtils
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadConnectListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import com.liulishuo.filedownloader.util.FileDownloadUtils
import vn.techres.line.data.model.chat.FileNodeJs
import vn.techres.line.fragment.chat.MediaFragment
import java.lang.ref.WeakReference

class TaskManagerFile {
    private object HolderClass {
        val INSTANCE = TaskManagerFile()
    }
    private var listMessage : ArrayList<FileNodeJs> = ArrayList()

    fun getImpl(): TaskManagerFile {
        return HolderClass.INSTANCE
    }

    fun addMessage(fileNodeJs: FileNodeJs){
        listMessage.add(fileNodeJs)
        listMessage.distinct()
    }

    private val taskSparseArray = SparseArray<BaseDownloadTask>()

    fun addTaskForViewHolder(task: BaseDownloadTask) {
        WriteLog.d("addTaskForViewHolder", task.id.toString() + "")
        taskSparseArray.put(task.id, task)
    }

    fun removeTaskForViewHolder(id: Int) {
        taskSparseArray.remove(id)
    }

    fun updateViewHolder(
        id: Int,
        holder: RecyclerView.ViewHolder?
    ) {
        val task : BaseDownloadTask = taskSparseArray[id] ?: return
        task.tag = holder
    }

    private fun releaseTask() {
        taskSparseArray.clear()
    }

    private var listener: FileDownloadConnectListener? = null

    private fun registerServiceConnectionListener(activityWeakReference: WeakReference<Fragment>?) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener)
        }
        listener = object : FileDownloadConnectListener() {
            override fun connected() {
                if (activityWeakReference?.get() == null
                ) {
                    return
                }
                when(activityWeakReference.get()){
                    is MediaFragment ->{
                        (activityWeakReference.get() as MediaFragment).postNotifyDataChanged()
                    }
                }
            }

            override fun disconnected() {
                if (activityWeakReference?.get() == null) {
                    return
                }
                when(activityWeakReference.get()){
                    is MediaFragment ->{
                        (activityWeakReference.get() as MediaFragment).postNotifyDataChanged()
                    }
                }
            }
        }
        FileDownloader.getImpl().addServiceConnectListener(listener)
    }

    private fun unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener)
        listener = null
    }

    fun onCreate(activityWeakReference: WeakReference<Fragment>) {
        if (!FileDownloader.getImpl().isServiceConnected) {
            FileDownloader.getImpl().bindService()
            registerServiceConnectionListener(activityWeakReference)
        }
    }

    fun onDestroy() {
        unregisterServiceConnectionListener()
        releaseTask()
    }

    fun isReady(): Boolean {
        return FileDownloader.getImpl().isServiceConnected
    }

    operator fun get(randomKey: String?): FileNodeJs? {
        return listMessage.find { it.link_original == randomKey }
    }

//    fun getById(id: Int): RecyclerView.ViewHolder? {
//        modelList?.let {
//            for (model in it) {
//                when(model){
//                    is FileMessageRightHolder ->{
//                        if (model.id == id) {
//                            return model
//                        }
//                    }
//                    is FileMessageLeftHolder ->{
//                        return model
//                    }
//                }
//            }
//        }
//
//        return null
//    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    fun isDownloaded(status: Int): Boolean {
        return status == FileDownloadStatus.completed.toInt()
    }

    fun getStatus(id: Int, path: String?): Int {
        return FileDownloader.getImpl().getStatus(id, path).toInt()
    }

    fun getTotal(id: Int): Long {
        return FileDownloader.getImpl().getTotal(id)
    }

    fun getSoFar(id: Int): Long {
        return FileDownloader.getImpl().getSoFar(id)
    }

//    fun getTaskCounts(): Int {
//        return modelList!!.size
//    }


    fun createPath(url: String?): String? {
        return if (TextUtils.isEmpty(url)) {
            null
        } else FileDownloadUtils.getDefaultSaveFilePath(url)
    }
}