package vilij.settings;

/**
 * This enumerable type lists the various high-level property types listed in the initial set of properties to be
 * loaded from the global properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see InitializationParams
 */
public enum PropertyTypes {

    /* high-level user interface properties */
    WINDOW_WIDTH,
    WINDOW_HEIGHT,
    IS_WINDOW_RESIZABLE,
    TITLE,

    /* resource files and folders */
    GUI_RESOURCE_PATH,
    CSS_RESOURCE_PATH,
    CSS_RESOURCE_FILENAME,
    ICONS_RESOURCE_PATH,

    /* user interface icon file names */
    NEW_ICON,
    PRINT_ICON,
    SAVE_ICON,
    LOAD_ICON,
    EXIT_ICON,
    LOGO,

    /* tooltips for user interface buttons */
    NEW_TOOLTIP,
    PRINT_TOOLTIP,
    SAVE_TOOLTIP,
    LOAD_TOOLTIP,
    EXIT_TOOLTIP,

    /* error titles (these reflect the type of error encountered */
    NOT_SUPPORTED_FOR_TEMPLATE_ERROR_TITLE,
    LOAD_ERROR_TITLE,
    SAVE_ERROR_TITLE,

    /* error messages for errors that require an argument */
    SAVE_ERROR_MSG,
    LOAD_ERROR_MSG,

    /* standard labels and titles */
    CLOSE_LABEL,
    SAVE_WORK_TITLE
}
