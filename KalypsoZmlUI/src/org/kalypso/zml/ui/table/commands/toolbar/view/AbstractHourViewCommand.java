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
package org.kalypso.zml.ui.table.commands.toolbar.view;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractHourViewCommand extends AbstractHandler implements IElementUpdater
{

  protected IStatus updateResulution( final ExecutionEvent event, final int resultion, final boolean mode )
  {
    final IZmlTable table = ZmlHandlerUtil.getTable( event );

    final ZmlViewResolutionFilter filter = resolveFilter( table );
    filter.setParameters( resultion, mode );

    table.getTableViewer().refresh();

    return Status.OK_STATUS;
  }

  protected IStatus updateOffset( final ExecutionEvent event, final int number )
  {
    final IZmlTable table = ZmlHandlerUtil.getTable( event );

    final ZmlViewResolutionFilter filter = resolveFilter( table );
    filter.add2Offset( number );

    table.getTableViewer().refresh();

    return Status.OK_STATUS;
  }

  public static ZmlViewResolutionFilter resolveFilter( final IZmlTable table )
  {
    final TableViewer viewer = table.getTableViewer();
    final ViewerFilter[] filters = viewer.getFilters();
    for( final ViewerFilter filter : filters )
    {
      if( filter instanceof ZmlViewResolutionFilter )
      {
        return (ZmlViewResolutionFilter) filter;
      }
    }

    return null;
  }

  @Override
  public void updateElement( final UIElement element, @SuppressWarnings("rawtypes") final Map parameters )
  {
    final IServiceLocator locator = element.getServiceLocator();
    final IZmlTable table = ZmlHandlerUtil.getTable( locator );

    if( table != null )
    {
      final ZmlViewResolutionFilter filter = resolveFilter( table );
      element.setChecked( isActive( filter ) );
    }
  }

  protected abstract boolean isActive( ZmlViewResolutionFilter filter );
}
