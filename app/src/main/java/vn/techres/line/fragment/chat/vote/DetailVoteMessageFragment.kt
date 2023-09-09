package vn.techres.line.fragment.chat.vote

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.chat.DetailVotedAdapter
import vn.techres.line.adapter.chat.vote.ChooseVoteAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentDetailVoteMessageBinding

class DetailVoteMessageFragment :
    BaseBindingFragment<FragmentDetailVoteMessageBinding>(FragmentDetailVoteMessageBinding::inflate) {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private lateinit var adapterChooseVote: ChooseVoteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapterChooseVote = ChooseVoteAdapter()
        binding.rcVote.layoutManager = LinearLayoutManager(mainActivity!!.baseContext, RecyclerView.VERTICAL, false)
        binding.rcVote.adapter = adapterChooseVote
        binding.btnVoted.setOnClickListener {

        }
        binding.lnAddPlan.setOnClickListener {

        }
        binding.btnSupportVote.setOnClickListener {
            dialog()
        }
        binding.imgBack.setOnClickListener {
            onBackPress()
        }
    }

    private fun bottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_detail_vote)
        bottomSheetDialog.setCancelable(true)
        val tabLayout = bottomSheetDialog.findViewById<TabLayout>(R.id.tbDetailVote)
        val pager = bottomSheetDialog.findViewById<ViewPager2>(R.id.pagerDetailVote)
        val adapter = DetailVotedAdapter(this, ArrayList())

        pager?.adapter = adapter
        if (tabLayout != null) {
            if (pager != null) {
                TabLayoutMediator(tabLayout, pager) { tab, position ->
                    tab.text = when (position) {
                        0 -> {
                            "OBJECT ${(position + 1)}"
                        }
                        1 -> {
                            "OBJECT ${(position + 1)}"
                        }
                        2 -> {
                            "OBJECT ${(position + 1)}"
                        }
                        else -> {
                            "OBJECT ${(position + 1)}"
                        }
                    }
                }.attach()
            }
        }
        bottomSheetDialog.show()
    }

    private fun dialog() {
        val dialog = mainActivity?.baseContext?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_quick_support_vote)
        dialog?.findViewById<TextView>(R.id.tvQuestionDialog)
        dialog?.findViewById<TextView>(R.id.tvPinnedHeader)
        dialog?.findViewById<TextView>(R.id.tvListVote)
        dialog?.findViewById<TextView>(R.id.tvVoteLock)
        dialog?.findViewById<TextView>(R.id.tvVoteUnLock)
        dialog?.show()
    }

    override fun onBackPress() : Boolean{
//        mainActivity?.findNavController(R.id.nav_host)?.popBackStack()
        return true
    }

}