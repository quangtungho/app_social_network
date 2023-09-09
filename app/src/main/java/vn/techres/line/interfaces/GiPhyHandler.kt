package vn.techres.line.interfaces

import com.giphy.sdk.core.models.Media

interface GiPhyHandler {
    fun onGiPhy(media : Media)
    fun closeGiPhy()
    fun onBackPressGiPhy()
}