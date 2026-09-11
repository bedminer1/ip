import javafx.application.Application;

/** Starts JavaFX without requiring {@link Main} to be the executable class. */
public final class Launcher {

    private Launcher() {
    }

    /** Launches the Hermes Mini graphical interface. */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
