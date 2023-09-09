package vn.techres.line.adapter.point

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemDenominationCardBinding
import vn.techres.line.holder.point.DenominationHolder
import vn.techres.line.interfaces.point.DenominationHandler
import vn.techres.line.data.model.card.DenominationCard

class DenominationAdapter(var context: Context) : RecyclerView.Adapter<DenominationHolder>() {
    private var dataSource = ArrayList<DenominationCard>()
    private var denominationHandler : DenominationHandler? = null
    fun setDataSource(dataSource: ArrayList<DenominationCard>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    fun setDenominationHandler(denominationHandler : DenominationHandler){
        this.denominationHandler = denominationHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenominationHolder {
        return DenominationHolder(
            ItemDenominationCardBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DenominationHolder, position: Int) {
        holder.bind(context, dataSource, position, denominationHandler)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }
}