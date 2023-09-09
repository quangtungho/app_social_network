package vn.techres.line.fragment.chat

import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import vn.techres.line.R
import vn.techres.line.adapter.chat.media.MediaManagerAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.databinding.FragmentMediaManagerBinding
import vn.techres.line.helper.techresenum.TechResEnumChat
import java.util.*

class MediaManagerFragment :
    BaseBindingChatFragment<FragmentMediaManagerBinding>(FragmentMediaManagerBinding::inflate) {

    private lateinit var adapter: MediaManagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitleHeader.text = requireActivity().baseContext.getString(R.string.archive_chat)
            .uppercase(Locale.getDefault())
        adapter = MediaManagerAdapter(this)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tbLayout, binding.pager) { tab, position ->
            tab.text = when(position) {
                0 -> {
                    requireActivity().getString(R.string.tab_image)
                }
                1 -> {
                    requireActivity().getString(R.string.tab_video)
                }
                2 -> {
                    requireActivity().getString(R.string.tab_link)
                }
                3 -> {
                    requireActivity().getString(R.string.tab_file)
                }
                else -> {
                    TechResEnumChat.TYPE_IMAGE.toString()
                }
            }
        }.attach()
        binding.tbLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.pager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.imgBack.setOnClickListener {
            activityChat?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onBackPress() : Boolean {
        return true
    }
}