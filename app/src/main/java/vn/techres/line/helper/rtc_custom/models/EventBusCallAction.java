package vn.techres.line.helper.rtc_custom.models;

public class EventBusCallAction {
    public EventBusCallAction(String type_action) {
        this.type_action = type_action;
    }

    String type_action = "";

    public String getType_action() {
        return type_action;
    }

    public void setType_action(String type_action) {
        this.type_action = type_action;
    }
}
