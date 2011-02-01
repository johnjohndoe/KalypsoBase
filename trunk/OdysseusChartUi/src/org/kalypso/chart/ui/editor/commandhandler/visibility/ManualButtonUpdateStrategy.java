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
package org.kalypso.chart.ui.editor.commandhandler.visibility;

import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.ui.menus.UIElement;

/**
 * @author Dirk Kuch
 */
public class ManualButtonUpdateStrategy implements IUpdateElementStrategy
{
  @SuppressWarnings("rawtypes")
  final Map m_parameters;

  private static String[] ACTIVE_COMMANDS;

  public ManualButtonUpdateStrategy( @SuppressWarnings("rawtypes") final Map parameters )
  {
    m_parameters = parameters;
  }

  /**
   * @see org.kalypso.chart.ui.editor.commandhandler.visibility.IUpdateElementStrategy#update(org.eclipse.ui.menus.UIElement)
   */
  @Override
  public void update( final UIElement element )
  {
    if( ArrayUtils.isEmpty( ACTIVE_COMMANDS ) )
    {
      element.setChecked( false );
      return;
    }

    final Boolean enabled = ArrayUtils.contains( ACTIVE_COMMANDS, getCommandURI() );
    element.setChecked( enabled );
  }

  private String getCommandURI( )
  {
    final String value = (String) m_parameters.get( ChangeVisibilityCommandHandler.LAYER_PARAMETER );

    return String.format( "%s#%s=%s", ChangeVisibilityCommandHandler.ID, ChangeVisibilityCommandHandler.LAYER_PARAMETER, value );
  }

  public static void setActiveCommands( final String[] commands )
  {
    ACTIVE_COMMANDS = commands;
  }

}
