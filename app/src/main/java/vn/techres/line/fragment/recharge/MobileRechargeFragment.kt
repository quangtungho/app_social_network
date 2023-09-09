package vn.techres.line.fragment.recharge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.point.DenominationAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.card.DenominationCard
import vn.techres.line.data.model.card.DenominationCardResponse
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentMobileRechargeBinding
import vn.techres.line.fragment.contact.ContactDeviceFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.setSnackBar
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.point.DenominationHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration
import java.util.*

class MobileRechargeFragment : BaseBindingFragment<FragmentMobileRechargeBinding>(FragmentMobileRechargeBinding::inflate), DenominationHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var listDenominationCard : ArrayList<DenominationCard>? = null
    private var adapter: DenominationAdapter? = null
    private var contactDevice: ContactDevice? = null
    private var denominationCard : DenominationCard? = null
    private var isCheck = true
    private val numberViettel = arrayOf("086", "096", "097", "098", "032", "033", "034", "035", "036", "037", "038", "039")
    private val numberVinaPhone = arrayOf("091", "094", "083", "084", "085", "081", "082", "088")
    private val numberMobiPhone = arrayOf("089", "090", "093", "070", "079", "077", "076", "078")
    private val numberVietnamobile = arrayOf("092", "056", "058")
    private val numberGmobile = arrayOf("099", "059")

    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.rcDenominations.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rcDenominations.addItemDecoration(GridSpacingDecoration(7, 3))
        adapter = DenominationAdapter(requireActivity())
        adapter?.setDenominationHandler(this)
        binding.rcDenominations.adapter = adapter
        binding.edtPhoneCustomer.addTextChangedListener(textWatcher)
        
        setData()
        setListener()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        onContact(contactDevice)
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun setData() {
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.mobile_recharge)
            .uppercase(Locale.getDefault())
        if ((listDenominationCard?.size ?: 0) > 0) {
            listDenominationCard?.let { adapter?.setDataSource(it) }
        } else {
            getDenominations()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContact(contactDevice: ContactDevice?) {
        this.contactDevice = contactDevice
        if (contactDevice != null) {
            isCheck = false
            binding.tipLastNameCustomer.hint = contactDevice.full_name ?: ""
            binding.edtPhoneCustomer.text =
                Editable.Factory.getInstance().newEditable(contactDevice.phone ?: "")
        } else {
            binding.edtPhoneCustomer.text = Editable.Factory.getInstance().newEditable(user.phone)
        }
    }

    private fun setListener() {
        binding.tvMeRecharge.setOnClickListener {
            isCheck = true
            binding.tipLastNameCustomer.hint = user.full_name
            binding.edtPhoneCustomer.text = Editable.Factory.getInstance().newEditable(user.phone)
        }
        binding.tipLastNameCustomer.setEndIconOnClickListener {
            closeKeyboard(binding.edtPhoneCustomer)
            mainActivity?.addOnceFragment(MobileRechargeFragment(), ContactDeviceFragment())
        }
        binding.btnRechargeNow.setOnClickListener {
            closeKeyboard(binding.edtPhoneCustomer)
            if(denominationCard != null){
                val bundle = Bundle()
                if (!isCheck) {
                    bundle.putString(
                        TechresEnum.CONTACT_DEVICE.toString(),
                        Gson().toJson(contactDevice)
                    )
                }
                bundle.putString(
                    TechresEnum.MOBILE_RECHARGE.toString(),
                    Gson().toJson(denominationCard)
                )
                val mobileRechargeBillFragment = MobileRechargeBillFragment()
                mobileRechargeBillFragment.arguments = bundle
                mainActivity?.addOnceFragment(MobileRechargeFragment(), mobileRechargeBillFragment)
            }else{
                setSnackBar(it, requireActivity())
            }

        }
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            if (s!!.isNotEmpty()) {
                if (s.length >= 3) {
                    selectHomeNetWork(s.toString())
                }
            }
        }

    }

    private fun selectHomeNetWork(string: String) {
        val number = string.slice(0..2)
        when {
            numberViettel.contains(number) -> {
                binding.imgHomeNetwork.setImageResource(R.drawable.ic_viettel)
            }
            numberMobiPhone.contains(number) -> {
                binding.imgHomeNetwork.setImageResource(R.drawable.ic_mobifone)
            }
            numberVietnamobile.contains(number) -> {
                binding.imgHomeNetwork.setImageResource(R.drawable.ic_vietnamobile)
            }
            numberVinaPhone.contains(number) -> {
                binding.imgHomeNetwork.setImageResource(R.drawable.ic_vinaphone)
            }
            numberGmobile.contains(number) ->{
                binding.imgHomeNetwork.setImageResource(R.drawable.ic_gmobile)
            }
        }
    }

    private fun getDenominations() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/mobile-topup-cards"
        restaurant().restaurant_id!!.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .getDenominations(baseRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DenominationCardResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: DenominationCardResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            if (!response.data.isNullOrEmpty()) {
                                listDenominationCard = response.data
                                listDenominationCard?.get(0)?.is_check = true
                                context?.let {
                                    binding.tvPriceRecharge.text = String.format(
                                        "%s%s",
                                        Currency.formatCurrencyDecimal(listDenominationCard?.get(0)?.amount),
                                        resources.getString(R.string.denominations)
                                    )
                                }
                                denominationCard = listDenominationCard?.get(0)
                            }
                            listDenominationCard?.let { it1 -> adapter?.setDataSource(it1) }
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onChooseDenomination(position: Int) {
        if (listDenominationCard?.get(0)?.is_check == false) {
            listDenominationCard?.forEach {
                it.is_check = false
                if (it == listDenominationCard?.get(0)) {
                    it.is_check = true
                    denominationCard = it
                }
            }
            adapter?.notifyDataSetChanged()
        }
        binding.tvPriceRecharge.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal(listDenominationCard?.get(0)?.amount),
            resources.getString(R.string.denominations)
        )

    }

    override fun onBackPress() : Boolean{
//        mainActivity?.findNavController(R.id.nav_host)?.popBackStack()
        return true
    }

}