package vn.techres.line.fragment.address

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.address.SavedAddressAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.address.response.AddressResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.databinding.FragmentSaveAddressBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.address.SaveAddressHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SaveAddressFragment : BaseBindingFragment<FragmentSaveAddressBinding>(FragmentSaveAddressBinding::inflate),
    OnRefreshFragment,
    SaveAddressHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var dataAddress = ArrayList<Address>()
    private var addressHome : Address? = null
    private var addressCompany : Address? = null
    private var adapter: SavedAddressAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        mainActivity?.setOnRefreshFragment(this)
        adapter = SavedAddressAdapter(requireActivity())
        adapter?.setSaveAddressHandler(this)
        binding.rcSavedAddress.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcSavedAddress.adapter = adapter
        getAddress()
        setListener()
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnRefreshFragment(this)
        mainActivity?.removeOnBackClick(this)
    }

    private fun setData(data : ArrayList<Address>){
        data.forEach {
            when(it.type){
                TechresEnum.HOME.toString().toInt() ->{
                    binding.tvHomeAddress.text = it.address_full_text
                    addressHome = it
                }
                TechresEnum.COMPANY.toString().toInt() ->{
                    binding.tvCompanyAddress.text = it.address_full_text
                    addressCompany = it
                }else ->{
                    dataAddress.add(it)
                }
            }
        }
        adapter?.setDataSource(dataAddress)
        if(dataAddress.size == 3){
            binding.lnAddAddress.hide()
        }else{
            binding.lnAddAddress.show()
        }
    }
    private fun setListener(){
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.lnHomeAddress.setOnClickListener {
            if(addressHome != null){
                val bundle = Bundle()
                bundle.putString(TechresEnum.SAVE_ADDRESS_FRAGMENT.toString(), Gson().toJson(addressHome))
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }else{
                val bundle = Bundle()
                bundle.putString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString(), TechresEnum.HOME.toString())
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }
        }
        binding.lnCompanyAddress.setOnClickListener {
            if(addressCompany != null){
                val bundle = Bundle()
                bundle.putString(TechresEnum.SAVE_ADDRESS_FRAGMENT.toString(), Gson().toJson(addressCompany))
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }else{
                val bundle = Bundle()
                bundle.putString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString(), TechresEnum.COMPANY.toString())
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }
        }
        binding.lnAddAddress.setOnClickListener {
            mainActivity?.addOnceFragment(this, RepairAddressFragment())
        }
    }
    private fun getAddress() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/customer-shipping-addresses"
        mainActivity!!.setLoading(true)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getAddress(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AddressResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity!!.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: AddressResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        setData(data)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onChooseAddressRepair(address: Address) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.SAVE_ADDRESS_FRAGMENT.toString(), Gson().toJson(address))
        val repairAddressFragment = RepairAddressFragment()
        repairAddressFragment.arguments = bundle
        mainActivity?.addOnceFragment(this, repairAddressFragment)
    }

    override fun onCallBack(bundle: Bundle) {
        if(bundle.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()) != null) {
            val addressBundle = Gson().fromJson<Address>(
                bundle.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()),
                object :
                    TypeToken<Address>() {}.type
            )
            when(addressBundle.type){
                TechresEnum.HOME.toString().toInt() ->{
                    addressHome = addressBundle
                    binding.tvHomeAddress.text = addressBundle.address_full_text
                }
                TechresEnum.COMPANY.toString().toInt() ->{
                    addressCompany = addressBundle
                    binding.tvCompanyAddress.text = addressBundle.address_full_text
                }
                else ->{
                    val position = dataAddress.indexOfFirst { it.id == addressBundle.id }
                    if( position != -1){
                        dataAddress[position] = addressBundle
                        adapter?.notifyItemInserted(position)
                    }
                }
            }
        }

    }

    override fun onBackPress(): Boolean {
        return true
    }

}