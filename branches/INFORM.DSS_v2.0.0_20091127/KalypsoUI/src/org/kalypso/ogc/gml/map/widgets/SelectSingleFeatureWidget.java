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
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder;
import org.kalypso.ogc.gml.map.widgets.builders.PointGeometryBuilder;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * A widget to select a single feature.<br>
 * De-selection is not possible.<br>
 * <br>
 * <code>Ctrl</code> toggles toggle-modus.
 * 
 * @author Gernot Belger
 */
public class SelectSingleFeatureWidget extends AbstractWidget
{
  private final IMapModellListener m_mapModellListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( final IMapModell source, final IKalypsoTheme theme )
    {
      reinit();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      reinit();
    }
  };

  private IGeometryBuilder m_geometryBuilder;

  private IKalypsoFeatureTheme[] m_themes;

  private final QName[] m_qnamesToSelect;

  private final QName m_geomQName;

  /** The feature the mouse is currently over */
  private Feature m_hoverFeature;

  /** The theme, the hover feature blongs to */
  private IKalypsoFeatureTheme m_hoverTheme;

  private Point m_currentPoint;

  private boolean m_toggle = false;


  public SelectSingleFeatureWidget( )
  {
    this( "single select widget", "", new QName[] { Feature.QNAME_FEATURE }, null ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param qnamesToSelect
   *          Only feature, that substitutes at least one of the given feature types (as qnames), will be selected from
   *          the map. If all feature should be selected, use new QName[]{ Feature.QNAME }
   * @param geomQName
   */
  public SelectSingleFeatureWidget( final String name, final String toolTip, final QName qnamesToSelect[], final QName geomQName )
  {
    super( name, toolTip );
    m_qnamesToSelect = qnamesToSelect;
    m_geomQName = geomQName;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    mapPanel.getMapModell().addMapModelListener( m_mapModellListener );

    reinit();
  }

  protected void reinit( )
  {
    m_geometryBuilder = new PointGeometryBuilder( getMapPanel().getMapModell().getCoordinatesSystem() );

    m_themes = null;
    m_hoverFeature = null;
    m_hoverTheme = null;

    final IMapPanel mapPanel = getMapPanel();
    final IMapModell mapModell = mapPanel.getMapModell();
    mapPanel.repaintMap();
    final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
    if( activeTheme instanceof IKalypsoFeatureTheme )
    {
      m_themes = new IKalypsoFeatureTheme[1];
      m_themes[0] = (IKalypsoFeatureTheme) activeTheme;
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#finish()
   */
  @Override
  public void finish( )
  {
    final IMapModell mapModell = getMapPanel().getMapModell();
    if( mapModell != null )
      mapModell.removeMapModelListener( m_mapModellListener );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#moved(java.awt.Point)
   */
  @Override
  public void moved( final Point p )
  {
    m_currentPoint = p;
    final IMapPanel mapPanel = getMapPanel();
    final GM_Point currentPos = MapUtilities.transform( mapPanel, p );

    m_hoverFeature = null;
    m_hoverTheme = null;

    if( m_themes == null || m_currentPoint == null )
      return;

    final double grabDistance = MapUtilities.calculateWorldDistance( mapPanel, currentPos, SelectFeatureWidget.GRAB_RADIUS * 2 );
    final GM_Envelope reqEnvelope = GeometryUtilities.grabEnvelopeFromDistance( currentPos, grabDistance );

    for( final IKalypsoFeatureTheme theme : m_themes )
    {
      if( theme == null )
        continue;

      final FeatureList featureList = theme.getFeatureList();
      if( featureList == null )
        continue;

      if( m_geometryBuilder instanceof PointGeometryBuilder )
      {
        /* Grab next feature */
        final QName[] geomQNames = SelectFeatureWidget.findGeomQName( theme, m_geomQName, IKalypsoFeatureTheme.PROPERTY_SELECTABLE_GEOMETRIES, null );

        final FeatureList visibleFeatures = theme.getFeatureListVisible( reqEnvelope );

        m_hoverFeature = GeometryUtilities.findNearestFeature( currentPos, grabDistance, visibleFeatures, geomQNames, m_qnamesToSelect );

        /* grab to the first feature that you can get */
        if( m_hoverFeature != null )
        {
          m_hoverTheme = theme;
          break;
        }
      }
    }

    repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#leftClicked(java.awt.Point)
   */
  @Override
  public void leftClicked( final Point p )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    try
    {
      /* just snap to grabbed feature */
      if( m_hoverFeature != null )
      {
        final List<Feature> selectedFeatures = new ArrayList<Feature>();
        selectedFeatures.add( m_hoverFeature );
        final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();
        if( selectionManager.size() == 1 && m_toggle )
        {
          // Do not allow deselection of last item
          if( selectionManager.getAllFeatures()[0].getFeature() == m_hoverFeature )
            return;
        }

        SelectFeatureWidget.changeSelection( selectionManager, selectedFeatures, m_themes, false, m_toggle );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      m_geometryBuilder.reset();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#keyPressed(java.awt.event.KeyEvent)
   */
  @Override
  public void keyPressed( final KeyEvent e )
  {
    m_toggle = false;

    final int keyCode = e.getKeyCode();
    switch( keyCode )
    {
      // "STRG": Toggle mode
      case KeyEvent.VK_CONTROL:
        m_toggle = true;
        break;
    }

    repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased( final KeyEvent e )
  {
    m_toggle = false;

    repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    SelectFeatureWidget.paintHoverFeature( g, getMapPanel(), m_geometryBuilder, m_currentPoint, m_hoverFeature, m_hoverTheme, m_geomQName );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#getToolTip()
   */
  @Override
  public String getToolTip( )
  {
    final StringBuffer sb = new StringBuffer().append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.1" ) ); //$NON-NLS-1$

    sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.4" ) ); //$NON-NLS-1$

    return sb.toString();
  }
}
