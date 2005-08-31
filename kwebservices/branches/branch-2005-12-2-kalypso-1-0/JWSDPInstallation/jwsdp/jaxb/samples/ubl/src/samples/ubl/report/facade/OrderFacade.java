/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report.facade;

import java.text.DateFormat;

import java.util.Calendar;
import java.util.ListIterator;
import java.util.Iterator;

import org.oasis.ubl.commonaggregatetypes.BuyerPartyType;
import org.oasis.ubl.commonaggregatetypes.PartyNameType;
import org.oasis.ubl.commonaggregatetypes.SellerPartyType;

import org.oasis.ubl.order.Order;

/**
 * The <code>OrderFacade</code> class provides a set of read-only methhods for
 * accessing data in a UBL order.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderFacade {

    Order order = null;

    /**
     * Creates a new <code>OrderFacade</code> instance.
     *
     * @param order an <code>Order</code> value
     */
    public OrderFacade(Order order) {
        this.order = order;
    }

    /**
     * Returns a <code>String</code> representing the name of a person familiar
     * with this order.
     *
     * @return a <code>String</code> value representing the name of a person
     * familiar with <code>Order</code>
     */
    public String getBuyerContact() {
        BuyerPartyType party = order.getBuyerParty();
        if (party != null) {
            if (party.getBuyerContact() != null) {
                return party.getBuyerContact().getName() != null
                    ? party.getBuyerContact().getName().getValue() : null;
            }
        }
        return "";             
    }

    /**
     * Returns a <code>String</code> representing the name of the entity placing
     * this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * placing this order
     */
    public String getBuyerName() {
        PartyNameType party = getBuyerParty(order.getBuyerParty());
        if (party != null) {
            return party.getName() != null
                ? party.getName().getValue() : null;
        }
        return "";             
    }

    /**
     * Returns the first <code>PartyNameType</code> in list order contained by
     * the <code>BuyerPartyType</code> representing the entity placing this
     * order.
     *
     * @param buyer a <code>BuyerPartyType</code> representing the entity
     * placing this order
     * @return a <code>PartyNameType</code> value representing the name of the
     * entity placing this order
     */
    private PartyNameType getBuyerParty(BuyerPartyType buyer) {
        if (buyer.getPartyName() != null) {
            ListIterator iter = buyer.getPartyName().listIterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof PartyNameType) {
                    return (PartyNameType) obj;
                }
            }
        }
        return null;            
    }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>BuyerPartyType</code> representing the entity placing this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of
     * the entity placing this order
     */
    public AddressFacade getBuyerAddress() {
        AddressFacade addr = null;
        try {
            addr = AddressFacade.getFirstAddress(order.getBuyerParty().getAddress());
        } catch (NullPointerException npe) {
        }
        return addr;
    }


    /**
     * Returns a <code>String</code> representing the name of the entity
     * fulfilling this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * fulfilling this order
     */
    public String getSellerName() {
        PartyNameType party = getSellerParty(order.getSellerParty());
        if (party != null) {
            return party.getName() != null
                ? party.getName().getValue() : null;
        }
        return null;             
    }


    /**
     * Returns the first <code>PartyNameType</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @param seller a <code>SellerPartyType</code> representing the entity
     * fulfilling this order
     * @return a <code>PartyNameType</code> value representing the name of the
     * entity fulfilling this order
     */
    private PartyNameType getSellerParty(SellerPartyType seller) {
        if (seller.getPartyName() != null) {
            ListIterator iter = seller.getPartyName().listIterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof PartyNameType) {
                    return (PartyNameType) obj;
                }
            }
        }
        return null;            
    }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of the
     * entity fulfilling this order
     */
    public AddressFacade getSellerAddress() {
        AddressFacade result = null;
        try {
            result= AddressFacade.getFirstAddress(order.getSellerParty().getAddress());
        } catch (NullPointerException npe) {
        }
        return result;
    }

    /**
     * Returns an UBL <code>Order</code> issue date in the <code>LONG</code>
     * format as defined by <code>java.text.DateFormat</code>.
     *
     * @return a <code>String</code> value representing the issue date of this
     * UBL <code>Order</code>
     */
    public String getLongDate() {
        DateFormat form = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = getCalendar();
        form.setTimeZone(cal.getTimeZone());
        return form.format(cal.getTime());
    }

    /**
     * Returns a <code>Calendar</code> representing the issue date of this UBL
     * <code>Order</code>.
     *
     * @return a <code>Calendar</code> representing the issue date of this UBL order
     */
    private Calendar getCalendar() {
        Calendar date = null;
        return order.getIssueDate() != null
            ? order.getIssueDate().getValue()
            : date;
    }

    /**
     * Returns an iterator over orders line items.
     *
     * @return an Iterator over OrderLineTypeFacade.
     */
    public Iterator getLineItemIter() {
        return new OrderLineTypeFacade.Iterator(order.getOrderLine());
    }
}
