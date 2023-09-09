package vn.techres.line.videocall;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */

import vn.techres.line.activity.TechResApplication;
import vn.techres.line.helper.CurrentUser;
public class SocketCallOnDataEnum {

    public static String RES_CALL_CONNECT() {
        return String.format("%s%s", "res-call-connect/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CALL_CANCEL() {
        return String.format("%s%s", "res-call-cancel/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CALL_ACCEPT() {
        return String.format("%s%s", "res-call-accept/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CALL_REJECT() {
        return String.format("%s%s", "res-call-reject/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CALL_COMPLETE() {
        return String.format("%s%s", "res-call-complete/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CALL_NO_ANSWER() {
        return String.format("%s%s", "res-call-no-answer/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CONNECT_TO_CAMERA() {
        return String.format("%s%s", "res-connect-to-camera/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_ICE_CANDIDATE() {
        return String.format("%s%s", "res-ice-candidate/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_MAKE_ANSWER() {
        return String.format("%s%s", "res-make-answer/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_MAKE_OFFER() {
        return String.format("%s%s", "res-make-offer/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_TURN_ON_OFF_CAMERA() {
        return String.format("%s%s", "res-turn-on-off-camera/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_TURN_ON_OFF_MIC() {
        return String.format("%s%s", "res-turn-on-off-mic/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_BUSY_USER() {
        return String.format("%s%s", "res-busy-user/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CLOSE_CALL() {
        return String.format("%s%s", "res-close-call/", CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId());
    }

    public static String RES_CHAT_VIDEO_CALL() {
        return "res-chat-video-call";
    }

    public static String RES_CHAT_AUDIO_CALL() {
        return "res-chat-audio-call";
    }
}
