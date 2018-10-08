package ut.ee.xtorrent.tracker;

public enum Event {

    START,
    STOP,
    COMPLETE,
    REPEATING;

    public static Event getEvent(String eventName) {
        if ("start".equalsIgnoreCase(eventName)) {
            return START;
        } else if ("stop".equalsIgnoreCase(eventName)) {
            return STOP;
        } else if ("complete".equalsIgnoreCase(eventName)) {
            return COMPLETE;
        } else if ("repeating".equalsIgnoreCase(eventName)) {
            return REPEATING;
        }


        throw new IllegalArgumentException("Unrecognized event: " + eventName);
    }

}
