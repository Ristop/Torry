package ut.ee.xtorrent.tracker;

import java.util.Optional;
import java.util.Set;

public class TrackerResponse {

    private final String failureReason;
    private final String warningMessage;
    private final Integer interval;
    private final Integer minInterval;
    private final String trackerId;
    private final Long complete;
    private final Long incomplete;
    private final Set<Peer> peers;

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
