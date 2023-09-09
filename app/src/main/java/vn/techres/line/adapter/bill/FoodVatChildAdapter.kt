package vn.techres.line.adapter.bill

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.bill.FoodVATDataDetail
import vn.techres.line.databinding.ItemFoodVatBinding
import vn.techres.line.helper.Currency

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
class FoodVatChildAdapter(val context: Context) :
    RecyclerView.Adapter<FoodVatChildAdapter.ItemHolder>() {
    private var dataSource = ArrayList<FoodVATDataDetail>()
    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<FoodVATDataDetail>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemFoodVatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFoodVatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val data = dataSource[position]
            binding.txtFoodName.text = data.food_name
            binding.txtVATAmountBefore.text = Currency.formatCurrencyDecimal(data.price!!.toFloat())
            binding.txtVATAmountAfter.text = Currency.formatCurrencyDecimal(data.vat_amount!!.toFloat())
        }
    }
}