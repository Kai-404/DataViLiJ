package xmlutil;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides general utility and helper methods for interacting with XML data. It is not a complete XML
 * library by any means, but it has adequate funcationality for the library we will be building. A large portion of this
 * class is taken from an example found in the following page on IBM developerWorks:
 * <ul>
 * <li>
 * <a href="http://www.ibm.com/developerworks/xml/library/x-javaxmlvalidapi/index.html">The Java XML Validation API</a>
 * </li>
 * </ul>
 *
 * @author Ritwik Banerjee
 * @author Richard Mckenna
 */
public class XMLUtilities {

    /** The standard XML schema namespace. It is <b>not</b> the schema itself. */
    public static final String SCHEMA_STANDARD_SPEC_URL = "http://www.w3.org/2001/XMLSchema";

    /** Default constructor */
    public XMLUtilities() {}

    /**
     * Validates the specified XML file against the specified XML Schema Definition (<code>.xsd</code>) file and returns
     * <code>true</code> if and only if the validity check passes.
     *
     * @param xmlFilePath   the specified XML file
     * @param xmlSchemapath the specified XML schema definition file
     * @return <code>true</code> if the file is valid as per the schema, <code>false</code> otherwise.
     */
    public boolean validateXML(URL xmlFilePath, URL xmlSchemapath) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(SCHEMA_STANDARD_SPEC_URL);
        try {
            Schema    schema    = schemaFactory.newSchema(xmlSchemapath);
            Validator validator = schema.newValidator();
            Source    source    = new StreamSource(xmlFilePath.openStream());
            validator.validate(source);
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }

    /**
     * Loads the specified XML file into a <code>org.w3c.dom.Document</code> and returns it.
     *
     * @param xmlFileURL    the <code>URL</code> of the specified XML file
     * @param schemaFileURL the <code>URL</code> of the schema definition provided
     * @return a normalized <code>org.w3c.dom.Document</code> object consisting of the data in the original XML file
     * @throws InvalidXMLFileFormatException if the file cannot be validated against the given schema definition
     * @see Document
     */
    public Document loadXMLDocument(URL xmlFileURL, URL schemaFileURL) throws InvalidXMLFileFormatException {
        if (!validateXML(xmlFileURL, schemaFileURL))
            throw new InvalidXMLFileFormatException(xmlFileURL.getFile(), schemaFileURL.getFile());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder  = documentBuilderFactory.newDocumentBuilder();
            Document        document = builder.parse(xmlFileURL.openStream());
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new InvalidXMLFileFormatException(xmlFileURL.getFile(), schemaFileURL.getFile());
        }
    }

    /**
     * Retrieves and return the node in the document that is an element of the type specified by the tag name. If no
     * such element is found, then <code>null</code> is returned.
     *
     * @param doc     the specified XML document
     * @param tagName the specified tag name
     * @return The first node found named tagName. If none is
     *         found in the document, null is returned.
     */
    public Node getNodeWithName(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : nodes.item(0);
    }

    /**
     * This method extracts the data corresponding to the specified tag name in the given document, and returns it as a
     * String. If no data is found, <code>null</code> is returned. This method can only handle unique tags in an XML
     * file. If there are multiple occurrences of a tag, the behavior is unspecified.
     *
     * @param doc     the DOM Document corresponding to a valid XML file
     * @param tagName the specified tag name
     * @return the data (as a String) found in the element with the specified tag name, and <code>null</code> if there
     *         is no such data.
     */
    public String getTextData(Document doc, String tagName) {
        Node node = getNodeWithName(doc, tagName);
        return node == null ? null : node.getTextContent();
    }

    /**
     * Retrieves and returns all children of the given parent node that have the specified tag name.
     *
     * @param parent  the given parent node
     * @param tagName the specified tag name
     * @return a list of child nodes of the type specified by the tag name
     */
    public List<Node> getChildrenWithName(Node parent, String tagName) {
        List<Node> children = new ArrayList<>();
        NodeList   nodes    = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node child = nodes.item(i);
            if (child.getNodeName().equals(tagName))
                children.add(child);
        }
        return children;
    }
}
