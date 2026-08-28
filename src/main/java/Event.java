import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A task with a specified start and end date or time. */
public class Event extends Task {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

    /** User-provided event start text. */
    private final LocalDateTime from;

    /** User-provided event end text. */
    private final LocalDateTime to;

    /** Creates an event task. */
    public Event(String description, LocalDateTime from, LocalDateTime to) {
        super(description, TaskType.EVENT);
        this.from = from;
        this.to = to;
    }

    @Override
    /** Returns the event task type icon. */
    public String getTypeIcon() {
        return "E";
    }

    @Override
    /** Returns the description and formatted event interval. */
    public String getDisplayText() {
        return getDescription() + " (from: " + from.format(DISPLAY_FORMAT)
                + " to: " + to.format(DISPLAY_FORMAT) + ")";
    }

    @Override
    /** Serializes this event for persistent storage. */
    public String toSaveString() {
        return "E | " + getSaveStatus() + " | " + getDescription()
                + " | " + from + " -> " + to;
    }
}
