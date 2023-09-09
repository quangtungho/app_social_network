package vn.techres.line.interfaces.permission

/**
 * request permission result listener
 */
interface PermissionResultListener {
    /**
     * Callback on permission denied
     */
    fun onPermissionRationaleShouldBeShown(namePermission : String, requestPermission: () -> Unit)

    /**
     * Callback on permission "Never show again" checked and denied
     */
    fun onPermissionPermanentlyDenied(namePermission : String, openAppSetting: () -> Unit)

    /**
     * Callback on permission granted
     */
    fun onPermissionGranted()
}