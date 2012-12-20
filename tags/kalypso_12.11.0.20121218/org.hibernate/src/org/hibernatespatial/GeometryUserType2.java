/**
 * $Id: GeometryUserType.java 253 2010-10-02 15:14:52Z maesenka $
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
package org.hibernatespatial;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * REMARK: copied from original hibernatespatial sources in order to overcome the problem with the fixed spatial
 * dialect.<br/>
 * <br/>
 * This class ensures that Hibernate can work with the JTS <code>Geometry</code> type.
 * <p/>
 * To properly convert <code>Geometry</code> objects to database specific wrapper objects, acces is needed to a
 * spatially enabled database dialect. This dialect can be specified as a parameter to the type. If no parameter is
 * supplied, the default Dialect will be used (set in HBSpatialExtension).
 * 
 * @author Karel Maesen
 */
public class GeometryUserType2 implements UserType, ParameterizedType, Serializable
{
  private SpatialDialect spatialDialect = null;

  private UserType delegate = null;

  public static String DIALECT_PARAM_NAME = "dialect"; //$NON-NLS-1$

  /**
   * We get the dialect from outside, in preference from the current configuration.
   */
  public GeometryUserType2( final SpatialDialect dialect )
  {
    spatialDialect = dialect;
  }

  private void configure( final Properties properties )
  {
    if( spatialDialect == null )
      throw new HibernateSpatialException( "No spatial Dialect could be created" ); //$NON-NLS-1$

    delegate = spatialDialect.getGeometryUserType();
    if( delegate instanceof ParameterizedType && properties != null )
      ((ParameterizedType) delegate).setParameterValues( properties );
  }

  private void initialize( )
  {
    if( delegate == null )
    {
      configure( null );
    }
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  @Override
  public Object assemble( final Serializable arg0, final Object arg1 ) throws HibernateException
  {
    initialize();
    return delegate.assemble( arg0, arg1 );
  }

  /**
   * @param arg0
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  @Override
  public Object deepCopy( final Object arg0 ) throws HibernateException
  {
    return delegate.deepCopy( arg0 );
  }

  /**
   * @param arg0
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
   */
  @Override
  public Serializable disassemble( final Object arg0 ) throws HibernateException
  {
    initialize();
    return delegate.disassemble( arg0 );
  }

  /**
   * @param arg0
   * @param arg1
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  @Override
  public boolean equals( final Object arg0, final Object arg1 ) throws HibernateException
  {
    initialize();
    return delegate.equals( arg0, arg1 );
  }

  /**
   * @param obj
   * @return
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    initialize();
    return delegate.equals( obj );
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    initialize();
    return delegate.hashCode();
  }

  /**
   * @param arg0
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  @Override
  public int hashCode( final Object arg0 ) throws HibernateException
  {
    initialize();
    return delegate.hashCode( arg0 );
  }

  /**
   * @return
   * @see org.hibernate.usertype.UserType#isMutable()
   */
  @Override
  public boolean isMutable( )
  {
    initialize();
    return delegate.isMutable();
  }

  /**
   * @param arg0
   * @param arg1
   * @param arg2
   * @return
   * @throws HibernateException
   * @throws SQLException
   * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
   */
  @SuppressWarnings("deprecation")
  @Override
  public Object nullSafeGet( final ResultSet arg0, final String[] arg1, final Object arg2 ) throws HibernateException, SQLException
  {
    initialize();
    return delegate.nullSafeGet( arg0, arg1, arg2 );
  }

  /**
   * @param arg0
   * @param arg1
   * @param arg2
   * @throws HibernateException
   * @throws SQLException
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  @SuppressWarnings("deprecation")
  @Override
  public void nullSafeSet( final PreparedStatement arg0, final Object arg1, final int arg2 ) throws HibernateException, SQLException
  {
    initialize();
    delegate.nullSafeSet( arg0, arg1, arg2 );
  }

  /**
   * @param arg0
   * @param arg1
   * @param arg2
   * @return
   * @throws HibernateException
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  @Override
  public Object replace( final Object arg0, final Object arg1, final Object arg2 ) throws HibernateException
  {
    initialize();
    return delegate.replace( arg0, arg1, arg2 );
  }

  /**
   * @return
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  @Override
  public Class< ? > returnedClass( )
  {
    initialize();
    return delegate.returnedClass();
  }

  /**
   * @return
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  @Override
  public int[] sqlTypes( )
  {
    initialize();
    return delegate.sqlTypes();
  }

  /**
   * @return
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    initialize();
    return delegate.toString();
  }

  @Override
  public void setParameterValues( final Properties properties )
  {
    configure( properties );
  }

}
