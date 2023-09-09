package vn.techres.line.adapter.order

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.OrderCustomer
import vn.techres.line.databinding.ItemOrderCustomerHistoryBinding
import vn.techres.line.helper.Currency
import vn.techres.line.interfaces.ChooseOrderCustomerHandler


class OrderCustomerAdapter(val context: Context) :
    RecyclerView.Adapter<OrderCustomerAdapter.ItemHolder>() {

    private var chooseOrderCustomerHandler: ChooseOrderCustomerHandler? = null
    private var orderCustomer = ArrayList<OrderCustomer>()

    fun setChooseOrderCustomerHandler(chooseOrderCustomerHandler: ChooseOrderCustomerHandler) {
        this.chooseOrderCustomerHandler = chooseOrderCustomerHandler
    }

    inner class ItemHolder(val binding: ItemOrderCustomerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemOrderCustomerHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return orderCustomer.size//bookingInformation.size
    }

    fun setDataSource(
        orderCustomer: ArrayList<OrderCustomer>
    ) {
        this.orderCustomer = orderCustomer
        notifyDataSetChanged()
    }

    fun getDataSource(): ArrayList<OrderCustomer> {
        return orderCustomer
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.tvOrderCode.text =
                String.format("%s%s", "#", orderCustomer[position].id.toString())
            binding.tvOrderTable.text = orderCustomer[position].table_name
            binding.tvTotalOrder.text =
                Currency.formatCurrencyDecimal(orderCustomer[position].total_amount!!.toFloat())
            binding.tvOrderDay.text = orderCustomer[position].payment_day!!.subSequence(0, 2)
            binding.tvOrderMonth.text =
                String.format("%s%s", "T", orderCustomer[position].payment_day!!.subSequence(3, 5))
            binding.tvOrderYear.text = orderCustomer[position].payment_day!!.subSequence(6, 10)
            when (orderCustomer[position].order_status) {

                1, 4 -> {
                    //WAITING_PAYMENT WAITING_COMPLETE
                    binding.tvStatusOrder.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green_gg
                        )
                    )
                    binding.lnOrderStatus.setBackgroundResource(R.drawable.bg_green_opacity)
                    binding.imgOrderStatus.setImageResource(R.drawable.ic_sandglass)
                    binding.tvStatusOrder.text =
                        context.resources.getString(R.string.waiting_payment)
                }
                2, 5 -> {
                    //DONE
                    binding.tvStatusOrder.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green_gg
                        )
                    )
                    binding.lnOrderStatus.setBackgroundResource(R.drawable.bg_green_opacity)
                    binding.imgOrderStatus.setImageResource(R.drawable.ic_check_green)
                    binding.tvStatusOrder.text = context.resources.getString(R.string.done)
                }
                8 -> {
                    //CANCELLED
                    binding.tvStatusOrder.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.color_wait_collect_money
                        )
                    )
                    binding.lnOrderStatus.setBackgroundResource(R.drawable.bg_red_opacity)
                    binding.imgOrderStatus.setImageResource(R.drawable.ic_cancel_red)
                    binding.tvStatusOrder.text = context.resources.getString(R.string.cancel)
                }
                else -> {
                    binding.tvStatusOrder.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue_logo
                        )
                    )
                    binding.lnOrderStatus.setBackgroundResource(R.drawable.bg_blue_opacity)
                    binding.imgOrderStatus.setImageResource(R.drawable.ic_food_tray)
                    binding.tvStatusOrder.text = context.resources.getString(R.string.serving)
                }
            }
            binding.tvNameBranchOrderCustomer.text = orderCustomer[position].branch_name
            if (orderCustomer[position].order_status == 2) {
                binding.tvTimeOrder.text = orderCustomer[position].payment_day!!.substring(11)
            } else {
                binding.tvTimeOrder.text = orderCustomer[position].created_at!!.substring(11)
            }
            if (orderCustomer[position].is_online == 1) {
                binding.tvOnlineOrder.text = context.resources.getString(R.string.order_online)
            } else {
                binding.tvOnlineOrder.text = context.resources.getString(R.string.order_offline)
            }
            itemView.setOnClickListener {
                chooseOrderCustomerHandler!!.chooseOrder(position)
            }
        }

    }


}