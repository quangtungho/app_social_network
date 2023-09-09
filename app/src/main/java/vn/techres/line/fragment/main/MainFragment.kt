package vn.techres.line.fragment.main


import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.account.MainAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.chat.request.NumberMessageAllGroupRequest
import vn.techres.line.data.model.chat.response.BackgroundResponse
import vn.techres.line.data.model.chat.response.CategoryStickerResponse
import vn.techres.line.data.model.chat.response.NumberMessageAllGroupResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.ScrollScreen
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMainBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.interfaces.util.OnBackHome
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService


class MainFragment : BaseBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate),
    OnPageChangeListener, OnBackHome {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var onPageChangeListener: OnPageChangeListener? = null
    private var mainAdapter: MainAdapter? = null

    private var currentPage: Int? = 2

    //socket
    private val application = TechResApplication()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null

    private var scrollScreen = ScrollScreen()

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    )
    private val requiredPermissionsCode = 200

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSocket = application.getSocketInstance(requireActivity().baseContext)
        if (mSocket != null) {
            mSocket?.connect()
            nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
            user = CurrentUser.getCurrentUser(requireActivity().baseContext)
            onSocket()
        }
        mainAdapter = MainAdapter(childFragmentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)

        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), TechresEnum.CURRENT_MAIN.toString())
        cacheManager.put(TechresEnum.MAIN_FRAGMENT.toString(), TechresEnum.MAIN_FRAGMENT.toString())
        cacheManager.put(TechresEnum.ORDER_REVIEW_KEY.toString(), "0")

        /**
         * Back New Feed
         * */
        mainActivity?.setOnBackHome(this)
        /**
         * Set current page
         * */
        if (!cacheManager.get(TechresEnum.CURRENT_PAGE.toString()).isNullOrBlank()) {
            currentPage = cacheManager.get(TechresEnum.CURRENT_PAGE.toString())?.toInt()
            cacheManager.remove(TechresEnum.CURRENT_PAGE.toString())
        }
        /**
         * Set register restaurant
         * */
        arguments?.let {
            if (it.getString(TechresEnum.REGISTER_RESTAURANT.toString()) != null) {
                currentPage = 0
            }
        }
        /**
         * Init data binding.pager
         * */
        binding.pager.adapter = mainAdapter
        onPageChangeListener = this
        binding.pager.setOnPageChangeListener(onPageChangeListener)
        binding.pager.offscreenPageLimit = 5

        setPager(currentPage ?: 0)


        /**
         * Controller [setListener]
         * */
        setListener()
        getCategorySticker()
        getBackground()


        /**
         * Check show permissions
         * */
        if (requireActivity().shouldAskPermissions(requiredPermissions)) {
            requestMultiplePermission()
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
        getMessageNotSeen()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun setPager(current: Int) {
        binding.pager.currentItem = current
        binding.imgHome.setImageResource(R.drawable.ic_home)
        binding.imgContacts.setImageResource(R.drawable.ic_contact)
        binding.imgNewFeed.setImageResource(R.drawable.logo_aloline_tab_gray)
        binding.imgChat.setImageResource(R.drawable.ic_chat)
        binding.imgAccount.setImageResource(R.drawable.ic_icon_account_tab)
        binding.tvHome.setTextColor(
            ContextCompat.getColor(
                requireActivity().baseContext,
                R.color.gray_darkxx
            )
        )
        binding.tvContacts.setTextColor(
            ContextCompat.getColor(
                requireActivity().baseContext,
                R.color.gray_darkxx
            )
        )
        binding.tvNewFeed.setTextColor(
            ContextCompat.getColor(
                requireActivity().baseContext,
                R.color.gray_darkxx
            )
        )
        binding.tvChat.setTextColor(
            ContextCompat.getColor(
                requireActivity().baseContext,
                R.color.gray_darkxx
            )
        )
        binding.tvAccount.setTextColor(
            ContextCompat.getColor(
                requireActivity().baseContext,
                R.color.gray_darkxx
            )
        )
        when (current) {
            0 -> {
                binding.pager.currentItem = 0
                currentPage = 0
                binding.imgHome.setImageResource(R.drawable.ic_home_orange)
                binding.tvHome.setTextColor(
                    ContextCompat.getColor(
                        requireActivity().baseContext,
                        R.color.main_bg
                    )
                )
            }
            1 -> {
                binding.pager.currentItem = 1
                currentPage = 1
                binding.imgContacts.setImageResource(R.drawable.ic_contact_orange)
                binding.tvContacts.setTextColor(
                    ContextCompat.getColor(
                        requireActivity().baseContext,
                        R.color.main_bg
                    )
                )
            }
            2 -> {
                binding.pager.currentItem = 2
                currentPage = 2
                binding.imgNewFeed.setImageResource(R.drawable.logo_aloline_tab)
                binding.tvNewFeed.setTextColor(
                    ContextCompat.getColor(
                        requireActivity().baseContext,
                        R.color.main_bg
                    )
                )
            }
            3 -> {
                binding.pager.currentItem = 3
                currentPage = 3
                binding.imgChat.setImageResource(R.drawable.ic_chat_orange)
                binding.tvChat.setTextColor(
                    ContextCompat.getColor(
                        requireActivity().baseContext,
                        R.color.main_bg
                    )
                )
            }
            4 -> {
                binding.pager.currentItem = 4
                currentPage = 4
                binding.imgAccount.setImageResource(R.drawable.ic_icon_account_tab_orange)
                binding.tvAccount.setTextColor(
                    ContextCompat.getColor(
                        requireActivity().baseContext,
                        R.color.main_bg
                    )
                )
            }
        }
    }

    /**
     * Permission contact
     * */
    private fun requestMultiplePermission() {
        requestPermissions(requiredPermissions, requiredPermissionsCode, object :
            RequestPermissionListener {

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

            }

            override fun onPermissionGranted() {
            }

        })
    }


    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
    }

    override fun onPageSelected(position: Int) {
        when (position) {
            0 -> currentPage = 0
            1 -> currentPage = 1
            2 -> currentPage = 2
            3 -> currentPage = 3
            4 -> currentPage = 4
        }
        setPager(currentPage ?: 0)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun setListener() {
        binding.rlHome.setOnClickListener {
            clickDelegate(0, currentPage ?: 0)
            currentPage = 0
            setPager(currentPage ?: 0)
        }
        binding.rlContacts.setOnClickListener {
            clickDelegate(1, currentPage ?: 0)
            currentPage = 1
            setPager(currentPage ?: 0)

        }
        binding.rlNewFeed.setOnClickListener {
            clickDelegate(2, currentPage ?: 0)
            currentPage = 2
            setPager(currentPage ?: 0)
        }
        binding.rlChat.setOnClickListener {
            clickDelegate(3, currentPage ?: 0)
            currentPage = 3
            setPager(currentPage ?: 0)
        }
        binding.rlAccount.setOnClickListener {
            clickDelegate(4, currentPage ?: 0)
            currentPage = 4
            setPager(currentPage ?: 0)
        }
    }

    private fun clickDelegate(tabIndex: Int, currentPage: Int) {
        scrollScreen.click = tabIndex
        scrollScreen.currentPage = currentPage
        EventBus.getDefault().post(scrollScreen)
    }

    private fun onSocket() {
        mSocket?.on(
            String.format(
                "%s/%s",
                TechResEnumChat.MESSAGE_NOT_SEEN_BY_ALL_GROUP.toString(),
                user.id
            )
        ) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("MESSAGE_NOT_SEEN_BY_ALL_GROUP", args[0].toString())
                val message = Gson().fromJson<NumberMessageAllGroupRequest>(
                    args[0].toString(),
                    object :
                        TypeToken<NumberMessageAllGroupRequest>() {}.type
                )
                context?.let {
                    when {
                        message.number ?: 0 == 0 -> {
                            binding.tvCountChat.hide()
                        }

                        message.number ?: 0 > 0 -> {
                            binding.tvCountChat.show()
                            binding.tvCountChat.text = message.number?.toString()
                        }

                        message.number ?: 0 >= 99 -> {
                            binding.tvCountChat.show()
                            binding.tvCountChat.text =
                                requireActivity().resources.getString(R.string.limit_count_chat)
                        }
                    }
                }

            }
        }
    }

    private fun getMessageNotSeen() {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/groups/message-not-seen-all"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesNotSeenAllGroup(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NumberMessageAllGroupResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: NumberMessageAllGroupResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val number = response.data.number ?: 0
                        if (number > 0) {
                            if (number <= 99) {
                                binding.tvCountChat.show()
                                binding.tvCountChat.text = number.toString()
                            } else {
                                binding.tvCountChat.show()
                                binding.tvCountChat.text =
                                    requireActivity().resources.getString(R.string.limit_count_chat)
                            }
                        } else {
                            binding.tvCountChat.hide()
                        }
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                }
            })
    }

//    private fun getGroupNotify(idGroup: String) {
//        val baseRequest = BaseParams()
//        baseRequest.http_method = AppConfig.GET
//        baseRequest.project_id = AppConfig.PROJECT_CHAT
//        baseRequest.request_url =
//            String.format("%s%s%s", "/api/group-party/", idGroup, "/get-group-notify")
//
//        ServiceFactory.createRetrofitServiceNode(
//            TechResService::class.java
//        )
//            .getOneGroup(baseRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<OneGroupResponse> {
//                override fun onComplete() {}
//                override fun onError(e: Throwable) {
//                    WriteLog.d("ERROR", e.message.toString())
//                }
//
//
//                override fun onSubscribe(d: Disposable) {}
//
//
//                @SuppressLint("ShowToast")
//                override fun onNext(response: OneGroupResponse) {
//                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        val data = response.data
//                        val bundle = Bundle()
//                        bundle.putString(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(data))
//                        cacheManager.put(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(data))
//                        cacheManager.put(TechresEnum.CURRENT_PAGE.toString(), 3.toString())
//                        if (data.conversation_type == requireActivity().getString(R.string.two_personal)) {
//                            cacheManager.put(
//                                TechresEnum.CHAT_PERSONAL.toString(),
//                                TechresEnum.GROUP_CHAT.toString()
//                            )
//
//                        }
//                    }
//
//                }
//            })
//
//    }

    private fun getCategorySticker() {
        val params = BaseParams()
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = "/api/stickers"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getCategorySticker(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CategoryStickerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("StaticFieldLeak")
                override fun onNext(response: CategoryStickerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        FileUtils.writeFile(TechResApplication.applicationContext(), Gson().toJson(response.data), "sticker_category.json")
                        UIView.setCategoryStickers(response.data.list_category)
                    }
                }
            })
    }

    private fun getBackground() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/backgrounds/get-all-background"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getBackground(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BackgroundResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BackgroundResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        UIView.setListBackground(response.data)
                        context?.let {
                            PrefUtils.savePreferences(
                                TechResEnumChat.CACHE_BACKGROUND.toString(),
                                Gson().toJson(
                                    response.data
                                )
                            )
                        }

                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    override fun onBackHome() {
        if (currentPage != 2) {
            clickDelegate(2, currentPage ?: 0)
            currentPage = 2
            setPager(currentPage ?: 0)
        } else {
            clickDelegate(2, currentPage ?: 0)
        }
    }

    override fun onBackPress(): Boolean {
        return false
    }


}
