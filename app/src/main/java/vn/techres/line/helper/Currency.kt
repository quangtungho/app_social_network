package vn.techres.line.helper

import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import java.text.DecimalFormat

object Currency {
    //decimal format
    const val CUSTOM_DECIMAL_FORMAT = "#.###"
    const val CUSTOM_DECIMAL_FORMAT_SLAH = "###,###,###"
    fun formatCurrency(number: Float): String {
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT)
        return formatter.format(number.toDouble())
    }

    fun formatPointDecimal(number: Float?): String {
        return String.format("%,.2f", number)
    }

    fun formatCurrencyDecimal(number: Float?): String {
        return String.format("%,.0f", number)
    }

    fun formatSlahCurrency(number: Float): String {
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT_SLAH)
        return formatter.format(number.toDouble())
    }

    fun StringToInt(number: String?): Int {
        return number?.toInt() ?: 0
    }

    fun formatCurrency(number: String?): String {
        if (number == null) return ""
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT)
        return formatter.format(number.toFloat().toDouble())
    }

    fun floatToString(number: Float): String {
        return java.lang.Float.toString(number)
    }

    fun intToString(number: Int): String {
        return Integer.toString(number)
    }

    fun formatCurrency(number: Int): String {
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT)
        return formatter.format(number.toLong())
    }

    fun formatCurrency(number: Long): String {
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT)
        return formatter.format(number)
    }

    fun checkEmptyString(text: String?): Boolean {
        return text == null || text == ""
    }

    fun formatTextSeeMore(text: Int): Spanned {
        val textFormat =
            "<font color=#aaaaaa>Xem thêm</font> <font color=#eb4f38>$text</font><font color=#aaaaaa> bình luận</font>"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    fun formatTextShopInfo(text: Int): Spanned {
        var textFormat = ""
        textFormat = if (text > 0) {
            "<font color=#eb4f38>$text</font> Điểm uy tín"
        } else {
            "Shop chưa được đánh giá"
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat)
        }
    }

    fun formatTextShopInfoItem(text: Int, title: String): Spanned {
        val textFormat = "<font color=#000000>( $text )</font><br>$title"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat)
        }
    }

    fun formatTextShopInfoRating(text: Int, title: String): Spanned {
        val textFormat = "<font color=#000000>( $text) </font> $title"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat)
        }
    }

    fun formatTextShopInfoShipping(level: Int, value: String): Spanned {
        val textFormat =
            "<font>Mức </font>$level: <font color=#69A228>$value</font> cho đơn hàng từ"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat)
        }
    }

    fun formatTextShopInfoAdd(text: String, content: String): Spanned {
        val textFormat =
            "<b><font color=#000000>$text</font></b><br>$content"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(textFormat, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(textFormat)
        }
    }

    fun htmlToString(text: String?): Spanned {
        return if (text != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(text)
            }
        } else {
            Html.fromHtml("")
        }
    }

    fun checkNull(text: String?): Boolean {
        return text != null
    }

    fun convertToNormal(text: String?): String {
        var text = text ?: return "0"
        if (text.contains(",")) text = text.replace(",", "")
        if (text.contains(".")) text = text.replace(".", "")
        return text
    }

    fun checkHour(text: String?): Boolean {
        return if (text != null) {
            text == "hour"
        } else {
            false
        }
    }
}