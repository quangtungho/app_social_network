package vn.techres.line.adapter.newsfeed

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_media_five.view.*
import kotlinx.android.synthetic.main.item_media_four.view.*
import kotlinx.android.synthetic.main.item_media_one.view.*
import kotlinx.android.synthetic.main.item_media_three.view.*
import kotlinx.android.synthetic.main.item_media_two.view.*
import kotlinx.android.synthetic.main.item_post_draft.view.*
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.draft.DraftHandler

/**
 * @Author: Nguyễn Mai Nghiêm
 * @Date: 2/3/2022
 */
class DraftsPostAdapter(var context: Context) :
    RecyclerView.Adapter<DraftsPostAdapter.ViewHolder>() {

    private var draftHandler: DraftHandler? = null
    private var dataSource = ArrayList<PostReview>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setClickGift(draftHandler: DraftHandler) {
        this.draftHandler = draftHandler
    }

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(context)
                .inflate(R.layout.item_post_draft, parent, false)
        return ViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataSource[position]

        holder.imgAvatarDraft.load(
            String.format(
                "%s%s",
                nodeJs.api_ads,
                data.customer.avatar.original
            )
        )
        {
            crossfade(true)
            scale(Scale.FILL)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            size(500, 500)
        }
        holder.tvNameDraft.text = data.customer.full_name
        holder.tvTimeDraft.text = data.created_at

        //Post detail
        //show see more
        var checkContentSeeMore = true

        if (data.title!!.trim() == "") {
            holder.tvTitle.text = context.resources.getString(R.string.not_yet_title)
        } else {
            holder.tvTitle.text = data.title
        }
        if (data.content!!.trim() == "") {
            holder.tvContent.text = context.resources.getString(R.string.not_yet_content)
        } else {
            if (data.content?.trim()!!.length >= 199) {
                val spannableContent = SpannableString(
                    String.format(
                        "%s%s",
                        data.content!!.trim().substring(0, 199),
                        "...Xem thêm"
                    )
                )
                spannableContent.setSpan(
                    ForegroundColorSpan(Color.BLUE),
                    199, // start
                    210, // end
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                holder.tvContent.text = spannableContent
                checkContentSeeMore = true
            } else {
                holder.tvContent.text = data.content!!.trim()
            }
            //click see more content
            holder.tvContent.setOnClickListener {
                if (data.content?.trim()!!.length >= 199) {
                    if (checkContentSeeMore) {
                        val spannableContent = SpannableString(
                            String.format(
                                "%s %s",
                                data.content!!.trim(),
                                "Thu gọn"
                            )
                        )
                        spannableContent.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            data.content!!.trim().length, // start
                            data.content!!.trim().length + 8, // end
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        holder.tvContent.text = spannableContent
                        checkContentSeeMore = false
                    } else {
                        val spannableContent = SpannableString(
                            String.format(
                                "%s%s",
                                data.content!!.trim().substring(0, 199),
                                "...Xem thêm"
                            )
                        )
                        spannableContent.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            199, // start
                            210, // end
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        holder.tvContent.text = spannableContent
                        checkContentSeeMore = true
                    }
                }

            }
        }


//        if(data.media_contents != null){
//            holder.imgDraft.load(String.format("%s%s", nodeJs.api_ads, data.media_contents))
//            {
//                crossfade(true)
//                scale(Scale.FILL)
//                placeholder(R.drawable.logo_alo_line_placeholder)
//                error(R.drawable.logo_alo_line_placeholder)
//                size(500, 500)
//            }
//        }

        holder.btnDraftPost.setOnClickListener {
            draftHandler!!.onPostDraft(data, position)
        }

        holder.btnDraftEdit.setOnClickListener {
            draftHandler!!.onEditDraft(data, position, 0)
        }

        holder.btnDeleteDraft.setOnClickListener {
            draftHandler!!.onDeleteDraft(data, position)
        }


        when (data.media_contents.size) {
            0 -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE
            }
            1 -> {
                holder.lnMediaOne.visibility = View.VISIBLE
                holder.lnMediaOne.imgDeleteOneOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaOne.lnVideo.visibility = View.VISIBLE
                    holder.lnMediaOne.imgOneTypeOne.visibility = View.GONE
                    holder.lnMediaOne.thumbnailVideo.visibility = View.VISIBLE
                    holder.lnMediaOne.imgPlay.visibility = View.VISIBLE
                    holder.lnMediaOne.playerView.visibility = View.GONE
                    holder.lnMediaOne.btnVolume.visibility = View.GONE
                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                data.media_contents[0].original
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(holder.lnMediaOne.thumbnailVideo)
                } else {
                    holder.lnMediaOne.lnVideo.visibility = View.GONE
                    holder.lnMediaOne.imgOneTypeOne.visibility = View.VISIBLE
                    holder.lnMediaOne.thumbnailVideo.visibility = View.GONE
                    holder.lnMediaOne.imgPlay.visibility = View.GONE
                    holder.lnMediaOne.playerView.visibility = View.GONE
                    holder.lnMediaOne.btnVolume.visibility = View.GONE
                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                data.media_contents[0].original
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(holder.lnMediaOne.imgOneTypeOne)
                }

            }
            2 -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.VISIBLE
                holder.lnMediaTwo.imgDeleteOneTwo.visibility = View.GONE
                holder.lnMediaTwo.imgDeleteTwoTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaTwo.imgOneTypeTwoPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaTwo.imgOneTypeTwoPlay.visibility = View.GONE

                if (data.media_contents[1].type == 0) {
                    holder.lnMediaTwo.imgTwoTypeTwoPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaTwo.imgTwoTypeTwoPlay.visibility = View.GONE

                getMediaGlide(
                    holder.lnMediaTwo.imgOneTypeTwo,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[0].original)
                )
                getMediaGlide(
                    holder.lnMediaTwo.imgTwoTypeTwo,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[1].original)
                )
            }
            3 -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.VISIBLE
                holder.lnMediaThree.imgDeleteOneThree.visibility = View.GONE
                holder.lnMediaThree.imgDeleteTwoThree.visibility = View.GONE
                holder.lnMediaThree.imgDeleteThreeThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaThree.imgOneTypeThreePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaThree.imgOneTypeThreePlay.visibility = View.GONE

                if (data.media_contents[1].type == 0) {
                    holder.lnMediaThree.imgTwoTypeThreePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaThree.imgTwoTypeThreePlay.visibility = View.GONE

                if (data.media_contents[2].type == 0) {
                    holder.lnMediaThree.imgThreeTypeThreePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaThree.imgThreeTypeThreePlay.visibility = View.GONE

                getMediaGlide(
                    holder.lnMediaThree.imgOneTypeThree,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[0].original)
                )
                getMediaGlide(
                    holder.lnMediaThree.imgTwoTypeThree,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[1].original)
                )
                getMediaGlide(
                    holder.lnMediaThree.imgThreeTypeThree,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[2].original)
                )
            }
            4 -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.VISIBLE
                holder.lnMediaFour.imgDeleteOneFour.visibility = View.GONE
                holder.lnMediaFour.imgDeleteTwoFour.visibility = View.GONE
                holder.lnMediaFour.imgDeleteThreeFour.visibility = View.GONE
                holder.lnMediaFour.imgDeleteFourFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaFour.imgOneTypeFourPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFour.imgOneTypeFourPlay.visibility = View.GONE

                if (data.media_contents[1].type == 0) {
                    holder.lnMediaFour.imgTwoTypeFourPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFour.imgTwoTypeFourPlay.visibility = View.GONE

                if (data.media_contents[2].type == 0) {
                    holder.lnMediaFour.imgThreeTypeFourPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFour.imgThreeTypeFourPlay.visibility = View.GONE

                if (data.media_contents[3].type == 0) {
                    holder.lnMediaFour.imgFourTypeFourPlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFour.imgFourTypeFourPlay.visibility = View.GONE

                getMediaGlide(
                    holder.lnMediaFour.imgOneTypeFour,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[0].original)
                )
                getMediaGlide(
                    holder.lnMediaFour.imgTwoTypeFour,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[1].original)
                )
                getMediaGlide(
                    holder.lnMediaFour.imgThreeTypeFour,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[2].original)
                )
                getMediaGlide(
                    holder.lnMediaFour.imgFourTypeFour,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[3].original)
                )
            }
            5 -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.tvMoreImage.visibility = View.GONE
                holder.lnMediaFive.visibility = View.VISIBLE
                holder.lnMediaFive.imgDeleteOneFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteTwoFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteThreeFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteFourFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteFiveFive.visibility = View.GONE

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaFive.imgOneTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgOneTypeFivePlay.visibility = View.GONE

                if (data.media_contents[1].type == 0) {
                    holder.lnMediaFive.imgTwoTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgTwoTypeFivePlay.visibility = View.GONE

                if (data.media_contents[2].type == 0) {
                    holder.lnMediaFive.imgThreeTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgThreeTypeFivePlay.visibility = View.GONE

                if (data.media_contents[3].type == 0) {
                    holder.lnMediaFive.imgFourTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgFourTypeFivePlay.visibility = View.GONE

                if (data.media_contents[4].type == 0) {
                    holder.lnMediaFive.imgFiveTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgFiveTypeFivePlay.visibility = View.GONE

                getMediaGlide(
                    holder.lnMediaFive.imgOneTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[0].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgTwoTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[1].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgThreeTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[2].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgFourTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[3].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgFiveTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[4].original)
                )
            }
            else -> {
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.tvMoreImage.visibility = View.VISIBLE
                holder.lnMediaFive.visibility = View.VISIBLE
                holder.lnMediaFive.imgDeleteOneFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteTwoFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteThreeFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteFourFive.visibility = View.GONE
                holder.lnMediaFive.imgDeleteFiveFive.visibility = View.GONE
                holder.lnMediaFive.tvMoreImage.text =
                    String.format("%s %s", "+", data.media_contents.size - 5)

                if (data.media_contents[0].type == 0) {
                    holder.lnMediaFive.imgOneTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgOneTypeFivePlay.visibility = View.GONE

                if (data.media_contents[1].type == 0) {
                    holder.lnMediaFive.imgTwoTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgTwoTypeFivePlay.visibility = View.GONE

                if (data.media_contents[2].type == 0) {
                    holder.lnMediaFive.imgThreeTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgThreeTypeFivePlay.visibility = View.GONE

                if (data.media_contents[3].type == 0) {
                    holder.lnMediaFive.imgFourTypeFivePlay.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgFourTypeFivePlay.visibility = View.GONE

                if (data.media_contents[4].type == 0) {
                    holder.lnMediaFive.imgFiveTypeFive.visibility = View.VISIBLE
                } else
                    holder.lnMediaFive.imgFiveTypeFive.visibility = View.GONE

                getMediaGlide(
                    holder.lnMediaFive.imgOneTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[0].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgTwoTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[1].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgThreeTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[2].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgFourTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[3].original)
                )
                getMediaGlide(
                    holder.lnMediaFive.imgFiveTypeFive,
                    String.format("%s%s", nodeJs.api_ads, data.media_contents[4].original)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    /**
     * Use glide show media
     */
    private fun getMediaGlide(imageView: ImageView, string: String?) {
        Glide.with(context)
            .load(string)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .error(R.drawable.ic_image_placeholder)
            .into(imageView)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatarDraft = itemView.imgAvatarDraft!!
        val tvNameDraft = itemView.tvNameDraft!!
        val tvTimeDraft = itemView.tvTimeDraft!!
        val tvTitle = itemView.tvTitle!!
        val tvContent = itemView.tvContent!!
        val btnDraftPost = itemView.btnPostDraft
        val btnDraftEdit = itemView.btnEditDraft
        val btnDeleteDraft = itemView.btnDeleteDraft
        val lnMediaOne = itemView.lnMediaOne
        val lnMediaTwo = itemView.lnMediaTwo
        val lnMediaThree = itemView.lnMediaThree
        val lnMediaFour = itemView.lnMediaFour
        val lnMediaFive = itemView.lnMediaFive

    }
}