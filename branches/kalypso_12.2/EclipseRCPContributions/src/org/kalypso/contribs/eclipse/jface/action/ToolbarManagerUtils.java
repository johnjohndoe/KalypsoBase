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
package org.kalypso.contribs.eclipse.jface.action;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Utilities for the {@link org.eclipse.jface.action.ToolBarManager}.
 * 
 * @author Gernot Belger
 */
public final class ToolbarManagerUtils
{
  private ToolbarManagerUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds commands to a toolbar.<br/>
   * 
   * @param commands
   *          A map of commandId -> commandStyle. To add a separator, use an empty commandId.
   */
  public static void addCommands( final ToolBarManager manager, final Map<String, Integer> commands, final IServiceLocator serviceLocator )
  {
    for( final Entry<String, Integer> entry : commands.entrySet() )
    {
      final String cmdId = entry.getKey();
      final Integer cmdStyle = entry.getValue();

      if( cmdId == null || cmdId.trim().isEmpty() )
      {
        final Separator sep = new Separator();
        sep.setVisible( true );
        manager.add( sep );
      }
      else
      {
        final CommandContributionItemParameter cmdParams = new CommandContributionItemParameter( serviceLocator, cmdId + "_item_", cmdId, cmdStyle ); //$NON-NLS-1$
        final CommandContributionItem contribItem = new CommandContributionItem( cmdParams );
        manager.add( contribItem );
      }
    }
  }
}