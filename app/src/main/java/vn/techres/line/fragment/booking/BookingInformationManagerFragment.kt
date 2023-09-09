package vn.techres.line.fragment.booking

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import vn.techres.line.R
import vn.techres.line.activity.CreateBookingActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.booking.BookingInformationManageAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentBookingInformaitonManagerBinding
import vn.techres.line.helper.utils.AlolineColorUtil


class BookingInformationManagerFragment : BaseBindingFragment<FragmentBookingInformaitonManagerBinding>(FragmentBookingInformaitonManagerBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: BookingInformationManageAdapter? = null
    private var fade: Animation? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.booking_information)

        fade = AnimationUtils.loadAnimation(context, R.anim.anim_birthday)
        //init tabLayout
        adapter = BookingInformationManageAdapter(childFragmentManager, requireActivity())
        binding.pager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        setFabAction()
    }
    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }


    private fun setFabAction(){
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.fabActionBooking.apply {
            this.alpha = 1F
            this.startAnimation(fade)
            this.show()
            this.setImageResource(R.drawable.ic_add_white)
            this.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(
                requireActivity(),
                R.color.white
                 )
            )
            this.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.main_bg
                )
            )
            this.setOnClickListener {
                val intent = Intent(mainActivity, CreateBookingActivity::class.java)
                startActivity(intent)
                mainActivity!!.overridePendingTransition(
                    R.anim.translate_from_right,
                    R.anim.translate_to_right
                )
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onBackPress() : Boolean{
        return true
    }
}
