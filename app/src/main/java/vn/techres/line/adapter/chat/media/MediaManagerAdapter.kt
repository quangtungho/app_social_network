package vn.techres.line.adapter.chat.media

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.techres.line.fragment.chat.MediaFragment
import vn.techres.line.helper.techresenum.TechResEnumChat

class MediaManagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 ->{
                MediaFragment().newInstance(TechResEnumChat.TYPE_IMAGE.toString())
            }
            1 ->{
                MediaFragment().newInstance(TechResEnumChat.TYPE_VIDEO.toString())
            }
            2 ->{
                MediaFragment().newInstance(TechResEnumChat.TYPE_LINK.toString())
            }
            3 ->{
                MediaFragment().newInstance(TechResEnumChat.TYPE_FILE.toString())
            }
            else ->{
                MediaFragment().newInstance(TechResEnumChat.TYPE_IMAGE.toString())
            }
        }
    }
}