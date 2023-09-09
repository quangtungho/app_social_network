package vn.techres.line.adapter.booking

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.data.model.booking.Booking
import vn.techres.line.databinding.ItemInformationBookingBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ProcessBookingHandler

class ProcessBookingAdapter(val context: Context) :
    RecyclerView.Adapter<ProcessBookingAdapter.ItemHolder>() {
    private var processBookingHandler: ProcessBookingHandler? = null
    private var dataSource = ArrayList<Booking>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDetailBooking(processBookingHandler: ProcessBookingHandler) {
        this.processBookingHandler = processBookingHandler
    }

    fun setDataSource(dataSource: ArrayList<Booking>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemInformationBookingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemInformationBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.imgLogoBranch.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    dataSource[position].branch_picture
                )
            )
            {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(300, 300)
                transformations(RoundedCornersTransformation(10F))
            }

            binding.txtNameBranch.text = dataSource[position].branch_name
            binding.txtAddressBranch.text = dataSource[position].branch_address
            binding.txtStatus.text = dataSource[position].booking_status_text

            when (dataSource[position].booking_status) {
                1 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_blue_4dp)
                }
                2 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_green_4dp)
//                binding.txtStatus.text = context.getString(R.string.confirmed_booking)
                }
                3 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_orange_4dp)
                }
                4 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_orange_4dp)
                }
                5 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_red_4dp)
                }
                6 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_purple_4dp)
                }
                7 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_green_4dp)
//                holder.txtStatus.text = context.getString(R.string.confirmed_booking)
                }
                8 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_red_4dp)
//                holder.txtStatus.text = context.getString(R.string.expire)
                }
                9 -> {
                    binding.txtStatus.setBackgroundResource(R.drawable.corners_blue_4dp)
                }
            }

            binding.txtDateTime.text = dataSource[position].booking_time
            itemView.setOnClickListener {
                processBookingHandler!!.onDetailBooking(position)
            }
        }

    }

}