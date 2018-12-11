package vilij.templates;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vilij.components.UIComponent;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;

import static java.io.File.separator;
import static vilij.settings.PropertyTypes.*;

/**
 * This class defines and creates a user interface using the {@link ApplicationTemplate} class. The interface created
 * here at the framework-level does not instantiate an actual workspace (which must be done by any application using
 * this framework).
 *
 * @author Ritwik Banerjee
 */
public class UITemplate implements UIComponent {

    private static final String UI_NOT_INITIALIZABLE_FOR_TEMPLATES = "The graphical user interface cannot be " +
                                                                     "initialized at template-level. A child " +
                                                                     "class must be implemented.";

    protected Pane    workspace;        // the top-level GUI element in the main workspace
    protected ToolBar toolBar;          // the top toolbar
    protected String  newiconPath;      // path to the 'new' icon
    protected String  printiconPath;    // path to the 'print' icon
    protected String  saveiconPath;     // path to the 'save' icon
    protected String  loadiconPath;     // path to the 'load' icon
    protected String  exiticonPath;     // path to the 'save' icon
    protected String  logoPath;         // path to the Vilij logo icon
    protected String  cssPath;          // path to the CSS file used for stylization of the user interface
    protected Stage   primaryStage;     // the application window
    protected Scene   primaryScene;     // the scene graph
    protected Pane    appPane;          // the root node in the scene graph, to organize the containers
    protected Button  newButton;        // button to create new data
    protected Button  saveButton;       // button to save progress on application
    protected Button  loadButton;       // button to load data for the application
    protected Button  exitButton;       // button to exit application
    protected Button  printButton;      // button to print a visualization
    protected String  applicationTitle; // the application title
    protected Image   logo;             // the Vilij logo
    protected int     windowWidth;
    protected int     windowHeight;

    /**
     * Creates the minimal user interface to be used by a Vilij application. It uses the window height and width
     * properties and creates a toolbar with the required buttons.
     *
     * @param primaryStage the window for the application
     */
    public UITemplate(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        this.windowWidth = manager.getPropertyValueAsInt(PropertyTypes.WINDOW_WIDTH.name());
        this.windowHeight = manager.getPropertyValueAsInt(PropertyTypes.WINDOW_HEIGHT.name());
        this.applicationTitle = manager.getPropertyValue(PropertyTypes.TITLE.name());
        this.primaryStage = primaryStage;

        setResourcePaths(applicationTemplate);   // set the correct paths to all the required resources
        setToolBar(applicationTemplate);         // initialize the top toolbar
        setToolbarHandlers(applicationTemplate); // set the toolbar button handlers
        setWindow(applicationTemplate);          // start the app window (without the application-specific workspace)
    }

    @Override
    public Stage getPrimaryWindow() { return primaryStage; }

    @Override
    public Scene getPrimaryScene() { return primaryScene; }

    @Override
    public String getTitle() { return applicationTitle; }

    /** Initialization is not provided at the template-level, and must be implemented by a child class. */
    @Override
    public void initialize() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(UI_NOT_INITIALIZABLE_FOR_TEMPLATES);
    }

    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        newButton = setToolbarButton(newiconPath, manager.getPropertyValue(NEW_TOOLTIP.name()), true);
        saveButton = setToolbarButton(saveiconPath, manager.getPropertyValue(SAVE_TOOLTIP.name()), true);
        loadButton = setToolbarButton(loadiconPath, manager.getPropertyValue(LOAD_TOOLTIP.name()), false);
        printButton = setToolbarButton(printiconPath, manager.getPropertyValue(PRINT_TOOLTIP.name()), true);
        exitButton = setToolbarButton(exiticonPath, manager.getPropertyValue(EXIT_TOOLTIP.name()), false);
        toolBar = new ToolBar(newButton, saveButton, loadButton, printButton, exitButton);
    }

    protected Button setToolbarButton(String iconPath, String tooltip, boolean disabled) {
        Button button = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(iconPath))));
        button.getStyleClass().add("toolbar-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setDisable(disabled);
        return button;
    }

    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));

        newiconPath = String.join(separator, iconsPath, manager.getPropertyValue(NEW_ICON.name()));
        saveiconPath = String.join(separator, iconsPath, manager.getPropertyValue(SAVE_ICON.name()));
        loadiconPath = String.join(separator, iconsPath, manager.getPropertyValue(LOAD_ICON.name()));
        exiticonPath = String.join(separator, iconsPath, manager.getPropertyValue(EXIT_ICON.name()));
        printiconPath = String.join(separator, iconsPath, manager.getPropertyValue(PRINT_ICON.name()));
        logoPath = String.join(separator, iconsPath, manager.getPropertyValue(LOGO.name()));

        cssPath = "/" + String.join(separator,
                                    manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                    manager.getPropertyValue(CSS_RESOURCE_PATH.name()),
                                    manager.getPropertyValue(CSS_RESOURCE_FILENAME.name()));

        logo = new Image(getClass().getResourceAsStream(logoPath));
    }

    /** Initialization is not provided at the template-level, and must be implemented by a child class. */
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) { /* squelch */ }

    protected void setWindow(ApplicationTemplate applicationTemplate) {
        primaryStage.setTitle(applicationTitle);
        primaryStage.setResizable(applicationTemplate.manager.getPropertyValueAsBoolean(IS_WINDOW_RESIZABLE.name()));
        appPane = new VBox();
        appPane.getChildren().add(toolBar);

        primaryScene = windowWidth < 1 || windowHeight < 1 ? new Scene(appPane)
                                                           : new Scene(appPane, windowWidth, windowHeight);
        primaryStage.getIcons().add(logo);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    /**
     * This action is not provided at the template-level, since there is nothing to clear out. Implementation must be
     * done in a child class at the application-specific level.
     */
    public void clear() {
        /* squelch */
    }

}
