package vn.techres.line.adapter.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.data.model.avert.Advert
import vn.techres.line.databinding.ItemContrainerBannerBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.SliderAdvertHandler

class SliderAdvertAdapter(var context: Context) :
    RecyclerView.Adapter<SliderAdvertAdapter.ItemHolder>() {

    private var sliderAdvertList = ArrayList<Advert>()
    private var sliderAdvertHandler: SliderAdvertHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setSliderBannerHandler(sliderAdvertHandler: SliderAdvertHandler) {
        this.sliderAdvertHandler = sliderAdvertHandler
    }

    fun setDataSource(sliderBannerList: ArrayList<Advert>) {
        this.sliderAdvertList = sliderBannerList
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemContrainerBannerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemContrainerBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return sliderAdvertList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.kbvBanner.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    sliderAdvertList[position].media_url?.original
                )
            ) {
                crossfade(true)
                scale(Scale.FIT)
                size(500, 500)
            }
            itemView.setOnClickListener {
                sliderAdvertHandler!!.onClickBanner(position)
            }
        }

    }


}


