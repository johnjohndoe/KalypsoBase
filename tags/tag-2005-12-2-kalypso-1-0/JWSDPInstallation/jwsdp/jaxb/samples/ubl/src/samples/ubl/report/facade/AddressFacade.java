/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report.facade;

import org.oasis.ubl.commonaggregatetypes.AddressType;
import java.util.ListIterator;

/**
 * The <code>AddressFacade</code> class provides a set of read-only
 * methods for accessing data in a UBL <code>AddressType</code>.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class AddressFacade {

    private AddressType address;
  
    /**
     * Creates a new <code>AddressFacade</code> instance.
     *
     * @param addr an <code>AddressType</code> value
     */
    public AddressFacade(AddressType addr) {
        address = addr;
    }

    /**
     * <code>getStreet</code> returns a <code>String</code> representing the
     * street in a UBL address.
     *
     * @return a <code>String</code> representing the street in a UBL address.
     */
    public String getStreet() {
        String result = "";
        try {
            result = address.getStreet().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getState</code> returns a <code>String</code> representing the
     * state in a UBL address.
     *
     * @return a <code>String</code> representing the state in a UBL address.
     */
    public String getState() {
        String result = "";
        try {
            result = address.getCountrySubEntity().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getCity</code> returns a <code>String</code> representing the city
     * in a UBL address.
     *
     * @return a <code>String</code> representing the city in a UBL address.
     */
    public String getCity() {
        String result = "";
        try {
            result = address.getCityName().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getZip</code> returns a <code>String</code representing the postal
     * zone in a UBL address.
     *
     * @return a <code>String</code> representing the postal zone in a UBL address.
     */
    public String getZip() {
        String result = "";
        try {
            result = address.getPostalZone().getValue();
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * <code>getFirstAddress</code> searches a <code>java.util.List</code> for a
     * <code>AddressFacade</code> and returns the first one it finds. If it
     * finds none, returns <code>null</code>.
     *
     * @param addresses a <code>java.util.List</code> value
     * @return an <code>AddressFacade</code> value
     */
    static public AddressFacade getFirstAddress(java.util.List addresses) {
        AddressFacade result = null;

        ListIterator iter = addresses.listIterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj != null && obj instanceof AddressType) {
                result =  new AddressFacade((AddressType) obj);
                break;
            }
        }
        return result;
    }
}
