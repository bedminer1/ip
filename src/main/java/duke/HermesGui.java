package duke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** Displays Hermes in a compact, resizable desktop chat window. */
public class HermesGui {
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color HERMES_COLOR = new Color(32, 56, 85);
    private static final Color USER_COLOR = new Color(27, 110, 166);
    private static final Color ERROR_COLOR = new Color(181, 45, 45);

    private final JTextPane conversation = new JTextPane();
    private final JTextField input = new JTextField();
    private final PipedOutputStream commandStream = new PipedOutputStream();

    /** Starts the graphical interface. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HermesGui().show());
    }

    /** Creates and displays the application window. */
    public void show() {
        JFrame frame = new JFrame("Hermes — Your Task Messenger");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(440, 360));
        frame.setSize(620, 560);
        frame.setLocationByPlatform(true);

        conversation.setEditable(false);
        conversation.setBackground(BACKGROUND_COLOR);
        conversation.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        conversation.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(USER_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        JPanel composer = new JPanel(new BorderLayout(8, 0));
        composer.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        composer.add(input, BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);

        Runnable sendCommand = this::sendCommand;
        input.addActionListener(event -> sendCommand.run());
        sendButton.addActionListener(event -> sendCommand.run());

        frame.add(new JScrollPane(conversation), BorderLayout.CENTER);
        frame.add(composer, BorderLayout.SOUTH);
        frame.setVisible(true);
        input.requestFocusInWindow();
        startChatbot();
    }

    /** Sends the current input to Hermes and displays it as a user message. */
    private void sendCommand() {
        String command = input.getText().trim();
        if (command.isEmpty()) {
            return;
        }
        appendMessage("You  ›  " + command + "\n", USER_COLOR, true);
        input.setText("");
        try {
            commandStream.write((command + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            commandStream.flush();
        } catch (IOException exception) {
            appendMessage("Hermes  ›  Unable to read that message.\n", ERROR_COLOR, false);
        }
    }

    /** Connects the existing command-line chatbot to the graphical interface. */
    private void startChatbot() {
        try {
            PipedInputStream chatbotInput = new PipedInputStream(commandStream);
            System.setIn(chatbotInput);
            System.setOut(new PrintStream(new ConversationOutputStream(), true, StandardCharsets.UTF_8));
            Thread chatbotThread = new Thread(() -> HermesMini.main(new String[0]), "hermes-chatbot");
            chatbotThread.setDaemon(true);
            chatbotThread.start();
        } catch (IOException exception) {
            appendMessage("Hermes  ›  Unable to start the conversation.\n", ERROR_COLOR, false);
        }
    }

    /** Appends styled text and keeps the newest message visible. */
    private void appendMessage(String text, Color color, boolean isUser) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument document = conversation.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);
            StyleConstants.setBold(style, isUser);
            StyleConstants.setAlignment(style, isUser ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            try {
                int start = document.getLength();
                document.insertString(start, text, style);
                document.setParagraphAttributes(start, text.length(), style, false);
                conversation.setCaretPosition(document.getLength());
            } catch (BadLocationException exception) {
                throw new IllegalStateException("Unable to update conversation", exception);
            }
        });
    }

    /** Redirects each complete chatbot output line to the conversation pane. */
    private class ConversationOutputStream extends OutputStream {
        private final StringBuilder line = new StringBuilder();

        @Override
        public void write(int value) {
            char character = (char) value;
            line.append(character);
            if (character == '\n') {
                String message = line.toString();
                Color color = message.contains("DELIVERY FAILED") ? ERROR_COLOR : HERMES_COLOR;
                appendMessage(message, color, false);
                line.setLength(0);
            }
        }
    }
}
