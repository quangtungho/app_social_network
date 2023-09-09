package vn.techres.line.data.model.chat.params

import vn.techres.line.data.model.chat.request.group.GroupRequest
import vn.techres.line.data.model.request.BaseRequest

class GroupParams(request : GroupRequest) : BaseRequest() {
    var params = request
}