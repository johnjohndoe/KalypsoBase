/**
 * $Id: OracleSpatialProjection.java 268 2010-10-28 19:16:54Z maesenka $
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.oracle.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;
import org.hibernatespatial.SpatialDialect;

/**
 * Template class for Spatial Projections
 *
 * @author Tom Acree
 */
public class OracleSpatialProjection extends SimpleProjection {

    private static final long serialVersionUID = 1L;

    private final String propertyName;

    private final int aggregate;

    public OracleSpatialProjection(int aggregate, String propertyName) {
        this.propertyName = propertyName;
        this.aggregate = aggregate;
    }

    public String toSqlString(Criteria criteria, int position,
                              CriteriaQuery criteriaQuery) throws HibernateException {

        SessionFactoryImplementor factory = criteriaQuery.getFactory();
        String[] columns = criteriaQuery.getColumnsUsingProjection(criteria,
                this.propertyName);
        Dialect dialect = factory.getDialect();
        if (dialect instanceof SpatialDialect) {
            SpatialDialect seDialect = (SpatialDialect) dialect;

            return new StringBuffer(seDialect.getSpatialAggregateSQL(
                    columns[0], this.aggregate)).append(" y").append(position)
                    .append("_").toString();
        } else {
            throw new IllegalStateException(
                    "Dialect must be spatially enabled dialect");
        }

    }

    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery)
            throws HibernateException {
        return new Type[]{criteriaQuery.getType(criteria, this.propertyName)};
    }

    public String toString() {
        return aggregate + "(" + propertyName + ")";
    }
}
