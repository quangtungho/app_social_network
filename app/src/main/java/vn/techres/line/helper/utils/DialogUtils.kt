package vn.techres.line.helper.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.techres.line.R
import vn.techres.line.helper.Utils.show
import vn.techres.line.interfaces.dialog.DialogPermissionHandler

/**
 * show single loading dialog
 */
var loadingDialog: Dialog? = null

fun Fragment.showLoadingDialog(
    cancelable: Boolean = false,
    canceledOnTouchOutside: Boolean = false
): AlertDialog? {
    return MaterialAlertDialogBuilder(context ?: return null).apply {
        setView(R.layout.layout_loading_dialog)
    }.create().let { dialog ->
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dialog.dismiss()
                if (loadingDialog === dialog) {
                    loadingDialog = null
                }
            }
        })
        loadingDialog = dialog
        dialog.show()
        dialog
    }
}

fun AppCompatActivity.showLoadingDialog(
    cancelable: Boolean = false,
    canceledOnTouchOutside: Boolean = false
): AlertDialog? {
    return MaterialAlertDialogBuilder(this).apply {
        setView(R.layout.layout_loading_dialog)
    }.create().let { dialog ->
        dialog.setCancelable(cancelable)
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dialog.dismiss()
                if (loadingDialog === dialog) {
                    loadingDialog = null
                }
            }
        })
        loadingDialog = dialog
        dialog.show()
        dialog
    }
}

fun dismissLLoadingDialog() {
    if (loadingDialog?.isShowing == true) {
        loadingDialog?.dismiss()
    }
}

/**
 * show single alert dialog
 */
var showingDialog: Dialog? = null

fun Fragment.showDialog(
    title: String? = null, message: String? = null,
    textPositive: String? = null, positiveListener: (() -> Unit)? = null,
    textNegative: String? = null, negativeListener: (() -> Unit)? = null,
    cancelable: Boolean = false, canceledOnTouchOutside: Boolean = false
): AlertDialog? {
    return MaterialAlertDialogBuilder(context ?: return null).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(textPositive) { dialog, which ->
            positiveListener?.invoke()
        }
        setNegativeButton(textNegative) { dialog, which ->
            negativeListener?.invoke()
        }
        setCancelable(cancelable)
    }.create().let { dialog ->
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        if (showingDialog?.isShowing == true) {
            showingDialog?.dismiss()
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dialog.dismiss()
                if (showingDialog === dialog) {
                    showingDialog = null
                }
            }
        })
        dialog.setOnDismissListener {
            showingDialog = null
        }
        showingDialog = dialog
        dialog.show()
        dialog
    }
}

fun AppCompatActivity.showDialog(
    title: String? = null, message: String? = null,
    textPositive: String? = null, positiveListener: (() -> Unit)? = null,
    textNegative: String? = null, negativeListener: (() -> Unit)? = null,
    cancelable: Boolean = false, canceledOnTouchOutside: Boolean = false
): AlertDialog {
    return MaterialAlertDialogBuilder(this).apply {
        setTitle(title)
        setMessage(message)
        setPositiveButton(textPositive) { dialog, which ->
            positiveListener?.invoke()
        }
        setNegativeButton(textNegative) { dialog, which ->
            negativeListener?.invoke()
        }
        setCancelable(cancelable)
    }.create().let { dialog ->
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        if (showingDialog?.isShowing == true) {
            showingDialog?.dismiss()
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dialog.dismiss()
                if (showingDialog === dialog) {
                    showingDialog = null
                }
            }
        })
        dialog.setOnDismissListener {
            showingDialog = null
        }
        showingDialog = dialog
        dialog.show()
        dialog
    }
}

fun dismissShowingDialog() {
    if (showingDialog?.isShowing == true) {
        showingDialog?.dismiss()
    }
}

fun <T : Fragment> T.dialogPermission(
    title: String, content: String,
    step: String, idRes: Int, stepTwo: String = "",
    idResTwo: Int = 0, dialogPermissionHandler: DialogPermissionHandler
){
    val context = context ?: return
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_permisson_app)
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.attributes = lp
    val tvTitlePermission = dialog.findViewById(R.id.tvTitlePermission) as TextView
    val tvContentPermission = dialog.findViewById(R.id.tvContentPermission) as TextView
    val imgCloseDialog = dialog.findViewById(R.id.imgCloseDialog) as ImageView
    val imgStepTwo = dialog.findViewById(R.id.imgStepTwo) as ImageView
    val tvContentStepTwo = dialog.findViewById(R.id.tvContentStepTwo) as TextView
    val btnConfirmPermission = dialog.findViewById(R.id.btnConfirmPermission) as Button
    val lnStepThree = dialog.findViewById(R.id.lnStepThree) as LinearLayout
    val imgStepThree = dialog.findViewById(R.id.imgStepThree) as ImageView
    val tvContentStepThree = dialog.findViewById(R.id.tvContentStepThree) as TextView
    tvTitlePermission.text = title
    tvContentPermission.text = content
    tvContentStepTwo.text = step
    imgStepTwo.setImageResource(idRes)
    if(stepTwo != "" && idResTwo != 0){
        lnStepThree.show()
        tvContentStepThree.text = stepTwo
        imgStepThree.setImageResource(idResTwo)
    }

    imgCloseDialog.setOnClickListener {
        dialogPermissionHandler.dismissDialog()
        dialog.dismiss()
    }

    btnConfirmPermission.setOnClickListener {
        dialogPermissionHandler.confirmDialog()
        dialog.dismiss()
    }

    dialog.show()
}
fun <T : Activity> T.dialogPermission(
    title: String, content: String,
    step: String, idRes: Int, stepTwo: String = "",
    idResTwo: Int = 0, dialogPermissionHandler: DialogPermissionHandler
){
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_permisson_app)
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.attributes = lp
    val tvTitlePermission = dialog.findViewById(R.id.tvTitlePermission) as TextView
    val tvContentPermission = dialog.findViewById(R.id.tvContentPermission) as TextView
    val imgCloseDialog = dialog.findViewById(R.id.imgCloseDialog) as ImageView
    val imgStepTwo = dialog.findViewById(R.id.imgStepTwo) as ImageView
    val tvContentStepTwo = dialog.findViewById(R.id.tvContentStepTwo) as TextView
    val btnConfirmPermission = dialog.findViewById(R.id.btnConfirmPermission) as Button
    val lnStepThree = dialog.findViewById(R.id.lnStepThree) as LinearLayout
    val imgStepThree = dialog.findViewById(R.id.imgStepThree) as ImageView
    val tvContentStepThree = dialog.findViewById(R.id.tvContentStepThree) as TextView
    tvTitlePermission.text = title
    tvContentPermission.text = content
    tvContentStepTwo.text = step
    imgStepTwo.setImageResource(idRes)
    if(stepTwo != "" && idResTwo != 0){
        lnStepThree.show()
        tvContentStepThree.text = stepTwo
        imgStepThree.setImageResource(idResTwo)
    }

    imgCloseDialog.setOnClickListener {
        dialogPermissionHandler.dismissDialog()
        dialog.dismiss()
    }

    btnConfirmPermission.setOnClickListener {
        dialogPermissionHandler.confirmDialog()
        dialog.dismiss()
    }

    dialog.show()
}

fun Context.dialogPermissionDeny(permission : () -> Unit,
    title: String, content: String, step: String,
    idRes: Int, stepTwo: String = "", idResTwo: Int = 0
){
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.dialog_permission_denied)
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.attributes = lp
    val tvTitlePermission = dialog.findViewById(R.id.tvTitlePermission) as TextView
    val tvContentPermission = dialog.findViewById(R.id.tvContentPermission) as TextView
    val imgCloseDialog = dialog.findViewById(R.id.imgCloseDialog) as ImageView
    val imgStepTwo = dialog.findViewById(R.id.imgStepTwo) as ImageView
    val tvContentStepTwo = dialog.findViewById(R.id.tvContentStepTwo) as TextView
    val btnConfirmPermission = dialog.findViewById(R.id.btnConfirmPermission) as Button
    val lnStepThree = dialog.findViewById(R.id.lnStepThree) as LinearLayout
    val imgStepThree = dialog.findViewById(R.id.imgStepThree) as ImageView
    val tvContentStepThree = dialog.findViewById(R.id.tvContentStepThree) as TextView
    tvTitlePermission.text = title
    tvContentPermission.text = content
    tvContentStepTwo.text = step
    imgStepTwo.setImageResource(idRes)
    if(stepTwo != "" && idResTwo != 0){
        lnStepThree.show()
        tvContentStepThree.text = stepTwo
        imgStepThree.setImageResource(idResTwo)
    }
    imgCloseDialog.setOnClickListener {
        dialog.dismiss()
    }

    btnConfirmPermission.setOnClickListener {
        permission.invoke()
        dialog.dismiss()
    }

    dialog.show()

}

fun setupFullHeight(context: Context, bottomSheetDialog : BottomSheetDialog) {
    val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
    val behavior = BottomSheetBehavior.from(bottomSheet)
    val layoutParams = bottomSheet.layoutParams
    val windowHeight = getWindowHeight(context)
    if (layoutParams != null) {
        layoutParams.height = windowHeight
    }
    bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    bottomSheet.layoutParams = layoutParams
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
}

private fun getWindowHeight(context: Context) : Int {
    // Calculate window height for fullscreen use
    val displayMetrics = DisplayMetrics()
    (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}