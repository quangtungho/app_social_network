package vn.techres.line.holder.point

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.databinding.ItemDenominationCardBinding
import vn.techres.line.interfaces.point.DenominationHandler
import vn.techres.line.data.model.card.DenominationCard

class DenominationHolder(private val binding : ItemDenominationCardBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(context : Context ,dataSource : ArrayList<DenominationCard>, position : Int, denominationHandler: DenominationHandler?){
        val denominationCard = dataSource[position]
        binding.tvNameDenomination.text = denominationCard.name
        if(denominationCard.is_check){
            binding.clImageChecked.visibility = View.VISIBLE
            binding.clDenomination.background = ContextCompat.getDrawable(context, R.drawable.stroke_corners_orange_5dp)
        }else{
            binding.clImageChecked.visibility = View.GONE
            binding.clDenomination.background = ContextCompat.getDrawable(context, R.drawable.stroke_corners_gray_5dp)
        }
        binding.root.setOnClickListener {
            denominationHandler?.onChooseDenomination(position)
        }
    }
}