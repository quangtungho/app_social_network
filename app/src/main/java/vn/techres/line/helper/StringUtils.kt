package vn.techres.line.helper

import android.util.Patterns
import java.text.DecimalFormat
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.roundToInt

object StringUtils {
    /**
     * Represents a failed index search.
     *
     * @since 2.1
     */
    private const val INDEX_NOT_FOUND = -1
    //decimal format
    private const val CUSTOM_DECIMAL_FORMAT = "###,###,###"

    const val DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£\$%&/=@#"
    private val RANDOM: Random = Random()


    fun isNullOrEmpty(str: String?): Boolean {
        return null == str || str.isEmpty()
    }

    fun removeUnicode(str: String?): String {
        val oldString =
            Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(oldString).replaceAll("")
    }

    fun isEmpty(cs: CharSequence?): Boolean {
        return cs == null || cs.isEmpty()
    }

    fun validEmail(email: String): Boolean {
        var emailTemp = email
        var result = false
        if (!isNullOrEmpty(emailTemp)) {
            emailTemp = emailTemp.trim { it <= ' ' }
        }
        if (Patterns.EMAIL_ADDRESS.matcher(emailTemp).matches() && emailTemp.isNotEmpty()) {
            result = true
        }
        return result
    }

    fun formatScore(number: Int): String {
        val formatter =
            DecimalFormat(CUSTOM_DECIMAL_FORMAT)
        val result = formatter.format(number.toLong())
        return result.replace(",".toRegex(), ".")
    }

    fun formatDecimal(number: Float): String {
        val epsilon = 0.004f // 4 tenths of a cent
        var result = "0"
        if (number > 0) {
            result = if (abs(number.roundToInt() - number) < epsilon) {
                String.format("%10.0f", number) // sdb
            } else {
                String.format("%10.2f", number) // dj_segfault
            }
        }
        return result.trim { it <= ' ' }
    }



    /**
     * So dien thoai hop le:
     * Chỉ chứa các kí tự số
     * Start "01": 11 ký tự
     * Start "09": 10 ký tự
     * Start "086", "088": 10 ký tự
     */
    fun validPhoneNumber(phoneNumber: String): Boolean {
        var phone = phoneNumber
        var result = false
        if (!isNullOrEmpty(phone)) {
            if (isNumber(phone)) {
                phone = phone.trim { it <= ' ' }
                val length = phone.length
                if (length in 10..11) {
                    if (phone.startsWith("01")) {
                        if (length == 11) {
                            result = true
                        }
                    } else if (phone.startsWith("09") || phone.startsWith("086") || phone.startsWith(
                            "088"
                        )
                    ) {
                        if (length == 10) {
                            result = true
                        }
                    }
                }
            }
        }
        return result
    }

    fun isNumber(string: String): Boolean {
        if (isNullOrEmpty(string)) {
            return false
        }
        var i = 0
        if (string[0] == '-') {
            if (string.length > 1) {
                i++
            } else {
                return false
            }
        }
        while (i < string.length) {
            if (!Character.isDigit(string[i])) {
                return false
            }
            i++
        }
        return true
    }

    /**
     * 0 --> first name (Ten).
     * 1 --> last name (Ho)
     */
    fun getLastNameFirstName(fullName: String): Array<String?> {
        var name = fullName
        val names = arrayOfNulls<String>(2)
        names[0] = ""
        names[1] = ""
        if (!isNullOrEmpty(name)) {
            name = name.trim { it <= ' ' }
            if (name.isNotEmpty()) {
                val lastIndex = name.lastIndexOf(" ")
                if (lastIndex > -1) {
                    names[0] = name.substring(lastIndex)
                    names[1] = name.substring(0, lastIndex)
                    if (names[0] == null) {
                        names[0] = ""
                    }
                    if (names[1] == null) {
                        names[1] = ""
                    }
                } else {
                    names[0] = name
                }
            }
        }
        return names
    }

    fun getStringAfterTrim(txt: String): String {
        var string = txt
        string = string.replace("\\s+".toRegex(), " ")
        if (string.startsWith(" ")) string = string.substring(1)
        if (string.endsWith(" ")) string = string.substring(0, string.length - 1)
        return string
    }
    // Reversing
//-----------------------------------------------------------------------
    /**
     *
     * Reverses a String as per [StringBuilder.reverse].
     *
     *
     *
     * A `null` String returns `null`.
     *
     *
     * <pre>
     * StringUtils.reverse(null)  = null
     * StringUtils.reverse("")    = ""
     * StringUtils.reverse("bat") = "tab"
    </pre> *
     *
     * @param str the String to reverse, may be null
     * @return the reversed String, `null` if null String input
     */
    fun reverse(str: String?): String? {
        return if (str == null) {
            null
        } else StringBuilder(str).reverse().toString()
    }
    /**
     *
     * Replaces a String with another String inside a larger String,
     * for the first `max` values of the search String.
     *
     *
     *
     * A `null` reference passed to this method is a no-op.
     *
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
    </pre> *
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or `-1` if no maximum
     * @return the text with any replacements processed,
     * `null` if null String input
     */
    /**
     *
     * Replaces all occurrences of a String within another String.
     *
     *
     *
     * A `null` reference passed to this method is a no-op.
     *
     *
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
    </pre> *
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     * `null` if null String input
     * @see .replace
     */
    @JvmOverloads
    fun replace(
        text: String,
        searchString: String,
        replacement: String?,
        max: Int = -1
    ): String {
        var maxTemp = max
        if (isEmpty(text) || isEmpty(
                searchString
            ) || replacement == null || maxTemp == 0
        ) {
            return text
        }
        var start = 0
        var end = text.indexOf(searchString, start)
        if (end == INDEX_NOT_FOUND) {
            return text
        }
        val replLength = searchString.length
        var increase = replacement.length - replLength
        increase = if (increase < 0) 0 else increase
        increase *= if (maxTemp < 0) 16 else if (maxTemp > 64) 64 else maxTemp
        val buf = StringBuilder(text.length + increase)
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement)
            start = end + replLength
            if (--maxTemp == 0) {
                break
            }
            end = text.indexOf(searchString, start)
        }
        buf.append(text.substring(start))
        return buf.toString()
    }
    // Count matches
//-----------------------------------------------------------------------
    /**
     *
     * Counts how many times the substring appears in the larger string.
     *
     *
     *
     * A `null` or empty ("") String input returns `0`.
     *
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
    </pre> *
     *
     * @param str the CharSequence to check, may be null
     * @param sub the substring to count, may be null
     * @return the number of occurrences, 0 if either CharSequence is `null`
     * @since 3.0 Changed signature from countMatches(String, String) to countMatches(CharSequence, CharSequence)
     */
    fun countMatches(str: CharSequence, sub: CharSequence): Int {
        if (isEmpty(str) || isEmpty(
                sub
            )
        ) {
            return 0
        }
        var count = 0
        var idx = 0
        while (indexOf(str, sub, idx).also {
                idx = it
            } != INDEX_NOT_FOUND) {
            count++
            idx += sub.length
        }
        return count
    }
    // Remove
//-----------------------------------------------------------------------
    /**
     *
     * Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.
     *
     *
     *
     * A `null` source string will return `null`.
     * An empty ("") source string will return the empty string.
     * A `null` search string will return the source string.
     *
     *
     * <pre>
     * StringUtils.removeStart(null, *)      = null
     * StringUtils.removeStart("", *)        = ""
     * StringUtils.removeStart(*, null)      = *
     * StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
     * StringUtils.removeStart("domain.com", "www.")       = "domain.com"
     * StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com"
     * StringUtils.removeStart("abc", "")    = "abc"
    </pre> *
     *
     * @param str    the source String to search, may be null
     * @param remove the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     * `null` if null String input
     * @since 2.1
     */
    fun removeStart(str: String, remove: String): String {
        if (isEmpty(str) || isEmpty(
                remove
            )
        ) {
            return str
        }
        return if (str.startsWith(remove)) {
            str.substring(remove.length)
        } else str
    }

    /**
     * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.
     *
     * @param cs         the `CharSequence` to be processed
     * @param searchChar the `CharSequence` to be searched for
     * @param start      the start index
     * @return the index where the search sequence was found
     */
    fun indexOf(cs: CharSequence, searchChar: CharSequence, start: Int): Int {
        return cs.toString().indexOf(searchChar.toString(), start)
    }

    fun repeatString(str: String, count: Int): String {
        var countTemp = count
        val buf = StringBuilder(str.length * countTemp)
        while (countTemp-- > 0) {
            buf.append(str)
        }
        return buf.toString()
    }

    fun randomString(len: Int): String? {
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(DATA[RANDOM.nextInt(DATA.length)])
        }
        return sb.toString()
    }
}