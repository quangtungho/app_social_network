package vn.techres.line.adapter.account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.mediaprofile.MediaProfile
import vn.techres.line.databinding.ItemProfileAlbumBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.CreateAlbumHandler

class ProfileAlbumAdapter(var context: Context) :
    RecyclerView.Adapter<ProfileAlbumAdapter.ItemHolder>() {
    private var mediaProfile = ArrayList<MediaProfile>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var createAlbumHandler: CreateAlbumHandler? = null

    fun setDataSource(mediaProfile: ArrayList<MediaProfile>) {
        this.mediaProfile = mediaProfile
        notifyDataSetChanged()
    }

    fun setClickAlbum(createAlbumHandler: CreateAlbumHandler) {
        this.createAlbumHandler = createAlbumHandler
    }

    inner class ItemHolder(val binding: ItemProfileAlbumBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemProfileAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return mediaProfile.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            if (position == 0) {
                binding.lnCreateAlbum.visibility = View.VISIBLE
                binding.lnAlbum.visibility = View.GONE

                binding.lnCreateAlbum.setOnClickListener {
                    createAlbumHandler!!.onClick(position)
                }


            } else {
                binding.lnCreateAlbum.visibility = View.GONE
                binding.lnAlbum.visibility = View.VISIBLE
            }

            try {
                getAvatar(
                    binding.imgAlbum,
                    getLinkDataNode(mediaProfile[position].avatar.toString())
                )
            } catch (e: Exception) {
            }
            binding.tvAlbumName.text = mediaProfile[position].name.toString()
            binding.tvAlbumNum.text = String.format(
                "%s %s",
                mediaProfile[position].total,
                context.resources.getString(R.string.image_image_album)
            )

            binding.lnAlbum.setOnClickListener {
                mediaProfile[position].id?.let { it1 ->
                    createAlbumHandler!!.onOneAlbum(
                        position,
                        it1
                    )
                }
            }
        }


    }

    private fun getAvatar(imageView: ImageView, string: String?) {
        imageView.load(string) {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.color.gray_dark)
            error(R.color.gray_dark)
            size(500, 500)
        }
    }

    private fun getLinkDataNode(url: String): String {
        return String.format(
            "%s%s",
            nodeJs.api_ads,
            url
        )
    }

}
