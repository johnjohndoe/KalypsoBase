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

package org.kalypso.contribs.eclipse.ui.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceScopes;

/**
 * @author Gernot Belger
 */
public class CommandUtilities
{
  private CommandUtilities( )
  {
    throw new UnsupportedOperationException( "Utility class, do not instantiate" );
  }

  /**
   * Refreshes all commands of a given command category.
   */
  public static void refreshElementsForWindow( final IWorkbenchWindow window, final String categoryId ) throws CommandException
  {
    final ICommandService commandService = (ICommandService) window.getService( ICommandService.class );

    final Map<Object, Object> filter = new HashMap<Object, Object>();
    filter.put( IServiceScopes.WINDOW_SCOPE, window );

    refreshElements( commandService, categoryId, filter );
  }

  /**
   * Refreshes all commands of a given command category.
   */
  public static void refreshElements( final ICommandService commandService, final String categoryId, final Map<Object, Object> filter ) throws CommandException
  {
    final Command[] commands = getCommands( commandService, categoryId );
    for( final Command command : commands )
      commandService.refreshElements( command.getId(), filter );
  }

  /**
   * Gets all commands of a given category id.
   */
  public static Command[] getCommands( final ICommandService commandService, final String categoryId ) throws CommandException
  {
    final List<Command> result = new ArrayList<Command>();

    final Category category = commandService.getCategory( categoryId );
    if( category == null )
      throw new NotDefinedException( "Unknown category: " + categoryId );

    final Command[] commands = commandService.getDefinedCommands();
    for( final Command command : commands )
    {
      final Category cmdCat = command.getCategory();
      if( cmdCat.equals( category ))
        result.add( command );
    }

    return result.toArray( new Command[result.size()] );
  }

}
