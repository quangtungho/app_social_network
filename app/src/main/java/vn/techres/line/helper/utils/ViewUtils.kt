package vn.techres.line.helper.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.*
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout


/**
 * @author Vo Van Chien
 */


fun View.invisible() = if (visibility != View.INVISIBLE) visibility = View.INVISIBLE else Unit

fun View.setMargins(
    left: Int = this.marginLeft,
    top: Int = this.marginTop,
    right: Int = this.marginRight,
    bottom: Int = this.marginBottom,
) {
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMargins(left, top, right, bottom)
    }
}

val View.animateCompat: ViewPropertyAnimatorCompat
    get() = ViewCompat.animate(this)

fun View.setBackgroundDrawableCompat(drawable: Drawable) {
    background = drawable
}

fun LayoutInflater.inflate(@LayoutRes layoutResId: Int): View = inflate(layoutResId, null)

fun ProgressBar.setColor(@ColorRes colorResId: Int) =
    DrawableCompat.setTint(indeterminateDrawable, context.getColorCompat(colorResId))

@Suppress("DEPRECATION")
fun SeekBar.setColor(@ColorRes colorResId: Int) {
    progressDrawable.setColorFilter(context.getColor(colorResId), PorterDuff.Mode.SRC_ATOP)
    thumb.setColorFilter(context.getColorCompat(colorResId), PorterDuff.Mode.SRC_ATOP)
}

fun MenuItem.iconifyTitle(context: Context, title: String, @DrawableRes iconResId: Int) {
    SpannableStringBuilder("#  $title ").apply {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setSpan(
                ImageSpan(context, iconResId, DynamicDrawableSpan.ALIGN_CENTER),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }.let { setTitle(it) }
}

fun TabLayout.disableTextAllCaps() {
    val viewGroup = getChildAt(0) as ViewGroup
    val tabsCount = viewGroup.childCount
    for (j in 0 until tabsCount) {
        val viewGroupChildAt = viewGroup.getChildAt(j) as ViewGroup
        val tabChildCount = viewGroupChildAt.childCount
        for (i in 0 until tabChildCount) {
            val tabViewChild = viewGroupChildAt.getChildAt(i)
            if (tabViewChild is TextView) {
                tabViewChild.isAllCaps = false
            }
        }
    }
}

fun NumberPicker.setDividerColor(@ColorInt color: Int) {
    val pickerFields = NumberPicker::class.java.declaredFields
    for (pf in pickerFields) {
        if (pf.name == "mSelectionDivider") {
            pf.isAccessible = true
            try {
                val colorDrawable = ColorDrawable(color)
                pf.set(this, colorDrawable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            break
        }
    }
}

//fun BottomNavigationView.applyFont(fontPath: String) {
//    val typeface = Typeface.createFromAsset(context.assets, fontPath)
//    for (i in 0 until childCount) {
//        val child = getChildAt(i)
//        if (child is BottomNavigationMenuView) {
//            for (j in 0 until child.childCount) {
//                val item = child.getChildAt(j)
//
//                val smallItemText = item.findViewById<View>(R.id.smallLabel)
//                if (smallItemText is TextView) smallItemText.typeface = typeface
//
//                val largeItemText = item.findViewById<View>(R.id.largeLabel)
//                if (largeItemText is TextView) largeItemText.typeface = typeface
//            }
//        }
//    }
//}

fun TabLayout.applyFont(fontPath: String) {
    val typeface = Typeface.createFromAsset(context.assets, fontPath)
    val viewGroup = getChildAt(0) as ViewGroup
    val tabsCount = viewGroup.childCount
    for (j in 0 until tabsCount) {
        val viewGroupChildAt = viewGroup.getChildAt(j) as ViewGroup
        val tabChildCount = viewGroupChildAt.childCount
        for (i in 0 until tabChildCount) {
            val tabViewChild = viewGroupChildAt.getChildAt(i)
            if (tabViewChild is AppCompatTextView) {
                TextViewCompat.setAutoSizeTextTypeWithDefaults(
                    tabViewChild,
                    TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
                )
                tabViewChild.setTypeface(typeface, Typeface.NORMAL)
            }
        }
    }
}
@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

@ColorInt
fun Fragment.getColorCompat(@ColorRes colorRes: Int) = activity?.getColorCompat(colorRes)

@ColorInt
fun Resources.getColorCompat(@ColorRes colorRes: Int) = ResourcesCompat.getColor(this, colorRes, null)