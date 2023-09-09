package vn.techres.line.helper

enum class Status {
    UPDATE,
    SUCCESS,
    ERROR,
    LOADING,
    UNAUTHORIZED, //400
    NOT_FOUND, //404
    TOKEN_EXPIRED, // 410
    DEVICE_NOT_FOUND, // 412
    INTERNAL_SERVER_ERROR, //500
}