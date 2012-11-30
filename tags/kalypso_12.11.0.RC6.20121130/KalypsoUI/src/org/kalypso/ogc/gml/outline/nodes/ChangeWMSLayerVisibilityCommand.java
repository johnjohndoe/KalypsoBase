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
package org.kalypso.ogc.gml.outline.nodes;

import org.kalypso.commons.command.ICommand;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot
 */
public class ChangeWMSLayerVisibilityCommand implements ICommand
{
  private final KalypsoWMSTheme m_wmsTheme;

  private final String[] m_layer;

  private final boolean m_visible;

  public ChangeWMSLayerVisibilityCommand( final KalypsoWMSTheme wmsTheme, final String[] names, final boolean visible )
  {
    m_wmsTheme = wmsTheme;
    m_layer = names;
    m_visible = visible;
  }

  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  @Override
  public void process( ) throws Exception
  {
    changeVisibility( m_visible );
  }

  @Override
  public void redo( ) throws Exception
  {
    changeVisibility( m_visible );
  }

  @Override
  public void undo( ) throws Exception
  {
    changeVisibility( !m_visible );
  }

  @Override
  public String getDescription( )
  {
    return String.format( Messages.getString( "ChangeWMSLayerVisibilityCommand_0" ), m_layer, m_wmsTheme.getLabel() ); //$NON-NLS-1$
  }

  private void changeVisibility( final boolean visible )
  {
    m_wmsTheme.setLayerVisible( m_layer, visible );
  }
}