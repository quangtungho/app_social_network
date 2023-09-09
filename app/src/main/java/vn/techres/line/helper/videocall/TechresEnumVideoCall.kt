package vn.techres.line.helper.videocall

enum class TechResEnumVideoCall {
    NOTIFY_DATA {
        override fun toString(): String {
            return "NOTIFY_DATA"
        }
    },
    GROUP_DATA {
        override fun toString(): String {
            return "GROUP_DATA"
        }
    },
    IS_CONNECT {
        override fun toString(): String {
            return "IS_CONNECT"
        }
    },
    TIME_CALLING {
        override fun toString(): String {
            return "TIME_CALLING"
        }
    },

    LOCAL_TRACK_ID {
        override fun toString(): String {
            return "local_track"
        }
    },
    LOCAL_STREAM_ID {
        override fun toString(): String {
            return "local_track"
        }
    },
    LOCAL_TRACK_AUDIO_ID {
        override fun toString(): String {
            return "local_track_audio"
        }
    },
    VIDEO_CODEC_VP8 {
        override fun toString(): String {
            return "VP8"
        }
    },

    VIDEO_CODEC_VP9 {
        override fun toString(): String {
            return "VP9"
        }
    },

    VIDEO_CODEC_H264 {
        override fun toString(): String {
            return "H264"
        }
    },

    VIDEO_CODEC_H264_BASELINE {
        override fun toString(): String {
            return "H264 Baseline"
        }
    },
    VIDEO_CODEC_H264_HIGH {
        override fun toString(): String {
            return "H264 High"
        }
    },
    AUDIO_CODEC_OPUS {
        override fun toString(): String {
            return "opus"
        }
    },
    AUDIO_CODEC_ISAC {
        override fun toString(): String {
            return "ISAC"
        }
    },
    VIDEO_CODEC_PARAM_START_BITRATE {
        override fun toString(): String {
            return "x-google-start-bitrate"
        }
    },
    VIDEO_FLEXFEC_FIELDTRIAL {
        override fun toString(): String {
            return "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/"
        }
    },
    VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL {
        override fun toString(): String {
            return "WebRTC-IntelVP8/Enabled/"
        }
    },
    DISABLE_WEBRTC_AGC_FIELDTRIAL {
        override fun toString(): String {
            return "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/"
        }
    },
    AUDIO_CODEC_PARAM_BITRATE {
        override fun toString(): String {
            return "maxaveragebitrate"
        }
    },
    AUDIO_ECHO_CANCELLATION_CONSTRAINT {
        override fun toString(): String {
            return "googEchoCancellation"
        }
    },
    AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT {
        override fun toString(): String {
            return "googAutoGainControl"
        }
    },
    AUDIO_HIGH_PASS_FILTER_CONSTRAINT {
        override fun toString(): String {
            return "googHighpassFilter"
        }
    },
    AUDIO_NOISE_SUPPRESSION_CONSTRAINT {
        override fun toString(): String {
            return "googNoiseSuppression"
        }
    },
    DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT {
        override fun toString(): String {
            return "DtlsSrtpKeyAgreement"
        }
    },
    SOCKET_LIBRARY {
        override fun toString(): String {
            return "SOCKET_LIBRARY"
        }
    },
    CURRENT_ID_LIBRARY {
        override fun toString(): String {
            return "CURRENT_ID_LIBRARY"
        }
    },
    MEMBER_ID_LIBRARY {
        override fun toString(): String {
            return "MEMBER_ID_LIBRARY"
        }
    },
    MEMBER_AVATAR_LIBRARY {
        override fun toString(): String {
            return "MEMBER_AVATAR_LIBRARY"
        }
    },
    MEMBER_NAME_LIBRARY {
        override fun toString(): String {
            return "MEMBER_NAME_LIBRARY"
        }
    },
    GROUP_ID_LIBRARY {
        override fun toString(): String {
            return "GROUP_ID_LIBRARY"
        }
    },
    TYPE_OPTION {
        override fun toString(): String {
            return "TYPE_OPTION"
        }
    },

    ROOM_ID {
        override fun toString(): String {
            return "ROOM_ID"
        }
    },

    /**
     * type
     * */
    CONVERSATION_TYPE {
        override fun toString(): String {
            return "CONVERSATION_TYPE"
        }
    },
    TYPE_CHOOSE_CALL {
        override fun toString(): String {
            return "TYPE_CHOOSE_CALL"
        }
    },
    TYPE_CALL_PHONE {
        override fun toString(): String {
            return "call_audio"
        }
    },
    TYPE_CALL_VIDEO {
        override fun toString(): String {
            return "call_video"
        }
    },

    /**
     * Notify
     * */
    ID_SERVICE {
        override fun toString(): String {
            return "ID_SERVICE"
        }
    },
    STATUS_SERVICE {
        override fun toString(): String {
            return "STATUS_SERVICE"
        }
    },

    /**
     * Type notify
     * */
    TYPE_NOTIFY_PERSONAL_VIDEO_CALL {
        override fun toString(): String {
            return "TYPE_NOTIFY_VIDEO_CALL"
        }
    },
    TYPE_PERSONAL_VIDEO_CALL {
        override fun toString(): String {
            return "TYPE_PERSONAL_VIDEO_CALL"
        }
    },

    /**
     * Notify call status
     * */
    CALL_NO_ANSWER {
        override fun toString(): String {
            return "call_no_answer"
        }
    },
    MISS_CALL {
        override fun toString(): String {
            return "miss_call"
        }
    },
    CALL_COMPLETE {
        override fun toString(): String {
            return "call_complete"
        }
    },
    CHANGE_CALL {
        override fun toString(): String {
            return "change_call"
        }
    },
    CALL_CONNECT {
        override fun toString(): String {
            return "call_connect"
        }
    },
    CALL_REJECT {
        override fun toString(): String {
            return "call_reject"
        }
    },
    CALL_CANCEL {
        override fun toString(): String {
            return "call_cancel"
        }
    },
    CALL_BUSY {
        override fun toString(): String {
            return "call_busy"
        }
    },

    /**
     * cache
     * */
    CACHE_CALL {
        override fun toString(): String {
            return "CACHE_CALL"
        }
    },
    CACHE_CALL_VIDEO {
        override fun toString(): String {
            return "CACHE_CALL_VIDEO"
        }
    },
    CACHE_CALL_PHONE {
        override fun toString(): String {
            return "CACHE_CALL_PHONE"
        }
    },
    EXTRA_IS_INCOMING_CALL {
        override fun toString(): String {
            return "EXTRA_IS_INCOMING_CALL"
        }
    },
    IS_MIC_CALL {
        override fun toString(): String {
            return "IS_MIC_CALL"
        }
    },
    IS_OUT_GOING {
        override fun toString(): String {
            return "IS_OUT_GOING"
        }
    },
    AUDIO_ONLY {
        override fun toString(): String {
            return "AUDIO_ONLY"
        }
    },
    IS_FLOATING_VIEW {
        override fun toString(): String {
            return "IS_FLOATING_VIEW"
        }
    },
    CHANGE_CAMERA {
        override fun toString(): String {
            return "CHANGE_CAMERA"
        }
    },
    CACHE_OFFER {
        override fun toString(): String {
            return "CACHE_OFFER"
        }
    },
    TARGET_ID {
        override fun toString(): String {
            return "TARGET_ID"
        }
    },
    EXTRA_FROM_FLOATING_VIEW {
        override fun toString(): String {
            return "EXTRA_FROM_FLOATING_VIEW"
        }
    },
    EXTRA_MO {
        override fun toString(): String {
            return "EXTRA_MO"
        }
    },
    EXTRA_AUDIO_ONLY {
        override fun toString(): String {
            return "EXTRA_AUDIO_ONLY"
        }
    },
    EXTRA_USER_NAME {
        override fun toString(): String {
            return "EXTRA_USER_NAME"
        }
    },
}