/** Represents a task and whether it has been completed. */
public class Task {

    /** The text describing this task. */
    private final String description;

    /** Whether this task has been marked as done. */
    private boolean isDone;

    /** Creates a task that is initially not done. */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as done. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as not done. */
    public void markAsNotDone() {
        isDone = false;
    }

    /** Returns {@code X} for a done task or a space otherwise. */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Returns the task description. */
    public String getDescription() {
        return description;
    }
}
