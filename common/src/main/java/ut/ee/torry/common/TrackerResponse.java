package ut.ee.torry.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class TrackerResponse {

    @JsonProperty
    private String failureReason;

    @JsonProperty
    private String warningMessage;

    @JsonProperty
    private Integer interval;

    @JsonProperty
    private Integer minInterval;

    @JsonProperty
    private String trackerId;

    @JsonProperty
    private Long complete;

    @JsonProperty
    private Long incomplete;

    @JsonProperty
    private Set<Peer> peers;

    public TrackerResponse() {
    }

    private TrackerResponse(TrackerResponseBuilder trb) {
        this.failureReason = trb.getFailureReason();
        this.warningMessage = trb.getWarningMessage();
        this.interval = trb.getInterval();
        this.minInterval = trb.getMinInterval();
        this.trackerId = trb.getTrackerId();
        this.complete = trb.getComplete();
        this.incomplete = trb.getIncomplete();
        this.peers = trb.getPeers();
    }

    public Optional<String> getFailureReason() {
        return failureReason != null ? Optional.of(failureReason) : Optional.empty();
    }

    public Optional<String> getWarningMessage() {
        return warningMessage != null ? Optional.of(warningMessage) : Optional.empty();
    }

    public Integer getInterval() {
        return interval;
    }

    public Optional<Integer> getMinInterval() {
        return minInterval != null ? Optional.of(minInterval) : Optional.empty();
    }

    public String getTrackerId() {
        return trackerId;
    }

    public Long getComplete() {
        return complete;
    }

    public Long getIncomplete() {
        return incomplete;
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public void setMinInterval(Integer minInterval) {
        this.minInterval = minInterval;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public void setComplete(Long complete) {
        this.complete = complete;
    }

    public void setIncomplete(Long incomplete) {
        this.incomplete = incomplete;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
    }

    public static TrackerResponseBuilder builder() {
        return new TrackerResponseBuilder();
    }

    public static class TrackerResponseBuilder {

        private String failureReason;
        private String warningMessage;
        private Integer interval;
        private Integer minInterval;
        private String trackerId;
        private Long complete;
        private Long incomplete;
        private Set<Peer> peers;

        public TrackerResponseBuilder setFailureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public TrackerResponseBuilder setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
            return this;
        }

        public TrackerResponseBuilder setInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public TrackerResponseBuilder setMinInterval(int minInterval) {
            this.minInterval = minInterval;
            return this;
        }

        public TrackerResponseBuilder setTrackerId(String trackerId) {
            this.trackerId = trackerId;
            return this;
        }

        public TrackerResponseBuilder setComplete(long complete) {
            this.complete = complete;
            return this;
        }

        public TrackerResponseBuilder setIncomplete(long incomplete) {
            this.incomplete = incomplete;
            return this;
        }

        public TrackerResponseBuilder setPeers(Set<Peer> peers) {
            this.peers = peers;
            return this;
        }

        public TrackerResponse build() {
            return new TrackerResponse(this);
        }

        public String getFailureReason() {
            return failureReason;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public Integer getInterval() {
            return interval;
        }

        public Integer getMinInterval() {
            return minInterval;
        }

        public String getTrackerId() {
            return trackerId;
        }

        public Long getComplete() {
            return complete;
        }

        public Long getIncomplete() {
            return incomplete;
        }

        public Set<Peer> getPeers() {
            return peers;
        }
    }

}
