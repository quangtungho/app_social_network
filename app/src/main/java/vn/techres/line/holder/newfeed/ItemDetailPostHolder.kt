package vn.techres.line.holder.newfeed

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.PlayerView
import com.like.LikeButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import vn.techres.line.R

class ItemDetailPostHolder(view : View) : RecyclerView.ViewHolder(view) {
    //Header
    val imgMore: ImageView = view.findViewById(R.id.imgMore)
    val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    val tvName: TextView = view.findViewById(R.id.tvName)
    val tvTimeCreate: TextView = view.findViewById(R.id.tvTimeCreate)
    val viewLineHeader: View = view.findViewById(R.id.viewLineHeader)
    val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    val tvContent: TextView = view.findViewById(R.id.tvContent)
    val lnNamePost: LinearLayout = view.findViewById(R.id.lnNamePost)

    //Link
    var rlLinkPreview: RelativeLayout = view.findViewById(R.id.rlLinkPreview)
    var imgPreview: ImageView = view.findViewById(R.id.imgPreview)
    var txtSiteLink: TextView = view.findViewById(R.id.txtSiteLink)
    var txtTitleLink: TextView = view.findViewById(R.id.txtTitleLink)
    var txtDescLink: TextView = view.findViewById(R.id.txtDescLink)

    //YouTube
    var youtubePlayerView: YouTubePlayerView = view.findViewById(R.id.youtubePlayerView)


    //Media
    //Size =1
    val lnMediaOne: RelativeLayout = view.findViewById(R.id.lnMediaOne)
    val imgOneTypeOne: ImageView = view.findViewById(R.id.imgOneTypeOne)
    val lnVideo: LinearLayout = view.findViewById(R.id.lnVideo)
    val playerView: PlayerView = view.findViewById(R.id.playerView)
    val thumbnailVideo: ImageView = view.findViewById(R.id.thumbnailVideo)
    val btnVolume: ImageButton = view.findViewById(R.id.btnVolume)

    //Size = 2
    val lnMediaTwo: LinearLayout = view.findViewById(R.id.lnMediaTwo)
    val imgOneTypeTwo: ImageView = view.findViewById(R.id.imgOneTypeTwo)
    val imgOneTypeTwoPlay: ImageView = view.findViewById(R.id.imgOneTypeTwoPlay)
    val imgTwoTypeTwo: ImageView = view.findViewById(R.id.imgTwoTypeTwo)
    val imgTwoTypeTwoPlay: ImageView = view.findViewById(R.id.imgTwoTypeTwoPlay)

    //Size = 3
    val lnMediaThree: LinearLayout = view.findViewById(R.id.lnMediaThree)
    val imgOneTypeThree: ImageView = view.findViewById(R.id.imgOneTypeThree)
    val imgOneTypeThreePlay: ImageView = view.findViewById(R.id.imgOneTypeThreePlay)
    val imgTwoTypeThree: ImageView = view.findViewById(R.id.imgTwoTypeThree)
    val imgTwoTypeThreePlay: ImageView = view.findViewById(R.id.imgTwoTypeThreePlay)
    val imgThreeTypeThree: ImageView = view.findViewById(R.id.imgThreeTypeThree)
    val imgThreeTypeThreePlay: ImageView = view.findViewById(R.id.imgThreeTypeThreePlay)

    //Size = 4
    val lnMediaFour: LinearLayout = view.findViewById(R.id.lnMediaFour)
    val imgOneTypeFour: ImageView = view.findViewById(R.id.imgOneTypeFour)
    val imgOneTypeFourPlay: ImageView = view.findViewById(R.id.imgOneTypeFourPlay)
    val imgTwoTypeFour: ImageView = view.findViewById(R.id.imgTwoTypeFour)
    val imgTwoTypeFourPlay: ImageView = view.findViewById(R.id.imgTwoTypeFourPlay)
    val imgThreeTypeFour: ImageView = view.findViewById(R.id.imgThreeTypeFour)
    val imgThreeTypeFourPlay: ImageView = view.findViewById(R.id.imgThreeTypeFourPlay)
    val imgFourTypeFour: ImageView = view.findViewById(R.id.imgFourTypeFour)
    val imgFourTypeFourPlay: ImageView = view.findViewById(R.id.imgFourTypeFourPlay)

    //Size = 5
    val lnMediaFive: LinearLayout = view.findViewById(R.id.lnMediaFive)
    val imgOneTypeFive: ImageView = view.findViewById(R.id.imgOneTypeFive)
    val imgOneTypeFivePlay: ImageView = view.findViewById(R.id.imgOneTypeFivePlay)
    val imgTwoTypeFive: ImageView = view.findViewById(R.id.imgTwoTypeFive)
    val imgTwoTypeFivePlay: ImageView = view.findViewById(R.id.imgTwoTypeFivePlay)
    val imgThreeTypeFive: ImageView = view.findViewById(R.id.imgThreeTypeFive)
    val imgThreeTypeFivePlay: ImageView = view.findViewById(R.id.imgThreeTypeFivePlay)
    val imgFourTypeFive: ImageView = view.findViewById(R.id.imgFourTypeFive)
    val imgFourTypeFivePlay: ImageView = view.findViewById(R.id.imgFourTypeFivePlay)
    val imgFiveTypeFive: ImageView = view.findViewById(R.id.imgFiveTypeFive)
    val imgFiveTypeFivePlay: ImageView = view.findViewById(R.id.imgFiveTypeFivePlay)
    val tvMoreImage: TextView = view.findViewById(R.id.tvMoreImage)

    //Branch information
    val tvRatingReview: TextView = view.findViewById(R.id.tvRatingReview)
    val imgBranchLogo: ImageView = view.findViewById(R.id.imgBranchLogo)
    val txtBranchName: TextView = view.findViewById(R.id.txtBranchName)
    val txtBranchAddress: TextView = view.findViewById(R.id.txtBranchAddress)
    val reviewRating: RatingBar = view.findViewById(R.id.reviewRating)
    val txtBranchVotes: TextView = view.findViewById(R.id.txtBranchVotes)
    val lbSaveBranch: LikeButton = view.findViewById(R.id.lbSaveBranch)
    val lnBranch: LinearLayout = view.findViewById(R.id.lnBranch)
    val lnReviewRating: LinearLayout = view.findViewById(R.id.lnReviewRating)

    //Reaction
    val lnReactionOne: LinearLayout = view.findViewById(R.id.lnReactionOne)
    val lnReactionTwo: LinearLayout = view.findViewById(R.id.lnReactionTwo)
    val lnReactionThree: LinearLayout = view.findViewById(R.id.lnReactionThree)
    val lnReactionFour: LinearLayout = view.findViewById(R.id.lnReactionFour)
    val lnReactionFive: LinearLayout = view.findViewById(R.id.lnReactionFive)
    val lnReactionSix: LinearLayout = view.findViewById(R.id.lnReactionSix)
    val tvOneCount: TextView = view.findViewById(R.id.tvOneCount)
    val tvTwoCount: TextView = view.findViewById(R.id.tvTwoCount)
    val tvThreeCount: TextView = view.findViewById(R.id.tvThreeCount)
    val tvFourCount: TextView = view.findViewById(R.id.tvFourCount)
    val tvFiveCount: TextView = view.findViewById(R.id.tvFiveCount)
    val tvSixCount: TextView = view.findViewById(R.id.tvSixCount)
    val tvReactionOne: TextView = view.findViewById(R.id.tvReactionOne)
    val tvReactionTwo: TextView = view.findViewById(R.id.tvReactionTwo)
    val tvReactionThree: TextView = view.findViewById(R.id.tvReactionThree)
    val tvReactionFour: TextView = view.findViewById(R.id.tvReactionFour)
    val tvReactionFive: TextView = view.findViewById(R.id.tvReactionFive)
    val tvReactionSix: TextView = view.findViewById(R.id.tvReactionSix)

    //Value
    val lnValue: LinearLayout = view.findViewById(R.id.lnValue)
    val tvPointValue: TextView = view.findViewById(R.id.tvPointValue)
    val tvCommentCount: TextView = view.findViewById(R.id.tvCommentCount)
    val tvLevelValueOne: TextView = view.findViewById(R.id.tvLevelValueOne)
    val tvLevelValueTwo: TextView = view.findViewById(R.id.tvLevelValueTwo)
    val tvLevelValueThree: TextView = view.findViewById(R.id.tvLevelValueThree)
    val tvLevelValueFour: TextView = view.findViewById(R.id.tvLevelValueFour)

    val lnComment: LinearLayout = view.findViewById(R.id.lnComment)
    val lnShare: LinearLayout = view.findViewById(R.id.lnShare)
}