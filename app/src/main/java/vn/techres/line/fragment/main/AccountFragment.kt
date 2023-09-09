package vn.techres.line.fragment.main


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.base.BaseBindingStubFragment
import vn.techres.line.data.model.eventbus.UserUpdateEventBus
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.ScrollScreen
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentAccountBinding
import vn.techres.line.fragment.booking.BookingInformationManagerFragment
import vn.techres.line.fragment.card.RestaurantCardManageFragment
import vn.techres.line.fragment.order.OrderManagerFragment
import vn.techres.line.fragment.qr.CodeBarFragment
import vn.techres.line.fragment.setting.SettingsFragment
import vn.techres.line.fragment.voucher.VoucherManagerFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class AccountFragment :
    BaseBindingStubFragment<FragmentAccountBinding>(R.layout.fragment_account, true) {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var configNodeJs = ConfigNodeJs()
    private var user = User()

    override fun onCreateViewAfterViewStubInflated(savedInstanceState: Bundle?) {
        user = CurrentUser.getCurrentUser(requireActivity())
        getProfile()
        setListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserUpdate(dataUser: UserUpdateEventBus) {
        stubBinding?.avtAccount?.load(
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                dataUser.data.avatar_three_image.thumb
            )
        )
        {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
            size(500, 500)
            transformations(RoundedCornersTransformation(10F))
        }
        stubBinding?.tvNameAccount?.text = dataUser.data.name
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun setData() {
        mSocket = application.getSocketInstance(requireActivity())
        mSocket?.connect()
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())

        stubBinding?.avtAccount?.load(
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                user.avatar_three_image.thumb
            )
        )
        {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
            size(500, 500)
            transformations(RoundedCornersTransformation(10F))
        }

        stubBinding?.tvNameAccount?.text = user.name
        stubBinding?.memberCardRank?.text = restaurant().restaurant_membership_card_name
        stubBinding?.tvVersion?.text = String.format(
            "%s %s",
            requireActivity().resources.getString(R.string.title_version),
            getVersionBuild()
        )

        if (user.is_allow_advert == 0) {
            stubBinding?.lnManagerAdvertisement?.visibility = View.GONE
            stubBinding?.lnRegistryAdvertisement?.visibility = View.VISIBLE
        } else {
            stubBinding?.lnManagerAdvertisement?.visibility = View.VISIBLE
            stubBinding?.lnRegistryAdvertisement?.visibility = View.GONE
        }
    }

    private fun getProfile() {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/customers/" + user.id

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getProfile(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UpdateProfileResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }

                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUBSCRIBE", "Subscribe")
                }

                override fun onNext(response: UpdateProfileResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        user = response.data
                        setData()

                    }

                }
            })
    }


    @SuppressLint("UseRequireInsteadOfGet", "ResourceAsColor")
    private fun setListener() {
        stubBinding?.llSaveBranch?.setOnClickListener {
            val intent = Intent(mainActivity, SaveBranchActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.llCodeBar?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), CodeBarFragment())
        }

        stubBinding?.lnNotification?.setOnClickListener {
            val intent = Intent(mainActivity, NotificationActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnInfo?.setOnClickListener {
            val intent = Intent(mainActivity, ProfileActivity::class.java)
            intent.putExtra(TechresEnum.ID_USER.toString(), user.id)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.suggestionsForDevelopers?.setOnClickListener {
            val intent = Intent(mainActivity, FeedbackForDevelopersActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.llIntroduceFriends?.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "Application Link : https://play.google.com/store/apps/details?id=${mainActivity!!.packageName}"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
        }
        stubBinding?.lnProductInformation?.setOnClickListener {
            val intent = Intent(mainActivity, ProductInformationActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.llOrderManager?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), OrderManagerFragment())
        }
        stubBinding?.reviewRatingApp?.setOnClickListener {
            val url = "https://play.google.com/store/apps/details?id=" + mainActivity!!.packageName
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this.context, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }
        stubBinding?.lnBookingInformation?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), BookingInformationManagerFragment())
        }
        stubBinding?.llSetting?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), SettingsFragment())
        }
        stubBinding?.llChooseRestaurent?.setOnClickListener {
            cacheManager.put(
                TechresEnum.ACCOUNT_FRAGMENT.toString(),
                TechresEnum.ACCOUNT_FRAGMENT.toString()
            )
            mainActivity?.addOnceFragment(MainFragment(), RestaurantCardManageFragment())
        }
        stubBinding?.lnRegistryAdvertisement?.setOnClickListener {
            val intent = Intent(mainActivity, AdvertisementActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnManagerAdvertisement?.setOnClickListener {
            val intent = Intent(mainActivity, AdvertPackageActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnDiscountCode?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), VoucherManagerFragment())
        }
        stubBinding?.lnPrivacyPolicy?.setOnClickListener {
            val intent = Intent(mainActivity, PrivacyPolicyActivity::class.java)
            intent.putExtra(TechresEnum.WEBVIEW_TYPE.toString(), 1)
            mainActivity?.startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnAlolineUseAccount?.setOnClickListener {
            val intent = Intent(mainActivity, AlolineUseAccountActivity::class.java)
            intent.putExtra(TechresEnum.WEBVIEW_TYPE.toString(), 1)
            mainActivity?.startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnCurrentRank?.setOnClickListener {
            val intent = Intent(mainActivity, RestaurantCardDetailActivity::class.java)
            intent.putExtra(TechresEnum.WEBVIEW_TYPE.toString(), 1)
            mainActivity?.startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.lnOrderReview?.setOnClickListener {
            val intent = Intent(mainActivity, OrderReviewActivity::class.java)
            mainActivity?.startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickTab(scrollScreen: ScrollScreen) {
        if (scrollScreen.currentPage == 4 && scrollScreen.click == 4) {
            stubBinding?.nsvAccount?.scrollTo(0, 0)
        }
    }

    private fun getVersionBuild(): String? {
        val packageManager = mainActivity!!.packageManager
        val packageName = mainActivity!!.packageName
        val myVersionName: String?
        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName
            return myVersionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "1.0.0"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    //checks if external storage is available for read and write
    fun isExternalStorageAvailable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    //checks if external storage is available for read
    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == state
    }

    fun getStorageDir(fileName: String): String? {
        //create folder
        val file = File(Environment.getExternalStorageDirectory(), "Aloline")
        if (!file.mkdirs()) {
            file.mkdirs()
        }
        return file.absolutePath + File.separator + fileName
    }

    override fun onBackPress(): Boolean {
        return true
    }

}