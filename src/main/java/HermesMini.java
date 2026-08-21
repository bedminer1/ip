import java.util.Scanner;

/**
 * HermesMini is a command-line chatbot that keeps track of the user's tasks.
 *
 * <p>This is the application entry point. It greets the user, then echoes each
 * command entered until the user types {@code bye}, which prints a farewell and
 * exits. Task-management commands arrive in later levels of the project.
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

    /**
     * Greets the user, then echoes each command until {@code bye} is entered.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printGreeting();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                printMessage("Bye. Hope to see you again soon!");
                break;
            }
            printMessage(command);
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
}
