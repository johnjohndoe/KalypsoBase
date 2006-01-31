/*
 * NewDatatypes.java
 *
 * This class shows how to use the new Datatype APIs introduced in JSR 206.
 * This samples just shows you few (out of many things) which is possible 
 * with new javax.xml.datatype APIs.
 *
 */

package Datatype;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 *
 * @author  Neeraj Bajaj, <neeraj.bajaj@sun.com> Sun Microsystems
 */
public class NewDatatypes {
    
    DatatypeFactory dfactory ;
    static final boolean DEBUG = false;
    
    /** Creates a new instance of MyDuration */
    public NewDatatypes() {
        try{
        dfactory = DatatypeFactory.newInstance();
        if(DEBUG)System.out.println("Datatype factory used is = " + dfactory);
        }catch(javax.xml.datatype.DatatypeConfigurationException ce){
            if(DEBUG){
            ce.printStackTrace();
            }
        }
    }
    
    /** Get the immutable Duration object for given String lexical representation
     *  "PnYnMnDTnHnMnS", as defined in XML Schema 1.0 section 3.2.6.1.
     * XML Schema Part 2: Datatypes, 3.2.6 duration, defines duration as:
     * 
     * Duration represents a duration of time. The value space of duration is a six-dimensional 
     * space where the coordinates designate the Gregorian year, month, day, hour, minute, and second 
     * components defined in Section 5.5.3.2 of [ISO 8601], respectively. These components 
     * are ordered in their significance by their order of appearance i.e. as year, month, day, hour, 
     * minute, and second. 
     *
     * @return Duration object
     */
    private Duration getDuration(String duration){
        return dfactory.newDuration(duration);
    }
    
    private XMLGregorianCalendar getXMLGregorianCalendar(String representation){
        return dfactory.newXMLGregorianCalendar(representation);
    }

    private XMLGregorianCalendar getCurrent(){     
        //get calendar corresponding to current date and time
        GregorianCalendar current = (GregorianCalendar)GregorianCalendar.getInstance();
        return dfactory.newXMLGregorianCalendar(current);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        NewDatatypes nd = new NewDatatypes();
                        
        //1.
        //lexical representation of Duration which represents 1 Year, 2 Months, 3 days, 10 hours, 30 minutes, 15 seconds
        //is "P1Y2M3DT10H30M15S" as specified in XML Schema Datatypes, Section 3.2.6.1
        //Get javax.xml.datatype.Duration datatype from this lexical representation. 
        
        Duration duration1 = nd.getDuration("P1Y2M3DT10H30M15S");
        //lexical representation of Duration which represents 1 Year, 2 Months, 3 days, 9 hours, 90 minutes, 15 seconds
        Duration duration2 = nd.getDuration("P1Y2M3DT09H90M15S");
        
        //Two different looking durations infact are equal..
        
        //2. 
        // Compare two durations to check which one is bigger
        int value = duration2.compare(duration1);
        
        
        switch(value){
            case DatatypeConstants.LESSER:{
                System.out.println("Duration2 " + duration2.toString() + " is less than " + duration1.toString() );
                break;
            }
            case DatatypeConstants.EQUAL:{
                System.out.println("Two durations " + duration1.toString() + " and " + duration2.toString() + " are equal. ");
                break;
            }
            case DatatypeConstants.GREATER:{
                System.out.println("Duration2 " + duration2.toString() + " is greater than " + duration1.toString() );
                break;                
            }
            case DatatypeConstants.INDETERMINATE:{
                System.out.println("Relationship between two durations " + duration1.toString() + " and " + duration2.toString() + " is indeterminate ");
            }
        }
        
        //There are number of operations which are possible on a given Duration object.
        //for ex. one  can get information about the all the 6 fields of Duration object
        //add two durations, subtract two durations etc. Checkout the javadoc 
        // of javax.xml.datatpe.Duration for all different functions.
        
        //3.
        //Now for ex. you need to know given Duration object maps to which Schema Datatype ?
        //You can know that using following function..
        
        QName schemaDT = duration1.getXMLSchemaType();
        System.out.println("Duration 1 " + duration1.toString() + " maps to W3C XML Schema " + schemaDT.toString() + " datatype.");
        
                
        //4.
        XMLGregorianCalendar gc = nd.getCurrent();
        //YOu can convert the given time in a format which is specified in XML Schema 
        //1.0 Part 2, Section 3.2.[7-14].1, Lexical Representation"."
        
        //This value can also be passed to element/attribute which is of datatype
        // XML Schema 1.0 Part 2, Section 3.2.[7-14]
        System.out.println("XML Schema Lexical representation = " + gc.toXMLFormat());
        
        //5. 
        //add duration to the XMLGregorianCalendar, for ex. expiry date of bread is 5 days from the date of packaging.
        //so the duration is
        String fresh = "P05D";
        gc.add(nd.getDuration(fresh));
        
        //6.
        //After doing computation print expiry date in a format which is specified in XML Schema 
        //1.0 Part 2, Section 3.2.[7-14].1, Lexical Representation"."
        System.out.println("Expiry date, XML Schema Lexical Represntation  = " + gc.toXMLFormat());
        
        
    }
    
}
