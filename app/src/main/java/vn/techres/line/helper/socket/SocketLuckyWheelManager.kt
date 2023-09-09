package vn.techres.line.helper.socket

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket
import org.json.JSONObject
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.SenderGameLuckyWheel
import vn.techres.line.data.model.game.luckywheel.*
import vn.techres.line.data.model.game.luckywheel.response.*
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.utils.User
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.PreferenceHelper
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumGame
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.socket.SocketLuckyWheelHandler
import java.io.IOException

class SocketLuckyWheelManager() {
    private var tag = "SocketLuckyWheelManager"
    private var context: Context? = null
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var user = User()
    private var socketLuckyWheelHandler : SocketLuckyWheelHandler? = null
    private var isFirst = true

    constructor(context: Context) : this(){
        this.context = context
        user = CurrentUser.getCurrentUser(context)
        mSocket = application.getSocketGameInstance(context, restaurant(context).restaurant_id ?: 0)
        mSocket?.connect()
        if(isFirst){
            onSocket()
            isFirst = false
        }
    }

    fun setSocketLuckyWheelHandler(socketLuckyWheelHandler : SocketLuckyWheelHandler){
        this.socketLuckyWheelHandler = socketLuckyWheelHandler
    }

    fun restaurant(context: Context) : RestaurantCard {
        var restaurant = RestaurantCard()
        val sharedPreference = PreferenceHelper(context)
        val restaurantJson = sharedPreference.getValueString(TechresEnum.RESTAURANT_CARD.toString())
        if(!restaurantJson.isNullOrBlank()){
            restaurant = Gson().fromJson(restaurantJson, RestaurantCard::class.java)
        }
        return restaurant
    }

    fun joinRoom(branchId : Int, restaurantId : Int, roomId : String, permission : String, articleGameId : String){
        val joinRoomLuckyWheel = JoinRoomLuckyWheel()
        joinRoomLuckyWheel.user_id = user.id
        joinRoomLuckyWheel.restaurant_id = restaurantId
        joinRoomLuckyWheel.branch_id = branchId
        joinRoomLuckyWheel.room_id = roomId
        joinRoomLuckyWheel.id_article_game = articleGameId
        joinRoomLuckyWheel.permission = permission
        val jsonObject = JSONObject(Gson().toJson(joinRoomLuckyWheel))
        mSocket?.emit(TechResEnumGame.JOIN_LUCKY_WHEEL_ROOM.toString(), jsonObject)
        WriteLog.d(TechResEnumGame.JOIN_LUCKY_WHEEL_ROOM.toString(), Gson().toJson(joinRoomLuckyWheel))
    }
    fun leaveRoom(branchId : Int, restaurantId : Int, roomId : String){
        val leaveRoomLuckyWheel = LeaveRoomLuckyWheel()
        leaveRoomLuckyWheel.user_id = user.id
        leaveRoomLuckyWheel.restaurant_id = restaurantId
        leaveRoomLuckyWheel.branch_id = branchId
        leaveRoomLuckyWheel.room_id = roomId
        val jsonObject = JSONObject(Gson().toJson(leaveRoomLuckyWheel))
        mSocket?.emit(TechResEnumGame.LEAVE_LUCKY_WHEEL_ROOM.toString(), jsonObject)
        WriteLog.d(TechResEnumGame.LEAVE_LUCKY_WHEEL_ROOM.toString(), Gson().toJson(leaveRoomLuckyWheel))
    }

    fun onPushReaction(typeReaction : Int, roomId: String){
        val reactionLuckyWheel = ReactionLuckyWheel()
        reactionLuckyWheel.user_id = user.id
        reactionLuckyWheel.type_reaction = typeReaction
        reactionLuckyWheel.room_id = roomId
        val jsonObject = JSONObject(Gson().toJson(reactionLuckyWheel))
        mSocket?.emit(TechResEnumGame.REACTION_GAME.toString(), jsonObject)
        WriteLog.d(TechResEnumGame.REACTION_GAME.toString(), Gson().toJson(reactionLuckyWheel))
    }
    fun chatText(message: String, restaurantId: Int, branchId: Int, roomId: String){
        val chatTextLuckyWheelRequest = ChatTextLuckyWheelRequest()
        chatTextLuckyWheelRequest.message = message
        chatTextLuckyWheelRequest.sender = SenderGameLuckyWheel(user.id, user.name ?: "", user.avatar_three_image, "customer")
        chatTextLuckyWheelRequest.restaurant_id = restaurantId
        chatTextLuckyWheelRequest.branch_id = branchId
        chatTextLuckyWheelRequest.room_id = roomId
        val jsonObject = JSONObject(Gson().toJson(chatTextLuckyWheelRequest))
        mSocket?.emit(TechResEnumGame.CHAT_TEXT_EMOJI.toString(), jsonObject)
        WriteLog.d(TechResEnumGame.CHAT_TEXT_EMOJI.toString(), Gson().toJson(chatTextLuckyWheelRequest))
    }
    fun chatSticker(url: String, restaurantId: Int, branchId: Int, roomId: String){
        val chatStickerLuckyWheelRequest = ChatStickerLuckyWheelRequest()
        chatStickerLuckyWheelRequest.message = url
        chatStickerLuckyWheelRequest.sender = SenderGameLuckyWheel(user.id, user.name ?: "", user.avatar_three_image, "customer")
        chatStickerLuckyWheelRequest.restaurant_id = restaurantId
        chatStickerLuckyWheelRequest.branch_id = branchId
        chatStickerLuckyWheelRequest.room_id = roomId
        val jsonObject = JSONObject(Gson().toJson(chatStickerLuckyWheelRequest))
        mSocket?.emit(TechResEnumGame.CHAT_STICKER.toString(), jsonObject)
        WriteLog.d(TechResEnumGame.CHAT_STICKER.toString(), Gson().toJson(chatStickerLuckyWheelRequest))
    }
    private fun onSocket(){
        WriteLog.d(tag, "onSocket")
        mSocket?.on(TechResEnumGame.RES_LUCKY_NUMBER.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_LUCKY_NUMBER", it[0].toString())
//                    socketLuckyWheelHandler?.onNumber()
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on( TechResEnumGame.RES_START.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_START", it[0].toString())
                    socketLuckyWheelHandler?.onStartLuckyWheel()
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_STOP.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_STOP", it[0].toString())
                    val stopLuckyWheelResponse = Gson().fromJson<StopLuckyWheelResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<StopLuckyWheelResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onStopLuckyWheel(stopLuckyWheelResponse)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_TOTAL_CUSTOMER_JOIN_ROOM.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_TOTAL_CUSTOMER_JOIN_ROOM", it[0].toString())
                    val totalCustomerJoinRoomResponse = Gson().fromJson<TotalCustomerJoinRoomResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<TotalCustomerJoinRoomResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onTotalMemberJoinRoom(totalCustomerJoinRoomResponse)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_CURRENT_LUCKY_WHEEL_TIME.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_CURRENT_LUCKY_WHEEL_TIME", it[0].toString())
                    val roundLuckyWheelResponse = Gson().fromJson<RoundLuckyWheelResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<RoundLuckyWheelResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onGameRound(roundLuckyWheelResponse)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_CHAT_TEXT_EMOJI.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_CHAT_TEXT_EMOJI", it[0].toString())
                    val messageGameLuckyWheel = Gson().fromJson<MessageGameLuckyWheel>(
                        it[0].toString(),
                        object :
                            TypeToken<MessageGameLuckyWheel>() {}.type
                    )
                    socketLuckyWheelHandler?.onChatText(messageGameLuckyWheel)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_CHAT_SICKER.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_CHAT_SICKER", it[0].toString())
                    val messageGameLuckyWheel = Gson().fromJson<MessageGameLuckyWheel>(
                        it[0].toString(),
                        object :
                            TypeToken<MessageGameLuckyWheel>() {}.type
                    )
                    socketLuckyWheelHandler?.onChatSticker(messageGameLuckyWheel)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_REACTION_GAME.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_REACTION_GAME", it[0].toString())
                    val reactionLuckyWheelResponse = Gson().fromJson<ReactionLuckyWheelResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<ReactionLuckyWheelResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onReaction(reactionLuckyWheelResponse)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(TechResEnumGame.RES_NEXT_MONEY.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_NEXT_MONEY", it[0].toString())
                    val moneyResponse = Gson().fromJson<MoneyResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<MoneyResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onNextMoney(moneyResponse.money ?: 10)
                } catch (e: IOException) {
                }
            }.start()
        }
        mSocket?.on(String.format("%s/%s", TechResEnumGame.RES_CURRENT_MONEY.toString(), user.id)) {
            Thread {
                try {
                    WriteLog.d("RES_CURRENT_MONEY", it[0].toString())
                    val moneyResponse = Gson().fromJson<MoneyResponse>(
                        it[0].toString(),
                        object :
                            TypeToken<MoneyResponse>() {}.type
                    )
                    socketLuckyWheelHandler?.onCurrentMoney(moneyResponse.money ?: 10)
                } catch (e: IOException) {
                }
            }.start()
        }
    }

    fun offSocket(){
        mSocket?.off(TechResEnumGame.RES_LUCKY_NUMBER.toString())
        mSocket?.off(TechResEnumGame.RES_START.toString())
        mSocket?.off(TechResEnumGame.RES_STOP.toString())
        mSocket?.off(TechResEnumGame.RES_TOTAL_CUSTOMER_JOIN_ROOM.toString())
        mSocket?.off(TechResEnumGame.RES_CURRENT_LUCKY_WHEEL_TIME.toString())
        mSocket?.off(TechResEnumGame.RES_CHAT_TEXT_EMOJI.toString())
        mSocket?.off(TechResEnumGame.RES_CHAT_SICKER.toString())
        mSocket?.off(TechResEnumGame.RES_NEXT_MONEY.toString())
        mSocket?.off(String.format("%s/%s", TechResEnumGame.RES_CURRENT_MONEY.toString(), user.id))
        mSocket?.disconnect()
        isFirst = true
    }

    fun isConnectSocket() : Boolean{
        return mSocket?.connected() == true
    }

    fun registrySocket(context: Context){
        mSocket = application.getSocketGameInstance(context, restaurant(context).restaurant_id ?: 0)
        mSocket?.connect()
        if(isFirst){
            onSocket()
            isFirst = false
        }
    }

}