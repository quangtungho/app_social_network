package vn.techres.line.adapter.branch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.like.LikeButton
import com.like.OnLikeListener
import kohii.v1.core.Manager
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import org.jetbrains.annotations.NotNull
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.databinding.*
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.holder.newfeed.ItemPostHolder
import vn.techres.line.interfaces.branch.BranchDetailHandler
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler

class BranchDetailAdapter(
    var context: Context, var kohii: Kohii, var manager: Manager, val lifecycle: Lifecycle,
    val width: Int, val height: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val cacheManager: CacheManager = CacheManager.getInstance()

    private var dataSource = ArrayList<PostReview>()
    private var dataBranchDetail: BranchDetail? = null
    private var listSaveBranchID = ArrayList<Int>()

    private var branchDetailHandler: BranchDetailHandler? = null
    private var newsFeedHandler: NewsFeedHandler? = null

    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    var playback: Playback? = null
    var volumeInfo: VolumeInfo? = null

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setDataBranchDetail(dataBranchDetail: BranchDetail) {
        this.dataBranchDetail = dataBranchDetail
        notifyDataSetChanged()
    }

    fun setBranchDetailHandler(branchDetailHandler: BranchDetailHandler) {
        this.branchDetailHandler = branchDetailHandler
    }


    fun setNewsFeedHandler(newsFeedHandler: NewsFeedHandler) {
        this.newsFeedHandler = newsFeedHandler
    }

    fun stopVideo() {
        kohii.lockManager(manager)
    }

    fun startVideo() {
        kohii.unlockManager(manager)
    }

    inner class ItemHolderOne(val binding: ItemBranchDetailOneBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ItemHolderTwo(val binding: ItemBranchDetailTwoBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ItemHolderThree(val binding: ItemBranchDetailThreeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            branchDetailOne -> {
                val binding = ItemBranchDetailOneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemHolderOne(binding)
            }
            branchDetailTwo -> {
                val binding = ItemBranchDetailTwoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemHolderTwo(binding)
            }
            branchDetailThree -> {
                val binding = ItemBranchDetailThreeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemHolderThree(binding)
            }
            else -> {
                ItemPostHolder(
                    ItemPostBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), context, kohii, manager, lifecycle, width, height
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                branchDetailOne
            }
            1 -> {
                branchDetailTwo
            }
            2 -> {
                branchDetailThree
            }
            else -> {
                branchDetailListReview
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(@NotNull holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            branchDetailOne -> {
                onBindBranchDetailOne(holder as ItemHolderOne)
            }
            branchDetailTwo -> {
                onBindBranchDetailTwo(holder as ItemHolderTwo)
            }
            branchDetailThree -> {
                onBindBranchDetailThree(holder as ItemHolderThree)
            }
            else -> {
                (holder as ItemPostHolder).bin(
                    dataSource[position],
                    position,
                    user,
                    configNodeJs,
                    newsFeedHandler!!
                )
            }
        }
    }

    private fun onBindBranchDetailOne(holder: ItemHolderOne) {
        //Slider banner header
        if (dataBranchDetail != null) {
            holder.binding.lnSliderBranch.visibility = View.VISIBLE
            val listMedia = ArrayList<String>()
            listMedia.add(String.format(
                "%s%s",
                configNodeJs.api_ads,
                dataBranchDetail!!.banner_image_url.original
            ))

            listMedia.let { holder.binding.imageLogoBranch.setItems(it) }
            holder.binding.imageLogoBranch.addTimerToSlide(5000)

            //            if (dataBranchDetail!!.image_urls.size == 0) {
//                holder.binding.lnSliderBranch.visibility = View.GONE
//            } else {
//                holder.binding.lnSliderBranch.visibility = View.VISIBLE
//                dataBranchDetail!!.image_urls = dataBranchDetail!!.image_urls.map {
//                    String.format(
//                        "%s%s",
//                        configNodeJs.api_ads,
//                        it
//                    )
//                } as ArrayList<String>
//                dataBranchDetail!!.image_urls.let { holder.binding.imageLogoBranch.setItems(it) }
//                holder.binding.imageLogoBranch.addTimerToSlide(5000)
//            }

            Utils.getImage(holder.binding.imgAvatarBranch, dataBranchDetail!!.image_logo_url.original, configNodeJs)

            holder.binding.tvRestaurantName.text = dataBranchDetail!!.name.toString()
            holder.binding.rbBranchRate.rating = dataBranchDetail!!.star!!.toFloat()
            holder.binding.tvTotalRate.text = String.format(
                "%s %s",
                dataBranchDetail!!.rating.rate_count.toString(),
                context.resources.getString(R.string.rating)
            )
            if (dataBranchDetail?.branch_business_types?.joinToString() == "" && dataBranchDetail?.branch_business_types == null) {
                holder.binding.tvBranchType.visibility = View.GONE
            } else {
                holder.binding.tvBranchType.visibility = View.VISIBLE
//                holder.binding.tvBranchType.text = dataBranchDetail?.branch_business_types?.joinToString()
                holder.binding.tvBranchType.text = dataBranchDetail?.menu
            }

            if (dataBranchDetail!!.serve_time.isNotEmpty()) {
                holder.binding.tvStatus.text = dataBranchDetail!!.serve_time[0].day_of_week_name
            }

            val averagePrice = dataBranchDetail!!.average_price?.toInt()
            holder.binding.tvPrice.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(averagePrice!!.toFloat()),
                context.resources.getString(R.string.average_price)
            )

            holder.binding.llRatingReview.setOnClickListener {
                branchDetailHandler!!.clickPostReview()
            }

            holder.binding.lnCheckIn.setOnClickListener {
                branchDetailHandler!!.clickCheckIn()
            }

            if (CacheManager.getInstance().get(TechresEnum.LIST_SAVE_BRANCH.toString()) != null) {
                listSaveBranchID = Gson().fromJson(
                    CacheManager.getInstance().get(TechresEnum.LIST_SAVE_BRANCH.toString()),
                    object :
                        TypeToken<ArrayList<Int>>() {}.type
                )
                holder.binding.imgSaveBranch.isLiked = listSaveBranchID.contains(dataBranchDetail!!.id)

                holder.binding.imgSaveBranch.setOnLikeListener(object : OnLikeListener {
                    override fun liked(likeButton: LikeButton) {
                        branchDetailHandler!!.clickSaveBranch(listSaveBranchID)
                    }

                    override fun unLiked(likeButton: LikeButton) {
                        branchDetailHandler!!.clickSaveBranch(listSaveBranchID)
                    }
                })
            }

        }
    }

    private fun onBindBranchDetailTwo(holder: ItemHolderTwo) {
        if (dataBranchDetail != null) {
            holder.binding.tvAddressDetailBranch.text = dataBranchDetail!!.address_full_text.toString()
            holder.binding.tvCall.setOnClickListener {
                try {

                    if (dataBranchDetail!!.phone_number != null) {
                        val dialIntent = Intent(Intent.ACTION_DIAL)
                        dialIntent.data = Uri.parse("tel:" + dataBranchDetail!!.phone_number)
                        context.startActivity(dialIntent)

                    } else {
                        Toast.makeText(
                            context,
                            "Số điện thoại hiện không khả dụng",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (ex: Exception) {
                }
            }
        }
    }

    private fun onBindBranchDetailThree(holder: ItemHolderThree) {
        if (dataBranchDetail != null) {
            if (dataBranchDetail!!.website == "") {
                holder.binding.tvWebsiteName.visibility = View.GONE
            } else {
                holder.binding.tvWebsiteName.visibility = View.VISIBLE
                holder.binding.tvWebsiteName.text = dataBranchDetail!!.website
                holder.binding.tvWebsiteName.setOnClickListener {
                    try {

                        if (dataBranchDetail!!.website != null) {
                            val dialIntent = Intent(Intent.ACTION_VIEW)
                            dialIntent.data = Uri.parse(dataBranchDetail!!.website)
                            context.startActivity(dialIntent)

                        } else {
                            Toast.makeText(
                                context,
                                "Nhà hàng sẽ cập nhật website trong thời gian sớm nhất",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (ex: Exception) {
                    }
                }
            }
            if (dataBranchDetail!!.facebook_page == "") {
                holder.binding.tvFanpageName.visibility = View.GONE
            } else {
                holder.binding.tvFanpageName.visibility = View.VISIBLE
                holder.binding.tvFanpageName.text = dataBranchDetail!!.facebook_page
                holder.binding.tvFanpageName.setOnClickListener {
                    try {

                        if (dataBranchDetail!!.facebook_page != null) {
                            val dialIntent = Intent(Intent.ACTION_VIEW)
                            dialIntent.data = Uri.parse(dataBranchDetail!!.facebook_page)
                            context.startActivity(dialIntent)

                        } else {
                            Toast.makeText(
                                context,
                                "Nhà hàng sẽ cập nhật fanpage trong thời gian sớm nhất",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (ex: Exception) {
                    }
                }
            }

            if (dataBranchDetail!!.is_have_air_conditioner == 1) {
                holder.binding.lvrBranchUtility1.visibility = View.VISIBLE
            } else {
                holder.binding.lvrBranchUtility1.visibility = View.GONE
            }

            if (dataBranchDetail!!.is_free_parking == 1) {
                holder.binding.lvrBranchUtility2.visibility = View.VISIBLE
            } else {
                holder.binding.lvrBranchUtility2.visibility = View.GONE
            }

            if (dataBranchDetail!!.is_have_wifi == 1) {
                holder.binding.lvrBranchUtility3.visibility = View.VISIBLE
            } else {
                holder.binding.lvrBranchUtility3.visibility = View.GONE
            }
            holder.binding.nameWifi.text = dataBranchDetail!!.wifi_name
            holder.binding.passWifi.text = dataBranchDetail!!.wifi_password

            holder.binding.ctDetailUtilities.setOnClickListener {
                branchDetailHandler!!.seeMore(dataBranchDetail!!)
            }
        }
    }

    companion object {
        private const val branchDetailOne = 0
        private const val branchDetailTwo = 1
        private const val branchDetailThree = 2
        private const val branchDetailListReview = 3
    }

}