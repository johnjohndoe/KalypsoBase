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
package org.kalypso.chart.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceScopes;
import org.kalypso.chart.ui.IChartPart;

/**
 * part listener for all chart parts
 * 
 * @author burtscher1
 * 
 */
public class ChartPartListener implements IPartListener2
{

  private final IChartPart m_chartPart;

  public static final String CHARTUI_COMMAND_CATEGORY = "org.kalypso.chart.ui.commands.category";

  public ChartPartListener( IChartPart part )
  {
    m_chartPart = part;
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partActivated( IWorkbenchPartReference partRef )
  {
    updateElements( partRef.getPart( false ) );
  }

  /**
   * helper function - it calls all commands from the chart ui commands category to refresh their state; function is
   * public as it is called from the command handlers so mulitple contributions of a command (e.g. toolbar and menu) are
   * synchronized
   */
  public static void updateElements( IWorkbenchPart part )
  {
    if( part instanceof IChartPart )
    {
      IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
      Map filter = new HashMap();
      filter.put( IServiceScopes.WINDOW_SCOPE, window );
      ICommandService commandService = (ICommandService) window.getService( ICommandService.class );
      Command[] commands = commandService.getDefinedCommands();
      for( int i = 0; i < commands.length; i++ )
      {
        Command command = commands[i];
        Category category = null;
        try
        {
          category = command.getCategory();
        }
        catch( NotDefinedException e )
        {
          // nothing to do; we just ignore the command
        }
        if( category != null && category.getId().equals( CHARTUI_COMMAND_CATEGORY ) )
          commandService.refreshElements( command.getId(), filter );
      }
    }
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partBroughtToTop( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

  /**
   * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partClosed( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

  /**
   * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partDeactivated( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

  /**
   * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partHidden( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

  /**
   * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partInputChanged( IWorkbenchPartReference partRef )
  {
    // nothing to do
  }

  /**
   * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partOpened( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

  /**
   * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
   */
  public void partVisible( IWorkbenchPartReference partRef )
  {
    // nothing to do

  }

}
