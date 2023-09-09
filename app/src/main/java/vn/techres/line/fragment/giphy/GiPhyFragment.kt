package vn.techres.line.fragment.giphy

import android.content.Context
import android.os.Bundle
import android.view.View
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.databinding.FragmentGiphyBinding
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.GiPhyHandler

class GiPhyFragment : BaseBindingFragment<FragmentGiphyBinding>(FragmentGiphyBinding::inflate){
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var settings = GPHSettings(
        gridType = GridType.carousel,
        useBlurredBackground = false,
        theme = GPHTheme.Light,
        stickerColumnCount = 3
    )

    private var giPhyHandler : GiPhyHandler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
    }

    override fun onResume() {
        super.onResume()
        val dialog = GiphyDialogFragment.newInstance(settings)
        dialog.gifSelectionListener = getGifSelectionListener()
        dialog.show(childFragmentManager, "gifs_dialog")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        super.onAttach(context)
        if(parentFragment is GiPhyHandler){
            giPhyHandler = parentFragment as GiPhyHandler
        }else{
//            throw RuntimeException(
//                context.toString()
//                        + " must implement OnChildFragmentInteractionListener"
//            )
        }
    }

    override fun onDetach() {
        giPhyHandler = null
        super.onDetach()
    }

    private fun getGifSelectionListener() = object : GiphyDialogFragment.GifSelectionListener {
        override fun onGifSelected(
            media: Media,
            searchTerm: String?,
            selectedContentType: GPHContentType
        ) {
            giPhyHandler?.onGiPhy(media)
        }

        override fun onDismissed(selectedContentType: GPHContentType) {
            giPhyHandler?.closeGiPhy()
        }

        override fun didSearchTerm(term: String) {
        }
    }
    override fun onBackPress() : Boolean {
        giPhyHandler?.onBackPressGiPhy()
        return true
    }
}