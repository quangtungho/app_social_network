package vn.techres.line.interfaces

import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel

interface MessageLuckyWheelHandler {
    fun onReaction(messageGameLuckyWheel : MessageGameLuckyWheel)
}