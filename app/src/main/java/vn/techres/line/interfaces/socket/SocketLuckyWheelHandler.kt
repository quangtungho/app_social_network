package vn.techres.line.interfaces.socket

import vn.techres.line.data.model.game.luckywheel.MessageGameLuckyWheel
import vn.techres.line.data.model.game.luckywheel.response.ReactionLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.RoundLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.StopLuckyWheelResponse
import vn.techres.line.data.model.game.luckywheel.response.TotalCustomerJoinRoomResponse

interface SocketLuckyWheelHandler {
    fun onNumber(number : Int)
    fun onStartLuckyWheel()
    fun onStopLuckyWheel(stopLuckyWheelResponse : StopLuckyWheelResponse)
    fun onTotalMemberJoinRoom(totalCustomerJoinRoomResponse : TotalCustomerJoinRoomResponse)
    fun onReaction(reactionLuckyWheelResponse : ReactionLuckyWheelResponse)
    fun onGameRound(roundLuckyWheelResponse : RoundLuckyWheelResponse)
    fun onChatText(messageGameLuckyWheel : MessageGameLuckyWheel)
    fun onChatSticker(messageGameLuckyWheel : MessageGameLuckyWheel)
    fun onNextMoney(money : Int)
    fun onCurrentMoney(money : Int)
}