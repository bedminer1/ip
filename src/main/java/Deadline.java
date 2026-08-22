/** A task that must be completed by a specified date or time. */
public class Deadline extends Task {

    /** The user-provided deadline text. */
    private final String by;

    /** Creates a deadline task. */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    @Override
    public String getDisplayText() {
        return getDescription() + " (by: " + by + ")";
    }
}
