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
package org.kalypso.zml.ui.table.layout;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.ZmlTableComposite;

/**
 * @author Dirk Kuch
 */
public class RevealTableCommand implements ICoreRunnableWithProgress
{

  private final ZmlTableComposite m_table;

  public RevealTableCommand( final ZmlTableComposite table )
  {
    m_table = table;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final Date forecastStart = findForecastDate();
    if( forecastStart == null )
      return Status.CANCEL_STATUS;

    final IZmlModel model = m_table.getDataModel();
    final IZmlModelRow[] rows = model.getRows();
    for( final IZmlModelRow row : rows )
    {
      final Object objIndex = row.getIndexValue();
      if( !(objIndex instanceof Date) )
        continue;

      final Date index = (Date) objIndex;

      if( index.equals( forecastStart ) )
      {
        final TableViewer tableViewer = m_table.getTableViewer();
        tableViewer.reveal( row );

        return Status.OK_STATUS;
      }
    }

    return Status.CANCEL_STATUS;
  }

  private Date findForecastDate( )
  {
    final IZmlModel model = m_table.getDataModel();
    final IZmlModelColumn[] columns = model.getColumns();
    for( final IZmlModelColumn column : columns )
    {
      if( column.isMetadataSource() )
      {
        final Date date = findForecastDate( column );
        if( date != null )
          return date;
      }
    }

    for( final IZmlModelColumn column : columns )
    {
      final Date date = findForecastDate( column );
      if( date != null )
        return date;
    }

    return null;
  }

  private Date findForecastDate( final IZmlModelColumn column )
  {
    final MetadataList metadata = column.getMetadata();
    return MetadataHelper.getForecastStart( metadata );
  }

}
