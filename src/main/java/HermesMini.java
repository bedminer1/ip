import java.util.Scanner;

/**
 * HermesMini is a command-line chatbot that keeps track of the user's tasks.
 *
 * <p>This is the application entry point. It greets the user, stores each
 * command entered, and lists the stored tasks on request, until the user types
 * {@code bye}, which prints a farewell and exits.
 */
public class HermesMini {

    /** ASCII-art banner, pre-indented to align with message content. */
    private static final String BANNER = """
      _                                                   _       _
     | |__   ___ _ __ _ __ ___   ___  ___       _ __ ___ (_)_ __ (_)
     | '_ \\ / _ \\ '__| '_ ` _ \\ / _ \\/ __|_____| '_ ` _ \\| | '_ \\| |
     | | | |  __/ |  | | | | | |  __/\\__ \\_____| | | | | | | | | | |
     |_| |_|\\___|_|  |_| |_| |_|\\___||___/     |_| |_| |_|_|_| |_|_|
""";

    /** Indent applied to text inside a message frame. */
    private static final String INDENT = "     ";

    /** Horizontal rule that frames each chatbot message. */
    private static final String DIVIDER = "    ____________________________________________________________";

    /** Maximum number of tasks the list can hold (spec assumes at most 100). */
    private static final int MAX_TASKS = 100;

    /**
     * Greets the user, then stores and lists tasks until {@code bye} is entered.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printGreeting();

        String[] tasks = new String[MAX_TASKS];
        boolean[] completed = new boolean[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                printMessage("Bye. Hope to see you again soon!");
                break;
            }
            if (command.equals("list")) {
                printTaskList(tasks, completed, taskCount);
            } else if (command.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(command.substring(5));
                completed[taskNumber - 1] = true;
                printMarkedTask(tasks[taskNumber - 1]);
            } else if (command.startsWith("unmark ")) {
                int taskNumber = Integer.parseInt(command.substring(7));
                completed[taskNumber - 1] = false;
                printUnmarkedTask(tasks[taskNumber - 1]);
            } else {
                tasks[taskCount++] = command;
                printMessage("added: " + command);
            }
        }
        scanner.close();
    }

    /** Prints the banner and greeting framed by dividers. */
    private static void printGreeting() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(INDENT + "Hello! I'm hermes-mini");
        System.out.println(INDENT + "What can I do for you?");
        System.out.println(DIVIDER);
    }

    /** Prints a single-line message framed by dividers. */
    private static void printMessage(String message) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + message);
        System.out.println(DIVIDER);
    }

    /** Prints the stored tasks and their completion status. */
    private static void printTaskList(String[] tasks, boolean[] completed, int count) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "Here are the tasks in your list:");
        for (int i = 0; i < count; i++) {
            String marker = completed[i] ? "[X]" : "[ ]";
            System.out.println(INDENT + (i + 1) + "." + marker + " " + tasks[i]);
        }
        System.out.println(DIVIDER);
    }

    /** Prints the confirmation shown after marking a task as done. */
    private static void printMarkedTask(String task) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "Nice! I've marked this task as done:");
        System.out.println(INDENT + "  [X] " + task);
        System.out.println(DIVIDER);
    }

    /** Prints the confirmation shown after marking a task as not done. */
    private static void printUnmarkedTask(String task) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "OK, I've marked this task as not done yet:");
        System.out.println(INDENT + "  [ ] " + task);
        System.out.println(DIVIDER);
    }
}
