package vn.techres.line.data.model.notification

import com.bluelinelabs.logansquare.annotation.JsonField
import vn.techres.line.data.model.Notifications

class NotificationData {
    @JsonField(name = ["limit"])
    var limit: Int? = 0

    @JsonField(name = ["list"])
    var list = ArrayList<Notifications>()

    @JsonField(name = ["total_record"])
    var total_record: Int? = 0
}