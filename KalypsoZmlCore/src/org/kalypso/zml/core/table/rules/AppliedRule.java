/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.zml.core.table.rules;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.zml.core.table.binding.CellStyle;

/**
 * @author Dirk Kuch
 */
public class AppliedRule
{
  private final CellStyle m_style;

  private final String m_label;

  private final Double m_severity;

  private final boolean m_headerIcon;

  public AppliedRule( final CellStyle style, final String label, final Double severity, final boolean headerIcon )
  {
    m_style = style;
    m_label = label;
    m_severity = severity;
    m_headerIcon = headerIcon;
  }

  public Double getSeverity( )
  {
    return m_severity;
  }

  public CellStyle getCellStyle( )
  {
    return m_style;
  }

  public String getLabel( )
  {
    return m_label;
  }

  public boolean hasHeaderIcon( )
  {
    return m_headerIcon;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof AppliedRule )
    {
      final AppliedRule other = (AppliedRule) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getLabel(), other.getLabel() );

      return builder.isEquals();
    }
    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getLabel() );

    return builder.toHashCode();
  }
}
