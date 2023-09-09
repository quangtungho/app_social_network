package vn.techres.line.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.deliveries.ConfigDeliveries
import vn.techres.line.databinding.ItemShippingUnitBinding
import vn.techres.line.helper.Currency
import vn.techres.line.interfaces.ShippingUnitHandler

class ShippingUnitAdapter(val context: Context) :
    RecyclerView.Adapter<ShippingUnitAdapter.ViewHolder>() {
    private var dataSource = ArrayList<ConfigDeliveries>()
    private var shippingUnitHandler: ShippingUnitHandler? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<ConfigDeliveries>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickShippingUnit(shippingUnitHandler: ShippingUnitHandler) {
        this.shippingUnitHandler = shippingUnitHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShippingUnitBinding.inflate(
                LayoutInflater.from(context),
                parent, false
            )
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.txtName.text = dataSource[position].name?:""
            binding.txtDescription.text = "Đây là đài truyền hình tiếng nói Việt Nam, phát thanh từ Hà Nội thủ đô nước Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam."
            binding.txtTotalMoney.text = "đ " + Currency.formatCurrencyDecimal(dataSource[position].shipping_fee!!.toFloat())
            binding.root.setOnClickListener {
                shippingUnitHandler!!.clickShippingUnit(dataSource[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    inner class ViewHolder(val binding: ItemShippingUnitBinding) :
        RecyclerView.ViewHolder(binding.root)

}