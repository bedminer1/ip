import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** A task that must be completed by a specified date or time. */
public class Deadline extends Task {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mma");

    /** The user-provided deadline text. */
    private final LocalDateTime by;

    /** Creates a deadline task. */
    public Deadline(String description, LocalDateTime by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    @Override
    /** Returns the deadline task type icon. */
    public String getTypeIcon() {
        return "D";
    }

    @Override
    /** Returns the description and formatted deadline. */
    public String getDisplayText() {
        return getDescription() + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }

    @Override
    /** Serializes this deadline for persistent storage. */
    public String toSaveString() {
        return "D | " + getSaveStatus() + " | " + getDescription() + " | " + by;
    }
}
