package vn.techres.line.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import vn.techres.line.helper.WriteLog
import java.util.concurrent.CopyOnWriteArrayList

class LifecycleHandler : Application.ActivityLifecycleCallbacks {
    private var foreground = false
    private val listeners = CopyOnWriteArrayList<Listener>()
    private val handler = Handler(Looper.myLooper()!!)
    private var check: Runnable? = null
    private val delay: Long = 500

    companion object{
        private var lifecycleHandler : LifecycleHandler? = null
        val instance: LifecycleHandler
            get() {
                if (lifecycleHandler == null) lifecycleHandler = LifecycleHandler()
                return lifecycleHandler!!
            }
    }

    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(instance)
    }

    interface Listener {
        fun onBecameForeground(activity: Activity)
        fun onBecameBackground(activity: Activity)
    }

    fun get(): LifecycleHandler {
        return instance
    }

    fun addListener(listener: Listener?) {
        listeners.add(listener)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        val wasBackground = !foreground
        foreground = true

        if (check != null) handler.removeCallbacks(check!!)

        if (wasBackground) {
            WriteLog.i("TAG", "went foreground")
            for (l in listeners) {
                try {
                    l.onBecameForeground(activity)
                } catch (exc: java.lang.Exception) {
                    WriteLog.e("TAG", "Listener threw exception!$exc")
                }
            }
        } else {
            //comment
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        val paused = true

        if (check != null) handler.removeCallbacks(check!!)

        handler.postDelayed(Runnable {
            if (foreground && paused) {
                foreground = false
                WriteLog.i("TAG", "went background")
                for (l in listeners) {
                    try {
                        l.onBecameBackground(activity)
                    } catch (exc: Exception) {
                        WriteLog.e("TAG", "Listener threw exception!$exc")
                    }
                }
            } else {
                WriteLog.i("TAG", "still foreground")
            }
        }.also { check = it }, delay)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        val paused = true

        if (check != null) handler.removeCallbacks(check!!)

        handler.postDelayed(Runnable {
            if (foreground && paused) {
                foreground = false
                Log.i("TAG", "close")
                for (l in listeners) {
                    try {
                        l.onBecameBackground(activity)
                    } catch (exc: Exception) {
                        Log.e("TAG", "Listener threw exception!", exc)
                    }
                }
            } else {
                Log.i("TAG", "still foreground")
            }
        }.also { check = it }, delay)
    }

}