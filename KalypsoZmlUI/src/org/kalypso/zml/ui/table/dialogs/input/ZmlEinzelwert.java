/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.dialogs.input;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.zml.ui.table.base.widgets.AbstractEnhancedWidget;

/**
 * @author Dirk Kuch
 */
public class ZmlEinzelwert
{
  Set<AbstractEnhancedWidget< ? >> m_widgets = new LinkedHashSet<AbstractEnhancedWidget< ? >>();

  private Date m_date;

  private Double m_value;

  private final ZmlEinzelwertModel m_model;

  public ZmlEinzelwert( final ZmlEinzelwertModel model, final Date date, final Double value )
  {
    m_model = model;
    m_date = date;
    m_value = value;
  }

  public Date getDate( )
  {
    return m_date;
  }

  public Double getValue( )
  {
    return m_value;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof ZmlEinzelwert )
    {
      final ZmlEinzelwert other = (ZmlEinzelwert) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getDate(), other.getDate() );
      builder.append( getValue(), other.getValue() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( this.getClass().getName() );
    builder.append( getDate() );
    builder.append( getValue() );

    return builder.toHashCode();
  }

  public void setDate( final Date selected )
  {
    m_date = selected;

    m_model.fireModelChanged();
  }

  public void setValue( final Double value )
  {
    m_value = value;

    m_model.fireModelChanged();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    final StringBuffer buffer = new StringBuffer();
    if( m_date != null )
    {
      final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
      buffer.append( String.format( "%s: ", sdf.format( m_date ) ) );
    }

    if( m_value != null )
    {
      buffer.append( String.format( "%.3f", m_value ) );
    }

    return buffer.toString();
  }

  public void addWidget( final AbstractEnhancedWidget< ? > widget )
  {
    m_widgets.add( widget );
  }

  public boolean isValid( )
  {
    for( final AbstractEnhancedWidget< ? > widget : m_widgets )
    {
      if( !widget.isValid() )
        return false;
    }

    return true;
  }

}
