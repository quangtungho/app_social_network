package vn.techres.line.videocall;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class SocketCallEmitDataEnum {

    public static String REVOKE_PINNED_MESSAGE() {
        return "revoke-pinned-message";
    }

    public static String CALL_CONNECT() {
        return "call-connect";
    }

    public static String CALL_CANCEL() {
        return "call-cancel";
    }

    public static String CALL_REJECT() {
        return "call-reject";
    }

    public static String CALL_ACCEPT() {
        return "call-accept";
    }

    public static String CALL_COMPLETE() {
        return "call-complete";
    }

    public static String CALL_NO_ANSWER() {
        return "call-no-answer";
    }

    public static String CHANGE_CALL() {
        return "change-call";
    }

    public static String CONNECT_TO_CAMERA() {
        return "connect-to-camera";
    }

    public static String TURN_ON_OFF_CAMERA() {
        return "turn-on-off-camera";
    }

    public static String TURN_ON_OFF_MIC() {
        return "turn-on-off-mic";
    }

    public static String MAKE_ANSWER() {
        return "make-answer";
    }

    public static String MAKE_OFFER() {
        return "make-offer";
    }

    public static String ICE_CANDIDATE() {
        return "ice-candidate";
    }
}
