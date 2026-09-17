package duke;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * HermesMini is a command-line chatbot that keeps track of the user's tasks.
 *
 * <p>This is the application entry point. It greets the user, stores each
 * command entered, and lists the stored tasks on request, until the user types
 * {@code bye}, which prints a farewell and exits.
 */
public class HermesMini {

    /** User interface responsible for displaying all chatbot output. */
    private static final Ui UI = new Ui();

    /** Storage responsible for persisting the task list. */
    private static final Storage STORAGE = new Storage(Task.getDataFile());
    private static final Parser PARSER = new Parser();

    /**
     * Greets the user, then stores and lists tasks until {@code bye} is entered.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        UI.showGreeting();

        TaskList taskList;
        try {
            taskList = new TaskList(STORAGE.load());
        } catch (IOException | DateTimeParseException exception) {
            UI.showError("I couldn't load the saved tasks.");
            taskList = new TaskList();
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            if (command.equals("bye")) {
                UI.showMessage("Bye. Hope to see you again soon!");
                break;
            }
            if (command.equals("list")) {
                UI.showTaskList(taskList.getTasks());
            } else if (command.startsWith("mark ")) {
                markTask(taskList, command.substring(5), true);
            } else if (command.startsWith("unmark ")) {
                markTask(taskList, command.substring(7), false);
            } else if (command.startsWith("delete ")) {
                deleteTask(taskList, command.substring(7));
            } else if (command.startsWith("find ")) {
                findTasks(taskList, command.substring(5).trim());
            } else if (command.equals("todo") || command.startsWith("todo ")) {
                String description = command.length() == 4 ? ""
                        : command.substring(5).trim();
                if (description.isEmpty()) {
                    UI.showError("A todo needs a description.");
                } else {
                    addTask(taskList, new Todo(description));
                }
            } else if (command.startsWith("deadline ")) {
                String[] parts = command.substring(9).split(" /by ", 2);
                if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    UI.showError("A deadline needs a description and a /by date or time.");
                } else {
                    LocalDateTime date = parseDate(parts[1].trim());
                    if (date != null) {
                        addTask(taskList, new Deadline(parts[0].trim(), date));
                    }
                }
            } else if (command.startsWith("event ")) {
                String[] parts = command.substring(6).split(" /from ", 2);
                if (parts.length < 2) {
                    UI.showError("An event needs a description, /from time, and /to time.");
                } else {
                    String[] end = parts[1].split(" /to ", 2);
                    if (end.length < 2 || parts[0].trim().isEmpty()
                            || end[0].trim().isEmpty() || end[1].trim().isEmpty()) {
                        UI.showError("An event needs a description, /from time, and /to time.");
                    } else {
                        LocalDateTime from = parseDate(end[0].trim());
                        LocalDateTime to = parseDate(end[1].trim());
                        if (from != null && to != null) {
                            addTask(taskList, new Event(parts[0].trim(), from, to));
                        }
                    }
                }
            } else {
                UI.showError("I don't recognise that command. Try todo, deadline, event, list, mark, or unmark.");
            }
        }
        scanner.close();
    }

    /** Prints tasks whose descriptions contain the supplied keyword. */
    private static void findTasks(TaskList taskList, String keyword) {
        if (keyword.isEmpty()) {
            UI.showError("Please provide a keyword to search for.");
            return;
        }
        UI.showMatchingTasks(taskList.getTasks(), keyword);
    }

    /** Marks or unmarks a task after validating the user-provided number. */
    private static void markTask(TaskList taskList, String numberText, boolean done) {
        try {
            int number = Integer.parseInt(numberText.trim());
            if (!taskList.contains(number)) {
                UI.showError("That task number is not in your list.");
                return;
            }
            Task task = taskList.get(number);
            if (done) {
                task.markAsDone();
                UI.showMarkedTask(task);
            } else {
                task.markAsNotDone();
                UI.showUnmarkedTask(task);
            }
            saveTasks(taskList);
        } catch (NumberFormatException exception) {
            UI.showError("Please provide a valid task number.");
        }
    }

    /** Deletes a task after validating the user-provided number. */
    private static void deleteTask(TaskList taskList, String numberText) {
        try {
            int number = Integer.parseInt(numberText.trim());
            if (!taskList.contains(number)) {
                UI.showError("That task number is not in your list.");
                return;
            }
            Task task = taskList.remove(number);
            saveTasks(taskList);
            UI.showDeletedTask(task, taskList.size());
        } catch (NumberFormatException exception) {
            UI.showError("Please provide a valid task number.");
        }
    }

    /** Parses the supported date/time format and reports invalid values. */
    private static LocalDateTime parseDate(String text) {
        try {
            return PARSER.parseDate(text);
        } catch (DateTimeParseException exception) {
            UI.showError("Use dates and times like 2019-12-02 18:00.");
            return null;
        }
    }

    /** Stores and reports a newly created task. */
    private static void addTask(TaskList taskList, Task task) {
        if (!taskList.add(task)) {
            UI.showError("Your task list is full.");
            return;
        }
        saveTasks(taskList);
        UI.showAddedTask(task, taskList.size());
    }

    /** Saves all tasks, creating the data directory when necessary. */
    private static void saveTasks(TaskList taskList) {
        try {
            STORAGE.save(taskList.getTasks());
        } catch (IOException exception) {
            UI.showError("I couldn't save the tasks.");
        }
    }
}
