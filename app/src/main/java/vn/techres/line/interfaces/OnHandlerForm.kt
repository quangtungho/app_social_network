package vn.techres.line.interfaces

import vn.techres.line.data.model.utils.InfoBirthdayGift

interface OnHandlerForm {
    fun onHandler(infoBirthdayGift: InfoBirthdayGift)
}