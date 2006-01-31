package validation;

import java.io.File;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
/**
 * SchemaValidator demonstrates the use of jaxp validation apis. 
 *
 */
public class SchemaValidator {
    
    protected static class Handler extends DefaultHandler {
        
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
    
    protected static class Resolver implements LSResourceResolver{
        
        /**
         *
         * @param str
         * @param str1
         * @param str2
         * @param str3
         * @param str4
         * @return
         */
        public org.w3c.dom.ls.LSInput resolveResource(String str, String str1, String str2, String str3, String str4) {
            //System.out.println("Resolving : "+str+ ":"+str1+":"+str2+":"+str3+":"+str4);
            return null;
            
        }
        
    }
    
    /**
     *
     * @param args
     */
    public static void main(String [] args) {
        try {
            if(args.length <2){
                printUsage();
                return;
            }
            
            Handler handler = new Handler();
            
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            schemaFactory.setErrorHandler(handler);
            //create a grammar object.
            Schema schemaGrammar = schemaFactory.newSchema(new File(args[0]));
            
            System.out.println("Created Grammar object for schema : "+args[0]);
            
            Resolver resolver = new Resolver();
            //create a validator to validate against grammar sch.
            Validator schemaValidator = schemaGrammar.newValidator();
            schemaValidator.setResourceResolver(resolver);
            schemaValidator.setErrorHandler(handler);
            
            System.out.println("Validating "+args[1] +" against grammar "+args[0]);
            //validate xml instance against the grammar.
            schemaValidator.validate(new StreamSource(args[1]));
            
        } catch (Exception e) {
            exit(1, "Fatal Error: " + e);
        }
    }
    
    /**
     *
     * @param errCode
     * @param msg
     */
    public static void exit(int errCode, String msg) {
        System.out.println(msg);
    }
    
    public static void printUsage(){
        System.out.println("Usage : validation.SchemaValidator <schemaFile>  <xmlFile>");
    }
}
