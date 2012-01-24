/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.lang.Arrays;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.widgets.builders.IGeometryBuilder;
import org.kalypso.ogc.gml.map.widgets.builders.PointGeometryBuilder;
import org.kalypso.ogc.gml.map.widgets.builders.PolygonGeometryBuilder;
import org.kalypso.ogc.gml.map.widgets.builders.RectangleGeometryBuilder;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.util.MapUtils;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * General selection widget that allows multiple ways of selection (by point, by polygon, by rectangle). The user can
 * change between them by pressing the 'SPACE' key. <br>
 * <br>
 * Advanced selection modi are included as follows:<br>
 * TOGGLE (on via pressed 'STRG' key):<br>
 * already selected features gets toggled by the new selection (already selected get de-selected, not selected get
 * selected).<br>
 * ADD (on via pressed 'SHIFT' key):<br>
 * the new selection is added to the current selection. INTERSECT / CONTAINS (via 'ALT' key):<br>
 * If pressed selection is done by using INTERSECT-method.
 * 
 * @author Thomas Jung
 */
public class SelectFeatureWidget extends AbstractWidget
{
  public static final int GRAB_RADIUS = 20;

  private static final String SETTINGS_MODE = "selectionMode";

  private IGeometryBuilder m_geometryBuilder;

  private IKalypsoFeatureTheme[] m_themes;

  private final QName[] m_qnamesToSelect;

  private final QName m_geomQName;

  /** The feature the mouse is currently over */
  private Feature m_hoverFeature;

  /** The theme, the hover feature blongs to */
  private IKalypsoFeatureTheme m_hoverTheme;

  private boolean m_toggleMode;

  private boolean m_addMode;

  private Point m_currentPoint;

  private boolean m_intersectMode;

  private final IMapModellListener m_mapModelListener = new MapModellAdapter()
  {
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      reinit();
    }
  };

  private int m_currentMode = 0;

  private UIJob m_selectOnHoverJob;

  private static final int MODE_HOVER = 3;

  /**
   * @param qnamesToSelect
   *          Only feature, that substitutes at least one of the given feature types (as qnames), will be selected from
   *          the map. If all feature should be selected, use new QName[]{ Feature.QNAME }
   * @param geomQName
   */
  public SelectFeatureWidget( final String name, final String toolTip, final QName qnamesToSelect[], final QName geomQName )
  {
    super( name, toolTip );
    m_qnamesToSelect = qnamesToSelect;
    m_geomQName = geomQName;
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final IMapModell mapModell = mapPanel.getMapModell();
    if( mapModell != null )
      mapModell.addMapModelListener( m_mapModelListener );

    reinit();
  }

  @Override
  public void finish( )
  {
    final IMapPanel mapPanel = getMapPanel();
    final IMapModell model = mapPanel == null ? null : mapPanel.getMapModell();
    if( model != null )
      model.removeMapModelListener( m_mapModelListener );

    super.finish();
  }

  protected void reinit( )
  {
    // default: selection by Rectangle

    final IDialogSettings settings = getSettings();
    if( settings != null )
    {
      try
      {
        m_currentMode = settings.getInt( SETTINGS_MODE );
      }
      catch( final NumberFormatException ignored )
      {
      }
    }

    m_geometryBuilder = createGeometryBuilder();

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

  @Override
  public void moved( final Point p )
  {
    m_currentPoint = p;
    final IMapPanel mapPanel = getMapPanel();
    final GM_Point currentPos = MapUtilities.transform( mapPanel, p );

    m_hoverFeature = null;
    m_hoverTheme = null;

    if( m_themes == null || currentPos == null )
      return;

    final double grabDistance = MapUtilities.calculateWorldDistance( mapPanel, currentPos, GRAB_RADIUS * 2 );
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
        /* Grab next feature. */
        final GMLXPath[] geomQNames = findGeometryPathes( theme, m_geomQName, IKalypsoFeatureTheme.PROPERTY_SELECTABLE_GEOMETRIES, null );
        final FeatureList visibleFeatures = theme.getFeatureListVisible( reqEnvelope );

        /* Grab to the first feature that you can get. */
        m_hoverFeature = GeometryUtilities.findNearestFeature( currentPos, grabDistance, visibleFeatures, geomQNames, m_qnamesToSelect );
        if( m_hoverFeature != null )
        {
          m_hoverTheme = theme;

          if( MODE_HOVER == m_currentMode )
          {
            if( Objects.isNotNull( m_selectOnHoverJob ) )
              m_selectOnHoverJob.cancel();

            m_selectOnHoverJob = new UIJob( "Select on hover" )
            {
              @Override
              public IStatus runInUIThread( final IProgressMonitor monitor )
              {
                if( monitor.isCanceled() )
                  return Status.CANCEL_STATUS;

                leftPressed( p );

                return Status.OK_STATUS;
              }
            };

            m_selectOnHoverJob.setSystem( true );
            m_selectOnHoverJob.setUser( false );
            m_selectOnHoverJob.schedule( 50 );

          }

          break;

        }

      }
    }

    repaintMap();
  }

  @Override
  public void dragged( final Point p )
  {
    m_currentPoint = p;

    final IMapPanel panel = getMapPanel();
    if( panel != null )
      panel.repaintMap();

    super.dragged( p );
  }

  @Override
  public void leftPressed( final Point p )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( Objects.isNull( mapPanel, m_geometryBuilder ) )
      return;

    try
    {
      if( m_geometryBuilder instanceof RectangleGeometryBuilder )
      {
        final GM_Point point = MapUtilities.transform( mapPanel, p );
        m_geometryBuilder.addPoint( point );
      }
      else if( m_geometryBuilder instanceof PointGeometryBuilder )
      {
        /* just snap to grabbed feature */
        if( m_hoverFeature != null )
        {
          final List<Feature> selectedFeature = Collections.singletonList( m_hoverFeature );
          final Map<IKalypsoFeatureTheme, List<Feature>> selection = Collections.singletonMap( m_hoverTheme, selectedFeature );

          final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();

          doChangeSelection( selectionManager, selection, m_addMode, m_toggleMode );
        }
        m_geometryBuilder.reset();
      }
      else if( m_geometryBuilder instanceof PolygonGeometryBuilder )
      {
        final GM_Point point = MapUtilities.transform( mapPanel, p );
        m_geometryBuilder.addPoint( point );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    super.leftPressed( p );
  }

  @Override
  public void leftClicked( final Point p )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null || m_geometryBuilder == null )
      return;

    super.leftClicked( p );
  }

  @Override
  public void doubleClickedLeft( final Point p )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( Objects.isNull( mapPanel, m_geometryBuilder ) )
      return;

    try
    {
      if( m_geometryBuilder instanceof PolygonGeometryBuilder )
      {
        final GM_Point point = MapUtilities.transform( mapPanel, p );
        m_geometryBuilder.addPoint( point );
        final GM_Object selectGeometry = m_geometryBuilder.finish();
        doSelect( selectGeometry );
        m_geometryBuilder.reset();
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    super.doubleClickedLeft( p );
  }

  @Override
  public void keyPressed( final KeyEvent e )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    m_toggleMode = false;
    m_addMode = false;
    m_intersectMode = false;

    final int keyCode = e.getKeyCode();

    switch( keyCode )
    {
    // "SHFT": Add mode
      case KeyEvent.VK_SHIFT:
        m_addMode = true;
        break;

      // "STRG": Toggle mode
      case KeyEvent.VK_CONTROL:
        m_toggleMode = true;
        break;

      // "ALT": switch between intersect / contains mode
      case KeyEvent.VK_ALT:
        m_intersectMode = true;
        break;

      // "SPACE": switch between polygon / rect mode
      case KeyEvent.VK_SPACE:
        changeGeometryBuilder();
        break;

      // "ESC": deselection
      case KeyEvent.VK_ESCAPE:
        m_geometryBuilder.reset();
        final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();
        selectionManager.clear();
        break;
    }

    mapPanel.repaintMap();
  }

  @Override
  public void keyReleased( final KeyEvent e )
  {
    m_toggleMode = false;
    m_addMode = false;
    m_intersectMode = false;

    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel != null )
      mapPanel.repaintMap();

    super.keyReleased( e );
  }

  private void changeGeometryBuilder( )
  {
    m_currentMode = (m_currentMode + 1) % 4;
    final IDialogSettings settings = getSettings();
    if( settings != null )
      settings.put( SETTINGS_MODE, m_currentMode );

    m_geometryBuilder = createGeometryBuilder();
  }

  protected IGeometryBuilder createGeometryBuilder( )
  {
    final IMapPanel mapPanel = getMapPanel();
    Assert.isNotNull( mapPanel );
    final String crs = mapPanel.getMapModell().getCoordinatesSystem();

    switch( m_currentMode )
    {
      case 0:
        return new RectangleGeometryBuilder( crs );

      case 1:
        return new PolygonGeometryBuilder( 0, crs );

      default:
        return new PointGeometryBuilder( crs );
    }
  }

  @Override
  public void paint( final Graphics g )
  {
    paintHoverFeature( g, getMapPanel(), m_geometryBuilder, m_currentPoint, m_hoverFeature, m_hoverTheme, m_geomQName );

    super.paint( g );
  }

  public static void paintHoverFeature( final Graphics g, final IMapPanel mapPanel, final IGeometryBuilder geometryBuilder, final Point currentPoint, final Feature hoverFeature, final IKalypsoFeatureTheme hoverTheme, final QName geomQName )
  {
    if( mapPanel == null )
      return;

    if( geometryBuilder != null && currentPoint != null )
      geometryBuilder.paint( g, mapPanel.getProjection(), currentPoint );

    if( hoverFeature != null )
    {
      final GMLXPath[] geomQNames = findGeometryPathes( hoverTheme, geomQName, IKalypsoFeatureTheme.PROPERTY_HOVER_GEOMETRIES, hoverFeature );
      for( final GMLXPath qName : geomQNames )
        MapUtils.paintGrabbedFeature( g, mapPanel, hoverFeature, qName );
    }
  }

  @Override
  public void leftReleased( final Point p )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( Objects.isNull( mapPanel, m_geometryBuilder ) )
      return;

    if( m_geometryBuilder instanceof RectangleGeometryBuilder )
    {
      try
      {
        final GM_Point point = MapUtilities.transform( mapPanel, p );

        final GM_Object selectGeometry = m_geometryBuilder.addPoint( point );
        if( selectGeometry != null )
        {
          doSelect( selectGeometry );
          m_geometryBuilder.reset();
        }
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        mapPanel.repaintMap();
      }
    }
  }

  private void doSelect( final GM_Object selectGeometry )
  {
    if( Objects.isNull( selectGeometry ) )
      return;
    if( Arrays.isEmpty( m_themes ) )
      return;

    final Map<IKalypsoFeatureTheme, List<Feature>> selection = new HashMap<IKalypsoFeatureTheme, List<Feature>>();

    for( final IKalypsoFeatureTheme theme : m_themes )
    {
      if( theme == null )
        continue;

      final FeatureList featureList = theme.getFeatureListVisible( selectGeometry.getEnvelope() );
      if( featureList == null )
        continue;

      final GMLXPath[] geomQNames = findGeometryPathes( theme, m_geomQName, IKalypsoFeatureTheme.PROPERTY_SELECTABLE_GEOMETRIES, null );

      final Collection<Feature> selectedSubList = selectFeatures( featureList, selectGeometry, m_qnamesToSelect, geomQNames, m_intersectMode );
      if( selectedSubList != null )
      {
        if( !selection.containsKey( theme ) )
          selection.put( theme, new ArrayList<Feature>( selectedSubList.size() ) );

        final List<Feature> selectedFeatures = selection.get( theme );
        selectedFeatures.addAll( selectedSubList );
      }
    }

    final IFeatureSelectionManager selectionManager = getMapPanel().getSelectionManager();
    doChangeSelection( selectionManager, selection, m_addMode, m_toggleMode );
  }

  /**
   * Finds the geometry properties to select from.<br>
   * If a default type is specified, this will always be used.<br>
   * Else, all geometry properties of the target type of the list will be taken.
   * 
   * @param propertyName
   *          The property of the theme, that may provide the geometry pathes.
   * @param feature
   *          The feature that provides the geometries. If <code>null</code>, the target feature type of the given theme
   *          is analyzed.
   */
  public static GMLXPath[] findGeometryPathes( final IKalypsoFeatureTheme theme, final QName defaultGeometry, final String propertyName, final Feature feature )
  {
    if( defaultGeometry != null )
      return new GMLXPath[] { new GMLXPath( defaultGeometry ) };

    // REMARK:
    // If no geometry is defined in this widget, first search for the 'selectableGeometry' property of the theme.
    final String selectionGeometries = theme.getProperty( propertyName, null );
    if( selectionGeometries != null )
    {
      // TODO: fetch namespace context from map xml
      final NamespaceContext namespaceContext = null;

      final String[] geomNames = selectionGeometries.split( "," );
      final GMLXPath[] geomPathes = new GMLXPath[geomNames.length];
      for( int i = 0; i < geomPathes.length; i++ )
        geomPathes[i] = new GMLXPath( geomNames[i], namespaceContext );

      return geomPathes;
    }

    // REMARK:
    // If no geometry is defined in this widget, second search the feature type of the feature...
    if( feature != null )
      return findGeometryPathes( feature.getFeatureType() );

    // REMARK:
    // ...if not possible, use the geometries of the feature-lists target feature type.
    final FeatureList featureList = theme.getFeatureList();
    final IRelationType parentFeatureTypeProperty = featureList.getPropertyType();
    if( parentFeatureTypeProperty == null )
      return null;

    return findGeometryPathes( parentFeatureTypeProperty.getTargetFeatureType() );
  }

  public static GMLXPath[] findGeometryPathes( final IFeatureType targetFeatureType )
  {
    final IValuePropertyType[] geomProperties = targetFeatureType.getAllGeomteryProperties();
    final GMLXPath[] result = new GMLXPath[geomProperties.length];
    for( int i = 0; i < geomProperties.length; i++ )
      result[i] = new GMLXPath( geomProperties[i].getQName() );

    return result;
  }

  /**
   * Overwritten, so sub classes can overwrite.
   */
  protected void doChangeSelection( final IFeatureSelectionManager selectionManager, final Map<IKalypsoFeatureTheme, List<Feature>> selection, final boolean add, final boolean toggle )
  {
    changeSelection( selectionManager, selection, add, toggle );
  }

  public static void changeSelection( final IFeatureSelectionManager selectionManager, final Map<IKalypsoFeatureTheme, List<Feature>> selection, final boolean add, final boolean toggle )
  {
    if( selection.size() == 0 )
      selectionManager.clear();

    final List<Feature> toRemove = new ArrayList<Feature>();
    final List<EasyFeatureWrapper> toAdd = new ArrayList<EasyFeatureWrapper>();

    for( final IKalypsoFeatureTheme theme : selection.keySet() )
    {
      /* consider the selection modes */
      final CommandableWorkspace workspace = theme.getWorkspace();

      final List<Feature> selectedFeatures = selection.get( theme );
      for( final Feature feature : selectedFeatures )
      {
        if( add )
        {
          if( !selectionManager.isSelected( feature ) )
            toAdd.add( new EasyFeatureWrapper( workspace, feature ) );
        }
        else if( toggle )
        {
          if( selectionManager.isSelected( feature ) )
            toRemove.add( feature );
          else
            toAdd.add( new EasyFeatureWrapper( workspace, feature ) );
        }
        else
          toAdd.add( new EasyFeatureWrapper( workspace, feature ) );
      }
    }

    if( !add && !toggle )
    {
      // REMARK: instead of invoking
      // selectionManager.clear();
      // We add all features to the remove-list; else we get two selection-change events here
      final EasyFeatureWrapper[] allFeatures = selectionManager.getAllFeatures();
      for( final EasyFeatureWrapper feature : allFeatures )
        toRemove.add( feature.getFeature() );
    }

    selectionManager.changeSelection( toRemove.toArray( new Feature[toRemove.size()] ), toAdd.toArray( new EasyFeatureWrapper[toAdd.size()] ) );
  }

  private Collection<Feature> selectFeatures( final FeatureList featureList, final GM_Object selectGeometry, final QName[] qnamesToSelect, final GMLXPath[] geometryPathes, final boolean intersectMode )
  {
    final Collection<Feature> selectedFeatures = new HashSet<Feature>();

    // Only works for surface geometries:: select everything that intersects this geometry
    if( selectGeometry instanceof GM_Surface< ? > )
    {
      final GM_Envelope envelope = selectGeometry.getEnvelope();
      final GMLWorkspace workspace = featureList.getOwner().getWorkspace();
      final List< ? > result = featureList.query( envelope, null );

      for( final Object object : result )
      {
        final Feature feature = FeatureHelper.getFeature( workspace, object );
        final IFeatureType featureType = feature.getFeatureType();

        if( GMLSchemaUtilities.substitutes( featureType, qnamesToSelect ) )
        {
          for( final GMLXPath geometryPath : geometryPathes )
          {
            try
            {
              final Object geomOrList = GMLXPathUtilities.query( geometryPath, feature );

              final GM_Object[] foundGeometries = GeometryUtilities.findGeometries( geomOrList, GM_Object.class );
              for( final GM_Object geometry : foundGeometries )
              {
                if( intersects( selectGeometry, geometry, intersectMode ) )
                  selectedFeatures.add( feature );
              }
            }
            catch( final GMLXPathException e )
            {
              e.printStackTrace();
            }
          }
        }
      }
    }

    return selectedFeatures;
  }

  private boolean intersects( final GM_Object selectGeom, final GM_Object geom, final boolean intersectMode )
  {
    if( geom == null )
      return false;

    if( intersectMode == true )
      return selectGeom.intersects( geom );

    return selectGeom.contains( geom );
  }

  @Override
  public String getToolTip( )
  {
    final StringBuffer sb = new StringBuffer().append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.1" ) ); //$NON-NLS-1$

    if( m_geometryBuilder instanceof PolygonGeometryBuilder )
      sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.2" ) ); //$NON-NLS-1$
    else if( m_geometryBuilder instanceof RectangleGeometryBuilder )
      sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.3" ) ); //$NON-NLS-1$
    else
    {
      if( m_currentMode == 2 )
        sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.4" ) ); //$NON-NLS-1$
      if( m_currentMode == 3 )
        sb.append( "Punkt (Hover-Mode) <SPACE>" );
    }

    if( m_addMode == true )
      sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.5" ) ); //$NON-NLS-1$
    if( m_toggleMode == true )
      sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.6" ) ); //$NON-NLS-1$
    if( m_intersectMode == true )
      sb.append( Messages.getString( "org.kalypso.ogc.gml.map.widgets.SelectFeatureWidget.7" ) ); //$NON-NLS-1$

    return sb.toString();
  }

  public void setThemes( final IKalypsoFeatureTheme[] themes )
  {
    m_themes = themes;
  }

  private IDialogSettings getSettings( )
  {
    return DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() );
  }
}