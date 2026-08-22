/** The supported categories of tasks. */
public enum TaskType {
    TODO("T"),
    DEADLINE("D"),
    EVENT("E");

    private final String icon;

    TaskType(String icon) {
        this.icon = icon;
    }

    /** Returns the one-letter icon used when displaying this type. */
    public String getIcon() {
        return icon;
    }
}
