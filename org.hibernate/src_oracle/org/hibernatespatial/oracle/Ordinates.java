package org.hibernatespatial.oracle;

import java.sql.Array;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 1, 2010
 */
class Ordinates {

    static final String TYPE_NAME = "MDSYS.SDO_ORDINATE_ARRAY";

    private Double[] ordinates;

    public Ordinates(Double[] ordinates) {
        this.ordinates = ordinates;
    }

    public Ordinates(Array array) {
        if (array == null) {
            this.ordinates = new Double[]{};
            return;
        }
        try {
            Number[] ords = (Number[]) array.getArray();
            this.ordinates = new Double[ords.length];
            for (int i = 0; i < ords.length; i++) {
                this.ordinates[i] = ords[i] != null ? ords[i].doubleValue()
                        : Double.NaN;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Double[] getOrdinateArray() {
        return this.ordinates;
    }

    public Double[] getOrdinatesArray(int startPosition, int endPosition) {
        Double[] a = new Double[endPosition - startPosition];
        System.arraycopy(this.ordinates, startPosition - 1, a, 0, a.length);
        return a;
    }

    public Double[] getOrdinatesArray(int startPosition) {
        Double[] a = new Double[this.ordinates.length - (startPosition - 1)];
        System.arraycopy(this.ordinates, startPosition - 1, a, 0, a.length);
        return a;
    }

    public String toString() {
        return SDOGeometry.arrayToString(this.ordinates);
    }

    public void addOrdinates(Double[] ordinatesToAdd) {
        Double[] newOrdinates = new Double[this.ordinates.length
                + ordinatesToAdd.length];
        System.arraycopy(this.ordinates, 0, newOrdinates, 0,
                this.ordinates.length);
        System.arraycopy(ordinatesToAdd, 0, newOrdinates,
                this.ordinates.length, ordinatesToAdd.length);
        this.ordinates = newOrdinates;
    }

//    public ARRAY toOracleArray(Connection conn) throws SQLException {
//        ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
//                TYPE_NAME, conn);
//        return new ARRAY(arrayDescriptor, conn, this.ordinates);
//    }

}