package vn.techres.line.data.model.request

import com.bluelinelabs.logansquare.annotation.JsonField

/**
 * @Author: Phạm Văn Nhân
 * @Date: 11/09/2022
 */
class PostReportRequest {
    @JsonField(name = ["branch_review_id"])
    var branch_review_id: String? = ""

    @JsonField(name = ["content"])
    var content: String? = ""

    @JsonField(name = ["customer_id"])
    var customer_id: Int? = 0
}