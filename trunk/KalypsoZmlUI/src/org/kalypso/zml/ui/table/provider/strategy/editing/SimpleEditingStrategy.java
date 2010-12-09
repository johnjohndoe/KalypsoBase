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

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class SimpleEditingStrategy extends AbstractEditingStrategy implements IZmlEditingStrategy
{
  public SimpleEditingStrategy( final ExtendedZmlTableColumn column )
  {
    super( column );
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy#getValue(java.lang.Object)
   */
  @Override
  public String getValue( final IZmlModelRow row )
  {
    try
    {

      final IZmlValueReference reference = row.get( getColumn().getColumnType().getType() );
      if( reference == null )
        return "";

      final Object value = reference.getValue();

      final CellStyle style = getStyle();
      return String.format( style.getTextFormat() == null ? "%s" : style.getTextFormat(), value );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.strategy.editing.IZmlEditingStrategy#setValue(org.kalypso.zml.ui.table.model.IZmlModelRow,
   *      java.lang.String)
   */
  @Override
  public void setValue( final IZmlModelRow element, final String value )
  {
    try
    {
      final IZmlModelRow row = element;
      final IZmlValueReference reference = row.get( getColumn().getColumnType().getType() );

      final Object targetValue = getTargetValue( value );
      reference.update( targetValue );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }
}
