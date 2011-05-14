/**
 * $Id: ConnectionFinder.java 268 2010-10-28 19:16:54Z maesenka $
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
package org.hibernatespatial.oracle;

import oracle.jdbc.OracleConnection;
import org.hibernatespatial.helper.FinderStrategy;

import java.sql.Connection;

/**
 * The <code>ConnectionFinder</code> returns an OracleConnection when given a
 * <code>Connection</code> object.
 * <p>
 * The SDOGeometryType requires access to an <code>OracleConnection</code>
 * object when converting a geometry to <code>SDOGeometry</code>, prior to
 * setting the geometry attribute in prepared statements. In some environments
 * the prepared statements do not return an <code>OracleConnection</code> but
 * a wrapper. Implementations of this interface attempt to retrieve the
 * <code>OracleConnection</code> from the wrapper in such cases.
 * </p>
 *
 * @author Karel Maesen
 */
public interface ConnectionFinder extends
        FinderStrategy<OracleConnection, Connection> {

}
