import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** JavaFX front end for Hermes Mini. */
public class Main extends Application {

    private final HermesMini chatbot = new HermesMini();
    private final VBox dialogContainer = new VBox(10);
    private final TextField userInput = new TextField();
    private final Button sendButton = new Button("Send");

    /** Creates and displays the chatbot window. */
    @Override
    public void start(Stage stage) {
        ScrollPane scrollPane = new ScrollPane(dialogContainer);
        scrollPane.setFitToWidth(true);
        dialogContainer.setPadding(new Insets(12));
        dialogContainer.heightProperty().addListener(
                observable -> scrollPane.setVvalue(1.0));

        userInput.setPromptText("Enter a command");
        HBox.setHgrow(userInput, Priority.ALWAYS);
        HBox inputRow = new HBox(8, userInput, sendButton);
        inputRow.setPadding(new Insets(10));

        userInput.setOnAction(event -> handleUserInput());
        sendButton.setOnAction(event -> handleUserInput());

        addMessage("Hermes", "Hello! I'm Hermes Mini. What can I do for you?");

        VBox root = new VBox(scrollPane, inputRow);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        stage.setTitle("Hermes Mini");
        stage.setScene(new Scene(root, 520, 640));
        stage.setMinWidth(400);
        stage.setMinHeight(480);
        stage.show();
        userInput.requestFocus();
    }

    /** Sends nonblank input to the chatbot and displays both messages. */
    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        userInput.clear();
        addMessage("You", input);
        Reply reply = chatbot.respond(input);
        addMessage("Hermes", reply.text());

        if (reply.shouldExit()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
        } else {
            userInput.requestFocus();
        }
    }

    private void addMessage(String sender, String text) {
        Label message = new Label(sender + ": " + text);
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        dialogContainer.getChildren().add(message);
    }
}
