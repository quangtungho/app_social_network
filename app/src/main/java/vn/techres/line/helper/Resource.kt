package vn.techres.line.helper

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {

    companion object {

        fun <T> success(msg: String, data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, msg)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> unauthorized(msg: String, data: T?): Resource<T> {
            return Resource(Status.UNAUTHORIZED, data, msg)
        }

        fun <T> notFound(msg: String, data: T?): Resource<T> {
            return Resource(Status.NOT_FOUND, data, msg)
        }

        fun <T> tokenExpired(msg: String, data: T?): Resource<T> {
            return Resource(Status.TOKEN_EXPIRED, data, msg)
        }

        fun <T> internalServerError(msg: String, data: T?): Resource<T> {
            return Resource(Status.INTERNAL_SERVER_ERROR, data, msg)
        }

        fun <T> update(msg: String, data: T?): Resource<T> {
            return Resource(Status.UPDATE, data, msg)
        }

    }

}