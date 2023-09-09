package vn.techres.line.data.model.chat.videocall

class PeerConnectionParameters(){
    var videoCallEnabled = false
    var loopback = false
    var tracing = false
    var videoWidth = 0
    var videoHeight = 0
    var videoFps = 0
    var videoMaxBitrate = 0
    var videoCodec: String? = null
    var videoCodecHwAcceleration = false
    var videoFlexfecEnabled = false
    var audioStartBitrate = 0
    var audioCodec: String? = null
    var noAudioProcessing = false
    var aecDump = false
    var useOpenSLES = false
    var disableBuiltInAEC = false
    var disableBuiltInAGC = false
    var disableBuiltInNS = false
    var enableLevelControl = false
    var disableWebRtcAGCAndHPF = false
    private var dataChannelParameters: DataChannelParameters? = null
    constructor(
        videoCallEnabled: Boolean, loopback: Boolean, tracing: Boolean,
        videoWidth: Int, videoHeight: Int, videoFps: Int, videoMaxBitrate: Int, videoCodec: String?,
        videoCodecHwAcceleration: Boolean, videoFlexfecEnabled: Boolean, audioStartBitrate: Int,
        audioCodec: String?, noAudioProcessing: Boolean, aecDump: Boolean, useOpenSLES: Boolean,
        disableBuiltInAEC: Boolean, disableBuiltInAGC: Boolean, disableBuiltInNS: Boolean,
        enableLevelControl: Boolean, disableWebRtcAGCAndHPF: Boolean,
        dataChannelParameters: DataChannelParameters?
    ) : this() {
        this.videoCallEnabled = videoCallEnabled
        this.loopback = loopback
        this.tracing = tracing
        this.videoWidth = videoWidth
        this.videoHeight = videoHeight
        this.videoFps = videoFps
        this.videoMaxBitrate = videoMaxBitrate
        this.videoCodec = videoCodec
        this.videoFlexfecEnabled = videoFlexfecEnabled
        this.videoCodecHwAcceleration = videoCodecHwAcceleration
        this.audioStartBitrate = audioStartBitrate
        this.audioCodec = audioCodec
        this.noAudioProcessing = noAudioProcessing
        this.aecDump = aecDump
        this.useOpenSLES = useOpenSLES
        this.disableBuiltInAEC = disableBuiltInAEC
        this.disableBuiltInAGC = disableBuiltInAGC
        this.disableBuiltInNS = disableBuiltInNS
        this.enableLevelControl = enableLevelControl
        this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF
        this.dataChannelParameters = dataChannelParameters
    }
}