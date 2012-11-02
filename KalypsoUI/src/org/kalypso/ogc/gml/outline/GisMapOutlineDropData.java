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
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;

/**
 * @author Gernot Belger
 */
public class GisMapOutlineDropData
{
  private final IKalypsoLayerModell m_layerModel;

  private final int m_insertionIndex;

  public GisMapOutlineDropData( final IKalypsoLayerModell layerModel, final int insertionIndex )
  {
    m_layerModel = layerModel;
    m_insertionIndex = insertionIndex;
  }

  public IKalypsoLayerModell getLayerModel( )
  {
    return m_layerModel;
  }

  public int getInsertionIndex( )
  {
    return m_insertionIndex;
  }

  public static GisMapOutlineDropData fromCurrentSelectionNonNull( final IKalypsoLayerModell mapModel, final Object currentTarget, final int currentLocation )
  {
    final GisMapOutlineDropData data = fromCurrentSelection( mapModel, currentTarget, currentLocation );
    if( data != null )
      return data;

    return new GisMapOutlineDropData( mapModel, 0 );
  }

  public static GisMapOutlineDropData fromCurrentSelection( final IKalypsoLayerModell mapModel, final Object currentTarget, final int currentLocation )
  {
    final int defaultInsertionIndex = 0;

    if( currentLocation == ViewerDropAdapter.LOCATION_NONE )
      return new GisMapOutlineDropData( mapModel, defaultInsertionIndex );

    final IKalypsoTheme theme = findCurrentTheme( currentTarget );
    if( theme == null )
      return null;

    if( theme instanceof IKalypsoCascadingTheme && currentLocation == ViewerDropAdapter.LOCATION_ON )
      return new GisMapOutlineDropData( (IKalypsoCascadingTheme)theme, defaultInsertionIndex );

    final IMapModell model = theme.getMapModell();
    if( model instanceof IKalypsoLayerModell )
    {
      final int insertionIndex = findIndexOf( model, theme, currentLocation );
      return new GisMapOutlineDropData( (IKalypsoLayerModell)model, insertionIndex );
    }

    return null;
  }

  private static int findIndexOf( final IMapModell model, final IKalypsoTheme theme, final int currentLocation )
  {
    final IKalypsoTheme[] allThemes = model.getAllThemes();
    final int index = ArrayUtils.indexOf( allThemes, theme );
    if( index == -1 )
      return 0;

    if( currentLocation == ViewerDropAdapter.LOCATION_AFTER )
      return index + 1;

    return index;
  }

  private static IKalypsoTheme findCurrentTheme( final Object currentTarget )
  {
    if( currentTarget instanceof IKalypsoTheme )
      return (IKalypsoTheme)currentTarget;

    if( currentTarget instanceof IThemeNode )
    {
      final IThemeNode node = (IThemeNode)currentTarget;
      final Object element = node.getElement();
      if( element instanceof IKalypsoTheme )
        return (IKalypsoTheme)element;
    }

    return null;
  }
}