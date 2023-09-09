package vn.techres.line.services

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class Authenticator: Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // This is a synchronous call
        val updatedToken = getNewToken()
        return response.request.newBuilder()
            .header("Authorization", updatedToken)
            .build()


    }

    private fun getNewToken(): String {
        HashMap<String, String>()
        //        val authTokenResponse = ApiClient.userApiService.getAuthenticationToken(requestParams).execute().body()!!
//
//        val newToken = "${authTokenResponse.tokenType} ${authTokenResponse.accessToken}"
//        SharedPreferenceUtils.saveString(Constants.PreferenceKeys.USER_ACCESS_TOKEN, newToken)
        return ""
    }

}