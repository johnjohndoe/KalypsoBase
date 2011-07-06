/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gml.ui.internal.shape;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Gernot Belger
 */
public class DBFFieldBean extends AbstractModelObject implements IDBFField
{
  public static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$

  public static final String PROPERTY_TYPE_LABEL = "typeLabel"; //$NON-NLS-1$

  public static final String PROPERTY_DECIMAL_COUNT = "decimalCount"; //$NON-NLS-1$

  public static final String PROPERTY_LENGTH = "length"; //$NON-NLS-1$

  public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

  private DBFField m_field;

  public DBFFieldBean( final DBFField field )
  {
    m_field = field;
  }

  @Override
  public String getName( )
  {
    return m_field.getName();
  }

  public void setName( final String name ) throws DBaseException
  {
    final String oldValue = m_field.getName();

    m_field = m_field.withName( name );

    firePropertyChange( PROPERTY_NAME, oldValue, name );
  }

  @Override
  public short getLength( )
  {
    return m_field.getLength();
  }

  public void setLength( final short length ) throws DBaseException
  {
    final Short oldValue = m_field.getLength();

    m_field = m_field.withLength( length );

    firePropertyChange( PROPERTY_LENGTH, oldValue, length );
  }

  public void setDecimalCount( final short count ) throws DBaseException
  {
    final Short oldValue = m_field.getDecimalCount();

    m_field = m_field.withDecimalCount( count );

    firePropertyChange( PROPERTY_DECIMAL_COUNT, oldValue, count );
  }

  public void setType( final FieldType type ) throws DBaseException
  {
    final FieldType oldValue = getType();
    final String oldLabel = getTypeLabel();

    m_field = m_field.withType( type );

    firePropertyChange( PROPERTY_TYPE, oldValue, type );
    firePropertyChange( PROPERTY_TYPE_LABEL, oldLabel, getTypeLabel() );
  }

  @Override
  public FieldType getType( )
  {
    return m_field.getType();
  }

  public String getTypeLabel( )
  {
    return m_field.getType().toString();
  }

  @Override
  public short getDecimalCount( )
  {
    return m_field.getDecimalCount();
  }

  /**
   * @see org.kalypso.shape.dbf.IDBFField#writeValue(java.io.DataOutput, java.lang.Object, java.nio.charset.Charset)
   */
  @Override
  public byte[] writeValue( final DataOutput output, final Object value, final Charset charset ) throws DBaseException, IOException
  {
    return m_field.writeValue( output, value, charset );
  }

  /**
   * @see org.kalypso.shape.dbf.IDBFField#readValue(java.io.DataInput, java.nio.charset.Charset)
   */
  @Override
  public Object readValue( final DataInput input, final Charset charset ) throws IOException, DBaseException
  {
    return m_field.readValue( input, charset );
  }

  /**
   * @see org.kalypso.shape.dbf.IDBFField#write(java.io.DataOutput, java.nio.charset.Charset)
   */
  @Override
  public void write( final DataOutput output, final Charset charset ) throws IOException
  {
    m_field.write( output, charset );
  }

  public DBFFieldBean copy( )
  {
    return new DBFFieldBean( m_field );
  }
}