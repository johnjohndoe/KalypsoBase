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
package org.kalypso.chart.ui.editor.commandhandler.utils;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Dirk Kuch
 */
public class CommandHandlerUtils
{

  /**
   * @return command is enable or not
   */
  public static boolean isEnabled( final ExecutionEvent event )
  {
    /**
     * event specifies an parameter?
     */
    final String parameter = event.getParameter( "enabled" ); // $NON-NLS-1$
    if( StringUtils.isNotEmpty( parameter ) )
      return Boolean.valueOf( parameter );

    /**
     * tool item state
     */
    final Object trigger = event.getTrigger();
    if( trigger instanceof Event )
    {
      final Event triggerEvent = (Event) trigger;
      final ToolItem item = (ToolItem) triggerEvent.widget;

      return item.getSelection();
    }

    return true;
  }

}
