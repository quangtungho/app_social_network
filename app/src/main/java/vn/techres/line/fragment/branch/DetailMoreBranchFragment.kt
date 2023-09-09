package vn.techres.line.fragment.branch

import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.header_item.view.*
import vn.techres.line.R
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.databinding.FragmentDetailMoreBranchBinding
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import java.util.*

@Suppress("DEPRECATION")
class DetailMoreBranchFragment : BaseBindingFragment<FragmentDetailMoreBranchBinding>(FragmentDetailMoreBranchBinding::inflate) {

    private var detail: BranchDetail? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)

        arguments?.let {
            detail = Gson().fromJson(it.getString(TechresEnum.BRANCH_DETAIL.toString()), object :
                TypeToken<BranchDetail>() {}.type)
            setDataMore(detail)
            setDataUtility(detail)
        }

        binding.imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }


    private fun setDataUtility(detail: BranchDetail?){
        if (detail!!.serve_time.isNotEmpty()){
            binding.time.tvPass.text = detail.serve_time[0].day_of_week_name
        }

        if (detail.is_have_card_payment == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility1.setImageResource(R.drawable.ic_utility_1_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility1.setTextColor(resources.getColor(R.color.main_bg))
        }
        if (detail.is_have_booking_online == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility3.setImageResource(R.drawable.ic_utility_3_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility3.setTextColor(resources.getColor(R.color.main_bg))
        }
        if (detail.is_have_booking_online == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility2.setImageResource(R.drawable.ic_utility_2_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility2.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_shipping == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility4.setImageResource(R.drawable.ic_utility_4_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility4.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_car_parking == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility5.setImageResource(R.drawable.ic_utility_5_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility5.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_private_room == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility9.setImageResource(R.drawable.ic_utility_9_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility9.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_outdoor == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility10.setImageResource(R.drawable.ic_utility_10_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility10.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_child_corner == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility11.setImageResource(R.drawable.ic_utility_11_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility11.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_live_music == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility12.setImageResource(R.drawable.ic_utility_12_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility12.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_karaoke == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility13.setImageResource(R.drawable.ic_utility_13_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility13.setTextColor(resources.getColor(R.color.main_bg))
        }
        if (detail.is_have_invoice == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility14.setImageResource(R.drawable.ic_utility_14_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility14.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_wifi == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility8.setImageResource(R.drawable.ic_utility_8_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility8.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_free_parking == 1){
            binding.utilities.utilitiesChild2.imgRestaurantUtility6.setImageResource(R.drawable.ic_utility_6_selected)
            binding.utilities.utilitiesChild2.tvRestaurantUtility6.setTextColor(resources.getColor(R.color.main_bg))
        }

        if (detail.is_have_air_conditioner == 1){
            binding.utilities.utilitiesChild1.imgRestaurantUtility7.setImageResource(R.drawable.ic_utility_7_selected)
            binding.utilities.utilitiesChild1.tvRestaurantUtility7.setTextColor(resources.getColor(R.color.main_bg))
        }

    }

    private fun setDataMore(detail: BranchDetail?) {
        binding.time.tvCreateAt.text = String.format("%s %s %s", detail!!.open_time, "-", detail.close_time)
    }
    override fun onBackPress() : Boolean{
        return true
    }
}
