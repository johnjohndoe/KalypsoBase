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

import org.apache.commons.lang3.ArrayUtils;
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
    setFeedbackEnabled( true );
  }

  @Override
  public boolean performDrop( final Object data )
  {
    final Shell shell = getViewer().getControl().getShell();

    final GisMapOutlineDropData dropData = findCurrentModell();
    if( data == null )
      return false;

    return m_layerDropper.performDrop( shell, data, dropData );
  }

  private GisMapOutlineDropData findCurrentModell( )
  {
    final int currentLocation = getCurrentLocation();

    final int defaultInsertionIndex = 0;

    if( currentLocation == LOCATION_NONE )
      return new GisMapOutlineDropData( m_mapModell, defaultInsertionIndex );

    final IKalypsoTheme theme = findCurrentTheme();
    if( theme == null )
      return null;
// return new GisMapOutlineDropData( m_mapModell, defaultInsertionIndex );

    if( theme instanceof IKalypsoCascadingTheme && currentLocation == LOCATION_ON )
      return new GisMapOutlineDropData( (IKalypsoCascadingTheme) theme, defaultInsertionIndex );

    final IMapModell model = theme.getMapModell();
    if( model instanceof IKalypsoLayerModell )
    {
      final int insertionIndex = findIndexOf( model, theme, currentLocation );
      return new GisMapOutlineDropData( (IKalypsoLayerModell) model, insertionIndex );
    }

    return null;
  }

  private int findIndexOf( final IMapModell model, final IKalypsoTheme theme, final int currentLocation )
  {
    final IKalypsoTheme[] allThemes = model.getAllThemes();
    final int index = ArrayUtils.indexOf( allThemes, theme );
    if( index == -1 )
      return 0;

    if( currentLocation == LOCATION_AFTER )
      return index + 1;

    return index;
  }

  private IKalypsoTheme findCurrentTheme( )
  {
    final Object currentTarget = getCurrentTarget();
    if( currentTarget instanceof IKalypsoTheme )
      return (IKalypsoTheme) currentTarget;

    if( currentTarget instanceof IThemeNode )
    {
      final IThemeNode node = (IThemeNode) currentTarget;
      final Object element = node.getElement();
      if( element instanceof IKalypsoTheme )
        return (IKalypsoTheme) element;
    }

    return null;
  }

  @Override
  public boolean validateDrop( final Object target, final int operation, final TransferData transferType )
  {
    /* Prvent drop on non-theme nodes like style elements */
    final GisMapOutlineDropData data = findCurrentModell();
    if( data == null )
      return false;

    return m_layerDropper.validateDrop( target, operation, transferType );
  }
}