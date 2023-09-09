package vn.techres.line.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidadvance.topsnackbar.TSnackbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.shashank.sony.fancytoastlib.FancyToast
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.helper.fresco.view.ImageViewer
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils.FOLDER_NAME
import vn.techres.photo.utils.FileUtils.*
import java.io.File
import java.io.FileInputStream
import java.io.UnsupportedEncodingException
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.NetworkInterface
import java.net.URL
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random


object Utils {
    const val recordId: Byte = 107
    private var imageBuilder: ImageViewer? = null

    @SuppressLint("HardwareIds")
    fun getIDDevice(): String? {
        return Settings.Secure.getString(
            TechResApplication.applicationContext().baseContext?.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun getResources(context: Context?): Resources? {
        return context?.resources
    }

    fun getString(context: Context?, stringID: Int): String? {
        return getResources(context)?.getString(stringID)
    }

    fun getString(stringID: Int): String? {
        return getResources(
            TechResApplication.applicationContext().baseContext
        )?.getString(stringID)
    }

    fun stopMedia() {
        imageBuilder.let {
            it?.stopVideo()
        }
    }

    fun getFileNameFromURL(url: String?): String {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = lastQMPos.coerceAtMost(lastHashPos)
        return url.substring(startIndex, endIndex)
    }

    fun getRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun getNameFileToPath(nameFile: String): String {
        return nameFile.substring(nameFile.lastIndexOf("/") + 1)
    }

    @Throws(Throwable::class)
    fun retriveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        val bitmap: Bitmap?
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath, HashMap())
            //   mediaMetadataRetriever.setDataSource(videoPath)
            bitmap = mediaMetadataRetriever.frameAtTime!!
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    fun getMediaGlide(imageView: ImageView, string: String?) {
        Glide.with(imageView)
            .load(string)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
//            .placeholder(R.drawable.logo_aloline_placeholder)
            .error(R.drawable.logo_aloline_placeholder)
            .into(imageView)
    }

    fun showImage(imageView: ImageView?, string: String?) {
        imageView?.let {
            Glide.with(imageView)
                .load(string)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
//                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(imageView)
        }
    }

    fun getGlide(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
//                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(it)
        }
    }

    fun getGlideVideo(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.color.gray_load_video)
                .error(R.color.gray_load_video)
                .into(it)
        }
    }

    fun getImage(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format("%s%s", configNodeJs.api_ads, string)
                )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(it)
        }
    }

    fun getImageCall(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format("%s%s", configNodeJs.api_ads, string)
                )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .placeholder(R.drawable.backgroundcallalolineblur)
                .error(R.drawable.backgroundcallalolineblur)
                .into(it)
        }
    }

    fun getAvatarCircle(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format("%s%s", configNodeJs.api_ads, string)
                )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(it)
        }
    }

    fun downLoadFile(context: Context?, string: String, configNodeJs: ConfigNodeJs) {
        context?.let {
            val downloadManager = it.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    string
                )
            )
            File(
                Environment.getExternalStorageDirectory(),
                Environment.DIRECTORY_DOWNLOADS + File.separator + FOLDER_NAME
            )
            val request = DownloadManager.Request(uri)
            request.setTitle(
                getFileNameFromURL(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
            )
            request.setDescription(it.resources.getString(R.string.download))
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                FOLDER_NAME + File.separator +
                        getFileNameFromURL(
                            String.format(
                                "%s%s",
                                configNodeJs.api_ads,
                                string
                            )
                        )
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager.enqueue(request)
            FancyToast.makeText(
                context,
                context.resources.getString(R.string.downloading),
                FancyToast.LENGTH_LONG,
                FancyToast.INFO,
                false
            ).show()
        }
    }

    fun getPath(context: Context?, uri: Uri?): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    id.toLong()

                )
                return getDataColumn(
                    context,
                    contentUri,
                    null,
                    null
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    TechResEnumChat.TYPE_IMAGE.toString() -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    TechResEnumChat.TYPE_VIDEO.toString() -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    TechResEnumChat.TYPE_AUDIO.toString() -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        }
        return null
    }

    fun setSnackBar(view: View, context: Context?) {
        val snackBar: TSnackbar = TSnackbar.make(
            view,
            context!!.resources.getString(R.string.not_support_mobile_recharge),
            TSnackbar.LENGTH_LONG
        )
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView: View = snackBar.view
        snackBarView.setBackgroundColor(Color.parseColor("#FFFF00"))
        val textView = snackBarView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackBar.show()
    }

    fun setSnackBar(view: View, s: String) {
        val snackBar: TSnackbar = TSnackbar.make(
            view,
            s,
            TSnackbar.LENGTH_LONG
        )
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView: View = snackBar.view
        snackBarView.setBackgroundColor(Color.parseColor("#FFA233"))
        val textView =
            snackBarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackBar.show()
    }

    fun base64(string: String?): String? {
        val data: ByteArray
        var base64: String? = ""
        try {
            data = string!!.toByteArray(charset("UTF-8"))
            base64 = Base64.encodeToString(
                data,
                Base64.NO_WRAP or Base64.URL_SAFE
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return base64
    }

    /**
     * Version App
     * */
    fun getVersionBuild(context: Context?): String {
        val packageManager = context?.packageManager
        val packageName = context?.packageName ?: ""
        val myVersionName: String?
        try {
            myVersionName = packageManager?.getPackageInfo(packageName, 0)?.versionName ?: "1.0.0"
            return myVersionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "1.0.0"
    }

    fun hideKeyboard(v: View) {
        val imm =
            v.context.applicationContext.getSystemService("input_method") as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }



    fun View.show() {
        visibility = View.VISIBLE
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun View.ifShow(b: Boolean) {
        if (b) {
            show()
        } else {
            hide()
        }
    }

    private fun rotateImage(bm: Bitmap?, rotation: Int): Bitmap? {
        if (rotation != 0 && bm != null) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
        }
        return bm
    }

    fun getBitmapRotationImage(file: File): Bitmap? {
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(file.path, bmOptions)
        val exif = ExifInterface(
            file.absolutePath
        )
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, -270)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, -180)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, -90)
            }
            else -> {
                bitmap
            }
        }
    }

    fun getBitmapRotationVideo(file: File): Bitmap? {
        val retriever = MediaMetadataRetriever()
        val inputStream = FileInputStream(file.absolutePath)
        retriever.setDataSource(inputStream.fd)
        val bitmap = retriever.frameAtTime
        val exif = ExifInterface(
            file.absolutePath
        )
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                rotateImage(bitmap, -270)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                rotateImage(bitmap, -180)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                rotateImage(bitmap, -90)
            }
            else -> {
                bitmap
            }
        }
    }

    fun setSizeFile(size: Int): String {
        val df = DecimalFormat("0.00")
        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb
        return when {
            size < sizeMb -> df.format((((size / sizeKb) * 100.0).roundToInt() / 100.0)) + " KB"
            size < sizeGb -> df.format((((size / sizeMb) * 100.0).roundToInt() / 100.0)) + " MB"
            size < sizeTerra -> df.format((((size / sizeGb) * 100.0).roundToInt() / 100.0)) + " GB"
            else -> {
                df.format(((size * 100.0).roundToInt() / 100.0)) + "TB"
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getViewBottomInset(rootView: View?): Int {
        try {
            val attachInfoField = View::class.java.getDeclaredField("mAttachInfo")
            attachInfoField.isAccessible = true
            val attachInfo = attachInfoField[rootView]
            if (attachInfo != null) {
                val stableInsetsField = attachInfo.javaClass.getDeclaredField("mStableInsets")
                stableInsetsField.isAccessible = true
                return (stableInsetsField[attachInfo] as Rect).bottom
            }
        } catch (noSuchFieldException: NoSuchFieldException) {
            noSuchFieldException.printStackTrace()
        } catch (illegalAccessException: IllegalAccessException) {
            illegalAccessException.printStackTrace()
        }
        return 0
    }

    fun getDisplayMetrics(context: Context?): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        val windowManager =
            context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics
    }

    fun permissionsIsGranted(context: Context, perms: Array<String>): Boolean {
        for (perm in perms) {
            val checkVal: Int =
                PermissionChecker.checkCallingOrSelfPermission(context, perm)
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("HardwareIds")
    fun generateID(activity: Activity?): String? {
        var deviceId: String? = ""
        activity?.let {
            @SuppressLint("HardwareIds")
            deviceId = Settings.Secure.getString(
                activity.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if ("9774d56d682e549c" == deviceId) {
                deviceId =
                    (activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                        .deviceId
                if (deviceId == null) {
                    val tmpRand = Random
                    deviceId = java.lang.String.valueOf(tmpRand.nextLong())
                }
                return deviceId
            }
        }
        return deviceId
    }

    fun isStringInt(s: String): Boolean {
        return try {
            s.toInt()
            true
        } catch (ex: NumberFormatException) {
            false
        }
    }

    fun getCompleteAddressString(context: Context, latitude: Double, longitude: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(
                latitude,
                longitude, 1
            )
            strAdd = if (addresses != null) {

                val returnedAddress: Address = addresses[0]
                WriteLog.d(
                    "getCompleteAddressString",
                    returnedAddress.toString()
                )
                returnedAddress.getAddressLine(0).toString()
            } else {
                ""
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    fun getCompleteWardString(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(
                latitude,
                longitude, 1
            )
            strAdd = if (addresses != null && addresses.isNotEmpty()) {
                if (!StringUtils.isNullOrEmpty(addresses[0].subAdminArea)) {
                    addresses[0].subAdminArea
                } else {
                    ""
                }
            } else {
                ""
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return strAdd
    }

    @ColorInt
    fun convertColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    fun resizeImage(context: Context, url: String, imageView: ImageView, view: View) {
        try {
            if (url.contains("public/")) {
                Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .load(ChatUtils.getUrl(url, CurrentConfigNodeJs.getConfigNodeJs(context)))
                    .into(object : CustomTarget<Bitmap?>() {

                        override fun onLoadCleared(placeholder: Drawable?) {
                            //onLoadCleared
                        }
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            val bitmap: Bitmap = resizeBitmap(
                                resource,
                                TechResApplication.widthView * 3 / 4
                            )
                            imageView.layoutParams.height = bitmap.height
                            imageView.layoutParams.width = bitmap.width
                            view.layoutParams.height = bitmap.height
                            view.layoutParams.width = bitmap.width
                            imageView.setImageBitmap(bitmap)
                        }
                    })
            } else {
                Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .load(url)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            val bitmap: Bitmap = resizeBitmap(
                                resource,
                                TechResApplication.widthView * 3/4
                            )
                            imageView.layoutParams.height = bitmap.height
                            imageView.layoutParams.width = bitmap.width
                            view.layoutParams.height = bitmap.height
                            view.layoutParams.width = bitmap.width
                            imageView.setImageBitmap(bitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            //onLoadCleared
                        }
                    })
            }
        } catch (e: Exception) {
            //e
        }
    }

    fun resizeVideo(context: Context, url: String, view: View, imageView: ImageView) {
        try {
            if (url.contains("public/")) {
                Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(ChatUtils.getUrl(url, CurrentConfigNodeJs.getConfigNodeJs(context)))
                    .into(object : CustomTarget<Bitmap?>() {

                        override fun onLoadCleared(placeholder: Drawable?) {
                            //onLoadCleared
                        }
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            val bitmap: Bitmap = resizeBitmapVideo(
                                resource,
                                TechResApplication.widthView * 3 / 4
                            )
                            view.layoutParams.height = bitmap.height
                            view.layoutParams.width = bitmap.width
                            imageView.layoutParams.height = bitmap.height
                            imageView.layoutParams.width = bitmap.width
                            imageView.setImageBitmap(bitmap)
                        }
                    })
            } else {
                Glide.with(TechResApplication.applicationContext())
                    .asBitmap()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            val bitmap: Bitmap = resizeBitmapVideo(
                                resource,
                                TechResApplication.widthView * 3 / 4
                            )
                            view.layoutParams.height = bitmap.height
                            view.layoutParams.width = bitmap.width
                            imageView.layoutParams.height = bitmap.height
                            imageView.layoutParams.width = bitmap.width
                            imageView.setImageBitmap(bitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            //onLoadCleared
                        }
                    })
            }
        } catch (e: Exception) {
            //e
        }
    }

    fun resizeBitmapVideo(source: Bitmap, maxLength: Int): Bitmap {
        return try {
            if (source.height > source.width) {
                val targetHeight = TechResApplication.heightView / 2
                if (source.height <= targetHeight) { // if image already smaller than the required height
                    return source
                }
                val aspectRatio = source.width.toDouble() / source.height.toDouble()
                var targetWidth = (targetHeight * aspectRatio).toInt()
                if (targetWidth > maxLength) {
                    targetWidth = maxLength
                }
                Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
            } else if (source.height == source.width) {
                Bitmap.createScaledBitmap(source, maxLength, maxLength, false)
            } else {
                if (source.width <= maxLength) { // if image already smaller than the required height
                    return source
                }
                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (maxLength * aspectRatio).toInt()
                Bitmap.createScaledBitmap(source, maxLength, targetHeight, false)
            }
        } catch (e: java.lang.Exception) {
            source
        }
    }

    fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        return try {
            if (source.height > source.width) {
                val targetHeight = TechResApplication.heightView / 2
                if (source.height <= targetHeight) { // if image already smaller than the required height
                    return source
                }
                val aspectRatio = source.width.toDouble() / source.height.toDouble()
                var targetWidth = (targetHeight * aspectRatio).toInt()
                if (targetWidth > maxLength) {
                    targetWidth = maxLength
                }
                Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
            } else if (source.height == source.width) {
                Bitmap.createScaledBitmap(source, maxLength, maxLength, false)
            } else {
                if (source.width <= maxLength) { // if image already smaller than the required height
                    return source
                }
                val aspectRatio = source.height.toDouble() / source.width.toDouble()
                val targetHeight = (maxLength * aspectRatio).toInt()
                Bitmap.createScaledBitmap(source, maxLength, targetHeight, false)
            }
        } catch (e: java.lang.Exception) {
            source
        }
    }

    fun getRoundedRectBitmap(bitmap: Bitmap?, pixels: Int): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, pixels, pixels)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawCircle(50F, 50F, 50F, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, rect, rect, paint)
            }
        } catch (ignored: NullPointerException) {
            //ignored
        }
        return result
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    fun RecyclerView?.getCurrentPosition(): Int {
        return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    /** Returns the consumer friendly device name  */
    fun getDeviceName(): String {
        return Build.MANUFACTURER + " - " + Build.MODEL
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr: String = addr.hostAddress ?: ""
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            //ex
        } // for now eat exceptions
        return ""
    }

    fun View.setClick() {
        isEnabled = false
        android.os.Handler().postDelayed({ isEnabled = true }, 1000)
    }

    fun splitMaxName(name: String): String? {
        val separated = name.split(" ".toRegex()).toTypedArray()
        return if (separated.size >= 2) {
            String.format(
                "%s %s", separated[separated.size - 2],
                separated[separated.size - 1]
            )
        } else {
            name
        }
    }

    fun formatNameFile(string: String): String {
        val re = Regex("[^A-Za-z0-9 ]")
        var name = ""
        name = string.replace("\\s+".toRegex(), "")
        name = re.replace(name, "")
        name = if (name.contains("png")) {
            name.replace("png", ".png");
        } else {
            name.replace("jpg", ".jpg");
        }
        return name
    }

    fun getBitmap(urlImage: String?, context: Context): Bitmap {
        val bitmapAvatar = if (StringUtils.isNullOrEmpty(urlImage)) {
            Glide.with(context)
                .asBitmap()
                .load(
                    R.drawable.logo_aloline_placeholder
                )
                .circleCrop()
                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .submit(100, 100)
                .get()
        } else {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(urlImage)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(
                        RequestOptions().placeholder(R.drawable.logo_aloline_placeholder)
                            .error(R.drawable.logo_aloline_placeholder)
                    )
                    .submit(100, 100)
                    .get()
            } catch (e: Exception) {
                Glide.with(context)
                    .asBitmap()
                    .load(
                        R.drawable.logo_aloline_placeholder
                    )
                    .circleCrop()
                    .placeholder(R.drawable.logo_aloline_placeholder)
                    .error(R.drawable.logo_aloline_placeholder)
                    .submit(100, 100)
                    .get()
            }
        }
        return bitmapAvatar
    }

    //Check network
    fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    //Get time in format
    fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        val formatted = current.format(formatter)
        println("Current Date and Time is: $formatted")
        return formatted
    }

    //Handle load data main activity
    fun saveCacheManagerMainActivity() {
        CacheManager.getInstance().put(TechresEnum.CURRENT_MAIN.toString(), TechresEnum.CURRENT_MAIN.toString())
        CacheManager.getInstance().put(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString(), "false").toString()
        CacheManager.getInstance().put(TechresEnum.CHECK_UPDATE_AVATAR.toString(), "false").toString()
        //Set volume infor
        CacheManager.getInstance().put(TechresEnum.CHECK_VOLUME_MUTE.toString(), "true")
    }

    fun TextView.makeLinks(links: ArrayList<Pair<String, View.OnClickListener>>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            if (!link.first.contains("@All")) {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(textPaint: TextPaint) {
                        // use this to change the link color
                        textPaint.color = textPaint.linkColor
                        textPaint.typeface = Typeface.DEFAULT_BOLD
                        // toggle below value to enable/disable
                        // the underline shown below the clickable text
                        textPaint.isUnderlineText = false
                    }

                    override fun onClick(view: View) {
                        Selection.setSelection((view as TextView).text as Spannable, 0)
                        view.invalidate()
                        link.second.onClick(view)
                    }

                }
                startIndexOfLink = this.text.toString().indexOf(
                    link.first,
                    startIndexOfLink + 1
                )
                if (startIndexOfLink != -1) {
                    spannableString.setSpan(
                        clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(textPaint: TextPaint) {
                        // use this to change the link color
                        textPaint.color = textPaint.linkColor
                        textPaint.typeface = Typeface.DEFAULT_BOLD
                        // toggle below value to enable/disable
                        // the underline shown below the clickable text
                        textPaint.isUnderlineText = false
                    }

                    override fun onClick(view: View) {
                    }

                }
                startIndexOfLink = this.text.toString().indexOf(
                    link.first,
                    startIndexOfLink + 1
                )
                if (startIndexOfLink != -1) {
                    spannableString.setSpan(
                        clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}
