package vn.techres.line.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.listener.OnStickerActions
import com.aghajari.emojiview.sticker.Sticker
import com.aghajari.emojiview.utils.Utils
import com.aghajari.emojiview.view.AXEmojiLayout
import com.aghajari.emojiview.view.AXEmojiPager
import com.aghajari.emojiview.view.AXStickerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.Background
import vn.techres.line.data.model.chat.CategorySticker
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.keyboard.AppProvider
import vn.techres.line.helper.keyboard.MediaKeyboard
import vn.techres.line.helper.keyboard.UtilitiesChatHandler
import vn.techres.line.helper.keyboard.UtilitiesKeyboard
import vn.techres.line.helper.techresenum.TechResEnumChat

object UIView {
    var mEmojiView = false
    var mSingleEmojiView = false
    var mStickerView = false
    var mCustomView = false
    var mFooterView = false
    var mCustomFooter = false
    var mWhiteCategory = false
    var darkMode = false
    private var categorySticker = ArrayList<CategorySticker>()
    private var utilitiesChatHandler: UtilitiesChatHandler? = null
    private var backgroundList: ArrayList<Background>? = null

    fun setUtilitiesChatHandler(utilitiesChatHandler: UtilitiesChatHandler) {
        UIView.utilitiesChatHandler = utilitiesChatHandler
    }

    fun loadTheme() {
        // release theme
        darkMode = false
        AXEmojiManager.resetTheme()

        // set EmojiView Theme
        AXEmojiManager.getEmojiViewTheme().isFooterEnabled = mFooterView && !mCustomFooter
        AXEmojiManager.getEmojiViewTheme().selectionColor = -0xbf7f
        AXEmojiManager.getEmojiViewTheme().footerSelectedItemColor = -0xbf7f
        AXEmojiManager.getStickerViewTheme().selectionColor = -0xbf7f
        if (mWhiteCategory) {
            AXEmojiManager.getEmojiViewTheme().selectionColor = Color.TRANSPARENT
            AXEmojiManager.getEmojiViewTheme().selectedColor = -0xbf7f
            AXEmojiManager.getEmojiViewTheme().categoryColor = Color.WHITE
            AXEmojiManager.getEmojiViewTheme().footerBackgroundColor = Color.WHITE
            AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(true)
            AXEmojiManager.getStickerViewTheme().selectedColor = -0xbf7f
            AXEmojiManager.getStickerViewTheme().categoryColor = Color.WHITE
            AXEmojiManager.getStickerViewTheme().setAlwaysShowDivider(true)
        }
        AXEmojiManager.setBackspaceCategoryEnabled(!mCustomFooter)
    }

    fun setCategoryStickers(categorySticker: ArrayList<CategorySticker>) {
        this.categorySticker = categorySticker
    }

    fun setListBackground(backgroundList: ArrayList<Background>?) {
        this.backgroundList = backgroundList
    }

    fun getListBackground(): ArrayList<Background> {
        return backgroundList ?: ArrayList()
    }

    fun loadView(context: Context, editText: EditText): AXEmojiPager {
        val emoPager = AXEmojiPager(context)
        val stickerView = AXStickerView(
            context,
            TechResEnumChat.TYPE_STICKER.toString(),
            AppProvider(context, categorySticker)
        )
        emoPager.addPage(stickerView, R.drawable.ic_sticker_tab)
        emoPager.editText = editText
        emoPager.setSwipeWithFingerEnabled(false)
        stickerView.setOnStickerActionsListener(object : OnStickerActions {
            override fun onClick(view: View?, sticker: Sticker<Any>?, fromRecent: Boolean) {
                sticker?.let {
                    utilitiesChatHandler?.onChooseSticker(it.data as vn.techres.line.data.model.chat.Sticker)
                }
            }

            override fun onLongClick(
                view: View?,
                sticker: Sticker<Any>?,
                fromRecent: Boolean
            ): Boolean {
                return false
            }

        })
        if (mCustomFooter) {
            initCustomFooter(context, emoPager)
        } else {
            emoPager.setLeftIcon(R.drawable.ic_search_white_24dp)
            emoPager.setOnFooterItemClicked { _, _ -> }
        }
        return emoPager
    }

    fun loadUtilities(context: Context, editText: EditText): UtilitiesKeyboard {
        val utilitiesKeyboard = UtilitiesKeyboard(context)
        utilitiesKeyboard.editText = editText
        return utilitiesKeyboard
    }
    fun loadMedia(context: Context, editText: EditText) : MediaKeyboard{
        val mediaKeyboard = MediaKeyboard(context)
        mediaKeyboard.editText = editText
        return mediaKeyboard
    }

    @SuppressLint("ClickableViewAccessibility", "RtlHardcoded")
    fun initCustomFooter(context: Context, emoPager: AXEmojiPager) {
        val footer = FrameLayout(context)
        val drawable = ContextCompat.getDrawable(context, R.drawable.cicle_search_white)
        if (darkMode) drawable?.let { DrawableCompat.setTint(it, -0xe4dbd3) }
        footer.background = drawable
        footer.elevation = Utils.dp(context, 4f).toFloat()

        val lp = AXEmojiLayout.LayoutParams(Utils.dp(context, 48f), Utils.dp(context, 48f))
        lp.rightMargin = Utils.dp(context, 12f)
        lp.bottomMargin = Utils.dp(context, 12f)
        lp.gravity = Gravity.RIGHT or Gravity.BOTTOM
        footer.layoutParams = lp
        emoPager.setCustomFooter(footer, true)

        val img = AppCompatImageView(context)
        val lp2 = FrameLayout.LayoutParams(Utils.dp(context, 22f), Utils.dp(context, 22f))
        lp2.gravity = Gravity.CENTER
        footer.addView(img, lp2)
        footer.hide()
        val clickListener = View.OnClickListener {
//            Toast.makeText(context, "Search Clicked", Toast.LENGTH_SHORT).show()
        }

        emoPager.setOnEmojiPageChangedListener { emoPager, base, _ ->
            val drawable: Drawable
            if (AXEmojiManager.isAXEmojiView(base)) {
                drawable = ContextCompat.getDrawable(context, R.drawable.emoji_backspace)!!
                Utils.enableBackspaceTouch(footer, emoPager.editText)
                footer.setOnClickListener(null)
            } else {
                drawable = ContextCompat.getDrawable(context, R.drawable.ic_search_white_24dp)!!
                footer.setOnTouchListener(null)
                footer.setOnClickListener(clickListener)
            }
            DrawableCompat.setTint(
                DrawableCompat.wrap(drawable),
                AXEmojiManager.getEmojiViewTheme().footerItemColor
            )
            img.setImageDrawable(drawable)
        }
    }
}