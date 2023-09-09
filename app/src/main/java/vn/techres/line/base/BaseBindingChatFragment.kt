package vn.techres.line.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import vn.techres.line.activity.ChatActivity
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.chat.OnBackClick

abstract class BaseBindingChatFragment<VB: ViewBinding>(private val inflate: Inflate<VB>, private val swipe : Boolean = false) : Fragment(),
    OnBackClick {

    protected abstract fun onBackPress() : Boolean
    private var _binding: VB? = null
    val binding get() = _binding!!
    val cacheManager: CacheManager = CacheManager.getInstance()
    val activityChat: ChatActivity?
        get() = activity as ChatActivity?

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
//        if(swipe){
//            swipeBackLayout.setEdgeOrientation(SwipeBackLayout.EDGE_LEFT)
//            setEdgeLevel(SwipeBackLayout.EdgeLevel.MED)
//        }
//        setSwipeBackEnable(false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activityChat?.setOnBackClick(this)
    }

    fun runOnUiThread(runnable: Runnable?) {
        if (activityChat != null) {
            activityChat?.runOnUiThread(runnable)
        }
    }

    fun closeKeyboard(edt: EditText) {

        edt.requestFocus()
        edt.isCursorVisible = false
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edt.windowToken, 0)
    }

    fun showKeyboard(edt: EditText) {
        edt.requestFocus()
        edt.isCursorVisible = true
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }

    fun getHeightScreen() : Int{
        val sharedPreference = activityChat?.baseContext?.let { PreferenceHelper(it) }
        return sharedPreference?.getValueInt(TechResEnumChat.CACHE_KEYBOARD.toString()) ?: 0
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        activityChat?.removeOnBackClick(this)
    }

    override fun onBack() : Boolean{
        return onBackPress()
    }
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}