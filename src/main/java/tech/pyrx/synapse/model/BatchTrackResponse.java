package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class BatchTrackResponse {

    private final int accepted;
    private final int rejected;

    public BatchTrackResponse(int accepted, int rejected) {
        this.accepted = accepted;
        this.rejected = rejected;
    }

    public int getAccepted() { return accepted; }
    public int getRejected() { return rejected; }

    public static BatchTrackResponse fromMap(Map<String, Object> map) {
        return new BatchTrackResponse(
            JsonUtil.intFromMap(map, "accepted"),
            JsonUtil.intFromMap(map, "rejected")
        );
    }
}
