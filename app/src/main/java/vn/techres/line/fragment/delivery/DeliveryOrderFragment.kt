package vn.techres.line.fragment.delivery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.delivery.DeliveryFoodAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.databinding.FragmentDeliveryOrderBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

class DeliveryOrderFragment :
    BaseBindingFragment<FragmentDeliveryOrderBinding>(FragmentDeliveryOrderBinding::inflate) {

    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var adapter: DeliveryFoodAdapter? = null
    private var branchDetail: BranchDetail? = null
    private var address: Address? = null
    private var idOrder = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        adapter = DeliveryFoodAdapter(requireActivity())
        binding.rcFoodOrder.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcFoodOrder.adapter = adapter
        arguments?.let {
            idOrder = it.getInt(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), 0)
            binding.header.tvTitleHomeHeader.text =
                String.format("#%s", idOrder)
            branchDetail = Gson().fromJson<BranchDetail>(
                it.getString(TechresEnum.BRANCH_DETAIL.toString()),
                object :
                    TypeToken<BranchDetail>() {}.type
            )
            address = Gson().fromJson<Address>(
                it.getString(TechresEnum.ADDRESS_FRAGMENT.toString()),
                object :
                    TypeToken<Address>() {}.type
            )
            setData()
        }
        setListener()
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun setData() {
        binding.tvTitleCodeDelivery.text = String.format(
            "%s #%s", requireActivity().resources.getString(
                R.string.code_order
            ), idOrder
        )
        binding.tvAddressRestaurant.text =
            String.format("%s %s", requireActivity().resources.getString(R.string.restaurant), branchDetail?.name ?: "")
        binding.tvAddressRestaurant.text = branchDetail?.address_full_text ?: ""
        binding.tvAddressDelivery.text = address?.address_full_text ?: ""
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        binding.btnSeenMap.setOnClickListener {

        }
        binding.tvSeeDetail.setOnClickListener {

        }
    }

    override fun onBackPress(): Boolean {
        mainActivity?.supportFragmentManager?.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        return true
    }

}