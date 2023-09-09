package vn.techres.line.data.local.message

import io.reactivex.Observable
import vn.techres.line.data.model.chat.GroupsResponse
import vn.techres.line.data.model.chat.params.GroupParams
import vn.techres.line.services.TechResService
import javax.inject.Inject

class GroupClient @Inject constructor(private val techResService: TechResService){

    suspend fun getGroups(
        params: GroupParams
    ): Observable<GroupsResponse> =
        techResService.getGroups(params)
}