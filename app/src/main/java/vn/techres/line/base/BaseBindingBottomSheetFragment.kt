package vn.techres.line.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.utils.setupFullHeight

abstract class BaseBindingBottomSheetFragment<VB : ViewBinding>(private val inflate: Inflate<VB>, val style : Int = 0) :
    BottomSheetDialogFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!
    val cacheManager: CacheManager = CacheManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setupFullHeight(requireActivity(), bottomSheetDialog)
        }
        return dialog
    }

    override fun getTheme(): Int {
        return if(style != 0){
            style
        }else{
            super.getTheme()
        }
    }
}