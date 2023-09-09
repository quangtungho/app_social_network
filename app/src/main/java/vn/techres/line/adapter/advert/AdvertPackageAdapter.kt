package vn.techres.line.adapter.advert

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.avert.Advert
import vn.techres.line.databinding.ItemAdvertPachageBinding
import vn.techres.line.helper.Currency

/**
 * @Author: Phạm Văn Nhân
 * @Date: 1/11/2022
 */
@Suppress("DEPRECATION")
class AdvertPackageAdapter(var context: Context) :
    RecyclerView.Adapter<AdvertPackageAdapter.ItemHolder>() {

    private var dataSource = ArrayList<Advert>()

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Advert>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItemCount() = dataSource.size

    inner class ItemHolder(val binding: ItemAdvertPachageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemAdvertPachageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val data = dataSource[position]
            binding.txtCreateTime.text = data.created_at
            binding.txtName.text = data.name
            if (data.type == 1) {
                binding.txtAdvertPackage.text = "PREMIUM"
                binding.txtAdvertPackage.setTextColor(context.resources.getColor(R.color.blue_gg))
                binding.txtPackageTime.text = String.format(
                    "%s %s = %s %s",
                    data.media_length_by_second,
                    context.resources.getText(R.string.second),
                    Currency.formatCurrencyDecimal(data.cost!!.toFloat()),
                    context.resources.getText(R.string.dong)
                )
            } else {
                binding.txtAdvertPackage.text = "BUSINESS"
                binding.txtAdvertPackage.setTextColor(context.resources.getColor(R.color.main_bg))
                binding.txtPackageTime.text = String.format(
                    "%s %s = %s %s",
                    data.media_length_by_second,
                    context.resources.getText(R.string.second),
                    Currency.formatCurrencyDecimal(data.cost!!.toFloat()),
                    context.resources.getText(R.string.point)
                )
            }

            if (data.status == 2 && data.is_running == 0 && data.play_count == 0) {
                //Đã duyệt
                binding.txtApproved.visibility = View.VISIBLE
                binding.txtRunning.visibility = View.GONE
                binding.txtRefuse.visibility = View.GONE
                binding.txtPending.visibility = View.GONE
                binding.txtPause.visibility = View.GONE
            } else if (data.status == 2 && data.is_running == 1) {
                //Đang chạy
                binding.txtApproved.visibility = View.GONE
                binding.txtRunning.visibility = View.VISIBLE
                binding.txtRefuse.visibility = View.GONE
                binding.txtPending.visibility = View.GONE
                binding.txtPause.visibility = View.GONE
            } else if (data.status == 1) {
                //Từ chối
                binding.txtApproved.visibility = View.GONE
                binding.txtRunning.visibility = View.GONE
                binding.txtRefuse.visibility = View.VISIBLE
                binding.txtPending.visibility = View.GONE
                binding.txtPause.visibility = View.GONE
            } else if (data.status == 0) {
                //Chờ duyệt
                binding.txtApproved.visibility = View.GONE
                binding.txtRunning.visibility = View.GONE
                binding.txtRefuse.visibility = View.GONE
                binding.txtPending.visibility = View.VISIBLE
                binding.txtPause.visibility = View.GONE
            } else if (data.status == 2 && data.is_running == 0 && data.play_count!! > 0) {
                //Tạm ngưng
                binding.txtApproved.visibility = View.GONE
                binding.txtRunning.visibility = View.GONE
                binding.txtRefuse.visibility = View.GONE
                binding.txtPending.visibility = View.GONE
                binding.txtPause.visibility = View.VISIBLE
            }

            binding.txtShowDisplay.text = data.play_count.toString()
        }

    }


}