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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureThemeInfo;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeInfo;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder;
import org.kalypso.ogc.gml.map.widgets.builders.PointGeometryBuilder;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.widgets.DeprecatedMouseWidget;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * A widget to select a single feature.<br>
 * De-selection is not possible.<br>
 * <br>
 * <code>Ctrl</code> toggles toggle-modus.
 * 
 * @author Gernot Belger
 */
public class SelectSingleFeatureWidget extends DeprecatedMouseWidget
{
  private static final String THEME_PROPERTY_SHOW_INFO = "singleSelectShowInfo"; //$NON-NLS-1$

  private final IMapModellListener m_mapModellListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeStatusChanged(org.kalypso.ogc.gml.mapmodel.IMapModell, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeStatusChanged( final IMapModell source, final IKalypsoTheme theme )
    {
      reinit();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell, org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      reinit();
    }
  };

  private final ToolTipRenderer m_tooltipRenderer = new ToolTipRenderer();

  private IGeometryBuilder m_geometryBuilder;

  private IKalypsoFeatureTheme[] m_themes;

  private final QName[] m_qnamesToSelect;

  private final QName m_geomQName;

  /** The feature the mouse is currently over */
  private Feature m_hoverFeature;

  /** The theme, the hover feature blongs to */
  private IKalypsoFeatureTheme m_hoverTheme;

  private Point m_currentPoint;

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
  public SelectSingleFeatureWidget( final String name, final String toolTip, final QName[] qnamesToSelect, final QName geomQName )
  {
    super( name, toolTip );
    m_qnamesToSelect = qnamesToSelect;
    m_geomQName = geomQName;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget, org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final IMapModell mapModell = mapPanel.getMapModell();
    if( mapModell == null )
      return;

    mapModell.addMapModelListener( m_mapModellListener );

    reinit();
  }

  protected void reinit( )
  {
    m_geometryBuilder = new PointGeometryBuilder( getMapPanel().getMapModell().getCoordinatesSystem() );

    m_themes = null;
    m_hoverFeature = null;
    m_hoverTheme = null;

    repaintMap();

    m_themes = initializeThemes();
  }

  /**
   * Initialized the themes, from which features can be selected.<br/>
   * The default implementation return the active theme.
   */
  protected IKalypsoFeatureTheme[] initializeThemes( )
  {
    final IMapPanel mapPanel = getMapPanel();
    final IMapModell mapModell = mapPanel.getMapModell();

    final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
    if( activeTheme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme[] themes = new IKalypsoFeatureTheme[1];
      themes[0] = (IKalypsoFeatureTheme)activeTheme;
      return themes;
    }

    return null;
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
    final GM_Point currentPoint = MapUtilities.transform( mapPanel, p );
    final GM_Position currentPos = currentPoint.getPosition();

    setHoverFeature( null, null, currentPos );

    final IKalypsoFeatureTheme[] themes = m_themes;
    if( themes == null || m_currentPoint == null )
      return;

    final double grabDistance = MapUtilities.calculateWorldDistance( mapPanel, currentPoint, SelectFeatureWidget.GRAB_RADIUS * 2 );
    final GM_Envelope reqEnvelope = GeometryUtilities.grabEnvelopeFromDistance( currentPoint, grabDistance );

    for( final IKalypsoFeatureTheme theme : themes )
    {
      if( theme == null )
        continue;

      final FeatureList featureList = theme.getFeatureList();
      if( featureList == null )
        continue;

      final Feature feature = findCurrentFeature( theme, currentPoint, grabDistance, reqEnvelope );
      /* grab to the first feature that you can get */
      if( feature != null )
      {
        setHoverFeature( feature, theme, currentPos );
        break;
      }
    }

    repaintMap();
  }

  private Feature findCurrentFeature( final IKalypsoFeatureTheme theme, final GM_Point currentPoint, final double grabDistance, final GM_Envelope requestEnvelope )
  {
    if( theme == null )
      return null;

    final FeatureList featureList = theme.getFeatureList();
    if( featureList == null )
      return null;

    if( !(m_geometryBuilder instanceof PointGeometryBuilder) )
      return null;

    /* Grab next feature */
    final GMLXPath[] geometryPathes = SelectFeatureWidget.findGeometryPathes( theme, m_geomQName, IKalypsoFeatureTheme.PROPERTY_SELECTABLE_GEOMETRIES, null );

    final FeatureList visibleFeatures = theme.getFeatureListVisible( requestEnvelope );

    return GeometryUtilities.findNearestFeature( currentPoint, grabDistance, visibleFeatures, geometryPathes, m_qnamesToSelect );
  }

  private void setHoverFeature( final Feature feature, final IKalypsoFeatureTheme theme, final GM_Position position )
  {
    m_hoverFeature = feature;
    m_hoverTheme = theme;

    m_tooltipRenderer.setTooltip( null );
    if( theme != null )
    {
      final boolean showInfo = Boolean.parseBoolean( m_hoverTheme.getProperty( THEME_PROPERTY_SHOW_INFO, "false" ) ); //$NON-NLS-1$
      if( showInfo )
      {
        final String info = getInfo( theme, m_hoverFeature, position );
        m_tooltipRenderer.setTooltip( info );
      }
    }
  }

  private String getInfo( final IKalypsoFeatureTheme theme, final Feature feature, final GM_Position position )
  {
    final IKalypsoThemeInfo themeInfo = (IKalypsoThemeInfo)theme.getAdapter( IKalypsoThemeInfo.class );
    if( themeInfo == null )
      return Messages.getString( "SelectSingleFeatureWidget.2" ); //$NON-NLS-1$

    final Formatter formatter = new Formatter();
    if( themeInfo instanceof IKalypsoFeatureThemeInfo )
      ((IKalypsoFeatureThemeInfo)themeInfo).formatInfo( formatter, feature );
    else
      themeInfo.appendQuickInfo( formatter, position );
    formatter.flush();
    return formatter.toString();
  }

  @Override
  public void mousePressed( final MouseEvent event )
  {
    if( event.getButton() != MouseEvent.BUTTON1 )
      return;

    event.consume();

    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    try
    {
      /* just snap to grabbed feature */
      if( m_hoverFeature != null && m_hoverTheme != null )
      {
        final List<Feature> selectedFeature = Collections.singletonList( m_hoverFeature );
        final Map<IKalypsoFeatureTheme, List<Feature>> selection = Collections.singletonMap( m_hoverTheme, selectedFeature );

        final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();

        final boolean toggle = event.isControlDown();
        if( selectionManager.size() == 1 && toggle )
        {
          // Do not allow deselection of last item
          if( selectionManager.getAllFeatures()[0].getFeature() == m_hoverFeature )
            return;
        }

        SelectFeatureWidget.changeSelection( selectionManager, selection, false, toggle );
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

  @Override
  public void mouseClicked( final MouseEvent e )
  {
  }

  @Override
  public void mouseEntered( final MouseEvent e )
  {
  }

  @Override
  public void mouseExited( final MouseEvent e )
  {
  }

  @Override
  public void mouseReleased( final MouseEvent e )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    SelectFeatureWidget.paintHoverFeature( g, mapPanel, m_geometryBuilder, m_currentPoint, m_hoverFeature, m_hoverTheme, m_geomQName );

    final Rectangle screenBounds = mapPanel.getScreenBounds();

    if( m_currentPoint != null )
      m_tooltipRenderer.paintToolTip( m_currentPoint, g, screenBounds );
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

  public void setThemes( final IKalypsoFeatureTheme[] themes )
  {
    m_themes = themes;
  }
}
