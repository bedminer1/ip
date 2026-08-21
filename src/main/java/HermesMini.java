/**
 * HermesMini is a command-line chatbot that keeps track of the user's tasks.
 *
 * <p>This is the application entry point. At this level it only greets the
 * user and exits; the read-eval loop and task-management commands arrive in
 * later levels of the project.
 */
public class HermesMini {

    /** ASCII-art banner printed once at start-up. */
    private static final String BANNER = """
 _                                                   _       _
| |__   ___ _ __ _ __ ___   ___  ___       _ __ ___ (_)_ __ (_)
| '_ \\ / _ \\ '__| '_ ` _ \\ / _ \\/ __|_____| '_ ` _ \\| | '_ \\| |
| | | |  __/ |  | | | | | |  __/\\__ \\_____| | | | | | | | | | |
|_| |_|\\___|_|  |_| |_| |_|\\___||___/     |_| |_| |_|_|_| |_|_|
""";

    /** Horizontal rule that frames each chatbot message. */
    private static final String SEPARATOR =
            "____________________________________________________________";

    /**
     * Greets the user, then says goodbye and exits.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println(SEPARATOR);
        System.out.println(BANNER);
        System.out.println("Hello! I'm hermes-mini");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(SEPARATOR);
    }
}
