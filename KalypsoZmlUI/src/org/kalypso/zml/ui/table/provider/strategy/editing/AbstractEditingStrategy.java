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
package org.kalypso.zml.ui.table.provider.strategy.editing;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractEditingStrategy implements IZmlEditingStrategy
{
  private final ExtendedZmlTableColumn m_column;

  public AbstractEditingStrategy( final ExtendedZmlTableColumn column )
  {
    m_column = column;
  }

  protected ExtendedZmlTableColumn getColumn( )
  {
    return m_column;
  }

  protected CellStyle getStyle( ) throws CoreException
  {
    final CellStyle editing = m_column.getColumnType().getDefaultEditingStyle();
    if( editing == null )
      return m_column.getColumnType().getDefaultStyle();

    return editing;
  }

  protected Number getTargetValue( final String value )
  {
    final IAxis axis = m_column.getModelColumn().getValueAxis();
    final Class< ? > clazz = axis.getDataClass();

    if( Double.class == clazz )
    {
      return NumberUtils.parseDouble( value );
    }
    else
      throw new NotImplementedException();
  }
}
