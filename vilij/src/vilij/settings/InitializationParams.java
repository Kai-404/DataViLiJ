package vilij.settings;

/**
 * This is the set of parameters specified for the proper initialization of a Vilij application. The specific
 * parameters are as follows:
 * <ul>
 * <li>{@link #PROPERTIES_XML}</li>
 * <li>{@link #WORKSPACE_PROPERTIES_XML}</li>
 * <li>{@link #SCHEMA_DEFINITION}</li>
 * </ul>
 * <p>
 * Additionally, two error-related parameters are included to handle the case when the property file(s) cannot be
 * loaded. These are
 * <ul>
 * <li>{@link #LOAD_ERROR_TITLE}</li>
 * <li>{@link #PROPERTIES_LOAD_ERROR_MESSAGE}</li>
 * </ul>
 *
 * @author Ritwik Banerjee
 */
public enum InitializationParams {

    /** Title for any type of loading error */
    LOAD_ERROR_TITLE("Load Error"),
    /** The standard error message if the properties cannot be loaded while starting an application. */
    PROPERTIES_LOAD_ERROR_MESSAGE("An error occured while loading the property file."),
    /** The global high-level properties file. */
    PROPERTIES_XML("properties.xml"),
    /** The properties file specific to the application's workspace requirements. */
    WORKSPACE_PROPERTIES_XML("app-properties.xml"),
    /** The XML schema definition for the properties files. */
    SCHEMA_DEFINITION("property-schema.xsd");

    private String parameterName;

    InitializationParams(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return this.parameterName;
    }
}
