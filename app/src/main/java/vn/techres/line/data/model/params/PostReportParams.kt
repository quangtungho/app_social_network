package vn.techres.line.data.model.params

import com.bluelinelabs.logansquare.annotation.JsonObject
import vn.techres.line.data.model.request.BaseRequest
import vn.techres.line.data.model.request.PostReportRequest

/**
 * @Author: Phạm Văn Nhân
 * @Date: 11/09/2022
 */

@JsonObject
class PostReportParams : BaseRequest() {
    var params = PostReportRequest()
}