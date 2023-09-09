package vn.techres.line.adapter.account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.mediaprofile.MediaProfile
import vn.techres.line.databinding.ItemProfileImageBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickMediaProfile

class ProfileImageAdapter(var context: Context) :
    RecyclerView.Adapter<ProfileImageAdapter.ViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProfileImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mediaProfile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            binding.imgMediaImage.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    mediaProfile[position].url.medium
                )
            ) {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.bg_gray_border)
                error(R.drawable.bg_gray_border)
                size(500, 500)
            }

            binding.imgMediaImage.setOnClickListener {
                clickMediaProfile!!.onClickMedia(mediaProfile, position)
            }

            binding.imgMediaImage.setOnLongClickListener {
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

    class ViewHolder(val binding: ItemProfileImageBinding) : RecyclerView.ViewHolder(binding.root)
}
