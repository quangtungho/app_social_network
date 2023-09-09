package vn.techres.line.adapter.voucher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import kotlinx.android.synthetic.main.item_contrainer_voucher.view.*
import vn.techres.line.R
import vn.techres.line.data.model.voucher.Voucher
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.VoucherHandler

class VoucherAdapter (var context: Context) : RecyclerView.Adapter<VoucherAdapter.ViewHolder>() {

    private var sliderVoucherList = ArrayList<Voucher>()
    private var voucherHandler : VoucherHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setVoucherHandler(voucherHandler : VoucherHandler){
        this.voucherHandler = voucherHandler
    }

    fun setDataSource(sliderVoucherList : ArrayList<Voucher>){
        this.sliderVoucherList = sliderVoucherList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_contrainer_voucher, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return sliderVoucherList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgBanner.load(String.format(
            "%s%s",
            nodeJs.api_ads,
            sliderVoucherList[position].restaurant_promotion.banner_image_url)
        ){
            crossfade(true)
            scale(Scale.FIT)
        }

        holder.txtName.text = sliderVoucherList[position].restaurant_promotion.name
//        holder.txtDescription.text = "Sale tụt quần, ăn thả ga"

        if (sliderVoucherList[position].restaurant_voucher_id == 0){
            holder.txtGetNow.visibility = View.VISIBLE
            holder.txtUse.visibility = View.GONE
        }else{
            holder.txtGetNow.visibility = View.GONE
            holder.txtUse.visibility = View.VISIBLE
        }

        holder.txtGetNow.setOnClickListener {
            voucherHandler!!.onClickTakeVoucher(sliderVoucherList[position])
            holder.txtGetNow.visibility = View.GONE
            holder.txtUse.visibility = View.VISIBLE
        }

        holder.txtUse.setOnClickListener {
            voucherHandler!!.onClickVoucher(sliderVoucherList[position])
        }

        holder.itemView.setOnClickListener {
            voucherHandler!!.onClickVoucher(sliderVoucherList[position])
        }
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var imgBanner = view.kbvBanner
        var txtName = view.txtName
        var txtDescription = view.txtDescription
        var txtGetNow = view.txtGetNow
        var txtUse = view.txtUse
    }

}

