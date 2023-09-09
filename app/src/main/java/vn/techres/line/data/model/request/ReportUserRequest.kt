package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

/**
 * @Author: Phạm Văn Nhân
 * @Date: 24/09/2022
 */
class ReportUserRequest {
    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0

    @JsonField(name = ["project_id"])
    var project_id: Int? = 0

    @JsonField(name = ["type"])
    var type: Int? = 0
}