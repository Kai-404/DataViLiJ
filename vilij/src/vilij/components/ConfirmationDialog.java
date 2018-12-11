package vilij.components;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

/**
 * This class provides the template for displaying three-way confirmation messages to the end user of a Vilij
 * application. It always provides the two controls: (i) a customizable message, and (ii) a set of three buttons
 * corresponding to the three standard options for the user. The three options (<code>Yes</code>, <code>No</code>, and
 * <code>Cancel</code> are modeled as the inner enumerable class {@link ConfirmationDialog.Option}.
 *
 * @author Ritwik Banerjee
 */
public class ConfirmationDialog extends Stage implements Dialog {

    public enum Option {

        YES("Yes"), NO("No"), CANCEL("Cancel");

        @SuppressWarnings("unused")
        private String option;

        Option(String option) { this.option = option; }
    }

    private static ConfirmationDialog dialog;

    private Label confirmationMessage = new Label();
    private Option selectedOption;

    private ConfirmationDialog() { /* empty constructor */ }

    public static ConfirmationDialog getDialog() {
        if (dialog == null)
            dialog = new ConfirmationDialog();
        return dialog;
    }

    private void setConfirmationMessage(String message) { confirmationMessage.setText(message); }

    private void deleteOptionHistory()                  { selectedOption = null; }

    /**
     * Completely initializes the error dialog to be used.
     *
     * @param owner the window on top of which the error dialog window will be displayed
     */
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        List<Button> buttons = Arrays.asList(new Button(Option.YES.name()),
                                             new Button(Option.NO.name()),
                                             new Button(Option.CANCEL.name()));

        buttons.forEach(button -> button.setOnAction((ActionEvent event) -> {
            this.selectedOption = Option.valueOf(((Button) event.getSource()).getText());
            this.hide();
        }));

        HBox buttonBox = new HBox(5);
        buttonBox.getChildren().addAll(buttons);

        VBox messagePane = new VBox(confirmationMessage, buttonBox);
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }

    /**
     * Loads the specified title and message into the error dialog and then displays the dialog.
     *
     * @param dialogTitle the specified dialog title
     * @param message     the specified message
     */
    @Override
    public void show(String dialogTitle, String message) {
        deleteOptionHistory();           // delete any previously selected option
        setTitle(dialogTitle);           // set the title of the dialog
        setConfirmationMessage(message); // set the main error message
        showAndWait();                   // open the dialog and wait for the user to click the close button
    }


    public Option getSelectedOption() { return selectedOption; }
}
