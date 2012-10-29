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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnEvent;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.model.view.ZmlModelViewportResolutionFilter;
import org.kalypso.zml.ui.i18n.Messages;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractHourViewCommand extends AbstractHandler implements IElementUpdater
{
  private static final int MAX_ROWS = 10000;

  protected boolean exceedsMaxSize( final ExecutionEvent event )
  {
    final IZmlTable table = ZmlHandlerUtil.getTable( event );
    final ZmlModelViewport viewport = table.getModelViewport();
    final IZmlModel model = viewport.getModel();

    if( ArrayUtils.getLength( model.getRows() ) > MAX_ROWS )
    {
      return true;
    }

    return false;
  }

  protected boolean openExceedMaxSizeDialog( )
  {
    final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

    final StringBuilder builder = new StringBuilder();
    builder.append( String.format( Messages.AbstractHourViewCommand_0, MAX_ROWS ) );
    builder.append( Messages.AbstractHourViewCommand_1 );
    builder.append( Messages.AbstractHourViewCommand_2 );

    return MessageDialog.openQuestion( shell, Messages.AbstractHourViewCommand_3, builder.toString() );
  }

  protected IStatus updateResulution( final ExecutionEvent event, final int resultion, final boolean mode )
  {
    final IZmlTableComposite composite = ZmlHandlerUtil.getTableComposite( event );
    final IZmlTable table = composite.getTable();

    final ZmlModelViewport viewport = table.getModelViewport();
    final ZmlModelViewportResolutionFilter filter = viewport.getFilter();
    doOffsetAdjustment( table, filter );
    filter.setParameters( resultion, mode );

    viewport.fireModelChanged( IZmlModelColumnEvent.RESULUTION_CHANGED );

    return Status.OK_STATUS;
  }

  private void doOffsetAdjustment( final IZmlTable table, final ZmlModelViewportResolutionFilter filter )
  {
    final IZmlModelRow[] rows = table.getModelViewport().getRows();
    if( ArrayUtils.isEmpty( rows ) )
      return;

    filter.resetOffset();
  }

  protected IStatus updateOffset( final ExecutionEvent event, final int number )
  {
    final IZmlTable table = ZmlHandlerUtil.getTable( event );
    final ZmlModelViewportResolutionFilter filter = table.getModelViewport().getFilter();

// final Period timestep = HourViewCommands.getTimeStep( event );

    filter.add2Offset( number );

    return Status.OK_STATUS;
  }

  public static ZmlModelViewportResolutionFilter resolveFilter( final IZmlTable table )
  {
    if( table == null )
      return null;

    final ZmlModelViewport model = table.getModelViewport();
    return model.getFilter();
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    final IServiceLocator locator = element.getServiceLocator();
    final IZmlTable table = ZmlHandlerUtil.getTable( locator );
    if( Objects.isNotNull( table ) )
    {
      final ZmlModelViewportResolutionFilter filter = resolveFilter( table );
      element.setChecked( isActive( filter ) );
    }
  }

  protected abstract boolean isActive( ZmlModelViewportResolutionFilter filter );
}
