package vn.techres.line.helper.keyboard

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.sticker.StickerCategory
import com.aghajari.emojiview.sticker.StickerLoader
import com.aghajari.emojiview.sticker.StickerProvider
import com.bumptech.glide.request.RequestOptions
import vn.techres.line.data.model.chat.CategorySticker
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.glide.GlideImageLoaderSticker

class AppProvider(var context: Context, var data: ArrayList<CategorySticker>) : StickerProvider{
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    override fun getCategories(): Array<StickerCategory<*>?> {
        val category: Array<StickerCategory<*>?> = arrayOfNulls(data.size)
        for (i in data.indices) {
            category[i] = AppSticker(data[i])
        }
        return category
    }

    override fun getLoader(): StickerLoader {
        return object : StickerLoader{
            override fun onLoadSticker(view: View?, progressBar : View?, sticker: Sticker<Any>?) {
                if(sticker?.isInstance(vn.techres.line.data.model.chat.Sticker::class.java) == true && view != null){
                    val requestOptions = RequestOptions()
                        .fitCenter()
//                            .placeholder(R.drawable.im_postfeed_sticker_d)
//                            .error(R.drawable.im_postfeed_sticker_d)
                    GlideImageLoaderSticker(view as ImageView, progressBar as ProgressBar).load(
                        String.format("%s%s", configNodeJs.api_ads, (sticker.data as vn.techres.line.data.model.chat.Sticker).link_original),
                        requestOptions
                    )
                }
            }

            override fun onLoadStickerCategory(
                icon: View?,
                progressBar : View?,
                stickerCategory: StickerCategory<Any>?,
                selected: Boolean
            ) {
                if (icon != null) {
                    val requestOptions = RequestOptions()
                        .fitCenter()
//                        .placeholder(R.drawable.im_postfeed_sticker_d)
//                        .error(R.drawable.im_postfeed_sticker_d)

                    GlideImageLoaderSticker(icon as ImageView, progressBar as ProgressBar).load(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            stickerCategory?.categoryData as String
                        ),
                        requestOptions
                    )
                }
            }

        }
    }

    override fun isRecentEnabled(): Boolean = true

}