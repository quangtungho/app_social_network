package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication.Companion.mSocket
import vn.techres.line.adapter.chat.ListBirthDayCartAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.BirthDayCard
import vn.techres.line.data.model.chat.request.BirthDayRequest
import vn.techres.line.data.model.chat.request.Position
import vn.techres.line.data.model.chat.response.BirthDayCardResponse
import vn.techres.line.data.model.chat.response.FileNodeJsResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivitySendBirthDayCartBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ClickBirthDayCard
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.widget.TechResEditTextBirthDay
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SendBirthDayCardActivity : BaseBindingActivity<ActivitySendBirthDayCartBinding>(),
    ClickBirthDayCard {
    private var mList = ArrayList<BirthDayCard>()
    private var mAdapter: ListBirthDayCartAdapter? = null
    private var configNodeJs: ConfigNodeJs? = null
    var edittext: TechResEditTextBirthDay? = null
    private var birthDayCardChosse: BirthDayCard? = null
    private val application = TechResApplication()
    private var memberId: Int? = 0
    private var userInvitedId: Int? = 0
    private var groupId: String? = ""
    private var eventImage: String? = ""
    override val bindingInflater: (LayoutInflater) -> ActivitySendBirthDayCartBinding
        get() = ActivitySendBirthDayCartBinding::inflate

    override fun onSetBodyView() {
        mSocket = application.getSocketInstance(this)
        mSocket?.connect()
        edittext = TechResEditTextBirthDay(applicationContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        binding.header.toolbarBack.setOnClickListener { finish() }
        binding.header.toolbarTitle.text = "Gửi thiệp mừng sinh nhật"
        mAdapter = ListBirthDayCartAdapter(applicationContext)
        ChatUtils.configRecyclerHoziView(binding.rclBirthDayCart, mAdapter)
        mAdapter?.setClickBirthDayCard(this)
        getBirthDayCard()
        binding.btnSendCard.setOnClickListener {
            binding.edtMessage.setBackgroundColor(Color.TRANSPARENT)
            val bitmapImage = getScreenshotToView(binding.rootScreenshot)

            val pathFile = saveBitmap(bitmapImage)
            val nameFile: String = getNameFileFormatTime(pathFile)

            uploadFile(pathFile!!, nameFile)
            getFile(nameFile, bitmapImage.width, bitmapImage.height)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")
    override fun onClick(position: Int) {
        mList.forEachIndexed { index, birthDayCard ->
            if (birthDayCard.isSelected) {
                birthDayCard.isSelected = false
                mAdapter?.notifyItemChanged(index)
            }
        }
        memberId = intent.getIntExtra(
            TechResEnumChat.MEMBER_ID.toString(), 0
        )
        userInvitedId = intent.getIntExtra(
            TechResEnumChat.USER_INVITE_ID.toString(), 0
        )
        groupId = intent.getStringExtra(TechResEnumChat.GROUP_ID.toString())
        birthDayCardChosse = mList[position]
        mList[position].isSelected = true
        mAdapter?.notifyItemChanged(position)
        configNodeJs?.let {
            ChatUtils.callPhotoBirtday(
                binding.imvBirthDayCard,
                mList[position].greeting_card_url,
                it
            )
        }
        val ratio = 3750 / binding.imvBirthDayCard.width
        binding.edtMessage.layoutParams.height =
            (mList[position].height / ratio)
        binding.edtMessage.layoutParams.width =
            (mList[position].width / ratio)
        (binding.imvBirthDayCard.width / 2 - mList[position].margin_left.toFloat() / ratio)
        (binding.imvBirthDayCard.height / 2 - mList[position].margin_top.toFloat() / ratio)
        binding.edtMessage.x =
            mList[position].margin_left.toFloat() / ratio
        binding.edtMessage.y =
            mList[position].margin_top.toFloat() / ratio
    }

    private fun getBirthDayCard() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/greeting-cards?status=1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getBirthDayCard(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BirthDayCardResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BirthDayCardResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mList.clear()
                        mList.addAll(response.data)
                        mList[0].isSelected = true
                        birthDayCardChosse = mList[0]
                        mAdapter?.setDataSource(mList)
                        configNodeJs?.let {
                            ChatUtils.callPhotoBirtday(
                                binding.imvBirthDayCard,
                                mList[0].greeting_card_url,
                                it
                            )
                        }
                        val ratio = 3750 / binding.imvBirthDayCard.width
                        binding.edtMessage.layoutParams.height =
                            (mList[0].height / ratio)
                        binding.edtMessage.layoutParams.width =
                            (mList[0].width / ratio)
                        binding.edtMessage.x =
                            mList[0].margin_left.toFloat() / ratio
                        binding.edtMessage.y =
                            mList[0].margin_top.toFloat() / ratio
                    }
                }
            })
    }

    private fun getScreenshotToView(view: View): Bitmap {
        var view = view
        if (view.visibility != View.VISIBLE) {
            view =
                getNextView(
                    view
                )
        }
        //Define a bitmap with the same size as the view
        val returnedBitmap: Bitmap = when (view) {
            is ScrollView -> {
                Bitmap.createBitmap(
                    (view as ViewGroup).getChildAt(0).width,
                    (view as ViewGroup).getChildAt(0).height,
                    Bitmap.Config.ARGB_8888
                )
            }
            is RecyclerView -> {
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                Bitmap.createBitmap(view.getWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888)
            }
            else -> {
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            }
        }

        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun getNextView(view: View): View {
        var view = view
        if (view.parent != null && view.parent is ViewGroup) {
            val group = view.parent as ViewGroup
            var child: View
            var getNext = false
            //Iterate through all views from parent
            for (i in 0 until group.childCount) {
                child = group.getChildAt(i)
                if (getNext) {
                    //Make sure the view is visible, else iterate again until we find a visible view
                    if (child.visibility == View.VISIBLE) {
                        view = child
                    }
                }
                //Iterate until we find out current view,
                // then we want to get the NEXT view
                if (child.id == view.id) {
                    getNext = true
                }
            }
        }
        return view
    }

    private fun saveBitmap(bitmap: Bitmap): String? {
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val dir = File(filepath.absolutePath + "/ALOLINE/")
        if (!dir.exists()) {
            dir.mkdir()
        }
        val outputStream: FileOutputStream
        val file = File(dir, System.currentTimeMillis().toString() + ".png")
        outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        try {
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.path
    }

    private fun getFile(
        nameFile: String,
        height: Int,
        width: Int
    ) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s",
            "/api-upload/get-link-server-chat?group_id=",
            groupId,
            "&type=",
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            "&name_file=",
            nameFile,
            "&width=",
            width,
            "&height=",
            height
        )

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getImageChat(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FileNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FileNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        WriteLog.d("uploading...", "success")
                        eventImage = response.data.link_original
                    } else {
                        Toast.makeText(baseContext, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            })
    }

    private fun uploadFile(
        path: String,
        name: String,
    ) {
        val serverUrlString = String.format(
            "%s/api-upload/upload-file-server-chat/%s/%s/%s",
            configNodeJs!!.api_ads,
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            groupId,
            name
        )
        val paramNameString = resources.getString(R.string.send_file)
        try {
            MultipartUploadRequest(
                this,
                serverUrlString
            )
                .setMethod("POST")
                .addFileToUpload(path, paramNameString)
                .addHeader(
                    getString(R.string.Authorization),
                    CurrentUser.getCurrentUser(baseContext).nodeAccessToken
                )
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        baseContext,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .subscribe(this, this, object : RequestObserverDelegate {
                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

                    }

                    override fun onCompletedWhileNotObserving() {

                    }

                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                    }

                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                    }

                    override fun onSuccess(
                        context: Context,
                        uploadInfo: UploadInfo,
                        serverResponse: ServerResponse
                    ) {
                        val birthDay = BirthDayRequest()
                        birthDay.member_id = memberId
                        birthDay.user_invited_id = userInvitedId
                        birthDay.group_id = groupId
                        birthDay.background = eventImage
                        birthDay.message_type = TechResEnumChat.INVITE_EVENT.toString()
                        birthDay.message = binding.edtMessage.text?.toString()
                        birthDay.position = Position(
                            birthDayCardChosse?.margin_top,
                            birthDayCardChosse?.margin_left,
                            birthDayCardChosse?.width,
                            birthDayCardChosse?.height
                        )
                        try {
                            val jsonObject = JSONObject(Gson().toJson(birthDay))
                            mSocket?.emit(
                                TechResEnumChat.CHAT_INVITE_CARD_ALOLINE.toString(),
                                jsonObject
                            )

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        finish()
                    }
                })
        } catch (exc: Exception) {
            WriteLog.d("uploadFile", exc.message + "")
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getNameFileFormatTime(path: String?): String {
        return String.format(
            "%s.%s",
            System.currentTimeMillis(), getMimeType(path!!)
        )
    }

    fun getMimeType(url: String): String? {
        try {
            return url.substring(url.lastIndexOf(".") + 1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

}