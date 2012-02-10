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
package org.kalypso.zml.ui.table.commands.menu.adjust.pages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;

/**
 * @author Dirk Kuch
 */
public class ShiftDateRunnable implements ICoreRunnableWithProgress
{
  private final Integer m_offset;

  private final IZmlModelColumn m_column;

  private final IZmlTableValueCell[] m_cells;

  public ShiftDateRunnable( final IZmlModelColumn column, final IZmlTableValueCell[] cells, final Integer offset )
  {
    m_column = column;
    m_cells = cells;
    m_offset = offset;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      final ShiftDateValuesVisitor visitor = new ShiftDateValuesVisitor( m_cells, m_offset );
      m_column.accept( visitor );

      visitor.doFinish();
    }
    catch( final SensorException e )
    {
      return StatusUtilities.createExceptionalErrorStatus( "Anpassen fehlgeschlagen", e );
    }

    return Status.OK_STATUS;
  }
}
