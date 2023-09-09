package vn.techres.line.call.utils;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */

import android.os.Build;

import timber.log.Timber;

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 */
public final class AppRTCUtils {
    private AppRTCUtils() {}

    /** Helper method which throws an exception  when an assertion has failed. */
    public static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    /** Helper method for building a string of thread information.*/
    public static String getThreadInfo() {
        return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
                + "]";
    }

    /** Information about the current build, taken from system properties. */
    public static void logDeviceInfo(String  tag) {
        Timber.d("Android SDK: " + Build.VERSION.SDK_INT + ", "
                + "Release: " + Build.VERSION.RELEASE + ", "
                + "Brand: " + Build.BRAND + ", "
                + "Device: " + Build.DEVICE + ", "
                + "Id: " + Build.ID + ", "
                + "Hardware: " + Build.HARDWARE + ", "
                + "Manufacturer: " + Build.MANUFACTURER + ", "
                + "Model: " + Build.MODEL + ", "
                + "Product: " + Build.PRODUCT);
    }
}
