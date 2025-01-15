package dto;

import util.EventType;

import java.io.Serializable;

public class Request implements Serializable {
    private EventType event;
    private Object payload;

    public Request(EventType event, Object payload) {
        this.event = event;
        this.payload = payload;
    }

    public EventType getEvent() {
        return event;
    }

    public Object getPayload() {
        return payload;
    }
}
