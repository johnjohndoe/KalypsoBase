package dom;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.traversal.NodeFilter;
/*
 *
 * Demonstrate the use of DOMLevel 3 LSParser and LSParseFilter. DOMLS sample
 * uses implementation of LSParser to parse a XML file and writes to an outputfile
 * using implementation of LSSerializer. DOMLS filters out all TEXT Nodes.
 *
 */

public class DOMLS {
    DOMImplementationLS implLS = null;
    LSParser lsParser = null;
    
    /**
     *
     * @param argv
     */
    public static void main(String[] argv) {
         if (argv.length < 2 ) {
            printUsage();
            System.exit(1);
        }
        DOMLS domLS = new DOMLS();
        domLS.runLS(argv[0],argv[1]);
    }
    
    /**
     *
     * @return DOMImplementation from Document object created using JAXP DocumentBuilderFactory.
     */
    public DOMImplementationLS createDOMImplementation(){
        DocumentBuilder parser = null;
        Document doc = null;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = parser.newDocument();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return (DOMImplementationLS) doc.getImplementation().getFeature("LS","3.0");
    }
    
    /**
     *
     * @return create LSParser
     */
    public LSParser createLSParser(){
        return implLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS,null);
    }
    
    /**
     *
     * @return implementation of LSParserFilter that filters out all TEXT Nodes
     */
    public LSParserFilter createFilter(){
        return new DOMFilter();
    }
    
    /**
     *
     * @param inputFile
     * @param outputFile
     */
    public void runLS(String inputFile,String outputFile){
        implLS = createDOMImplementation();
        lsParser = createLSParser();
        LSInput src = implLS.createLSInput();
        src.setSystemId(inputFile);
        lsParser.setFilter(createFilter());
        Document document = lsParser.parse(src);
        DOMErrorHandlerImpl eh = new DOMErrorHandlerImpl();
        Output out = new Output();
        LSSerializer writer=  implLS.createLSSerializer();
        writer.getDomConfig().setParameter("error-handler",new DOMErrorHandlerImpl());
        out.setSystemId(outputFile);
        writer.write(document,out);
    }
    
    class Output implements LSOutput {
        
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
    
    class DOMErrorHandlerImpl implements DOMErrorHandler {
        public boolean handleError(DOMError error) {
            System.out.println("Error occured : "+error.getMessage());
            return true;
        }
    }
    
    /*
     * DOMFilter filters out all TEXT Nodes.
     */
    class DOMFilter implements LSParserFilter{
        public short startElement(Element elt) {
            return FILTER_ACCEPT;
        }
        public short acceptNode(Node enode) {
            if(enode.getNodeType()==Node.TEXT_NODE)
                return FILTER_REJECT;
            return FILTER_ACCEPT;
        }
        public int getWhatToShow() {
            return NodeFilter.SHOW_ALL;
        }
    }
 
     /** Prints the usage. */
    private static void printUsage() {        
        System.err.println("Usage: java  dom.DOMLS  <xml instance document> <output filename>");
        System.err.println();
        
        
    } 
    
}
