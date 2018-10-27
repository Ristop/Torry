package ut.ee.torry.tracker;

public enum Event {

    START,
    STOP,
    COMPLETE,
    PERIODIC;

    public static Event getEvent(String eventName) {
        if ("start".equalsIgnoreCase(eventName)) {
            return START;
        } else if ("stop".equalsIgnoreCase(eventName)) {
            return STOP;
        } else if ("complete".equalsIgnoreCase(eventName)) {
            return COMPLETE;
        } else if ("repeating".equalsIgnoreCase(eventName)) {
            return PERIODIC;
        }

        throw new IllegalArgumentException("Unrecognized event: " + eventName);
    }

}
