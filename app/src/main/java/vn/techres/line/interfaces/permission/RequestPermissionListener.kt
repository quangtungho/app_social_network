package vn.techres.line.interfaces.permission

/**
 * Callback on various cases on checking permission
 *
 * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
 * If permission is already granted, onPermissionGranted() would be called.
 *
 * 2.  Equal and Above M, if the permission is being asked first time onNeedPermission() would be called.
 *
 * 3.  Equal and Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
 * would be called.
 *
 * 4.  Equal and Above M, if the permission is disabled by device policy or the user checked "Never ask again"
 * check box on previous request permission, onPermissionDisabled() would be called.
 */
interface RequestPermissionListener {
    /**
     * first time asking multiple permissions
     * */
    fun onCallPermissionFirst(namePermission : String, requestPermission: () -> Unit)
    /**
     * Callback on permission previously denied
     * should show permission rationale and continue permission request
     */
    fun onPermissionRationaleShouldBeShown(namePermission : String, requestPermission: () -> Unit)

    /**
     * Callback on permission "Never show again" checked and denied
     * should show message and open app setting
     */
    fun onPermissionPermanentlyDenied(namePermission : String, openAppSetting: () -> Unit)

    /**
     * Callback on permission granted
     */
    fun onPermissionGranted()
}