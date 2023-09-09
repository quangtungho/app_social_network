package vn.techres.line.fragment.qr

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.profile.QRCodeManagerAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.utils.EventBusChangeCamera
import vn.techres.line.data.model.utils.EventBusFlashLight
import vn.techres.line.databinding.FragmentQRManagerCodeBinding
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.invisible

class QRCodeManagerFragment :
    BaseBindingFragment<FragmentQRManagerCodeBinding>(FragmentQRManagerCodeBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    var isFragmentLoaded = false

    private var adapter: QRCodeManagerAdapter? = null
    private var isCamera = false
    private var isFlashLight = false
    private var fadeIn: Animation? = null
    private var fadeOut: Animation? = null
    private var topToBottom: Animation? = null
    private var bottomToTop: Animation? = null

//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
//        if(isVisibleToUser && !isFragmentLoaded){
//            adapter!!.getItem(0)
//            isFragmentLoaded = true
//        }else
//            adapter!!.getItem(1)
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
        mainActivity?.setOnBackClick(this)
        binding.tvTitle.text = resources.getString(R.string.qr_code)
        fadeIn = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_in)
        fadeOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.fade_out)
        topToBottom = AnimationUtils.loadAnimation(requireActivity(), R.anim.top_to_bottom)
        bottomToTop = AnimationUtils.loadAnimation(requireActivity(), R.anim.anim_birthday)
        adapter = QRCodeManagerAdapter(childFragmentManager, requireActivity())
        binding.tabLayout.setupWithViewPager(binding.pager)
//        binding.pager.setOffscreenPageLimit(0)
        binding.pager.adapter = adapter
        binding.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        setListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.imgFlashLight.setOnClickListener {
            if (hasFlash() == false) {
                context?.let {
                    Toast.makeText(
                        it,
                        it.resources.getString(R.string.empty_flash_light),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                isFlashLight = !isFlashLight
                EventBus.getDefault().post(EventBusFlashLight(isFlashLight))
            }
        }
        binding.imgChangeCamera.setOnClickListener {
            isCamera = !isCamera
            EventBus.getDefault().post(EventBusChangeCamera(isCamera))
        }
//        binding.imgLibrary.setOnClickListener {
//            EventBus.getDefault().post(EventBusQrCodeArchive())
//        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.lnTitleCode.show()
                        binding.groupController.invisible()
                        binding.lnTitleCode.startAnimation(fadeIn)
                        binding.groupController.startAnimation(topToBottom)
                    }
                    1 -> {
                        binding.lnTitleCode.invisible()
                        binding.groupController.show()
                        binding.lnTitleCode.startAnimation(fadeOut)
                        binding.groupController.startAnimation(bottomToTop)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean? {
        return mainActivity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onBackPress(): Boolean {

        return true
    }

}