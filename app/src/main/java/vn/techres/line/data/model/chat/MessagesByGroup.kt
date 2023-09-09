package vn.techres.line.data.model.chat

import androidx.room.*
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.giphy.sdk.core.models.Media
import vn.techres.line.data.local.message.GiphyDBConvert
import vn.techres.line.data.local.message.ListSenderDBConvert
import vn.techres.line.data.model.chat.response.TypingUser
import java.io.Serializable

@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "messageTable")
class MessagesByGroup : Serializable {

    @PrimaryKey(autoGenerate = true)
    var uid : Int? = null

    @ColumnInfo(name = "_id")
    @JsonField(name = ["_id"])
    var _id: String? = ""

    @ColumnInfo(name = "receiver_id")
    @JsonField(name = ["receiver_id"])
    var receiver_id: String? = ""

    @ColumnInfo(name = "message_type")
    @JsonField(name = ["message_type"])
    var message_type: String? = ""

    @ColumnInfo(name = "status")
    @JsonField(name = ["status"])
    var status: Int? = 0

    @ColumnInfo(name = "today")
    @JsonField(name = ["today"])
    var today: Int? = 0

    @ColumnInfo(name = "sender")
    @JsonField(name = ["sender"])
    @TypeConverters(Sender::class)
    var sender = Sender()

    @ColumnInfo(name = "message_reply")
    @JsonField(name = ["message_reply"])
    @TypeConverters(Reply::class)
    var message_reply : Reply? = null

    @ColumnInfo(name = "message_pinned")
    @JsonField(name = ["message_pinned"])
    @TypeConverters(Pinned::class)
    var message_pinned : Pinned? = null

    @ColumnInfo(name = "message_complete_bill")
    @JsonField(name = ["message_complete_bill"])
    @TypeConverters(MessageCompleteBill::class)
    var message_complete_bill : MessageCompleteBill? = null

    @ColumnInfo(name = "message_gift")
    @JsonField(name = ["message_gift"])
    @TypeConverters(MessageGiftNotification::class)
    var message_gift : MessageGiftNotification? = null

    @ColumnInfo(name = "message_information")
    @JsonField(name = ["message_information"])
    @TypeConverters(MessageGiftNotification::class)
    var message_information : MessageGiftNotification? = null

    @ColumnInfo(name = "message_gettings")
    @JsonField(name = ["message_gettings"])
    @TypeConverters(MessageSettings::class)
    var message_gettings : MessageSettings? = null

    @ColumnInfo(name = "message_share")
    @JsonField(name = ["message_share"])
    @TypeConverters(ShareMessage::class)
    var message_share : ShareMessage? = null

    @ColumnInfo(name = "message_phone")
    @JsonField(name = ["message_phone"])
    @TypeConverters(PhoneMessage::class)
    var message_phone : PhoneMessage? = null

    @ColumnInfo(name = "message_link")
    @JsonField(name = ["message_link"])
    @TypeConverters(LinkMessage::class)
    var message_link : LinkMessage? = null

    @ColumnInfo(name = "message_vote")
    @JsonField(name = ["message_vote"])
    @TypeConverters(Vote::class)
    var message_vote : Vote? = null

    @ColumnInfo(name = "message_video_call")
    @JsonField(name = ["message_video_call"])
    @TypeConverters(MessageVideoCall::class)
    var message_video_call : MessageVideoCall? = null

    @ColumnInfo(name = "message_event")
    @JsonField(name = ["message_event"])
    @TypeConverters(MessageEvent::class)
    var message_event : MessageEvent = MessageEvent()

    @ColumnInfo(name = "message")
    @JsonField(name = ["message"])
    var message: String? = ""

    @ColumnInfo(name = "reactions")
    @JsonField(name = ["reactions"])
    @TypeConverters(Reaction::class)
    var reactions = Reaction()

    @ColumnInfo(name = "list_member")
    @JsonField(name = ["list_member"])
    @TypeConverters(ListSenderDBConvert::class)
    var list_member : ArrayList<Sender> = ArrayList()

    @ColumnInfo(name = "created_at")
    @JsonField(name = ["created_at"])
    var created_at: String? = ""

    @ColumnInfo(name = "files")
    @JsonField(name = ["files"])
    @TypeConverters(FileNodeJs::class)
    var files : ArrayList<FileNodeJs> = ArrayList()

    @ColumnInfo(name = "is_local")
    @JsonField(name = ["is_local"])
    var local: Int? = 0

    @JsonField(name = ["giphy"])
    @TypeConverters(GiphyDBConvert::class)
    var giphy : Media? = null

    @ColumnInfo(name = "random_key")
    @JsonField(name = ["random_key"])
    var random_key: String? = ""

    @ColumnInfo(name = "key_message")
    @JsonField(name = ["key_message"])
    var key_message: String? = ""

    @ColumnInfo(name = "group_name")
    @JsonField(name = ["group_name"])
    var group_name: String? = ""

    @ColumnInfo(name = "avatar")
    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @ColumnInfo(name = "member_id")
    @JsonField(name = ["member_id"])
    var member_id: Int? = 0

    @ColumnInfo(name = "background")
    @JsonField(name = ["background"])
    var background: String? = ""

    @ColumnInfo(name = "progress_number")
    @JsonField(name = ["progress_number"])
    var progress_number: Int? = 0

    @ColumnInfo(name = "typing_off")
    @JsonField(name = ["typing_off"])
    @TypeConverters(TypingUser::class)
    var typing_off = TypingUser()

    @ColumnInfo(name = "interval_of_time")
    @JsonField(name = ["interval_of_time"])
    var interval_of_time: String? = ""

    @ColumnInfo(name = "list_tag_name")
    @JsonField(name = ["list_tag_name"])
    @TypeConverters(TagName::class)
    var list_tag_name : ArrayList<TagName> = ArrayList()

    @ColumnInfo(name = "message_viewed")
    @JsonField(name = ["message_viewed"])
    @TypeConverters(MessageViewer::class)
    var message_viewed  : ArrayList<MessageViewer> = ArrayList()

    @ColumnInfo(name = "list_sticker")
    @JsonField(name = ["list_sticker"])
    @TypeConverters(Sticker::class)
    var list_sticker  : ArrayList<Sticker>?= null

    @Ignore
    @ColumnInfo(name = "stroke")
    @JsonField(name = ["stroke"])
    var is_stroke : Int? = 0

    @ColumnInfo(name = "user_id")
    @JsonField(name = ["user_id"])
    var user_id = 0

    @ColumnInfo(name = "group_id")
    @JsonField(name = ["group_id"])
    var group_id : Int? = 0

    @ColumnInfo(name = "key_message_error")
    @JsonField(name = ["key_message_error"])
    var key_message_error : String = ""

    @ColumnInfo(name = "offline")
    @JsonField(name = ["offline"])
    var offline = 0

    //    @ColumnInfo(name = "is_playing")
//    @JsonField(name = ["is_playing"])
//    var is_playing: Boolean = false

}
