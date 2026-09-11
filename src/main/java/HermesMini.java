import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Core chatbot logic shared by the command-line and graphical interfaces. */
public class HermesMini {

    private static final String BANNER = """
      _                                                   _       _
     | |__   ___ _ __ _ __ ___   ___  ___       _ __ ___ (_)_ __ (_)
     | '_ \\ / _ \\ '__| '_ ` _ \\ / _ \\/ __|_____| '_ ` _ \\| | '_ \\| |
     | | | |  __/ |  | | | | | |  __/\\__ \\_____| | | | | | | | | | |
     |_| |_|\\___|_|  |_| |_| |_|\\___||___/     |_| |_| |_|_|_| |_|_|
""";
    private static final String INDENT = "     ";
    private static final String DIVIDER =
            "    ____________________________________________________________";
    private static final int MAX_TASKS = 100;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Task> tasks = new ArrayList<>();

    /** Creates a chatbot and loads its persisted tasks. */
    public HermesMini() {
        loadTasks();
    }

    /** Runs the terminal front end using the same response API as the GUI. */
    public static void main(String[] args) {
        HermesMini chatbot = new HermesMini();
        printGreeting();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                Reply reply = chatbot.respond(scanner.nextLine());
                printMessage(reply.text());
                if (reply.shouldExit()) {
                    break;
                }
            }
        }
    }

    /** Handles one command without depending on a particular user interface. */
    public Reply respond(String input) {
        if (input.equals("bye")) {
            return new Reply("Bye. Hope to see you again soon!", true);
        }
        if (input.equals("list")) {
            return reply(taskList());
        }
        if (input.startsWith("mark ")) {
            return reply(markTask(input.substring(5), true));
        }
        if (input.startsWith("unmark ")) {
            return reply(markTask(input.substring(7), false));
        }
        if (input.startsWith("delete ")) {
            return reply(deleteTask(input.substring(7)));
        }
        if (input.startsWith("find ")) {
            return reply(findTasks(input.substring(5).trim()));
        }
        if (input.equals("todo") || input.startsWith("todo ")) {
            String description = input.substring(4).trim();
            return description.isEmpty()
                    ? error("A todo needs a description.")
                    : reply(addTask(new Todo(description)));
        }
        if (input.startsWith("deadline ")) {
            return reply(addDeadline(input.substring(9)));
        }
        if (input.startsWith("event ")) {
            return reply(addEvent(input.substring(6)));
        }
        return error("I don't recognise that command. Try todo, deadline, event, "
                + "list, mark, or unmark.");
    }

    private String taskList() {
        return formatTasks("Here are the tasks in your list:", tasks);
    }

    private String findTasks(String keyword) {
        if (keyword.isEmpty()) {
            return "OOPS!!! Please provide a keyword to search for.";
        }
        List<String> matches = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                matches.add(formatTask(i, task));
            }
        }
        return joinLines("Here are the matching tasks:", matches);
    }

    private String markTask(String numberText, boolean done) {
        Integer index = parseTaskIndex(numberText);
        if (index == null) {
            return "OOPS!!! Please provide a valid task number.";
        }
        if (index < 0 || index >= tasks.size()) {
            return "OOPS!!! That task number is not in your list.";
        }

        Task task = tasks.get(index);
        if (done) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        String message = done
                ? "Nice! I've marked this task as done:\n  [X] " + task.getDescription()
                : "OK, I've marked this task as not done yet:\n  [ ] "
                        + task.getDescription();
        return withSaveStatus(message);
    }

    private String deleteTask(String numberText) {
        Integer index = parseTaskIndex(numberText);
        if (index == null) {
            return "OOPS!!! Please provide a valid task number.";
        }
        if (index < 0 || index >= tasks.size()) {
            return "OOPS!!! That task number is not in your list.";
        }

        Task task = tasks.remove((int) index);
        String message = "Noted. I've removed this task:\n  [" + task.getTypeIcon()
                + "][" + task.getStatusIcon() + "] " + task.getDisplayText()
                + "\nNow you have " + tasks.size() + " tasks in the list.";
        return withSaveStatus(message);
    }

    private String addDeadline(String body) {
        String[] parts = body.split(" /by ", 2);
        if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            return "OOPS!!! A deadline needs a description and a /by date or time.";
        }
        try {
            LocalDateTime by = LocalDateTime.parse(parts[1].trim(), DATE_TIME_FORMAT);
            return addTask(new Deadline(parts[0].trim(), by));
        } catch (DateTimeParseException exception) {
            return invalidDate();
        }
    }

    private String addEvent(String body) {
        String[] parts = body.split(" /from ", 2);
        if (parts.length < 2) {
            return invalidEvent();
        }
        String[] times = parts[1].split(" /to ", 2);
        if (times.length < 2 || parts[0].trim().isEmpty()
                || times[0].trim().isEmpty() || times[1].trim().isEmpty()) {
            return invalidEvent();
        }
        try {
            LocalDateTime from = LocalDateTime.parse(times[0].trim(), DATE_TIME_FORMAT);
            LocalDateTime to = LocalDateTime.parse(times[1].trim(), DATE_TIME_FORMAT);
            return addTask(new Event(parts[0].trim(), from, to));
        } catch (DateTimeParseException exception) {
            return invalidDate();
        }
    }

    private String addTask(Task task) {
        if (tasks.size() >= MAX_TASKS) {
            return "OOPS!!! Your task list is full.";
        }
        tasks.add(task);
        String message = "Got it. I've added this task:\n  [" + task.getTypeIcon()
                + "][ ] " + task.getDisplayText() + "\n\nNow you have "
                + tasks.size() + " tasks in the list.";
        return withSaveStatus(message);
    }

    private Integer parseTaskIndex(String numberText) {
        try {
            return Integer.parseInt(numberText.trim()) - 1;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String formatTasks(String heading, List<Task> taskList) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            lines.add(formatTask(i, taskList.get(i)));
        }
        return joinLines(heading, lines);
    }

    private String formatTask(int index, Task task) {
        return (index + 1) + ".[" + task.getTypeIcon() + "]["
                + task.getStatusIcon() + "] " + task.getDisplayText();
    }

    private String joinLines(String heading, List<String> lines) {
        return lines.isEmpty() ? heading : heading + "\n" + String.join("\n", lines);
    }

    private String withSaveStatus(String message) {
        return saveTasks() ? message : message + "\nOOPS!!! I couldn't save the tasks.";
    }

    private void loadTasks() {
        Path file = Task.getDataFile();
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split(" \\| ", -1);
                Task task = parseSavedTask(parts);
                if (task != null && (parts[1].equals("0") || parts[1].equals("1"))) {
                    if (parts[1].equals("1")) {
                        task.markAsDone();
                    }
                    tasks.add(task);
                }
            }
        } catch (IOException | DateTimeParseException exception) {
            tasks.clear();
        }
    }

    private Task parseSavedTask(String[] parts) {
        if (parts.length == 3 && parts[0].equals("T")) {
            return new Todo(parts[2]);
        }
        if (parts.length == 4 && parts[0].equals("D")) {
            return new Deadline(parts[2], LocalDateTime.parse(parts[3]));
        }
        if (parts.length == 4 && parts[0].equals("E")) {
            String[] times = parts[3].split(" -> ", 2);
            if (times.length == 2) {
                return new Event(parts[2], LocalDateTime.parse(times[0]),
                        LocalDateTime.parse(times[1]));
            }
        }
        return null;
    }

    private boolean saveTasks() {
        try {
            Path file = Task.getDataFile();
            Files.createDirectories(file.getParent());
            Files.write(file, tasks.stream().map(Task::toSaveString).toList());
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static Reply reply(String text) {
        return new Reply(text, false);
    }

    private static Reply error(String text) {
        return reply("OOPS!!! " + text);
    }

    private static String invalidEvent() {
        return "OOPS!!! An event needs a description, /from time, and /to time.";
    }

    private static String invalidDate() {
        return "OOPS!!! Use dates and times like 2019-12-02 18:00.";
    }

    private static void printGreeting() {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(INDENT + "Hello! I'm hermes-mini");
        System.out.println(INDENT + "What can I do for you?");
        System.out.println(DIVIDER);
    }

    private static void printMessage(String message) {
        System.out.println(DIVIDER);
        message.lines().forEach(line -> System.out.println(INDENT + line));
        System.out.println(DIVIDER);
    }
}
