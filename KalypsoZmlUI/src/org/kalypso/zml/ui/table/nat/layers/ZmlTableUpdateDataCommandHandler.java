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
package org.kalypso.zml.ui.table.nat.layers;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.edit.command.UpdateDataCommand;
import net.sourceforge.nattable.edit.command.UpdateDataCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.event.CellVisualChangeEvent;

/**
 * @author Dirk Kuch
 */
public class ZmlTableUpdateDataCommandHandler extends UpdateDataCommandHandler implements ILayerCommandHandler<UpdateDataCommand>
{

  private final DataLayer m_dataLayer;

  public ZmlTableUpdateDataCommandHandler( final DataLayer dataLayer )
  {
    super( dataLayer );
    m_dataLayer = dataLayer;
  }

  @Override
  protected boolean doCommand( final UpdateDataCommand command )
  {
    try
    {
      final int columnPosition = command.getColumnPosition();
      final int rowPosition = command.getRowPosition();

      m_dataLayer.getDataProvider().setDataValue( columnPosition, rowPosition, command.getNewValue() );
      m_dataLayer.fireLayerEvent( new CellVisualChangeEvent( m_dataLayer, columnPosition, rowPosition ) );

      return true;
    }
    catch( final UnsupportedOperationException e )
    {
      e.printStackTrace( System.err );
      System.err.println( "Failed to update value to: " + command.getNewValue() );
      return false;
    }
  }

}
