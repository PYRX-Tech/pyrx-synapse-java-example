package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class TrackResponse {

    private final String eventId;
    private final String status;

    public TrackResponse(String eventId, String status) {
        this.eventId = eventId;
        this.status = status;
    }

    public String getEventId() { return eventId; }
    public String getStatus() { return status; }

    public static TrackResponse fromMap(Map<String, Object> map) {
        return new TrackResponse(
            JsonUtil.stringFromMap(map, "event_id"),
            JsonUtil.stringFromMap(map, "status")
        );
    }
}
