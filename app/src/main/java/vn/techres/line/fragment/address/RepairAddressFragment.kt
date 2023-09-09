package vn.techres.line.fragment.address

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.params.ShippingAddressParams
import vn.techres.line.data.model.response.ShippingAddressResponse
import vn.techres.line.databinding.FragmentRepairAddressBinding
import vn.techres.line.helper.AloLineToast
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class RepairAddressFragment :
    BaseBindingFragment<FragmentRepairAddressBinding>(FragmentRepairAddressBinding::inflate),
    OnRefreshFragment {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var address = Address()
    private var idAddress = 0
    private var type = 0
    private var title = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        mainActivity?.setOnRefreshFragment(this)
        arguments?.let {
            if(it.getString(TechresEnum.SAVE_ADDRESS_FRAGMENT.toString()) != null){
                address = Gson().fromJson(it.getString(TechresEnum.SAVE_ADDRESS_FRAGMENT.toString()), object :
                    TypeToken<Address>() {}.type)
                title = address.name ?: ""
                idAddress = address.id ?: 0
                type = address.type ?: 0
                binding.editAddress.text = Editable.Factory.getInstance().newEditable(address.address_full_text ?: "")
                binding.editNote.text = Editable.Factory.getInstance().newEditable(address.note ?: "")
            }else{
                title = when (it.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString())) {
                    TechresEnum.HOME.toString() -> {
                        binding.editNameAddress.isEnabled = false
                        address.name = "Nhà"
                        type = 1
                        "Nhà"
                    }
                    TechresEnum.COMPANY.toString() -> {
                        binding.editNameAddress.isEnabled = false
                        address.name = "Công ty"
                        type = 2
                        "Công ty"
                    }
                    else -> {
                        type = 0
                        it.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()).toString()
                    }
                }
                idAddress = it.getInt(TechresEnum.ID_ADDRESS.toString(), 0)

            }
            binding.editNameAddress.text = Editable.Factory.getInstance().newEditable(title)
        }
        setListener()
    }

    private fun setListener() {
        binding.editAddress.setOnClickListener {
            mainActivity?.addOnceFragment(this, SearchMapsFragment())
        }
        binding.btnConfirmAddress.setOnClickListener {
            isCheckAddress()
        }
        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun isCheckAddress() {
        if (binding.editAddress.toString().isEmpty()) {
            AloLineToast.makeText(
                requireActivity(),
                requireActivity().resources.getString(R.string.empty_address),
                AloLineToast.LENGTH_LONG,
                AloLineToast.WARNING
            ).show()
        }
        if (binding.editNameAddress.toString().isEmpty()) {
            AloLineToast.makeText(
                requireActivity(),
                requireActivity().resources.getString(R.string.empty_name_address),
                AloLineToast.LENGTH_LONG,
                AloLineToast.WARNING
            ).show()
        }
        if (binding.editAddress.toString().isNotEmpty() && binding.editNameAddress.toString()
                .isNotEmpty()
        ) {
            mainActivity?.removeOnRefreshFragment(this)
            address.name = binding.editNameAddress.text.toString()
            address.note = binding.editNote.text.toString()
            createAddress(
                idAddress,
                address.name ?: "",
                address.lat ?: 0.0,
                address.lng ?: 0.0,
                address.address_full_text ?: "",
                address.note ?: "",
                type
            )
            val bundle = Bundle()
            if (arguments?.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()) != null) {
                bundle.putString(
                    TechresEnum.ADDRESS_FRAGMENT.toString(),
                    arguments?.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString())
                )
            }
            bundle.putString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString(), Gson().toJson(address))
            mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onStop() {
        super.onStop()
        closeKeyboard(binding.editAddress)
        closeKeyboard(binding.editNameAddress)
        closeKeyboard(binding.editNote)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeKeyboard(binding.editAddress)
        closeKeyboard(binding.editNameAddress)
        closeKeyboard(binding.editNote)
        mainActivity?.removeOnBackClick(this)
        mainActivity?.removeOnRefreshFragment(this)
    }

    private fun createAddress(
        id: Int,
        name: String,
        latitude: Double,
        longitude: Double,
        address: String,
        note: String,
        type: Int
    ) {
        val shippingAddressParams = ShippingAddressParams()
        shippingAddressParams.http_method = AppConfig.POST
        shippingAddressParams.request_url = "/api/customer-shipping-addresses/0"
        shippingAddressParams.params.id = id
        shippingAddressParams.params.lat = latitude
        shippingAddressParams.params.lng = longitude
        shippingAddressParams.params.name = name
        shippingAddressParams.params.address_full_text = address
        shippingAddressParams.params.is_default = 1
        shippingAddressParams.params.note = note
        shippingAddressParams.params.type = type
        shippingAddressParams.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .updateAddress(shippingAddressParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ShippingAddressResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: ShippingAddressResponse) {
                    }
                })
        }
    }

    override fun onCallBack(bundle: Bundle) {
        val addressBundle = bundle.getString(TechresEnum.SEARCH_ADDRESS_FRAGMENT.toString())
        if (addressBundle != null) {
            val address = Gson().fromJson<Address>(addressBundle, object :
                TypeToken<Address>() {}.type)
            binding.editAddress.text =
                Editable.Factory.getInstance().newEditable(address.address_full_text)
            this.address.address_full_text = address.address_full_text
            if (!StringUtils.isNullOrEmpty(address.note)) {
                this.address.note = address.note
            }
        }
    }

    override fun onBackPress(): Boolean {
        return true
    }

}