package vn.techres.line.data.model.contact

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.*
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
class CallLogData() : Serializable {
    @JsonField(name = ["id"])
    var id: Int? = 0

    @JsonField(name = ["call_log_id"])
    var call_log_id: Long? = 0L

    @JsonField(name = ["full_name"])
    var full_name: String? = ""

    @JsonField(name = ["phone"])
    var phone: String? = ""

    @JsonField(name = ["avatar"])
    var avatar: String? = ""

    @JsonField(name = ["type"])
    var type: String? = ""

    @JsonField(name = ["call_day_time"])
    var call_day_time: Date? = Date()

    @JsonField(name = ["call_duration"])
    var call_duration: String? = ""

    @JsonField(name = ["color"])
    var color: Int = 0

    @JsonField(name = ["isCheck"])
    var isCheck: Boolean = false

    constructor(id: Int?, call_log_id: Long?, full_name: String?, phone: String?, avatar: String?, type: String?, call_day_time: Date?,
                call_duration: String?, color: Int) :this(){
        this.id = id
        this.call_log_id = call_log_id
        this.full_name = full_name
        this.phone = phone
        this.avatar = avatar
        this.type = type
        this.call_day_time = call_day_time
        this.call_duration = call_duration
        this.color = color
    }
}