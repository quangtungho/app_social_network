package vn.techres.line.fragment.qr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.style.PictureSelectorUIStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.game.LuckyWheelActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.eventbus.EventBusQrCodeArchive
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.params.QRCodeGameParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.game.JoinGameResponse
import vn.techres.line.data.model.utils.EventBusChangeCamera
import vn.techres.line.data.model.utils.EventBusFlashLight
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentQRCodeBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File

class QRCodeFragment : BaseBindingFragment<FragmentQRCodeBinding>(FragmentQRCodeBinding::inflate),
    QRCodeView.Delegate {

    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var lastText = ""
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
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
        requestMultiplePermissionCamera()
        BGAQRCodeUtil.setDebug(true)
        binding.barcodeView.startCamera()
        binding.barcodeView.startSpotAndShowRect()
        binding.barcodeView.setDelegate(this)
        binding.barcodeView.changeToScanQRCodeStyle()
        binding.barcodeView.setType(BarcodeType.ONLY_QR_CODE, null)
        binding.barcodeView.startSpotAndShowRect()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFlashLight(event: EventBusFlashLight) {
        if (flashMode == 0) {
            binding.barcodeView.openFlashlight()
            flashMode = 1
        } else if (flashMode == 1) {
            flashMode = 0
            binding.barcodeView.closeFlashlight()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAchiveQr(event: EventBusQrCodeArchive) {
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
            .isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
            .isOriginalImageControl(true)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
            .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选
            .isSingleDirectReturn(false)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
            .isPreviewImage(true)// 是否可预览图片
            .isPreviewVideo(false)// 是否可预览视//.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
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
            .cutOutQuality(90)// 裁剪输出质量 默认100
            .minimumCompressSize(100)// 小于多少kb的图片不压缩
            .selectionData(null)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChangeCamera(event: EventBusChangeCamera) {
        cameraId = if (cameraId == 0) {
            1
        } else {
            0
        }
        binding.barcodeView.stopCamera()
        binding.barcodeView.startCamera(cameraId)
        binding.barcodeView.startSpotAndShowRect()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            if (PictureSelector.obtainMultipleResult(data).isNotEmpty()) {
                Toast.makeText(context, PictureSelector.obtainMultipleResult(data)[0].realPath, Toast.LENGTH_SHORT).show()
                binding.barcodeView.decodeQRCode(PictureSelector.obtainMultipleResult(data)[0].path)
            }
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
                    binding.barcodeView.stopCamera()
                    binding.barcodeView.startCamera(cameraId)
                    binding.barcodeView.startSpotAndShowRect()
                }

            })
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
                    context?.let {
                        FancyToast.makeText(
                            it,
                            it.resources.getString(R.string.join_room_game_false),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false
                        ).show()
                        lastText = ""
                        binding.barcodeView.startSpotAndShowRect()
                    }

                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: JoinGameResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val qrCodeGame = response.data
//                        listDataToFile.add(qrCodeGame!!)
                        if (qrCodeGame?.status == 1) {
                            context?.let {
                                FancyToast.makeText(
                                    it,
                                    it.resources.getString(R.string.join_room_game_success),
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.SUCCESS,
                                    false
                                ).show()
                            }

                            val bundle = Bundle()
                            val baseFolder =
                                context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath
                                    ?: ""

                            File("$baseFolder/Game/data_room.json")

//                            writeJsonStream(FileOutputStream(file), listDataToFile)


                            bundle.putInt(
                                TechresEnum.BRANCH_ID.toString(),
                                qrCodeGame.branch_id ?: 0
                            )

                            bundle.putInt(
                                TechresEnum.RESTAURANT_ID.toString(),
                                qrCodeGame.restaurant_id ?: 0
                            )

                            bundle.putString(
                                TechresEnum.ROOM_ID.toString(),
                                arguments?.getString(
                                    TechresEnum.ROOM_ID.toString()
                                ).toString()
                            )

                            bundle.putString(
                                TechresEnum.ID_ARTICLE_GAME.toString(),
                                arguments!!.getString(TechresEnum.ID_ARTICLE_GAME.toString())
                            )
                            val intent =
                                Intent(requireActivity(), LuckyWheelActivity::class.java).apply {
                                    putExtra(TechresEnum.IS_GAME.toString(), true)
                                    putExtra(
                                        TechresEnum.BRANCH_ID.toString(),
                                        qrCodeGame.branch_id ?: 0
                                    )
                                    putExtra(
                                        TechresEnum.RESTAURANT_ID.toString(),
                                        qrCodeGame.restaurant_id ?: 0
                                    )
                                    putExtra(
                                        TechresEnum.ID_ARTICLE_GAME.toString(),
                                        arguments?.getString(TechresEnum.ID_ARTICLE_GAME.toString())
                                    )
                                    putExtra(
                                        TechresEnum.ROOM_ID.toString(), arguments?.getString(
                                            TechresEnum.ROOM_ID.toString()
                                        ).toString()
                                    )
                                }
                            mainActivity?.startActivity(intent)
                            mainActivity?.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            context?.let {
                                lastText = ""
                                FancyToast.makeText(
                                    it,
                                    it.resources.getString(R.string.join_room_game_false),
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.WARNING,
                                    false
                                ).show()
                                binding.barcodeView.startSpotAndShowRect()
                            }
                        }

                    } else
                        context?.let {
                            Toast.makeText(
                                it,
                                response.message,
                                Toast.LENGTH_LONG
                            ).show()
                            lastText = ""
                            binding.barcodeView.startSpotAndShowRect()
                        }


                }
            })
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
                    binding.barcodeView.stopCamera()
                    binding.barcodeView.startCamera(cameraId)
                    binding.barcodeView.startSpotAndShowRect()
                }
            }
        )
    }

    override fun onBackPress(): Boolean {
        return true
    }

    override fun onScanQRCodeSuccess(qrResult: String?) {
        vibrate()
        if (qrResult != null) {
            binding.barcodeView.stopCamera()
            mainActivity?.setLoading(true)
            val addFriendParams = FriendParams()
            addFriendParams.http_method = AppConfig.POST
            addFriendParams.request_url = "/api/contact-tos/request"
            addFriendParams.project_id = AppConfig.PROJECT_CHAT
            addFriendParams.params.contact_to_user_id = qrResult.split(":").first().toInt()
            addFriendParams.let {
                ServiceFactory.createRetrofitServiceNode(
                    TechResService::class.java
                )
                    .addFriend(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<BaseResponse> {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {
                            WriteLog.d("ERROR", e.message.toString())
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(response: BaseResponse) {
                            Toast.makeText(context,""+response.message,Toast.LENGTH_LONG).show()
                            mainActivity?.setLoading(false)
                        }
                    })
            }
        } else {
            binding.barcodeView.stopCamera()
            binding.barcodeView.startCamera(cameraId)
            binding.barcodeView.startSpotAndShowRect()
            Toast.makeText(context, "khong hop le", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibrate() {
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
    }

    override fun onScanQRCodeOpenCameraError() {
    }

}