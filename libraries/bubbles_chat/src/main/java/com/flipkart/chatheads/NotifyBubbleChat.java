package com.flipkart.chatheads;

import java.io.Serializable;

public class NotifyBubbleChat implements Serializable {
    public NotifyBubbleChat(String value, String avatar, String full_name, String badgeCount, String type_message, String conversation_type, Integer type) {
        this.value = value;
        this.avatar = avatar;
        this.full_name = full_name;
        this.badgeCount = badgeCount;
        this.type_message = type_message;
        this.conversation_type = conversation_type;
        this.type = type;
    }

    String value;
    String avatar;
    String full_name;
    String badgeCount;
    String type_message;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(String badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getType_message() {
        return type_message;
    }

    public void setType_message(String type_message) {
        this.type_message = type_message;
    }

    public String getConversation_type() {
        return conversation_type;
    }

    public void setConversation_type(String conversation_type) {
        this.conversation_type = conversation_type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    String conversation_type;
    Integer type;
}
