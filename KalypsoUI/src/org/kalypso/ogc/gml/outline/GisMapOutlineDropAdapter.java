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
package org.kalypso.ogc.gml.outline;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.ThemeNodeUtils;
import org.kalypso.ui.addlayer.dnd.AddLayerDndSupport;

/**
 * DropAdapter
 *
 * @author schlienger
 */
class GisMapOutlineDropAdapter extends ViewerDropAdapter
{
  private final AddLayerDndSupport m_layerDropper;

  private final IKalypsoLayerModell m_mapModell;

  GisMapOutlineDropAdapter( final Viewer viewer, final ICommandTarget commandTarget, final IKalypsoLayerModell mapModell )
  {
    super( viewer );

    m_mapModell = mapModell;

    m_layerDropper = new AddLayerDndSupport( commandTarget );

    setScrollExpandEnabled( true );
    setSelectionFeedbackEnabled( true );
    // TODO: allow to insert after/before
    setFeedbackEnabled( false );
  }

  @Override
  public boolean performDrop( final Object data )
  {
    final Shell shell = getViewer().getControl().getShell();

    final IMapModell mapModell = findCurrentModell();
    if( !(mapModell instanceof IKalypsoLayerModell) )
      return false;

    return m_layerDropper.performDrop( shell, data, (IKalypsoLayerModell) mapModell );
  }

  // TODO: not only find model but also find insert position
  private IMapModell findCurrentModell( )
  {
    final int currentLocation = getCurrentLocation();

    switch( currentLocation )
    {
      case LOCATION_NONE:
        return m_mapModell;

      case LOCATION_ON:
      {
        final IKalypsoTheme theme = findCurrentTheme();
        if( theme == null )
          return m_mapModell;

        if( theme instanceof IKalypsoCascadingTheme )
          return (IMapModell) theme;

        return theme.getMapModell();
      }

      case LOCATION_AFTER:
      case LOCATION_BEFORE:
      default:
      {
        final IKalypsoTheme theme = findCurrentTheme();
        if( theme == null )
          return m_mapModell;

        return theme.getMapModell();
      }
    }
  }

  private IKalypsoTheme findCurrentTheme( )
  {
    final Object currentTarget = getCurrentTarget();
    if( currentTarget instanceof IKalypsoTheme )
      return (IKalypsoTheme) currentTarget;

    if( currentTarget instanceof IThemeNode )
    {
      final IThemeNode node = (IThemeNode) currentTarget;
      return ThemeNodeUtils.findTheme( node );
    }

    return null;
  }

  @Override
  public boolean validateDrop( final Object target, final int operation, final TransferData transferType )
  {
    return m_layerDropper.validateDrop( target, operation, transferType );
  }
}