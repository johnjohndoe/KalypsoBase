package dom;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * DOMSerialzer sample demonstrates the use of DOM Level 3 Load and Save Api's
 * to serialize a DOM tree.
 *
 */
public class DOMSerializer {
    
    /**
     * Main program entry point.
     * @param argv
     */
    public static void main(String argv[]) {
        
        if (argv.length < 2 ) {
            printUsage();
            System.exit(1);
        }
        try{
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            //parse the xml file.
            DocumentBuilder parser = dbf.newDocumentBuilder();
            parser.setErrorHandler(new ErrorHandlerImpl());
            Document doc = parser.parse(argv[0]);
            
            //write the output to specified file.
            DOMImplementation impl = doc.getImplementation();
            DOMImplementationLS implLS =  (DOMImplementationLS) impl.getFeature("LS","3.0");
            DOMErrorHandlerImpl eh = new DOMErrorHandlerImpl();
            Output out = new Output();
            
            LSSerializer writer=  implLS.createLSSerializer();
            writer.getDomConfig().setParameter("error-handler",new DOMErrorHandlerImpl());            
            //URI reference [IETF RFC 2396], 
            out.setSystemId(new File(argv[1]).toURI().toString());
            writer.write(doc,out);
            
        }catch(Exception ex){
            if(ex.getCause() != null){
                System.out.println(ex.getCause().getMessage());
                ex.getCause().printStackTrace();
            }
            System.out.println("Error occurred " + ex);
        }
    }
    
    //
    // Private static methods
    //
    
    /** Prints the usage. */
    private static void printUsage() {
        
        System.err.println("Usage: java  dom.DOMSerializer  <xml instance document> <output filename>");
        System.err.println();
        
        
    } // printUsage()
    
    static class Output implements LSOutput {
        
        OutputStream bs;
        Writer cs;
        String sId;
        String enc;
        
        public Output() {
            bs = null;
            cs = null;
            sId = null;
            enc = "UTF-8";
        }
        
        public OutputStream getByteStream() {
            return bs;
        }
        public void setByteStream(OutputStream byteStream) {
            bs = byteStream;
        }
        public Writer getCharacterStream() {
            return cs;
        }
        public void setCharacterStream(Writer characterStream) {
            cs = characterStream;
        }
        public String getSystemId() {
            return sId;
        }
        public void setSystemId(String systemId) {
            sId = systemId;
        }
        public String getEncoding() {
            return enc;
        }
        public void setEncoding(String encoding) {
            enc = encoding;
        }
    }
    
    static class DOMErrorHandlerImpl implements DOMErrorHandler {
        public boolean handleError(DOMError error) {
            System.out.println("Error occured : "+error.getMessage());
            return true;
        }
    }
    
    static class ErrorHandlerImpl implements ErrorHandler {
        
        /**
         *
         * @param sAXParseException
         * @throws SAXException
         */
        public void error(SAXParseException sAXParseException) throws SAXException {
            System.out.println(sAXParseException);
        }
        
        /**
         *
         * @param sAXParseException
         * @throws SAXException
         */
        public void fatalError(SAXParseException sAXParseException) throws SAXException {
            System.out.println(sAXParseException);
        }
        
        /**
         *
         * @param sAXParseException
         * @throws SAXException
         */
        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            System.out.println(sAXParseException);
        }
        
    }
    
}
