package vn.techres.line.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.adapter.chat.BackgroundAdapter
import vn.techres.line.data.model.chat.Background
import vn.techres.line.databinding.ToolsChatBinding
import vn.techres.line.helper.StringUtils.isNullOrEmpty
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.keyboard.UtilitiesKeyboard
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.topbottombehavior.LockableBottomSheetBehavior
import vn.techres.line.helper.topbottombehavior.TopSheetBehavior
import java.util.*

class ToolsChat {
    private var context: Context? = null
    private val TAG = "ToolsChat"
    private var _binding: ToolsChatBinding? = null
    val binding get() = _binding!!
    /**
     * ui keyboard
     * */
    private lateinit var utilities : UtilitiesKeyboard
    /**
     * top sheet background
     * */
    private lateinit var bottomSheetBehavior: TopSheetBehavior<ConstraintLayout>
    val bottomSheet get() = bottomSheetBehavior
    var adapter: BackgroundAdapter? = null

    @SuppressLint("InflateParams")
    fun initView(view: ViewGroup?) {
        if (view == null) {
            showErrorLog("initView ViewGroup can't be NULL")
            return
        }
        context = view.context
        view.removeAllViews()
        _binding = ToolsChatBinding.inflate(LayoutInflater.from(view.context))
        view.addView(binding.root)
        binding.edtMessageChat.isCursorVisible = false
        binding.edtMessageChat.addTextChangedListener(textWatcher)
        utilities = UIView.loadUtilities(view.context, binding.edtMessageChat)
        binding.layoutKeyboard.initPopupView(UIView.loadView(view.context, binding.edtMessageChat), utilities)
        //set topSheet Background
        bottomSheetBehavior = TopSheetBehavior.from(binding.bottomSheet.ctlBackground)
        (bottomSheetBehavior as LockableBottomSheetBehavior<ConstraintLayout>).setLocked(true)
        adapter = BackgroundAdapter(view.context)
        val backgroundJSon = PrefUtils.readPreferences(TechResEnumChat.CACHE_BACKGROUND.toString(), null)
        if(backgroundJSon != null){
            val background = Gson().fromJson<ArrayList<Background>>(
                backgroundJSon, object :
                    TypeToken<ArrayList<Background>>() {}.type
            )
            adapter?.setDataSource(background)
        }
        binding.bottomSheet.rcBackground.layoutManager = GridLayoutManager(view.context, 5)
        binding.bottomSheet.rcBackground.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.bottomSheet.rcBackground.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false
        binding.bottomSheet.rcBackground.adapter = adapter

        bottomSheet.setTopSheetCallback(object : TopSheetBehavior.TopSheetCallback(){
            override fun onStateChanged(topSheet: View, newState: Int) {
                if(newState == TopSheetBehavior.STATE_COLLAPSED){
                    binding.cdlListMessage.hide()
                }
            }

            override fun onSlide(topSheet: View, slideOffset: Float, isOpening: Boolean?) {
            }

        })
    }

    private fun showErrorLog(s: String) {
        WriteLog.e(TAG, s)
    }


    fun getUtilitiesKeyBoard() : UtilitiesKeyboard{
        return utilities
    }
    fun showController() {
        binding.imgSent.visibility = View.GONE
        binding.lnControllerMessage.clearAnimation()
        binding.lnControllerMessage.visibility = View.VISIBLE
        binding.txtCountCharacter.visibility = View.GONE
        binding.lnControllerMessage.animate()
            .translationX(0F)
            .alpha(1f)
            .setDuration(100)
            .setListener(null)
            .start()
    }

    fun showLink() {
        binding.utilities.rlLink.show()

//        binding.utilities.rlLink.clearAnimation()
//        binding.utilities.rlLink.animate()
//            .translationY(binding.utilities.rlLink.width.toFloat())
//            .alpha(1f)
//            .setDuration(300)
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator?) {
//                    super.onAnimationEnd(animation)
//                    binding.utilities.rlLink.show()
//                }
//            })
//            .start()
    }

    fun hideLink() {
        binding.utilities.rlLink.hide()

//        binding.utilities.rlLink.clearAnimation()
//        binding.utilities.rlLink.animate()
//            .translationX(0F)
//            .alpha(0.0f)
//            .setDuration(300)
//            .setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator?) {
//                    super.onAnimationEnd(animation)
//                    binding.utilities.rlLink.hide()
//                }
//            })
//            .start()
    }

    fun hideController() {
        binding.lnControllerMessage.clearAnimation()
        binding.lnControllerMessage.animate()
            .translationX(binding.lnControllerMessage.width.toFloat())
            .alpha(0.0f)
            .setDuration(100)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    binding.lnControllerMessage.visibility = View.GONE
                    binding.imgSent.visibility = View.VISIBLE
                }
            })
            .start()
    }



    fun saveCacheCopy(string: String, context: Context) {
        val sharedPreference = PreferenceHelper(context)
        sharedPreference.save(TechResEnumChat.CACHE_COPY.toString(), string)
    }

    fun getCacheCopy(context: Context): String? {
        val sharedPreference = PreferenceHelper(context)
        return sharedPreference.getValueString(TechResEnumChat.CACHE_COPY.toString())
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isNullOrEmpty(s.toString())) {
                showController()
            }else{
                hideController()
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (isNullOrEmpty(s.toString())) {
                showController()
            }else{
                hideController()
            }
        }

    }

}