package vn.techres.line.data.model.chat

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
class SocketMemberLeaveGroup() : Serializable {
   @JsonField(name = ["receiver_id"])
   var receiver_id : String = ""
   @JsonField(name = ["conversation_type"])
   var conversation_type : String = ""
   @JsonField(name = ["message_type"])
   var message_type : String = ""
   @JsonField(name = ["random_key"])
   var random_key : String = ""
   @JsonField(name = ["user_id"])
   var user_id : Int = 0
   @JsonField(name = ["sender"])
   var sender = Sender()
}