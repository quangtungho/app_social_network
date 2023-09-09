package vn.techres.line.interfaces.contact

import vn.techres.line.data.model.contact.CallLogData

interface RecentCallsHandler {
    fun onChoose(callLogData: CallLogData)
    fun onRemove(callLogData: CallLogData)
}