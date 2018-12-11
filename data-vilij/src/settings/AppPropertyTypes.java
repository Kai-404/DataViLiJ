package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    MORE_THAN_TEN_LINES,
    MORE_THAN_TEN_LINES_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    SCREENSHOT_FILE_EXT,
    SCREENSHOT_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    DISPLAY_BUTTON_TEXT,
    READONLY_CHECKBOX_TEXT,
    CSS_FILE_PATH,
    CLUSTERER_RANDOM_NAME,
    CLUSTERER_RANDOM_PATH,
    CLASSIFIER_RANDOM_NAME,
    CLASSIFIER_RANDOM_PATH,
    CLUSTERER_KMEANS_NAME,
    CLUSTERER_KMEANS_PATH,
    EXIT_WHILE_RUNNING_WARNING,
    
    
}
