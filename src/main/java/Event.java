/** A task with a specified start and end date or time. */
public class Event extends Task {

    /** User-provided event start text. */
    private final String from;

    /** User-provided event end text. */
    private final String to;

    /** Creates an event task. */
    public Event(String description, String from, String to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    public String getTypeIcon() {
        return "E";
    }

    @Override
    public String getDisplayText() {
        return getDescription() + " (from: " + from + " to: " + to + ")";
    }

    @Override
    public String toSaveString() {
        return "E | " + getSaveStatus() + " | " + getDescription()
                + " | " + from + " -> " + to;
    }
}
