package vn.techres.line.activity

import android.content.Intent
import android.view.LayoutInflater
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityAdvertisementRegistrySuccessBinding

class AdvertisementRegistrySuccessActivity :
    BaseBindingActivity<ActivityAdvertisementRegistrySuccessBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityAdvertisementRegistrySuccessBinding
        get() = ActivityAdvertisementRegistrySuccessBinding::inflate

    override fun onSetBodyView() {
        binding.txtCallBackMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
    }
}