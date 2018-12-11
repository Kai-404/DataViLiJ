package vilij.propertymanager;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import vilij.settings.InitializationParams;
import xmlutil.InvalidXMLFileFormatException;
import xmlutil.XMLUtilities;

import java.net.URL;
import java.util.*;

import static java.io.File.separator;

/**
 * This is the core class that defines all the global properties to be used by the Vilij framework.
 *
 * @author Ritwik Banerjee
 */
public class PropertyManager {

    private static final XMLUtilities xmlUtilities = new XMLUtilities();

    private static PropertyManager propertyManager = null;

    private Map<String, String>       properties;
    private Map<String, List<String>> propertyOptions;

    // Constants required to load the elements and their properties from the XML properties file
    public static final String PROPERTY_ELEMENT              = "property";
    public static final String PROPERTY_LIST_ELEMENT         = "property_list";
    public static final String PROPERTY_OPTIONS_LIST_ELEMENT = "property_options_list";
    public static final String PROPERTY_OPTIONS_ELEMENT      = "property_options";
    public static final String OPTION_ELEMENT                = "option";
    public static final String NAME_ATTRIBUTE                = "name";
    public static final String VALUE_ATTRIBUTE               = "value";

    /** Path of the properties resource folder, relative to the root resource folder for the application */
    public static final String PROPERTIES_RESOURCE_RELATIVE_PATH = "properties";

    private PropertyManager() {
        properties = new HashMap<>();
        propertyOptions = new HashMap<>();
    }

    public static PropertyManager getManager() {
        if (propertyManager == null) {
            propertyManager = new PropertyManager();
            try {
                propertyManager.loadProperties(propertyManager.getClass(),
                                               InitializationParams.PROPERTIES_XML.getParameterName(),
                                               InitializationParams.SCHEMA_DEFINITION.getParameterName());
            } catch (InvalidXMLFileFormatException e) {
                propertyManager = null;
            }
        }
        return propertyManager;
    }

    public void addProperty(String property, String value) {
        properties.put(property, value);
    }

    public String getPropertyValue(String property) {
        return properties.get(property);
    }

    public int getPropertyValueAsInt(String property) throws NullPointerException, NumberFormatException {
        return Integer.parseInt(properties.get(property));
    }

    public boolean getPropertyValueAsBoolean(String property) {
        return Boolean.parseBoolean(properties.get(property));
    }

    public void addPropertyOption(String property, String option) {
        if (properties.get(property) == null)
            throw new NoSuchElementException(String.format("Property \"%s\" does not exist.", property));
        List<String> propertyoptionslist = propertyOptions.get(property);
        if (propertyoptionslist == null)
            propertyoptionslist = new ArrayList<>();
        propertyoptionslist.add(option);
        propertyOptions.put(property, propertyoptionslist);
    }

    public List<String> getPropertyOptions(String property) {
        if (properties.get(property) == null)
            throw new NoSuchElementException(String.format("Property \"%s\" does not exist.", property));
        return propertyOptions.get(property);
    }

    public boolean hasProperty(Object property) {
        return properties.get(property.toString()) != null;
    }

    public void loadProperties(Class klass, String xmlfilename, String schemafilename) throws
                                                                                       InvalidXMLFileFormatException {
        URL xmlFileResource = klass.getClassLoader()
                                   .getResource(PROPERTIES_RESOURCE_RELATIVE_PATH + separator + xmlfilename);
        URL schemaFileResource = klass.getClassLoader()
                                      .getResource(PROPERTIES_RESOURCE_RELATIVE_PATH + separator + schemafilename);

        Document   document         = xmlUtilities.loadXMLDocument(xmlFileResource, schemaFileResource);
        Node       propertyListNode = xmlUtilities.getNodeWithName(document, PROPERTY_LIST_ELEMENT);
        List<Node> propNodes        = xmlUtilities.getChildrenWithName(propertyListNode, PROPERTY_ELEMENT);
        for (Node n : propNodes) {
            NamedNodeMap attributes = n.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                String attName  = attributes.getNamedItem(NAME_ATTRIBUTE).getTextContent();
                String attValue = attributes.getNamedItem(VALUE_ATTRIBUTE).getTextContent();
                properties.put(attName, attValue);
            }
        }

        Node propertyOptionsListNode = xmlUtilities.getNodeWithName(document, PROPERTY_OPTIONS_LIST_ELEMENT);
        if (propertyOptionsListNode != null) {
            List<Node> propertyOptionsNodes = xmlUtilities.getChildrenWithName(propertyOptionsListNode,
                                                                               PROPERTY_OPTIONS_ELEMENT);
            for (Node n : propertyOptionsNodes) {
                NamedNodeMap      attributes = n.getAttributes();
                String            name       = attributes.getNamedItem(NAME_ATTRIBUTE).getNodeValue();
                ArrayList<String> options    = new ArrayList<>();
                propertyOptions.put(name, options);
                List<Node> optionsNodes = xmlUtilities.getChildrenWithName(n, OPTION_ELEMENT);
                for (Node oNode : optionsNodes) {
                    String option = oNode.getTextContent();
                    options.add(option);
                }
            }
        }
    }

}
