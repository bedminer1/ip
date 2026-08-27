import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    

    /**
     * Greets the user, then stores and lists tasks until {@code bye} is entered.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        printGreeting();

        List<Task> tasks = new ArrayList<>();
        loadTasks(tasks);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                printMessage("Bye. Hope to see you again soon!");
                break;
            }
            if (command.equals("list")) {
                printTaskList(tasks);
            } else if (command.startsWith("mark ")) {
                markTask(tasks, command.substring(5), true);
            } else if (command.startsWith("unmark ")) {
                markTask(tasks, command.substring(7), false);
            } else if (command.startsWith("delete ")) {
                deleteTask(tasks, command.substring(7));
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                String description = command.length() == 4 ? ""
                        : command.substring(5).trim();
                if (description.isEmpty()) {
                    printError("A todo needs a description.");
                } else {
                    addTask(tasks, new Todo(description));
                }
            } else if (command.startsWith("deadline ")) {
                String[] parts = command.substring(9).split(" /by ", 2);
                if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    printError("A deadline needs a description and a /by date or time.");
                } else {
                    LocalDateTime date = parseDate(parts[1].trim());
                    if (date != null) {
                        addTask(tasks, new Deadline(parts[0].trim(), date));
                    }
                }
            } else if (command.startsWith("event ")) {
                String[] parts = command.substring(6).split(" /from ", 2);
                if (parts.length < 2) {
                    printError("An event needs a description, /from time, and /to time.");
                } else {
                    String[] end = parts[1].split(" /to ", 2);
                    if (end.length < 2 || parts[0].trim().isEmpty()
                            || end[0].trim().isEmpty() || end[1].trim().isEmpty()) {
                        printError("An event needs a description, /from time, and /to time.");
                    } else {
                        LocalDateTime from = parseDate(end[0].trim());
                        LocalDateTime to = parseDate(end[1].trim());
                        if (from != null && to != null) {
                            addTask(tasks, new Event(parts[0].trim(), from, to));
                        }
                    }
                }
            } else {
                printError("I don't recognise that command. Try todo, deadline, event, list, mark, or unmark.");
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
    private static void printTaskList(List<Task> tasks) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println(INDENT + (i + 1) + ".[" + task.getTypeIcon() + "]["
                    + task.getStatusIcon() + "] " + task.getDisplayText());
        }
        System.out.println(DIVIDER);
    }

    /** Prints the confirmation shown after marking a task as done. */
    private static void printMarkedTask(Task task) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "Nice! I've marked this task as done:");
        System.out.println(INDENT + "  [X] " + task.getDescription());
        System.out.println(DIVIDER);
    }

    /** Prints the confirmation shown after marking a task as not done. */
    private static void printUnmarkedTask(Task task) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "OK, I've marked this task as not done yet:");
        System.out.println(INDENT + "  [ ] " + task.getDescription());
        System.out.println(DIVIDER);
    }

    /** Marks or unmarks a task after validating the user-provided number. */
    private static void markTask(List<Task> tasks, String numberText, boolean done) {
        try {
            int number = Integer.parseInt(numberText.trim());
            if (number < 1 || number > tasks.size()) {
                printError("That task number is not in your list.");
                return;
            }
            Task task = tasks.get(number - 1);
            if (done) {
                task.markAsDone();
                printMarkedTask(task);
            } else {
                task.markAsNotDone();
                printUnmarkedTask(task);
            }
            saveTasks(tasks);
        } catch (NumberFormatException exception) {
            printError("Please provide a valid task number.");
        }
    }

    /** Deletes a task after validating the user-provided number. */
    private static void deleteTask(List<Task> tasks, String numberText) {
        try {
            int number = Integer.parseInt(numberText.trim());
            if (number < 1 || number > tasks.size()) {
                printError("That task number is not in your list.");
                return;
            }
            Task task = tasks.remove(number - 1);
            saveTasks(tasks);
            System.out.println(DIVIDER);
            System.out.println(INDENT + "Noted. I've removed this task:");
            System.out.println(INDENT + "  [" + task.getTypeIcon() + "]["
                    + task.getStatusIcon() + "] " + task.getDisplayText());
            System.out.println(INDENT + "Now you have " + tasks.size()
                    + " tasks in the list.");
            System.out.println(DIVIDER);
        } catch (NumberFormatException exception) {
            printError("Please provide a valid task number.");
        }
    }

    /** Prints a framed error without terminating the chatbot. */
    private static void printError(String message) {
        System.out.println(DIVIDER);
        System.out.println(INDENT + "OOPS!!! " + message);
        System.out.println(DIVIDER);
    }

    /** Parses the supported date/time format and reports invalid values. */
    private static LocalDateTime parseDate(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            printError("Use dates and times like 2019-12-02 18:00.");
            return null;
        }
    }

    /** Stores and reports a newly created task. */
    private static void addTask(List<Task> tasks, Task task) {
        if (tasks.size() >= MAX_TASKS) {
            printError("Your task list is full.");
            return;
        }
        tasks.add(task);
        saveTasks(tasks);
        System.out.println(DIVIDER);
        System.out.println(INDENT + "Got it. I've added this task:");
        System.out.println(INDENT + "  [" + task.getTypeIcon() + "][ ] "
                + task.getDisplayText());
        System.out.println();
        System.out.println(INDENT + "Now you have " + tasks.size()
                + " tasks in the list.");
        System.out.println(DIVIDER);
    }

    /** Loads valid saved tasks, leaving the list empty when the file is absent. */
    private static void loadTasks(List<Task> tasks) {
        Path file = Task.getDataFile();
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split(" \\| ", -1);
                Task task = null;
                if (parts.length == 3 && parts[0].equals("T")) {
                    task = new Todo(parts[2]);
                } else if (parts.length == 4 && parts[0].equals("D")) {
                    task = new Deadline(parts[2], LocalDateTime.parse(parts[3]));
                } else if (parts.length == 4 && parts[0].equals("E")) {
                    String[] times = parts[3].split(" -> ", 2);
                    if (times.length == 2) {
                        task = new Event(parts[2], LocalDateTime.parse(times[0]),
                                LocalDateTime.parse(times[1]));
                    }
                }
                if (task != null && (parts[1].equals("0") || parts[1].equals("1"))) {
                    if (parts[1].equals("1")) {
                        task.markAsDone();
                    }
                    tasks.add(task);
                }
            }
        } catch (IOException | DateTimeParseException exception) {
            printError("I couldn't load the saved tasks.");
        }
    }

    /** Saves all tasks, creating the data directory when necessary. */
    private static void saveTasks(List<Task> tasks) {
        try {
            Path file = Task.getDataFile();
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(task.toSaveString());
            }
            Files.write(file, lines);
        } catch (IOException exception) {
            printError("I couldn't save the tasks.");
        }
    }
}
