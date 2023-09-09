package vn.techres.line.adapter.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import kotlinx.android.synthetic.main.item_order_review.view.*
import vn.techres.line.R
import vn.techres.line.data.model.OrderReview
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.OnLickItem

class OrderReviewAdapter(var context: Context) :
    RecyclerView.Adapter<OrderReviewAdapter.RecyclerViewHolder>() {
    private var dataSource = ArrayList<OrderReview>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var onLickItem: OnLickItem? = null

    fun setDataSource(dataSource: ArrayList<OrderReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setOnlickItem(onLickItem: OnLickItem) {
        this.onLickItem = onLickItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order_review, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.txtRestaurantName.text = dataSource[position].restaurant_name
        holder.txtBranchName.text = dataSource[position].branch_name
        holder.txtDateTimeOrder.text = dataSource[position].payment_date
//        Glide.with(context)
//            .load(String.format("%s%s", nodeJs.api_ads, dataSource[position].branch_avatar))
//            .placeholder(R.drawable.logo_aloline_plancehoder)
//            .into(holder.imgLogo)
        holder.imgLogo.load(
            String.format(
                "%s%s",
                nodeJs.api_ads,
                dataSource[position].branch_avatar
            )
        ) {
            crossfade(true)
            scale(Scale.FILL)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            size(800, 800)
        }
        holder.btnReview.setOnClickListener {
            onLickItem!!.lickPosition(position)
        }
    }

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgLogo = itemView.imgLogo!!
        var txtRestaurantName = itemView.txtRestaurantName!!
        var txtBranchName = itemView.txtBranchName!!
        var txtDateTimeOrder = itemView.txtDateTimeOrder!!
        var btnReview = itemView.btnReview!!
    }


}