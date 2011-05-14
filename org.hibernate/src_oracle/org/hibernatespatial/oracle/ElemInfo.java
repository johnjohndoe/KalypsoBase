package org.hibernatespatial.oracle;

import java.math.BigDecimal;
import java.sql.Array;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 1, 2010
 */
class ElemInfo {

    static final String TYPE_NAME = "MDSYS.SDO_ELEM_INFO_ARRAY";

    private BigDecimal[] triplets;

    public ElemInfo(int size) {
        this.triplets = new BigDecimal[3 * size];
    }

    public ElemInfo(BigDecimal[] elem_info) {
        this.triplets = elem_info;
    }

    public ElemInfo(Array array) {
        if (array == null) {
            this.triplets = new BigDecimal[]{};
            return;
        }
        try {
            triplets = (BigDecimal[]) array.getArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public BigDecimal[] getElements() {
        return this.triplets;
    }

    public int getSize() {
        return this.triplets.length / 3;
    }

    public int getOrdinatesOffset(int i) {
        return this.triplets[i * 3].intValue();
    }

    public void setOrdinatesOffset(int i, int offset) {
        this.triplets[i * 3] = new BigDecimal(offset);
    }

    public ElementType getElementType(int i) {
        int etype = this.triplets[i * 3 + 1].intValue();
        int interp = this.triplets[i * 3 + 2].intValue();
        ElementType et = ElementType.parseType(etype, interp);
        return et;
    }

    public boolean isCompound(int i) {
        return getElementType(i).isCompound();
    }

    public int getNumCompounds(int i) {
        if (getElementType(i).isCompound()) {
            return this.triplets[i * 3 + 2].intValue();
        } else {
            return 1;
        }
    }

    public void setElement(int i, int ordinatesOffset, ElementType et,
                           int numCompounds) {
        if (i > getSize()) {
            throw new RuntimeException(
                    "Attempted to set more elements in ElemInfo Array than capacity.");
        }
        this.triplets[i * 3] = new BigDecimal(ordinatesOffset);
        this.triplets[i * 3 + 1] = new BigDecimal(et.getEType());
        this.triplets[i * 3 + 2] = et.isCompound() ? new BigDecimal(numCompounds) : new BigDecimal(et
                .getInterpretation());
    }

    public String toString() {
        return SDOGeometry.arrayToString(this.triplets);
    }

    public void addElement(BigDecimal[] element) {
        BigDecimal[] newTriplets = new BigDecimal[this.triplets.length + element.length];
        System.arraycopy(this.triplets, 0, newTriplets, 0,
                this.triplets.length);
        System.arraycopy(element, 0, newTriplets, this.triplets.length,
                element.length);
        this.triplets = newTriplets;
    }

    public void addElement(ElemInfo element) {
        this.addElement(element.getElements());
    }

    public BigDecimal[] getElement(int i) {
        BigDecimal[] ea = null;
        if (this.getElementType(i).isCompound()) {
            int numCompounds = this.getNumCompounds(i);
            ea = new BigDecimal[numCompounds + 1];
        } else {
            ea = new BigDecimal[3];
        }
        System.arraycopy(this.triplets, 3 * i, ea, 0, ea.length);
        return ea;
    }

//    public ARRAY toOracleArray(Connection conn) throws SQLException {
//        ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
//                TYPE_NAME, conn);
//        return new ARRAY(arrayDescriptor, conn, this.triplets);
//    }
}