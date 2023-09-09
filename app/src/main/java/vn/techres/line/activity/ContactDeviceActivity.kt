package vn.techres.line.activity

import android.view.LayoutInflater
import androidx.appcompat.widget.SearchView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import vn.techres.line.R
import vn.techres.line.adapter.contact.ContactManagerAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityContactDeviceBinding
import vn.techres.line.interfaces.contact.ContactHandler
import vn.techres.line.interfaces.contact.ContactNumberHandler
import java.util.*

class ContactDeviceActivity : BaseBindingActivity<ActivityContactDeviceBinding>(), ContactNumberHandler{

    private lateinit var adapter: ContactManagerAdapter
    private lateinit var contactHandler: ContactHandler
    override val bindingInflater: (LayoutInflater) -> ActivityContactDeviceBinding
        get() = ActivityContactDeviceBinding::inflate

    override fun onSetBodyView() {
        adapter = ContactManagerAdapter(this)
        binding.pager.adapter = adapter
        binding.svContact.clearFocus()
        binding.tvTitleContact.text = resources.getString(R.string.title_contacts).uppercase(
            Locale.getDefault()
        )
        TabLayoutMediator(binding.tbLayout, binding.pager) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    resources.getString(R.string.saved)
                }
                1 -> {
                    resources.getString(R.string.recently)
                }
                else -> {
                    resources.getString(R.string.recently)
                }
            }
        }.attach()
        binding.tbLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.pager.currentItem = tab?.position ?: 0
//                positionTab = tab?.position ?: 0
//                contactHandler.onNumberChoose(
//                    if (positionTab == 0) numberChooseContact else numberChooseRecentCall
//                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.svContact.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    contactHandler.onResearch(it)
                }
                return true
            }

        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun setContactHandler(contactHandler: ContactHandler) {
        this.contactHandler = contactHandler
    }


    override fun onDismissDialog() {
        runOnUiThread {

        }
    }

    override fun onNumberChoose(number: Int) {
        runOnUiThread {

        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}