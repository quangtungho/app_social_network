package vn.techres.line.helper.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions

/**
 * @author aminography
 */

val Context.isDisplayPortrait: Boolean
    get() = (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)

val Context.androidId: String
    @SuppressLint("HardwareIds")
    get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

val Context.deviceIMEI: String
    @SuppressLint("HardwareIds")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    get() {
        var uniqueIdentifier = ""
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (shouldAskPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE))) {
            uniqueIdentifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei ?: androidId
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.deviceId ?: androidId
            }
        }
        return uniqueIdentifier
    }