package vn.techres.line.activity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityTurnOnBubbleChatBinding

class TurnOnBubbleChatActivity : BaseBindingActivity<ActivityTurnOnBubbleChatBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityTurnOnBubbleChatBinding
        get() = ActivityTurnOnBubbleChatBinding::inflate

    override fun onSetBodyView() {

        binding.btnSetting.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE)
        }

        binding.btnOtherTime.setOnClickListener {
            onBackPressed()
        }

    }
}