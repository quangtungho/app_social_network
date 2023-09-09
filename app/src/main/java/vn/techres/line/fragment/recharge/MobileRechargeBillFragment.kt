package vn.techres.line.fragment.recharge

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.card.DenominationCard
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMobileRechargeBillBinding
import vn.techres.line.fragment.account.ForgotPasswordFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class MobileRechargeBillFragment : BaseBindingFragment<FragmentMobileRechargeBillBinding>(FragmentMobileRechargeBillBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var denominationCard = DenominationCard()
    private var restaurantCard = RestaurantCard()
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        setData()
        setListener()
    }

    private fun setData() {
        getPointCard()
        val jsonContactDevice = arguments?.getString(TechresEnum.CONTACT_DEVICE.toString())
        if (jsonContactDevice != null) {
            val contactDevice = Gson().fromJson<ContactDevice>(jsonContactDevice, object :
                TypeToken<ContactDevice>() {}.type)
            binding.tvNameRecharge.text = contactDevice.full_name
            binding.tvPhoneRecharge.text = contactDevice.phone
        } else {
            binding.tvNameRecharge.text = user.name
            binding.tvPhoneRecharge.text = user.phone
        }
        denominationCard =
            Gson().fromJson(arguments?.getString(TechresEnum.MOBILE_RECHARGE.toString()), object :
                TypeToken<DenominationCard>() {}.type)
        binding.tvPricePoint.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal(denominationCard.amount),
            resources.getString(R.string.denominations)
        )
        binding.tvTotalMoney.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal(denominationCard.amount),
            resources.getString(R.string.denominations)
        )
    }

    private fun setListener() {
        binding.tvChangePoint.setOnClickListener {

        }
        binding.btnRechargeConfirm.setOnClickListener {
            showBottomSheet()
        }
    }
    private fun showBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_confirm_password)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.dismissWithAnimation = true
        val imgCloseBottomSheet =
            bottomSheetDialog.findViewById<ImageButton>(R.id.imgCloseBottomSheet)
        val edtPassword = bottomSheetDialog.findViewById<EditText>(R.id.edtPassword)
        val tvForgotPassword = bottomSheetDialog.findViewById<TextView>(R.id.tvForgotPassword)
        val btnOne = bottomSheetDialog.findViewById<Button>(R.id.btnOne)
        val btnTwo = bottomSheetDialog.findViewById<Button>(R.id.btnTwo)
        val btnThree = bottomSheetDialog.findViewById<Button>(R.id.btnThree)
        val btnFour = bottomSheetDialog.findViewById<Button>(R.id.btnFour)
        val btnFive = bottomSheetDialog.findViewById<Button>(R.id.btnFive)
        val btnSix = bottomSheetDialog.findViewById<Button>(R.id.btnSix)
        val btnSeven = bottomSheetDialog.findViewById<Button>(R.id.btnSeven)
        val btnEight = bottomSheetDialog.findViewById<Button>(R.id.btnEight)
        val btnNice = bottomSheetDialog.findViewById<Button>(R.id.btnNice)
        val btnZero = bottomSheetDialog.findViewById<Button>(R.id.btnZero)
        val imgDelete = bottomSheetDialog.findViewById<ImageButton>(R.id.imgDelete)
        val imgConfirmBottomSheet =
            bottomSheetDialog.findViewById<ImageButton>(R.id.imgConfirmBottomSheet)
        tvForgotPassword?.setOnClickListener {
            mainActivity?.addOnceFragment(MobileRechargeBillFragment(), ForgotPasswordFragment())
        }
        btnOne?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.one))
        }
        btnTwo?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.two))
        }
        btnThree?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.three))
        }
        btnFour?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.four))
        }
        btnFive?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.five))
        }
        btnSix?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.six))
        }
        btnSeven?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.seven))
        }
        btnEight?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.eight))
        }
        btnNice?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.nice))
        }
        btnZero?.setOnClickListener {
            edtPassword?.append(resources.getString(R.string.zero))
        }
        imgDelete?.setOnClickListener {
            val length = edtPassword!!.text!!.length
            if (length > 0) {
                edtPassword.text?.delete(length - 1, length)
            }
        }
        imgConfirmBottomSheet?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        imgCloseBottomSheet?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun getPointCard() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format("%s%s", "/api/membership-cards/", restaurant().id)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getRestaurantCardDetail(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardDetailResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantCardDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        restaurantCard = response.data
                        saveRestaurantInfo(restaurantCard)
                    }
                    mainActivity?.setLoading(false)
                }
            })

    }

    override fun onBackPress() : Boolean {

        return true
    }

}