package vn.techres.line.interfaces

import vn.techres.line.data.model.chat.Background

interface SelectBackgroundHandler {
    fun onSelectBackground(background : Background)
}