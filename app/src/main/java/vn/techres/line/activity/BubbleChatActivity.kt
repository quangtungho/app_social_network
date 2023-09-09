package vn.techres.line.activity

import android.view.LayoutInflater
import androidx.fragment.app.add
import androidx.fragment.app.commit
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityBubbleChatBinding
import vn.techres.line.fragment.chat.group.ChatGroupFragment

class BubbleChatActivity : BaseBindingActivity<ActivityBubbleChatBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityBubbleChatBinding
        get() = ActivityBubbleChatBinding::inflate

    override fun onSetBodyView() {
        supportFragmentManager.commit {
            add<ChatGroupFragment>(R.id.frameContainer)
            addToBackStack(ChatGroupFragment().tag)
        }
    }

}