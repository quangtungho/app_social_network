package vn.techres.line.adapter.bill

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_list_bill.view.*
import vn.techres.line.R
import vn.techres.line.data.model.OrderDetailBill
import vn.techres.line.helper.Currency
import vn.techres.line.interfaces.bill.BillHandler


class BillAdapter(var context: Context) :
    RecyclerView.Adapter<BillAdapter.RecyclerViewHolder>() {

    private val order = 2
    private val orderNotUnderline = 3
    private var billHandler: BillHandler? = null
    private var dataSource = ArrayList<OrderDetailBill>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        return if (viewType == order) {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_list_bill, parent, false)
            RecyclerViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_bill_not_underline, parent, false)
            RecyclerViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == dataSource.size - 1) {
            orderNotUnderline
        } else {
            order
        }
    }

    fun setBillHandler(billHandler: BillHandler) {
        this.billHandler = billHandler
    }

    fun setDataSource(
        dataSource: ArrayList<OrderDetailBill>
    ) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = dataSource[position]

        if (item.is_gift == 1) {
            holder.imgGiftBill.visibility = View.VISIBLE
            holder.lnNameFoodBill.isEnabled = true
        } else {
            holder.imgGiftBill.visibility = View.GONE
            holder.lnNameFoodBill.isEnabled = false
        }

        if (item.is_purchase_by_point == 1) {
            holder.tvUnitPrice.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(item.point_to_purchase!!.toFloat()),
                context.getString(R.string.point_mini)
            )
            holder.tvTotalItemPoint.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(item.total_point_to_purchase!!.toFloat()),
                context.getString(R.string.point_mini)
            )
            holder.tvTotalItemPoint.visibility = View.VISIBLE
        } else {
            holder.tvUnitPrice.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(item.price!!.toFloat()),
                context.getString(R.string.denominations)
            )
            holder.tvTotalItemPoint.visibility = View.GONE
        }
        if (item.note!!.isNotEmpty()) {
            holder.tvNameFoodBill.text = Html.fromHtml(
                String.format(
                    "%s%s",
                    item.food_name,
                    "<font color='#198be3' >(i)</font>"
                ), Html.FROM_HTML_MODE_COMPACT
            )

        } else {
            holder.tvNameFoodBill.text = item.food_name
        }

        holder.tvCountFoodBill.text = item.quantity.toString()
        holder.tvTotalItemAmount.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(item.total_amount!!.toFloat()),
            context.getString(R.string.denominations)
        )

        if (item.category_type == 2 || item.category_type == 3) {

            if (item.order_status == 0) {
                //Dang cho xuat kho
                holder.tvStatusFoodBill.text = String.format("( %s)", "Đang xuất kho")
                holder.tvStatusFoodBill.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.main_bg
                    )
                )
            } else if (item.order_status == 2 && item.approved_drink == 0) {
                // dang su dung
//                    holder.tvStatusFoodBill.text = String.format("( %s)", "Đang sử dụng")
//                    holder.tvStatusFoodBill.setTextColor(ContextCompat.getColor(context, R.color.blue_home_item))
                holder.tvStatusFoodBill.text = String.format("( %s)", "Hoàn tất")
                holder.tvStatusFoodBill.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.green_gg
                    )
                )
            } else if (item.order_status == 2 && item.approved_drink == 1) {
                // done
                holder.tvStatusFoodBill.text = String.format("( %s)", "Hoàn tất")
                holder.tvStatusFoodBill.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.green_gg
                    )
                )
            } else if (item.order_status == 3) {
                //het
                holder.tvStatusFoodBill.text = String.format("( %s)", "Hết món")
                holder.tvStatusFoodBill.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else if (item.order_status == 4) {
                // huy
                holder.tvTotalItemAmount.background =
                    ContextCompat.getDrawable(context, R.drawable.striking_text)
                holder.tvTotalItemPoint.background =
                    ContextCompat.getDrawable(context, R.drawable.striking_text)
                holder.tvTotalItemAmount.foreground =
                    ContextCompat.getDrawable(context, R.drawable.striking_text)
                holder.tvTotalItemPoint.foreground =
                    ContextCompat.getDrawable(context, R.drawable.striking_text)
                holder.tvStatusFoodBill.text = String.format("( %s)", "Hủy món")
                holder.tvStatusFoodBill.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
        } else {
            when (item.order_status) {

                0 -> {
                    //PENDING(Đang chờ nấu)


                    holder.tvStatusFoodBill.text = String.format("( %s)", "Đang chờ nấu")
                    holder.tvStatusFoodBill.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.main_bg
                        )
                    )


                }
                1 -> {
                    //COOKING(Đang nấu)
                    holder.tvStatusFoodBill.text = String.format("( %s)", "Đang nấu")
                    holder.tvStatusFoodBill.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue_home_item
                        )
                    )
                }
                2 -> {
                    //DONE(Hoàn tất)
                    holder.tvStatusFoodBill.text = String.format("( %s)", "Hoàn tất")
                    holder.tvStatusFoodBill.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green_gg
                        )
                    )
                }
                3 -> {
                    //OUTSTOCK(Hết hàng)
                    holder.tvStatusFoodBill.text = String.format("( %s)", "Hết món")
                    holder.tvStatusFoodBill.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.red
                        )
                    )
                }
                4 -> {
                    //CANCEL(Hủy)
                    holder.tvStatusFoodBill.text = String.format("( %s)", "Hủy món")

                    holder.tvStatusFoodBill.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.red
                        )
                    )
                }
            }
        }
        if (item.order_status == -1) {
            holder.tvStatusFoodBill.text = String.format("( %s)", "Chờ xác nhận")
            holder.tvStatusFoodBill.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
        }
        holder.lnNameFoodBill.setOnClickListener {
            billHandler?.onNoteFood(item)
        }

        holder.imgGiftBill.setOnClickListener {
            billHandler?.onGiftFood(item)
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNameFoodBill = itemView.tvNameFoodBill!!
        val tvCountFoodBill = itemView.tvCountFoodBill!!
        val tvUnitPrice = itemView.tvUnitPrice!!
        val tvTotalItemAmount = itemView.tvTotalFoodAmount!!
        val tvTotalItemPoint = itemView.tvTotalFoodPoint!!
        val tvStatusFoodBill = itemView.tvStatusFoodBill!!
        val imgGiftBill = itemView.imgGiftBill!!
        val lnNameFoodBill = itemView.lnNameFoodBill!!
    }

}