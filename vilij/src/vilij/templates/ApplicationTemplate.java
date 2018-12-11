package vilij.templates;

import javafx.application.Application;
import javafx.stage.Stage;
import vilij.components.*;
import vilij.propertymanager.PropertyManager;
import vilij.settings.InitializationParams;
import xmlutil.InvalidXMLFileFormatException;

import static vilij.settings.InitializationParams.*;
import static vilij.settings.PropertyTypes.NOT_SUPPORTED_FOR_TEMPLATE_ERROR_TITLE;

/**
 * This class is the minimal template for a Vilij application. It does not create an actual workspace within the window,
 * and does not perform any actions.
 *
 * @author Ritwik Banerjee
 */
public class ApplicationTemplate extends Application {

    public final    PropertyManager    manager            = PropertyManager.getManager();
    protected final ErrorDialog        errorDialog        = ErrorDialog.getDialog();
    protected final ConfirmationDialog confirmationDialog = ConfirmationDialog.getDialog();

    protected DataComponent   dataComponent;
    protected UIComponent     uiComponent;
    protected ActionComponent actionComponent;

    @Override
    public void start(Stage primaryStage) throws Exception {
        dialogsAudit(primaryStage);
        if (propertyAudit())
            userInterfaceAudit(primaryStage);
    }

    protected void dialogsAudit(Stage primaryStage) {
        errorDialog.init(primaryStage);
        confirmationDialog.init(primaryStage);
    }

    protected boolean propertyAudit() {
        boolean failed = manager == null || !loadProperties(PROPERTIES_XML);
        if (failed)
            errorDialog.show(LOAD_ERROR_TITLE.getParameterName(), PROPERTIES_LOAD_ERROR_MESSAGE.getParameterName());
        return !failed;
    }

    protected void userInterfaceAudit(Stage primaryStage) {
        uiComponent = new UITemplate(primaryStage, this);
        try {
            uiComponent.initialize();
        } catch (UnsupportedOperationException e) {
            errorDialog.show(manager.getPropertyValue(NOT_SUPPORTED_FOR_TEMPLATE_ERROR_TITLE.name()), e.getMessage());
        }
    }

    public boolean loadProperties(InitializationParams propertyParam) {
        try {
            manager.loadProperties(ApplicationTemplate.class,
                                   propertyParam.getParameterName(),
                                   InitializationParams.SCHEMA_DEFINITION.getParameterName());
        } catch (InvalidXMLFileFormatException e) {
            Dialog errorDialog = ErrorDialog.getDialog();
            errorDialog.show(manager.getPropertyValue(LOAD_ERROR_TITLE.getParameterName()),
                             manager.getPropertyValue(PROPERTIES_LOAD_ERROR_MESSAGE.getParameterName()));
            return false;
        }
        return true;
    }

    public DataComponent getDataComponent()                   { return dataComponent; }

    public UIComponent getUIComponent()                       { return uiComponent; }

    public ActionComponent getActionComponent()               { return actionComponent; }

    public void setDataComponent(DataComponent component)     { this.dataComponent = component; }

    public void setUIComponent(UIComponent component)         { this.uiComponent = component; }

    public void setActionComponent(ActionComponent component) { this.actionComponent = component; }

    public Dialog getDialog(Dialog.DialogType dialogType) {
        Dialog dialog;
        switch (dialogType) {
            case ERROR:
                dialog = errorDialog;
                break;
            case CONFIRMATION:
                dialog = confirmationDialog;
                break;
            default:
                throw new IllegalArgumentException(String.format("%s is not a valid dialog type.", dialogType.name()));
        }
        return dialog;
    }
    
    public static void main(String[] args) {
        launch();
    }
    
}