package vn.techres.line.call.helper;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.LinkedList;

import timber.log.Timber;
import vn.techres.line.activity.TechResApplication;
import vn.techres.line.data.model.utils.User;
import vn.techres.line.helper.CurrentUser;
import vn.techres.line.helper.WriteLog;
import vn.techres.line.helper.utils.ChatUtils;
import vn.techres.line.videocall.SocketCallEmitDataEnum;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */
public class KurentoRTCClient implements AppRTCClient {

    private final SignalingEvents events;
    private User user = CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext());

    public KurentoRTCClient(SignalingEvents events) {
        this.events = events;
    }

    @Override
    public void connectToRoom(RoomConnectionParameters connectionParameters) {
        SignalingParameters parameters = new SignalingParameters(
                new LinkedList<PeerConnection.IceServer>() {
                    {
                        add(new PeerConnection.IceServer("stun:stun.aloapp.vn:3478", "kelvin", "12345"));
                        add(new PeerConnection.IceServer("turn:turn.aloapp.vn:3478", "kelvin", "12345"));
                    }
                }, true, null, null, null, null, null);
        events.onConnectedToRoom(parameters);
    }

    @Override
    public void sendAnswer(String answer) {
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.ANSWER, answer);
        events.onRemoteDescription(sdp);
    }

    @Override
    public void sendOfferSdp(SessionDescription sdp, String groupID, String keyRoom, int idCreate, String message_type) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("member_id", user.getId());
            obj.put("group_id", groupID);
            obj.put("key_room", keyRoom);
            obj.put("call_member_create", idCreate);
            obj.put("message_type", message_type);
            obj.put("offer", sdp.description);

            ChatUtils.INSTANCE.emitSocket(SocketCallEmitDataEnum.MAKE_OFFER(), obj);
            WriteLog.INSTANCE.d("MAKE_OFFER", obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAnswerSdp(SessionDescription sdp, String groupID, String keyRoom, int idCreate, String message_type) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("member_id", user.getId());
            obj.put("group_id", groupID);
            obj.put("key_room", keyRoom);
            obj.put("call_member_create", idCreate);
            obj.put("message_type", message_type);
            obj.put("answer", sdp.description);

            ChatUtils.INSTANCE.emitSocket(SocketCallEmitDataEnum.MAKE_ANSWER(), obj);
            WriteLog.INSTANCE.d("MAKE_ANSWER", obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidate(IceCandidate iceCandidate, String groupID, String keyRoom, int idCreate, String message_type) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("member_id", user.getId());
            obj.put("group_id", groupID);
            obj.put("key_room", keyRoom);
            obj.put("call_member_create", idCreate);
            obj.put("message_type", message_type);

            JSONObject candidate = new JSONObject();
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            obj.put("candidate", candidate);

            events.onRemoteIceCandidate(iceCandidate);

            ChatUtils.INSTANCE.emitSocket(SocketCallEmitDataEnum.ICE_CANDIDATE(), obj);
            WriteLog.INSTANCE.d("LOG_ICE_CANDIDATE", iceCandidate.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {
        Timber.e("sendLocalIceCandidateRemovals: ");
    }

    @Override
    public void disconnectFromRoom() {
        Timber.e("disconnectFromRoom: ");
    }
}
