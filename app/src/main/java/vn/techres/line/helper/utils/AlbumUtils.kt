package vn.techres.line.helper.utils

import android.content.Context
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancytoastlib.FancyToast
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.album.EventBus.EventBusImageUpload
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.helper.PrefUtils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.FileUtils.getMimeType
import java.io.File

object AlbumUtils {
    var size = 0
    fun configRecyclerViewFolder(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?,
        context: Context
    ) {
        val gridLayoutManager = GridLayoutManager(context, 4)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    fun configRecyclerViewImage(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?,
        context: Context
    ) {
        val gridLayoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    fun uploadFile(
        file: File,
        name: String,
        configNodeJs: ConfigNodeJs,
        user: User,
        context: Context,
        categoryId: String,
        requireActivity: FragmentActivity
    ) {
        val nameFile: String = getNameFileFormatTime(name)!!
        val serverUrlString = String.format(
            "%s/api-upload/upload-file/%s/%s",
            configNodeJs.api_ads,
            categoryId,
            nameFile
        )
        val paramNameString = requireActivity.getString(R.string.send_file)
        try {
            MultipartUploadRequest(
                requireActivity,
                serverUrlString
            )
                .setMethod("POST")
                .addFileToUpload(file.path, paramNameString)
                .addHeader(context.getString(R.string.Authorization), user.nodeAccessToken)
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        requireActivity,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .subscribe(requireActivity, requireActivity, object : RequestObserverDelegate {
                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                        WriteLog.d("onCompleted", "onCompleted")
                    }

                    override fun onCompletedWhileNotObserving() {
                        WriteLog.d("onCompletedWhileNotObserving", "onCompletedWhileNotObserving")
                    }

                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                        FancyToast.makeText(
                            requireActivity.baseContext,
                            requireActivity.resources.getString(R.string.sever_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                    }

                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                        WriteLog.d("onProgress", "onProgress")
                    }

                    override fun onSuccess(
                        context: Context,
                        uploadInfo: UploadInfo,
                        serverResponse: ServerResponse
                    ) {
                        size++
                        if(size == PrefUtils.getInstance().getInt("NumberFile", 0))
                        {
                            Toast.makeText(context, "Tải ảnh thành công", Toast.LENGTH_LONG).show()
                            EventBus.getDefault().post(EventBusImageUpload(1))
                            size = 0
                        }
                    }
                })
            // these are the different exceptions that may be thrown
        } catch (exc: Exception) {
            WriteLog.d("uploadFile", exc.message + "")
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getNameFileFormatTime(path: String?): String? {
        return String.format(
            "%s.%s",
            System.currentTimeMillis(),
            path?.let { getMimeType(it) }
        )
    }
    fun setForceShowIcon(popupMenu: PopupMenu) {
        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popupMenu]!!
                    val classPopupHelper = Class.forName(
                        menuPopupHelper
                            .javaClass.name
                    )
                    val setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon", Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}