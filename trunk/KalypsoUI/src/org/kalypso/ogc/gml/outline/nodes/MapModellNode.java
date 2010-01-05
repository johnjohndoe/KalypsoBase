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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;

/**
 * @author Gernot Belger
 */
class MapModellNode extends AbstractThemeNode<IMapModell>
{
  private final IMapModellListener m_modelListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      final NodeFinder nodeFinder = new NodeFinder( MapModellNode.this );
      final IThemeNode[] elements = nodeFinder.find( new Object[] { previouslyActive, nowActive } );
      ViewerUtilities.update( getViewer(), elements, null, true );
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( final IMapModell source, final IKalypsoTheme theme )
    {
      refreshViewer( null );
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeOrderChanged(org.kalypso.ogc.gml.mapmodel.IMapModell)
     */
    @Override
    public void themeOrderChanged( final IMapModell source )
    {
      refreshViewer( null );
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      final TreeViewer viewer = getViewer();
      ViewerUtilities.refresh( viewer, true );
    }
  };

  private final TreeViewer m_viewer;

  MapModellNode( final IMapModell modell, final TreeViewer viewer )
  {
    super( null, modell );

    m_viewer = viewer;

    modell.addMapModelListener( m_modelListener );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#dispose()
   */
  @Override
  public void dispose( )
  {
    getElement().removeMapModelListener( m_modelListener );

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#resolveI18nString(java.lang.String)
   */
  @Override
  public String resolveI18nString( final String text )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#getElementChildren()
   */
  @Override
  protected Object[] getElementChildren( )
  {
    final IKalypsoTheme[] themes = getElement().getAllThemes().clone();
    ArrayUtils.reverse( themes );
    return themes;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#hasChildrenCompact()
   */
  @Override
  public boolean hasChildrenCompact( )
  {
    return getElementChildren().length > 0;
  }

  @Override
  public IThemeNode[] getChildrenCompact( )
  {
    return getChildren();
  }

  @Override
  public String getLabel( )
  {
    return getElement().getLabel();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#getViewer()
   */
  @Override
  protected TreeViewer getViewer( )
  {
    return m_viewer;
  }
}
