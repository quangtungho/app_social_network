package vn.techres.line.interfaces.chat

import im.ene.toro.helper.ToroPlayerHelper

interface VideoMessageHandler {
    fun onSoundVideo(helper : ToroPlayerHelper)
}