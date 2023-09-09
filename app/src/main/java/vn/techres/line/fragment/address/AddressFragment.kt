package vn.techres.line.fragment.address

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.piruin.quickaction.ActionItem
import me.piruin.quickaction.QuickAction
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.address.AddressAdapter
import vn.techres.line.adapter.address.SavedAddressAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.address.response.AddressResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.EventBusMap
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentAddressBinding
import vn.techres.line.fragment.map.MapsFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.ChooseAddressHandler
import vn.techres.line.interfaces.DeleteAddressHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.address.SaveAddressHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.OnMenuMoreClick
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class AddressFragment :
    BaseBindingFragment<FragmentAddressBinding>(FragmentAddressBinding::inflate),
    ChooseAddressHandler,
    DeleteAddressHandler,
    OnMenuMoreClick,
    OnRefreshFragment,
    SaveAddressHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var dataAddress = ArrayList<Address>()
    private var dataSavedAddress = ArrayList<Address>()
    private var adapter: AddressAdapter? = null
    private var adapterSaved: SavedAddressAdapter? = null
    private var quickActionComment: QuickAction? = null
    private var handler: Handler? = null
    private var isCheckBack = true
    private var position: Int = 0
    private val idCancelComment = 1
    private var keySearch = ""
    private var user = User()
    private var addressHome: Address? = null
    private var addressCompany: Address? = null
    private var address: Address? = null

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val requiredPermissionsCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)

    }

    override fun onStop() {
        super.onStop()
        binding.svAddress.clearFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.svAddress.clearFocus()
        mainActivity?.removeOnRefreshFragment(this)
        mainActivity?.removeOnBackClick(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        mainActivity?.setOnRefreshFragment(this)
        binding.header.tvTitleHomeHeader.text =
            requireActivity().resources.getString(R.string.address_list).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        setupAction()
        mainActivity?.setOnMenuMoreClick(this)
        quickActionComment?.setOnActionItemClickListener(mainActivity)
        adapter = AddressAdapter(requireActivity())
        adapterSaved = SavedAddressAdapter(requireActivity())
        adapter?.setChooseAddressHandler(this)
        adapter?.setDeleteAddressHandler(this)
        adapter?.setDataSource(dataAddress)
        adapterSaved?.setDataSource(dataSavedAddress)
        binding.rcNearAddress.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcNearAddress.adapter = adapter
        binding.rcSearchAddress.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcSearchAddress.adapter = adapter
        binding.rcSavedAddress.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcSavedAddress.adapter = adapterSaved
        getAddress()
        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.tvRepairAddress.setOnClickListener {
            mainActivity?.addOnceFragment(this, SaveAddressFragment())
        }
        binding.imgPlacePoint.setOnClickListener {
            requestMultiplePermissionWithListener()
        }

        binding.lnHomeAddress.setOnClickListener {
            if (addressHome != null) {
                val bundle = Bundle()
                bundle.putString(
                    TechresEnum.CONFIRM_ORDER_FRAGMENT.toString(),
                    Gson().toJson(addressHome)
                )
                mainActivity?.removeOnRefreshFragment(this)
                mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
                mainActivity?.supportFragmentManager?.popBackStack()
            } else {
                val bundle = Bundle()
                bundle.putString(
                    TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString(),
                    TechresEnum.HOME.toString()
                )
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }

        }
        binding.lnCompanyAddress.setOnClickListener {
            if (addressCompany != null) {
                val bundle = Bundle()
                bundle.putString(
                    TechresEnum.CONFIRM_ORDER_FRAGMENT.toString(),
                    Gson().toJson(addressCompany)
                )
                mainActivity?.removeOnRefreshFragment(this)
                mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
                mainActivity?.supportFragmentManager?.popBackStack()
            } else {
                val bundle = Bundle()
                bundle.putString(
                    TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString(),
                    TechresEnum.COMPANY.toString()
                )
                val repairAddressFragment = RepairAddressFragment()
                repairAddressFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, repairAddressFragment)
            }
        }
        binding.lnAddAddress.setOnClickListener {
            mainActivity?.addOnceFragment(this, RepairAddressFragment())
        }
        binding.svAddress.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!StringUtils.isNullOrEmpty(newText) && newText != null) {
                    keySearch = newText
                    if (handler != null) {
                        handler?.removeCallbacks(runnable)
                    } else {
                        handler = Handler(Looper.getMainLooper())
                    }
                    handler?.postDelayed(runnable, 500)
                } else {
                    binding.groupAddress.show()
                    binding.rcSearchAddress.hide()
                }
                return false
            }
        })
    }

    private val runnable = Runnable {
        geoLocate(keySearch)
    }

    private fun geoLocate(key: String) {
        dataAddress = ArrayList()
        val geocode = Geocoder(mainActivity)
        var arrayAddress: List<android.location.Address>? = ArrayList()
        try {
            arrayAddress = geocode.getFromLocationName(key, 3)
        } catch (e: IOException) {
            WriteLog.d("SearchMapsFragment", e.localizedMessage ?: "")
        }
        arrayAddress?.let { list ->
            if (list.isNotEmpty()) {
                list.forEach {
                    val address = Address()
                    address.id = (1..999).random()
                    address.address_full_text = it.getAddressLine(0).toString()
                    address.name = it.subAdminArea
                    address.lat = it.latitude
                    address.lng = it.longitude
                    dataAddress.add(address)
                }
                adapter?.setDataSource(dataAddress)
            }
            binding.groupAddress.hide()
            binding.rcSearchAddress.show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun postAddressMap(event: EventBusMap) {
        val address = Address()
        address.id = (1..999).random()
        address.address_full_text = event.address_full_text
        address.name = event.name_address
        address.lat = event.latitude
        address.lng = event.longitude
        address.note = event.note
        dataAddress.add(address)
        adapter?.notifyItemInserted(dataAddress.size)
    }

    private fun requestMultiplePermissionWithListener() {
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
                dialogPermission(
                    String.format(
                        requireActivity().resources.getString(R.string.title_permission),
                        namePermission
                    ),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.step_two_open_permission_location),
                    R.drawable.ic_location,
                    "",
                    0,
                    object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                            WriteLog.d("dismissDialog", "dismissDialog")
                        }

                    }
                )
            }

            override fun onPermissionGranted() {
//                val intent = Intent(mainActivity, MapsActivity::class.java)
//                mainActivity?.startActivity(intent)
//                mainActivity?.overridePendingTransition(
//                    R.anim.translate_from_right,
//                    R.anim.translate_to_right
//                )
                mainActivity?.addOnceFragment(this@AddressFragment, MapsFragment())
            }
        })
    }

    private fun setupAction() {
        val acCancel = ActionItem(
            idCancelComment,
            resources.getString(R.string.title_delete_image),
            R.drawable.ic_cancel
        )
        quickActionComment = QuickAction(requireActivity(), QuickAction.VERTICAL)
        quickActionComment?.addActionItem(acCancel)
    }

    private fun setData(data: ArrayList<Address>) {
        context?.let { _ ->
            data.forEach {
                when (it.type) {
                    TechresEnum.HOME.toString().toInt() -> {
                        addressHome = it
                        context?.let { _ ->
                            binding.tvHomeAddress.text = it.address_full_text
                        }
                    }
                    TechresEnum.COMPANY.toString().toInt() -> {
                        addressCompany = it
                        context?.let { _ ->
                            binding.tvCompanyAddress.text = it.address_full_text
                        }
                    }
                    else -> {
                        dataSavedAddress.add(it)
                    }
                }
            }
            adapterSaved?.setDataSource(dataSavedAddress)
            if (dataSavedAddress.size == 3) {
                binding.lnAddAddress.hide()
            } else {
                binding.lnAddAddress.show()
            }
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
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity!!.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUBSCRIBE", "Subscribe")
                }
                override fun onNext(response: AddressResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        setData(data)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                    mainActivity?.setLoading(false)
                }
            })

    }

    override fun onChooseAddress(address: Address) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.CONFIRM_ORDER_FRAGMENT.toString(), Gson().toJson(address))
        mainActivity?.removeOnRefreshFragment(this)
        mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
        mainActivity?.supportFragmentManager?.popBackStack()
    }

    override fun onDeleteAddress(address: Address, view: View) {
        quickActionComment?.show(view)
        this.position = dataAddress.indexOfFirst { it.id == address.id }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMenuMore(id: Int) {
        when (id) {
            1 -> dataAddress[position].id?.let {
                dataAddress.removeAt(position)
                adapter?.notifyDataSetChanged()
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
            requiredPermissionsCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    dialogPermission(
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission_location),
                        R.drawable.ic_location,
                        "",
                        0,
                        object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                                WriteLog.d("dismissDialog", "dismissDialog")
                            }
                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission_location),
                        R.drawable.ic_location,
                        "",
                        0,
                        object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                                WriteLog.d("dismissDialog", "dismissDialog")
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
//                    val intent = Intent(mainActivity, MapsActivity::class.java)
//                    mainActivity?.startActivity(intent)
//                    mainActivity?.overridePendingTransition(
//                        R.anim.translate_from_right,
//                        R.anim.translate_to_right
//                    )
                    mainActivity?.addOnceFragment(this@AddressFragment, MapsFragment())
                }
            }
        )
    }

    override fun onCallBack(bundle: Bundle) {
        if (bundle.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()) != null) {
            val addressBundle = Gson().fromJson<Address>(
                bundle.getString(TechresEnum.REPAIR_ADDRESS_FRAGMENT.toString()),
                object :
                    TypeToken<Address>() {}.type
            )
            this.address = addressBundle
            when (bundle.getString(TechresEnum.ADDRESS_FRAGMENT.toString())) {
                TechresEnum.HOME.toString() -> {
                    addressHome = addressBundle
                    binding.tvHomeAddress.text = addressBundle.address_full_text
                }
                TechresEnum.COMPANY.toString() -> {
                    addressCompany = addressBundle
                    binding.tvCompanyAddress.text = addressBundle.address_full_text
                }
                else -> {
                    dataSavedAddress.add(addressBundle)
                    adapterSaved?.notifyItemInserted(dataSavedAddress.size)
                    if(dataAddress.size == 3){
                        binding.lnAddAddress.hide()
                    }
                }
            }
        }
    }

    override fun onChooseAddressRepair(address: Address) {
        WriteLog.d("onChooseAddressRepair", "onChooseAddressRepair")
    }

    override fun onBackPress(): Boolean {
        isCheckBack = false
        return true
    }
}
