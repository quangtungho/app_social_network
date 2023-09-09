package vn.techres.line.adapter.restaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.restaurant.Prerogatives
import vn.techres.line.databinding.ItemPrerogativesCardBinding

class PrerogativesCardAdapter(var context: Context) :
    RecyclerView.Adapter<PrerogativesCardAdapter.ItemHolder>() {
    private var data = ArrayList<Prerogatives>()

    fun setDataSource(data: ArrayList<Prerogatives>) {
        this.data = data
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemPrerogativesCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemPrerogativesCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val item = data[position]
            binding.imgPrerogatives.setImageResource(R.drawable.ic_diamond)
            binding.tvNamePrerogatives.text = item.content
            binding.tvContentPrerogatives.text = item.description
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


}