package vilij.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;

/**
 * This class provides the template for displaying error messages to the end user of a Vilij application. It always
 * provides the two controls: (i) a customizable error message, and (ii) a single button to close the dialog.
 *
 * @author Ritwik Banerjee
 */
public class ErrorDialog extends Stage implements Dialog {

    private static ErrorDialog dialog;

    private Label errorMessage = new Label();

    private ErrorDialog() {/* empty constructor */ }

    public static ErrorDialog getDialog() {
        if (dialog == null)
            dialog = new ErrorDialog();
        return dialog;
    }

    private void setErrorMessage(String message) {
        this.errorMessage.setText(message);
    }

    /**
     * Completely initializes the error dialog to be used.
     *
     * @param owner the window on top of which the error dialog window will be displayed
     */
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        PropertyManager manager     = PropertyManager.getManager();
        Button          closeButton = new Button(manager.getPropertyValue(PropertyTypes.CLOSE_LABEL.name()));
        VBox            messagePane = new VBox();

        closeButton.setOnAction(e -> this.close());
        messagePane.setAlignment(Pos.CENTER);
        messagePane.getChildren().add(errorMessage);
        messagePane.getChildren().add(closeButton);
        messagePane.setPadding(new Insets(80, 60, 80, 60));
        messagePane.setSpacing(20);

        Scene messageScene = new Scene(messagePane);
        this.setScene(messageScene);
    }

    /**
     * Loads the specified title and message into the error dialog and then displays the dialog.
     *
     * @param errorDialogTitle the specified error dialog title
     * @param errorMessage     the specified error message
     */
    @Override
    public void show(String errorDialogTitle, String errorMessage) {
        setTitle(errorDialogTitle);    // set the title of the dialog
        setErrorMessage(errorMessage); // set the main error message
        showAndWait();                 // open the dialog and wait for the user to click the close button
    }
}
