package vn.techres.line.adapter.account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.data.model.mediaprofile.MediaProfile
import vn.techres.line.databinding.ItemProfileVideoBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickMediaProfile

class ProfileVideoAdapter(var context: Context) :
    RecyclerView.Adapter<ProfileVideoAdapter.ItemHolder>() {

    private var mediaProfile = ArrayList<MediaProfile>()
    private var clickMediaProfile: ClickMediaProfile? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(mediaProfile: ArrayList<MediaProfile>) {
        this.mediaProfile = mediaProfile
        notifyDataSetChanged()
    }

    fun setClickMediaProfile(clickMediaProfile: ClickMediaProfile) {
        this.clickMediaProfile = clickMediaProfile
    }

    inner class ItemHolder(val binding: ItemProfileVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemProfileVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return mediaProfile.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            Glide.with(context)
                .load(
                    String.format("%s%s", nodeJs.api_ads, mediaProfile[position].url.original)
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgMedia)

            binding.imgMedia.setOnClickListener {
                clickMediaProfile!!.onClickMedia(mediaProfile, position)
            }

            binding.imgMedia.setOnLongClickListener {
                mediaProfile[position].id?.let { it1 ->
                    clickMediaProfile!!.onLongClickMedia(
                        it1,
                        position
                    )
                }
                true
            }
        }


    }
}
