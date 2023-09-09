package vn.techres.line.adapter.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_bill.view.*
import vn.techres.line.R
import vn.techres.line.helper.Currency
import vn.techres.line.data.model.OrderDetail

class OrderDetailAdapter(val context : Context) : RecyclerView.Adapter<OrderDetailAdapter.ViewHolder>(){

    private var orderDetailList = ArrayList<OrderDetail>()
    private val ORDER = 2
    private val ORDERNOTUNDERLINE = 3


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return if (viewType == ORDER) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_list_bill, parent, false)
            ViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_list_bill_not_underline, parent, false)
            ViewHolder(view)
        }
    }

    private var orderDetail: OrderDetail? = null

    override fun getItemViewType(position: Int): Int {
        return if (position == orderDetailList.size - 1) {
            ORDERNOTUNDERLINE
        } else {
            ORDER
        }
    }

    fun setDataSource(
        orderDetailList: ArrayList<OrderDetail>
    ) {
        this.orderDetailList = orderDetailList
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return orderDetailList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = orderDetailList[position]
        holder.tvNameFoodBill.text= item.food_name
        holder.tvCountFoodBill.text=item.quantity.toString()
        holder.tvUnitPrice.text= Currency.formatCurrencyDecimal(item.price!!.toFloat())
        holder.tvTotalItemAmount.text= String.format("%s %s", Currency.formatCurrencyDecimal(item.total_amount!!.toFloat()), context.resources.getString(R.string.denominations))
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNameFoodBill = itemView.tvNameFoodBill!!
        val tvCountFoodBill = itemView.tvCountFoodBill!!
        val tvUnitPrice = itemView.tvUnitPrice!!
        val tvTotalItemAmount = itemView.tvTotalFoodAmount!!
        val tvTotalItemPoint = itemView.tvTotalFoodPoint!!
    }
}