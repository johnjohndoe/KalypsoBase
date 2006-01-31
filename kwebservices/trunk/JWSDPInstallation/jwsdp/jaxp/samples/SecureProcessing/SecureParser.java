package SecureProcessing;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * SecureParser demonstrates the use of SecureProcessing feature in JAXP 1.3.
 * Secureprocessing feature was in added in JAXP 1.3 to prevent denial of service attack.
 */

public class SecureParser {
    
    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String [] args){
        try{
            if(args.length < 2){
                printUsage();
                return;
            }
            //Set the limit for parser specific properties.
            //for more information on parser specific properties
            //refer to jaxp1.3 release notes.
            
            System.setProperty("elementAttributeLimit", "1");
            System.setProperty("entityExpansionLimit", "2");
            
            int index = args[0].indexOf("=");
            if(index <=0 ){
                printUsage();
                return;
            }
            String value = args[0].substring(index+1);
            boolean enableSecureProcessing = false;
            if(value != null){
                enableSecureProcessing = Boolean.valueOf(value).booleanValue();
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //enable or disable secureprocessing feature provided by jaxp1.3
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,enableSecureProcessing);
            
            DocumentBuilder parser =  dbf.newDocumentBuilder();
            parser.parse(args[1]);
            
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    public static void printUsage(){
        System.out.println("Usage :java  SecureProcessing.SecureParser -enableSecureProcessing=<true/false> <xmlFile>");
    }
}
