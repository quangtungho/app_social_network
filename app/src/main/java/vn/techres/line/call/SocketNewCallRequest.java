package vn.techres.line.call;

import vn.techres.line.activity.TechResApplication;
import vn.techres.line.helper.CurrentUser;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/2/2022
 */
public class SocketNewCallRequest {
    private int member_id = CurrentUser.INSTANCE.getCurrentUser(TechResApplication.Companion.applicationContext()).getId();
    private String group_id;
    private String key_room;
    private String message_type;
    private int call_member_create;
    private String call_time;

    public SocketNewCallRequest(String group_id, String key_room, String message_type, int call_member_create, String call_time) {
        this.group_id = group_id;
        this.key_room = key_room;
        this.call_member_create = call_member_create;
        this.message_type = message_type;
        this.call_time = call_time;
    }

    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getKey_room() {
        return key_room;
    }

    public void setKey_room(String key_room) {
        this.key_room = key_room;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public int getCall_member_create() {
        return call_member_create;
    }

    public void setCall_member_create(int call_member_create) {
        this.call_member_create = call_member_create;
    }

    public SocketNewCallRequest(String group_id) {
        this.group_id = group_id;
    }

    public String getCall_time() {
        return call_time;
    }

    public void setCall_time(String call_time) {
        this.call_time = call_time;
    }

    public SocketNewCallRequest(String group_id, String key_room) {
        this.group_id = group_id;
        this.key_room = key_room;
    }
}
