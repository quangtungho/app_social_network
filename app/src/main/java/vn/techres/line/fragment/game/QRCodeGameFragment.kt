package vn.techres.line.fragment.game

import android.Manifest
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.style.PictureSelectorUIStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.luck.picture.lib.tools.SdkVersionUtils
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.game.LuckyWheelActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.params.QRCodeGameParams
import vn.techres.line.data.model.response.game.JoinGameResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentQRCodeGameBinding
import vn.techres.line.fragment.game.luckywheel.LuckyWheelGameFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.Utils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.stream.Collectors


@Suppress("UNREACHABLE_CODE")
class QRCodeGameFragment :
    BaseBindingFragment<FragmentQRCodeGameBinding>(FragmentQRCodeGameBinding::inflate),
    QRCodeView.Delegate {
    private val luckyWheelActivity: LuckyWheelActivity?
        get() = activity as LuckyWheelActivity?

    private var listCode = ArrayList<String>()
    private var qrCodeGame: QRCodeGame? = null
    private var listDataToFile = ArrayList<QRCodeGame>()
    private var gson: Gson? = null
    private var roomId = ""
    private val requiredPermissionCamera = arrayOf(
        Manifest.permission.CAMERA
    )
    private val requiredPermissionCameraCode = 1002
    private var user = User()
    private var flashMode = 0
    private var cameraId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gson = GsonBuilder().create()
        requestMultiplePermissionCamera()
        arguments?.let {
            roomId = it.getString(TechresEnum.ROOM_ID.toString(), "")
        }
//        read()
        binding.imgBack.setOnClickListener {
            luckyWheelActivity?.finish()
            luckyWheelActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        BGAQRCodeUtil.setDebug(true)
        binding.zbarview.startCamera()
        binding.zbarview.startSpotAndShowRect()
        binding.zbarview.setDelegate(this)
        binding.zbarview.changeToScanQRCodeStyle()
        binding.zbarview.setType(BarcodeType.ONLY_QR_CODE, null)
        binding.zbarview.startSpotAndShowRect()
        binding.openFlashlight.setOnClickListener {
            if (flashMode == 0) {
                binding.zbarview.openFlashlight()
                binding.openFlashlight.setBackgroundResource(R.drawable.ic_flash_on)
                flashMode = 1
            } else if (flashMode == 1) {
                flashMode = 0
                binding.openFlashlight.setBackgroundResource(R.drawable.ic_flash_off)
                binding.zbarview.closeFlashlight()
            }
        }
        binding.chooseQrcdeFromGallery.setOnClickListener {
            val animationMode = AnimationType.DEFAULT_ANIMATION
            val language = 7
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .setPictureUIStyle(PictureSelectorUIStyle.ofSelectNumberStyle())
                .setPictureWindowAnimationStyle(PictureWindowAnimationStyle.ofDefaultWindowAnimationStyle())// 自定义相册启动退出动画
                .isWeChatStyle(false)// 是否开启微信图片选择风格
                .isUseCustomCamera(false)// 是否使用自定义相机
                .setLanguage(language)// 设置语言，默认中文
                .isPageStrategy(false)// 是否开启分页策略 & 每页多少条；默认开启
                .setRecyclerAnimationMode(animationMode)// 列表动画效果
                .isWithVideoImage(false)// 图片和视频是否可以同选,只在ofAll模式下有效
                .isMaxSelectEnabledMask(false)// 选择数到了最大阀值列表是否启用蒙层效果
                //.isAutomaticTitleRecyclerTop(false)// 连续点击标题栏RecyclerView是否自动回到顶部,默认true
                //.loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                //.setOutputCameraPath(createCustomCameraOutPath())// 自定义相机输出目录
                //.setButtonFeatures(CustomCameraView.BUTTON_STATE_BOTH)// 设置自定义相机按钮状态
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(0)// 最小选择数量
                .maxVideoSelectNum(0) // 视频最大选择数量
                //.minVideoSelectNum(1)// 视频最小选择数量
                //.closeAndroidQChangeVideoWH(!SdkVersionUtils.checkedAndroid_Q())// 关闭在AndroidQ下获取图片或视频宽高相反自动转换
                .imageSpanCount(4)// 每行显示个数
                .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                .closeAndroidQChangeWH(true)//如果图片有旋转角度则对换宽高,默认为true
                .closeAndroidQChangeVideoWH(!SdkVersionUtils.checkedAndroid_Q())// 如果视频有旋转角度则对换宽高,默认为false
                .isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                .isOriginalImageControl(true)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                //.bindCustomPlayVideoCallback(new MyVideoSelectedPlayCallback(getContext()))// 自定义视频播放回调控制，用户可以使用自己的视频播放界面
                //.bindCustomPreviewCallback(new MyCustomPreviewInterfaceListener())// 自定义图片预览回调接口
                //.bindCustomCameraInterfaceListener(new MyCustomCameraInterfaceListener())// 提供给用户的一些额外的自定义操作回调
                //.cameraFileName(System.currentTimeMillis() +".jpg")    // 重命名拍照文件名、如果是相册拍照则内部会自动拼上当前时间戳防止重复，注意这个只在使用相机时可以使用，如果使用相机又开启了压缩或裁剪 需要配合压缩和裁剪文件名api
                //.renameCompressFile(System.currentTimeMillis() +".jpg")// 重命名压缩文件名、 如果是多张压缩则内部会自动拼上当前时间戳防止重复
                //.renameCropFileName(System.currentTimeMillis() + ".jpg")// 重命名裁剪文件名、 如果是多张裁剪则内部会自动拼上当前时间戳防止重复
                .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选
                .isSingleDirectReturn(false)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                .isPreviewImage(true)// 是否可预览图片
                .isPreviewVideo(false)// 是否可预览视频
                //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                .isEnablePreviewAudio(false) // 是否可播放音频
                .isCamera(true)// 是否显示拍照按钮
                //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                //.isMultipleRecyclerAnimation(false)// 多图裁剪底部列表显示动画效果
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg,Android Q使用PictureMimeType.PNG_Q
                .isEnableCrop(false)// 是否裁剪
                //.basicUCropConfig()//对外提供所有UCropOptions参数配制，但如果PictureSelector原本支持设置的还是会使用原有的设置
                .isCompress(true)// 是否压缩
                //.compressQuality(80)// 图片压缩后输出质量 0~ 100
                .synOrAsy(false)//同步true或异步false 压缩 默认同步
                //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                //.compressSavePath(getPath())//压缩图片保存地址
                //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效 注：已废弃
                //.glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度 注：已废弃
                .isGif(true)// 是否显示gif图片
                //.isWebp(false)// 是否显示webp图片,默认显示
                //.isBmp(false)//是否显示bmp图片,默认显示
                .circleDimmedLayer(false)// 是否圆形裁剪
                //.setCropDimmedColor(ContextCompat.getColor(getContext(), R.color.app_color_white))// 设置裁剪背景色值
                //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// 设置圆形裁剪边框色值
                //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                .isOpenClickSound(true)// 是否开启点击声音
                //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                //.videoMinSecond(10)// 查询多少秒以内的视频
                //.videoMaxSecond(15)// 查询多少秒以内的视频
                //.recordVideoSecond(10)//录制视频秒数 默认60s
                //.isPreviewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                .cutOutQuality(90)// 裁剪输出质量 默认100
                .minimumCompressSize(100)// 小于多少kb的图片不压缩
                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                //.cropImageWideHigh()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                //.rotateEnabled(false) // 裁剪是否可旋转图片
                //.scaleEnabled(false)// 裁剪是否可放大缩小图片
                //.videoQuality()// 视频录制质量 0 or 1
                //.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
                .selectionData(null)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }

        binding.openTheFrontCamera.setOnClickListener {
            cameraId = if (cameraId == 0) {
                1
            } else {
                0
            }
            binding.zbarview.stopCamera()
            binding.zbarview.startCamera(cameraId)
            binding.zbarview.startSpotAndShowRect()
        }
    }

    private fun requestMultiplePermissionCamera() {
        requestPermissions(requiredPermissionCamera, requiredPermissionCameraCode,
            object : RequestPermissionListener {
                override fun onCallPermissionFirst(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    requestPermission.invoke()
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
                    binding.zbarview.stopCamera()
                    binding.zbarview.startCamera(cameraId)
                    binding.zbarview.startSpotAndShowRect()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.zbarview.onDestroy()
        luckyWheelActivity?.removeOnBackClick(this)
    }

    override fun onResume() {
        super.onResume()
        luckyWheelActivity?.setOnBackClick(this)
    }

    override fun onStop() {
        super.onStop()
        binding.zbarview.stopCamera()
    }

    private fun subCode(qrCodeGame: String): List<String> {
        return qrCodeGame.split(":").map { it.trim() }
    }

    private fun checkRoom(restaurantId: Int, branchId: Int, roomId: String) {
        val params = QRCodeGameParams()
        params.http_method = AppConfig.POST
        params.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        params.request_url = "/api/qr-code/check-qr-code-befor-join-room"
        params.params.branch_id = branchId
        params.params.restaurant_id = restaurantId
        params.params.room_id = roomId

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .checkRoom(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<JoinGameResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    FancyToast.makeText(
                        requireActivity(),
                        requireActivity().getString(R.string.join_room_game_false),
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        false
                    ).show()
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: JoinGameResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        qrCodeGame = response.data
                        listDataToFile = ArrayList()
                        TechResApplication.instance?.getQrCodeDao()?.let { listDataToFile.addAll(it.getListRoom()) }
                        listDataToFile = listDataToFile.stream()
                            .filter { x: QRCodeGame -> qrCodeGame?.room_id != x.room_id }
                            .collect(Collectors.toList()) as ArrayList<QRCodeGame>
                        qrCodeGame?.let { listDataToFile.add(it) }
                        TechResApplication.instance?.getQrCodeDao()?.deleteAllQrCode()
                        TechResApplication.instance?.getQrCodeDao()?.insertAllQrCode(listDataToFile)
//                        listDataToFile.add(qrCodeGame!!)
                        if (qrCodeGame?.status == 1) {
                            binding.zbarview.stopCamera()
                            FancyToast.makeText(
                                requireActivity(),
                                luckyWheelActivity!!.baseContext.getString(R.string.join_room_game_success),
                                FancyToast.LENGTH_LONG,
                                FancyToast.SUCCESS,
                                false
                            ).show()
                            val bundle = Bundle()
//                            val baseFolder =
//                                context!!.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath
//
//                            val file = File("$baseFolder/Game/data_room.json")
//
//                            writeJsonStream(FileOutputStream(file), listDataToFile)
//                            TechResApplication.instance.

                            bundle.putInt(
                                TechresEnum.BRANCH_ID.toString(),
                                qrCodeGame!!.branch_id!!
                            )

                            bundle.putInt(
                                TechresEnum.RESTAURANT_ID.toString(),
                                qrCodeGame!!.restaurant_id!!
                            )

                            bundle.putString(
                                TechresEnum.ROOM_ID.toString(),
                                arguments?.getString(
                                    TechresEnum.ROOM_ID.toString()
                                ).toString()
                            )
                            qrCodeGame!!.row?.let {
                                bundle.putInt(
                                    TechresEnum.ROW_GAME.toString(),
                                    it
                                )
                            }
                            bundle.putString(
                                TechresEnum.ID_ARTICLE_GAME.toString(),
                                arguments!!.getString(TechresEnum.ID_ARTICLE_GAME.toString())
                            )
                            val luckyWheelGameFragment = LuckyWheelGameFragment()
                            luckyWheelGameFragment.arguments = bundle
                            luckyWheelActivity?.replaceOnceFragment(
                                this@QRCodeGameFragment,
                                luckyWheelGameFragment
                            )
                        } else {
                            FancyToast.makeText(
                                context,
                                luckyWheelActivity!!.baseContext.getString(R.string.join_room_game_false),
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                            binding.zbarview.startSpotAndShowRect()
                        }
                    } else{
                        Toast.makeText(
                            luckyWheelActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.zbarview.startSpotAndShowRect()
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            if (!PictureSelector.obtainMultipleResult(data).isEmpty()) {
                binding.zbarview.decodeQRCode(PictureSelector.obtainMultipleResult(data)[0].realPath)
//                binding.zbarview.stopCamera()
//                binding.zbarview.startCamera(cameraId)
//                binding.zbarview.startSpotAndShowRect()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionCameraCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
//                    registryCamera()
                    binding.zbarview.stopCamera()
                    binding.zbarview.startCamera(cameraId)
                    binding.zbarview.startSpotAndShowRect()
                }

            }
        )
    }

    override fun onBackPress(): Boolean {
        WriteLog.d("QRCodeGameFragment", "QRCodeGameFragment")
        return true
    }

    override fun onScanQRCodeSuccess(result: String?) {
        vibrate()
        if (result != null) {
            luckyWheelActivity?.runOnUiThread {
                listCode = result.let { subCode(it) } as ArrayList<String>
                if (listCode.size > 4 && listCode[0].contains("Aloline") && listCode[1].contains(
                        "Game"
                    ) && Utils.isStringInt(listCode[2]) && Utils.isStringInt(listCode[3])
                ) {
                    if (listCode[4] == roomId && listCode[2].toInt() == restaurant().restaurant_id) {
                        checkRoom(listCode[2].toInt(), listCode[3].toInt(), roomId)
                    } else if (listCode[4] == roomId && listCode[2].toInt() != restaurant().restaurant_id) {
                        context?.let {
                            FancyToast.makeText(
                                it,
                                it.resources.getString(R.string.scan_qr_code_wrong_restaurant),
                                FancyToast.LENGTH_LONG, FancyToast.ERROR,
                                false
                            ).show()
                        }
                    } else {
                        context?.let {
                            FancyToast.makeText(
                                it,
                                it.resources.getString(R.string.scan_qr_code_wrong_room),
                                FancyToast.LENGTH_LONG, FancyToast.ERROR,
                                false
                            ).show()
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.zbarview.startSpotAndShowRect()
                        }, 3000)
                    }
                } else {
                    context?.let {
                        FancyToast.makeText(
                            it,
                            it.resources.getString(R.string.scan_qr_code_error),
                            FancyToast.LENGTH_LONG, FancyToast.ERROR,
                            false
                        ).show()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.zbarview.startSpotAndShowRect()
                    }, 3000)
                }
            }
        } else {
            binding.zbarview.stopCamera()
            binding.zbarview.startCamera(cameraId)
            binding.zbarview.startSpotAndShowRect()
        }
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
    }

    override fun onScanQRCodeOpenCameraError() {
    }

    private fun vibrate() {
        val vibrator = activity?.getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }
}