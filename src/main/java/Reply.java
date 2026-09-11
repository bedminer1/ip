/** The text and control signal produced by one chatbot command. */
public record Reply(String text, boolean shouldExit) {
}
