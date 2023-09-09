package vn.techres.line.helper.rtc

import org.webrtc.Logging
import org.webrtc.VideoFrame
import org.webrtc.VideoSink

class ProxyVideoSink : VideoSink {
    private var target: VideoSink? = null
    override fun onFrame(p0: VideoFrame?) {
        if (target == null) {
            Logging.d(
                "ProxyVideoSink",
                "Dropping frame in proxy because target is null."
            )
            return
        }
        target?.onFrame(p0)
    }

    @Synchronized
    fun setTarget(target: VideoSink?) {
        this.target = target
    }
}