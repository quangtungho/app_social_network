package vn.techres.line.helper

import android.content.Context
import java.util.*
import java.io.IOException as IOException1

object AppConfig {
    const val SUCCESS_CODE = 200
    const val UNAUTHORIZED = 400
    const val NOT_FOUND = 404
    const val TOKEN_EXPIRED = 410
    const val INTERNAL_SERVER_ERROR = 500

    const val POST = 1
    const val GET = 0
    const val PROJECT_OAUTH = 8888
    const val PROJECT_ALOLINE = 8082
    const val PROJECT_CHAT = 1485
    private const val PROJECT_CHAT_BETA = 1485
    const val PROJECT_UPLOAD = 1488
    const val PROJECT_OAUTH_NODE = 9999
    const val PROJECT_LOG = 1487
    const val PROJECT_LUCKY_WHEEL = 3979


    val cacheManager: CacheManager = CacheManager.getInstance()

    val project_id = "net.aloline.vn"

    var client_secret = ""
        internal set

    var defaultToken = ""
        internal set

    var projectIdNode = "cHJvamVjdC51c2VyLmtleSZhYmMkXiYl"
        internal set

    var baseEndPoint = ""
        internal set

    var production_mode = 0
        internal set


    fun loadConfig(context: Context) {
        val resources = context.resources
        val assetManager = resources.assets

        try {
            val inputStream = assetManager.open("appconfig.properties")
            val properties = Properties()
            properties.load(inputStream)
            projectIdNode = properties.getProperty("PROJECT_ID_NODE")
//            project_id = properties.getProperty("PROJECT_ID")
            client_secret = properties.getProperty("CLIENT_SECRET")
            defaultToken = properties.getProperty("DEFAULT_TOKEN")
            baseEndPoint = properties.getProperty("BASE_ENDPOINT")
            production_mode = properties.getProperty("PRODUCTION_MODE").toInt()
        } catch (e: IOException1) {
            System.err.println("Failed to open property file")
            e.printStackTrace()
        }

    }

    fun  getProductionMode():Int{
        return production_mode
    }
    fun getProjectChat() : Int{
        return if(production_mode == 0){
            PROJECT_CHAT_BETA
        } else PROJECT_CHAT
    }
    fun getBaseURL(): String {
        return baseEndPoint
    }
}
