package vilij.components;

import javafx.stage.Stage;

/**
 * This interface defines the minimal behavioral requirements of pop-up dialogs used by the Vilij framework.
 *
 * @author Ritwik Banerjee
 */
public interface Dialog {

    enum DialogType {
        ERROR, CONFIRMATION
    }

    void show(String title, String message);

    void init(Stage owner);

}
