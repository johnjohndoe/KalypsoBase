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
package org.kalypso.commons.eclipse.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypso.contribs.eclipse.jface.action.ToolbarManagerUtils;

/**
 * Helper for source objects (like map, chart, ...) that are embedded into another part. This helper controls the
 * execution of commands in this context.
 *
 * @author Gernot Belger
 */
public class EmbeddedSourceToolbarManager
{
  private final Collection<IExecutionListener> m_executionListeners = new ArrayList<>();

  private final IServiceLocator m_serviceLocator;

  private final String m_sourceName;

  private final Object m_sourceValue;

  public EmbeddedSourceToolbarManager( final IServiceLocator locator, final String sourceName, final Object sourceValue )
  {
    m_serviceLocator = locator;
    m_sourceName = sourceName;
    m_sourceValue = sourceValue;
  }

  public void fillToolbar( final ToolBarManager manager, final CommandWithStyle[] commands )
  {
    final ToolBar toolBar = manager.getControl();
    if( commands.length == 0 )
      return;

    final IWorkbench serviceLocator = PlatformUI.getWorkbench();
    ToolbarManagerUtils.addCommands( manager, commands, serviceLocator );
    manager.update( true );

    final ICommandService cmdService = (ICommandService)serviceLocator.getService( ICommandService.class );
    final IHandlerService handlerService = (IHandlerService)serviceLocator.getService( IHandlerService.class );

    final String[] commandIDs = getCommandIDs( commands );
    final EmbeddedSourceExecutionListener executionListener = new EmbeddedSourceExecutionListener( commandIDs, handlerService, toolBar, m_sourceName, m_sourceValue );
    cmdService.addExecutionListener( executionListener );
    m_executionListeners.add( executionListener );
  }

  private String[] getCommandIDs( final CommandWithStyle[] commands )
  {
    final String[] ids = new String[commands.length];
    for( int i = 0; i < ids.length; i++ )
      ids[i] = commands[i].getCommandID();
    return ids;
  }

  public void dispose( )
  {
    /* Unhook all previously created listeners */
    final ICommandService cmdService = (ICommandService)m_serviceLocator.getService( ICommandService.class );
    for( final IExecutionListener listener : m_executionListeners )
      cmdService.removeExecutionListener( listener );
  }

  public static void executeCommand( final IServiceLocator serviceLocator, final ToolBarManager manager, final String commandID )
  {
    try
    {
      final IHandlerService handlerService = (IHandlerService)serviceLocator.getService( IHandlerService.class );
      final Event event = new Event();
      if( manager != null )
        event.widget = manager.getControl();

      if( !StringUtils.isBlank( commandID ) )
        handlerService.executeCommand( commandID, event );
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
    }
  }
}