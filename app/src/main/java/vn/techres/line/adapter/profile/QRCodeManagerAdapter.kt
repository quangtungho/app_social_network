package vn.techres.line.adapter.profile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vn.techres.line.fragment.qr.QRCodeFragment
import vn.techres.line.R
import vn.techres.line.fragment.qr.YourQRCodeFragment

class QRCodeManagerAdapter(fr: FragmentManager, private val context: Context) : FragmentPagerAdapter(fr, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private lateinit var fragment: Fragment
    private val yourQRCodeFragment = YourQRCodeFragment()
    private val qrCodeFragment = QRCodeFragment()
    override fun getPageTitle(position: Int): CharSequence {
        return when(position){
            0 -> context.getString(R.string.your_qr_code)
            1 -> context.getString(R.string.scan_qr)
            else -> ""
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        fragment = when(position){
            0 -> yourQRCodeFragment
            1 -> qrCodeFragment
            else -> yourQRCodeFragment
        }
        return fragment
    }
}