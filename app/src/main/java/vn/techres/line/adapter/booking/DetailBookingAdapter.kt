package vn.techres.line.adapter.booking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.data.model.booking.BookingFood
import vn.techres.line.databinding.ItemBookingDetailBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.booking.DetailBookingHandler

class DetailBookingAdapter(var context: Context) :
    RecyclerView.Adapter<DetailBookingAdapter.ItemHolder>() {

    private var bookingList = ArrayList<BookingFood>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var detailBookingHandler: DetailBookingHandler? = null

    fun setDataSource(bookingList: ArrayList<BookingFood>) {
        this.bookingList = bookingList
        notifyDataSetChanged()
    }

    fun setDetailBookingHandler(detailBookingHandler: DetailBookingHandler) {
        this.detailBookingHandler = detailBookingHandler
    }

    inner class ItemHolder(val binding: ItemBookingDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemBookingDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val booking = bookingList[position]
            binding.imgFoodBooking.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    bookingList[position].avatar?.medium
                )
            )
            {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
                size(300, 300)
            }
            binding.tvNameFood.text = bookingList[position].name
            binding.tvPriceFood.text = String.format(
                "%s%s",
                Currency.formatCurrencyDecimal(bookingList[position].price!!.toFloat()),
                context.resources.getString(
                    R.string.denominations
                )
            )
            binding.tvTotalAmount.text = String.format(
                "%s %s%s",
                context.resources.getString(R.string.equal),
                Currency.formatCurrencyDecimal(bookingList[position].total_amount!!.toFloat()),
                context.resources.getString(
                    R.string.denominations
                )
            )
            binding.tvQuantity.text = String.format(
                "%s%s",
                context.resources.getString(R.string.quality_item_cart),
                bookingList[position].quantity.toString()
            )
            binding.tvStatusGift.text = if (booking.is_confirm == 1) {
                context.getString(R.string.received_gifts)
            } else {
                context.getString(R.string.received_not_gifts)
            }
            if (booking.is_gift == 1) {
                binding.imgGiftBooking.visibility = View.VISIBLE
                binding.tvStatusGift.visibility = View.VISIBLE
            } else {
                binding.imgGiftBooking.visibility = View.GONE
                binding.tvStatusGift.visibility = View.GONE
            }
            binding.imgGiftBooking.setOnClickListener {
                detailBookingHandler!!.onGift(booking)
            }
        }

    }


}