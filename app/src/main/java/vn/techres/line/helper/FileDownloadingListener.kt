package vn.techres.line.helper

import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener

open class FileDownloadingListener : FileDownloadListener(){
    override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {

    }

    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
    }

    override fun completed(task: BaseDownloadTask?) {
    }

    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
    }

    override fun error(task: BaseDownloadTask?, e: Throwable?) {
    }

    override fun warn(task: BaseDownloadTask?) {
    }
}