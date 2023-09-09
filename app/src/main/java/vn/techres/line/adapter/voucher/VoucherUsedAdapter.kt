package vn.techres.line.adapter.voucher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_voucher.view.*
import vn.techres.line.R
import vn.techres.line.data.model.voucher.Voucher
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.VoucherHandler

class VoucherUsedAdapter (var context: Context) : RecyclerView.Adapter<VoucherUsedAdapter.ViewHolder>() {

    private var data = ArrayList<Voucher>()
    private var voucherHandler : VoucherHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setVoucherHandler(voucherHandler : VoucherHandler){
        this.voucherHandler = voucherHandler
    }

    fun setDataSource(data : ArrayList<Voucher>){
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(String.format("%s%s", nodeJs.api_ads, data[position].restaurant_promotion.banner_image_url?:""))
            .into(holder.imgVoucher)

        holder.itemView.setOnClickListener {
            voucherHandler!!.onClickVoucher(data[position])
        }
        
        if (data[position].is_used == 1){
            holder.vVoucher.visibility = View.VISIBLE    
            holder.imgUsed.visibility = View.VISIBLE
        }else{
            holder.vVoucher.visibility = View.GONE
            holder.imgUsed.visibility = View.GONE
        }
        
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var imgVoucher = view.imgVoucher
        var vVoucher = view.vVoucher
        var imgUsed = view.imgUsed
    }

}

