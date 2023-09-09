package vn.techres.line.helper.privacyprolicy

import android.app.Activity
import android.content.Intent
import android.webkit.JavascriptInterface
import vn.techres.line.helper.techresenum.TechresEnum

class WebAppInterface(private val activity: Activity) {
    @JavascriptInterface
    fun onShare(){
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody = String.format("Application Link : %s", TechresEnum.PRIVACY_POLICY_URL.toString())
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        activity.startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
    }
}