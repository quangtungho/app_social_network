package vn.techres.line.fragment.address

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.address.AddressAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.utils.EventBusMap
import vn.techres.line.databinding.FragmentSearchMapsBinding
import vn.techres.line.fragment.map.MapsFragment
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ChooseAddressHandler
import java.io.IOException

class SearchMapsFragment :
    BaseBindingFragment<FragmentSearchMapsBinding>(FragmentSearchMapsBinding::inflate),
    ChooseAddressHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var adapter: AddressAdapter? = null
    private var dataAddress = ArrayList<vn.techres.line.data.model.address.Address>()
    private var handler : Handler? = null
    private var keySearch : String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        adapter = AddressAdapter(requireActivity())
        adapter?.setChooseAddressHandler(this)
        adapter?.setDataSource(dataAddress)
        binding.rcNearAddress.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcNearAddress.adapter = adapter
        setListener()
    }

    private fun setListener() {

        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }

        binding.imgPlacePoint.setOnClickListener {
            mainActivity?.addOnceFragment(this, MapsFragment())
        }

        binding.svAddress.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    keySearch = newText
                    if(handler != null){
                        handler?.removeCallbacks(runnable)
                    }else{
                        handler = Handler(Looper.getMainLooper())
                    }
                    handler?.postDelayed(runnable, 500)
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
        var arrayAddress: List<Address>? = ArrayList()
        try {
            arrayAddress = geocode.getFromLocationName(key, 5)
        } catch (e: IOException) {
            WriteLog.d("SearchMapsFragment", e.message ?: "")
        }
        arrayAddress?.let { list ->
            if (list.isNotEmpty()) {
                list.forEach {
                    val address = vn.techres.line.data.model.address.Address()
                    address.id = (1..999).random()
                    address.address_full_text = it.getAddressLine(0).toString()
                    address.name = it.subAdminArea
                    address.lat = it.latitude
                    address.lng = it.longitude
                    dataAddress.add(address)
                }
                adapter?.setDataSource(dataAddress)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun postAddressMap(event: EventBusMap) {
        val address = vn.techres.line.data.model.address.Address()
        address.id = (1..999).random()
        address.address_full_text = event.address_full_text
        address.name = event.name_address
        address.lat = event.latitude
        address.lng = event.longitude
        address.note = event.note
        dataAddress.add(address)
        adapter?.notifyItemInserted(dataAddress.size - 1)
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

    override fun onChooseAddress(address: vn.techres.line.data.model.address.Address) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.SEARCH_ADDRESS_FRAGMENT.toString(), Gson().toJson(address))
        mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
        mainActivity?.supportFragmentManager?.popBackStack()
    }

    override fun onBackPress(): Boolean {

        handler?.removeCallbacks(runnable)
        return true
    }

}