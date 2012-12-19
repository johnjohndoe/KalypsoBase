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
package org.kalypso.zml.ui.table.commands.toolbar.view;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.util.FindTimeStepOperation;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;

/**
 * @author Dirk Kuch
 */
public final class HourViewCommands
{
  private HourViewCommands( )
  {
  }

  public static Period getTimeStep( final ExecutionEvent event )
  {
    final IZmlTable table = ZmlHandlerUtil.getTable( event );

    return getTimeStep( table );
  }

  public static Period getTimeStep( final IZmlTable table )
  {
    if( table == null )
      return null;

    final ZmlModelViewport viewport = table.getModelViewport();

    final IZmlModelColumn[] columns = viewport.getColumns();
    for( final IZmlModelColumn column : columns )
    {
      final MetadataList metadata = column.getMetadata();
      final Period timestep = MetadataHelper.getTimestep( metadata );
      if( Objects.isNotNull( timestep ) )
        return timestep;
    }

    for( final IZmlModelColumn column : columns )
    {
      final IObservation observation = column.getObservation();
      if( Objects.isNull( observation ) )
        continue;

      final FindTimeStepOperation operation = new FindTimeStepOperation( observation );
      operation.execute( new NullProgressMonitor() );

      final Period timestep = operation.getTimestep();
      if( Objects.isNotNull( timestep ) )
        return timestep;
    }

    return null;
  }
}
