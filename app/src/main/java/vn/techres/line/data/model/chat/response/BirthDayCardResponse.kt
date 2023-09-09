package vn.techres.line.data.model.chat.response

import vn.techres.line.data.model.chat.BirthDayCard
import vn.techres.line.data.model.response.BaseResponse

class BirthDayCardResponse: BaseResponse() {
    var data = ArrayList<BirthDayCard>()
}