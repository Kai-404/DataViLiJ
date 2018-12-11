package xmlutil;

/**
 * This is a checked exception that represents the event where an XML document cannot be validated against a provided
 * XML schema definition (which is a <code>.xsd</code> file).
 *
 * @author Ritwik Banerjee
 * @author Richard McKenna
 */
public class InvalidXMLFileFormatException extends Exception {

    private final String xmlFileName; // name of the XML file
    private final String xsdFileName; // name of the XML schema definition file

    /**
     * @param xmlFileName XML doc file name that didn't validate
     * @param xsdFileName XML schema file used in validation
     */
    public InvalidXMLFileFormatException(String xmlFileName, String xsdFileName) {
        this.xmlFileName = xmlFileName;
        this.xsdFileName = xsdFileName;
    }

    @Override
    public String toString() {
        return String.format("InvalidXMLFileFormatException {xmlFileName: '%s'; xsdFileName: '%s'} ",
                             xmlFileName,
                             xsdFileName);
    }
}