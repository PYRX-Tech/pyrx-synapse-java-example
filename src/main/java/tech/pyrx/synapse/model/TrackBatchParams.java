package tech.pyrx.synapse.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrackBatchParams {

    private final List<TrackParams> events;

    public TrackBatchParams(List<TrackParams> events) {
        this.events = events;
    }

    public List<TrackParams> getEvents() { return events; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Object> eventList = new ArrayList<>();
        for (TrackParams event : events) {
            eventList.add(event.toMap());
        }
        map.put("events", eventList);
        return map;
    }
}
