package vn.techres.line.data.model.network

object GroupBaseUrl {
    fun getGroups(page: Int, limit: Int)
    : String = String.format("%s%s%s%s", "/api/groups?limit=", limit, "&page=", page)

    fun getFriendOnline(page: Int, limit: Int)
    : String = String.format("%s%s%s%s", "/api/friends/online?limit=", limit, "&page=", page)
}