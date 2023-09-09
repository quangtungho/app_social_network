package vn.techres.line.adapter.restaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.restaurant.RestaurantCardLevel
import vn.techres.line.databinding.ItemLevelCardBinding

class RestaurantCardDetailAdapter(var context: Context) :
    RecyclerView.Adapter<RestaurantCardDetailAdapter.ItemHolder>() {
    private var data = ArrayList<RestaurantCardLevel>()

    fun setDataSource(data: ArrayList<RestaurantCardLevel>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemLevelCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemLevelCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val item = data[position]
            binding.tvLevelCard.text = item.name
            binding.tvCashBackToPointPercent.text = String.format(
                "%s: %s%s",
                context.resources.getString(R.string.cash_back_to_point_percent),
                item.cashback_to_point_percent,
                context.resources.getString(R.string.percent)
            )
            if (item.is_my_level == 1) {
                binding.tvLevelCurrent.visibility = View.VISIBLE
            } else {
                binding.tvLevelCurrent.visibility = View.INVISIBLE
            }

            if (item.status == 1) {
                binding.imgOverlay.visibility = View.GONE
            } else {
                binding.imgOverlay.visibility = View.VISIBLE
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

}