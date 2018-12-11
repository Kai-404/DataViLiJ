package vilij.components;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This interface defines the minimal functionality of the graphical user interface of a Vilij application.
 *
 * @author Ritwik Banerjee
 */
public interface UIComponent {

    /**
     * Accessor method to get the application's window, which is the primary stage within which the full application GUI
     * is placed.
     *
     * @return the application's primary stage
     */
    Stage getPrimaryWindow();

    /**
     * Accessor method to get the scene graph of the application's main window.
     *
     * @return the application's scene graph
     */
    Scene getPrimaryScene();

    /** @return the application title */
    String getTitle();

    /** Method to enforce the initialization of the user interface styles for any Vilij application. */
    void initialize();

    /** Method to clear any existing visualization on the graphical user interface front-end. */
    void clear();
}
