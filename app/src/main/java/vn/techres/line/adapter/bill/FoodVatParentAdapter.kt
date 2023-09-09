package vn.techres.line.adapter.bill

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.bill.FoodVATData
import vn.techres.line.databinding.ItemFoodVatHeaderBinding
import vn.techres.line.helper.Currency

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class FoodVatParentAdapter(val context: Context) :
    RecyclerView.Adapter<FoodVatParentAdapter.ItemHolder>() {
    private var dataSource = ArrayList<FoodVATData>()
    private var childAdapter: FoodVatChildAdapter? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<FoodVATData>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemFoodVatHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFoodVatHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val data = dataSource[position]
            binding.txtVATPercent.text = String.format("%s %s (%s)", "Thuế suất", data.vat_percent.toInt().toString() + "%", data.order_details.size)
            binding.txtVATAmountTotal.text = String.format("%s %s", "Thuế:", Currency.formatCurrencyDecimal(data.vat_amount.toFloat()))

            childAdapter = FoodVatChildAdapter(context)
            binding.recyclerViewChild.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            binding.recyclerViewChild.adapter = childAdapter
            childAdapter!!.setDataSource(data.order_details)
        }

    }
}