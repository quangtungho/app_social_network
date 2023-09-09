package vn.techres.line.adapter.point

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.ListDetailsMemberCard
import vn.techres.line.databinding.ItemDetailsMemberCardBinding
import vn.techres.line.helper.Currency

class ListDetailsMemberCardAdapter(
    var context: Context
) : RecyclerView.Adapter<ListDetailsMemberCardAdapter.ItemHolder>() {
    private var dataSource = ArrayList<ListDetailsMemberCard>()

    inner class ItemHolder(val binding: ItemDetailsMemberCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemDetailsMemberCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }
    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setDataSource(
        dataSource: ArrayList<ListDetailsMemberCard>
    ) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun getDataSource(): List<ListDetailsMemberCard> {
        return dataSource
    }

    @SuppressLint("ResourceAsColor", "Range")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val item: ListDetailsMemberCard = dataSource[position]
            if (item.order_id != 0 && item.type == 1) {
                binding.tvName.text = String.format("%s %s", context.getString(R.string.bill_point_card) , item.order_id)
                binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvPoint.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.tvPointText.setTextColor(ContextCompat.getColor(context, R.color.red))
                binding.lineDot.setColorFilter(ContextCompat.getColor(context, R.color.black))
                binding.tvPoint.text = String.format("%s%s", context.getString(R.string.minus),Currency.formatCurrencyDecimal(item.total_all_point!!.toFloat()))
            } else {
                if (item.type == 0) {
                    binding.tvName.text = context.getString(R.string.addPoint)
                    binding.tvPoint.text = String.format("%s%s", context.getString(R.string.plus),Currency.formatCurrencyDecimal(item.total_all_point!!.toFloat()))
                    binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.green))
                    binding.tvPoint.setTextColor(ContextCompat.getColor(context, R.color.green))
                    binding.tvPointText.setTextColor(ContextCompat.getColor(context, R.color.green))
                    binding.lineDot.setColorFilter(ContextCompat.getColor(context, R.color.green))

                } else {
                    binding.tvName.text = context.getString(R.string.subPoint)
                    binding.tvPoint.text = String.format("%s%s", context.getString(R.string.minus),Currency.formatCurrencyDecimal(item.total_all_point!!.toFloat()))
                    binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.red))
                    binding.tvPoint.setTextColor(ContextCompat.getColor(context, R.color.red))
                    binding.tvPointText.setTextColor(ContextCompat.getColor(context, R.color.red))
                    binding.lineDot.setColorFilter(ContextCompat.getColor(context, R.color.red))
                }
            }
            binding.tvDatePoint.text = String.format("%s %s", context.getString(R.string.day), item.created_at)
            binding.tvStatus.text = item.note

        }

    }


}
