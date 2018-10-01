package ut.ee.xtorrent.tracker;

public enum Event {

    START,
    STOP,
    COMPLETE;

    public static Event getEvent(String eventName) {
        if ("start".equals(eventName)) {
            return START;
        } else if ("stop".equalsIgnoreCase(eventName)) {
            return STOP;
        } else if ("complete".equalsIgnoreCase(eventName)) {
            return COMPLETE;
        }

        throw new IllegalArgumentException("Unrecognized event: " + eventName);
    }

}
