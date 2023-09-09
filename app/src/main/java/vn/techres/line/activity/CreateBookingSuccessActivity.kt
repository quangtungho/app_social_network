package vn.techres.line.activity

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityCreateBookingSuccessBinding
import vn.techres.line.helper.techresenum.TechresEnum

class CreateBookingSuccessActivity : BaseBindingActivity<ActivityCreateBookingSuccessBinding>() {
    private var phoneNumber = ""
    override val bindingInflater: (LayoutInflater) -> ActivityCreateBookingSuccessBinding
        get() = ActivityCreateBookingSuccessBinding::inflate

    override fun onSetBodyView() {

        intent.let {
            phoneNumber = it.getStringExtra(TechresEnum.BRANCH_PHONE.toString())?:""
        }

        binding.btnCallHotLine.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(dialIntent)
        }

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