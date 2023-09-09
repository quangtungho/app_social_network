package vn.techres.line.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import vn.techres.line.R
import vn.techres.line.adapter.chat.ListBirthDayCartAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.BirthDayCard
import vn.techres.line.databinding.ActivitySendBirthDayCartBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ClickBirthDayCard

class SendBirthDayCartActivity : BaseBindingActivity<ActivitySendBirthDayCartBinding>(), ClickBirthDayCard {
    private var mList = ArrayList<BirthDayCard>()
    private var mAdapter : ListBirthDayCartAdapter? = null
    override val bindingInflater: (LayoutInflater) -> ActivitySendBirthDayCartBinding
        get() = ActivitySendBirthDayCartBinding::inflate

    override fun onSetBodyView() {
        binding.header.toolbarBack.setOnClickListener { finish() }
        binding.header.toolbarTitle.text = "Gửi thiệp mừng sinh nhật"
        mAdapter = ListBirthDayCartAdapter(applicationContext)
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList.add(BirthDayCard(""))
        mList[0].isSelected = true
        ChatUtils.configRecyclerHoziView(binding.rclBirthDayCart, mAdapter)
        mAdapter?.setDataSource(mList)
        mAdapter?.setClickBirthDayCard(this)
    }

    @SuppressLint("NotifyDataSetChanged", "UseCompatLoadingForDrawables")
    override fun onClick(position: Int) {
        mList.forEachIndexed { index, birthDayCard ->
            if(birthDayCard.isSelected) {
                birthDayCard.isSelected = false
                mAdapter?.notifyItemChanged(index)
            }
        }
        mList[position].isSelected = true
        mAdapter?.notifyItemChanged(position)
        Glide.with(applicationContext)
            .load(
                resources.getDrawable(R.drawable.background_happy_birthday)
            )
            .centerCrop()
            .error(R.drawable.illus_empty_photos)
            .into(binding.imvBirthDayCard)
    }
}