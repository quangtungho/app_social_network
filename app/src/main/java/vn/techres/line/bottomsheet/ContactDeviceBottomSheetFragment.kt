package vn.techres.line.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import vn.techres.line.R
import vn.techres.line.base.BaseBindingBottomSheetFragment
import vn.techres.line.databinding.FragmentContactDeviceBottomSheetBinding
import vn.techres.line.interfaces.contact.ContactHandler
import vn.techres.line.interfaces.contact.ContactNumberHandler

class ContactDeviceBottomSheetFragment :
    BaseBindingBottomSheetFragment<FragmentContactDeviceBottomSheetBinding>(
        FragmentContactDeviceBottomSheetBinding::inflate,
        R.style.CustomBottomSheetDialog
    ), ContactNumberHandler {

    private lateinit var contactHandler: ContactHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    fun setContactHandler(contactHandler: ContactHandler) {
        this.contactHandler = contactHandler
    }

    override fun onDismissDialog() {
        activity?.runOnUiThread {
            dismiss()
        }
    }

    override fun onNumberChoose(number: Int) {
        activity?.runOnUiThread {

        }
    }
}