/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report.facade;

import org.oasis.ubl.commonaggregatetypes.OrderLineType;
import org.oasis.ubl.commonaggregatetypes.BasePriceType;
import org.oasis.ubl.commonaggregatetypes.ItemType;

/**
 * The <code>OrderLineTypeFacade</code> class provides a set of read-only
 * methods for accessing data in a UBL order line.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderLineTypeFacade {
    private OrderLineType lineItem;
  
    /**
     * Creates a new <code>OrderLineTypeFacade</code> instance.
     *
     * @param olt an <code>OrderLineType</code> value
     */
    public OrderLineTypeFacade(OrderLineType olt) {
        lineItem = olt;
    }

    /**
     * Returns the part number associated with a line item.
     *
     * @return a <code>String</code> representing the part number for this line
     * item
     */
    public String getItemPartNumber() {
        String num = "";
        try {
            num = lineItem.getItem().getSellersItemIdentification().getID().getValue();
        } catch (NullPointerException npe) {
        }
        return num;
    }

    /**
     * Returns the description associated with a line item.
     *
     * @return a <code>String</code> representing the description of this line item
     */
    public String getItemDescription() {
        String descr = "";
        try {
            descr = lineItem.getItem().getDescription().getValue();
        } catch (NullPointerException npe){
        }
        return descr;
    }

    /**
     * Returns the price associated with a line item.
     *
     * @return a <code>double</code> representing the price of this line item
     */
    public double getItemPrice() {
        double price = 0.0;
        try {
            price = getTheItemPrice().getPriceAmount().getValue().doubleValue();
        } catch (NullPointerException npe){
        }
        return price;
    }

    /**
     * Returns the quantity associated with a line item.
     *
     * @return an <code>int</code> representing the quantity of this line item
     */
    public int getItemQuantity() {
        int quantity = 0;
        try {
            quantity = lineItem.getQuantity().getValue().intValue();
        } catch (NullPointerException npe){
        }
        return quantity;
    }

    /**
     * Returns the <code>BasePriceType</code> associated with a line item
     *
     * @return a <code>BasePriceType</code> representing the price of this item
     */
    private BasePriceType getTheItemPrice() {
        BasePriceType result = null;
        java.util.ListIterator iter = 
            lineItem.getItem().getBasePrice().listIterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof BasePriceType) {
                result =(BasePriceType) obj;
                break;
            }
        }
        return result;
    }
 
    static public class Iterator implements java.util.Iterator {
        java.util.Iterator iter;
        
        /** List of OrderLineType */
        public Iterator(java.util.List lst) {
            iter = lst.iterator();
        }
       
        public Object next() {
            return new OrderLineTypeFacade((OrderLineType)iter.next());
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
