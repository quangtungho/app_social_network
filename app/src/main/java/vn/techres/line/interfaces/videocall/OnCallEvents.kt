package vn.techres.line.interfaces.videocall

import org.webrtc.RendererCommon.ScalingType

interface OnCallEvents {
    fun onAcceptCall(boolean: Boolean)
    fun onRejectChangeCall(option : Int)
    fun onVisibleBackground()
    fun onCallHangUp()
    fun onChangeAudioDevice(audio: Boolean?)
    fun onCameraSwitch()
    fun onVideoScalingSwitch(scalingType: ScalingType?)
    fun onCaptureFormatChange(width: Int, height: Int, framerate: Int)
    fun onToggleMic(): Boolean
    fun onChangeTypeCall()
    fun onTurnCamera(boolean: Boolean)
    fun onSplitScreen()
}