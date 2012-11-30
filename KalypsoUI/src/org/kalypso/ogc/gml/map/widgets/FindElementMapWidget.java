/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.outline.ViewContentOutline;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypso.ui.editor.mapeditor.views.MapWidgetView;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.sld.CssParameter;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.sld.PolygonSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Stroke_Impl;

/**
 * This tool helps to find elements on the map. The elements will be searched only in selected layers. Searching will be
 * done for all inserted values, last element found will be centered on the map, in case of filled "X" and "Y" fields
 * the shown position will be centered always according to this coordinates.
 * 
 * @author ig
 */
public class FindElementMapWidget extends AbstractWidget implements IWidgetWithOptions
{
  private final FindElementWidgetFace m_widgetFace = new FindElementWidgetFace( this );

  private final ToolTipRenderer m_tooltip = new ToolTipRenderer();

  private IMapPanel m_mapPanel = null;

  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private ISelectionProvider m_selectionProvider = null;

  private IKalypsoTheme[] m_themesAct;

  private final int m_maxStrLen = 500;

  public FindElementMapWidget( )
  {
    this( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" ), Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public FindElementMapWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    m_mapPanel = getMapPanel();

    // TODO: what happens if active theme changes? -> map modell listener?

    final ContentOutline outlineView = findOutlineView();
    if( outlineView == null )
    {
      m_themesAct = m_mapPanel.getMapModell().getAllThemes();
      m_mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" ) ); //$NON-NLS-1$
      return;
    }

    // REMARK: we get selection from outline, so outlines mapPanel should be the same as the widgets ones
    final IMapPanel outlineMapPanel = findOutlineMapPanel( outlineView );
    if( outlineMapPanel != mapPanel )
    {
      m_mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.2" ) ); //$NON-NLS-1$
      return;
    }

    m_selectionProvider = outlineView.getSite().getSelectionProvider();
    m_selectionProvider.addSelectionChangedListener( m_selectionListener );

    handleSelectionChanged( m_selectionProvider.getSelection() );
  }

  public static IMapPanel findOutlineMapPanel( final ContentOutline outlineView )
  {
    final IPage currentPage = outlineView.getCurrentPage();
    if( currentPage instanceof GisMapOutlinePage )
      return ((GisMapOutlinePage)currentPage).getMapPanel();

    return null;
  }

  public static IWorkbenchPage findActivePage( )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

    return window.getActivePage();

  }

  public static ContentOutline findOutlineView( )
  {
    final IWorkbenchPage activePage = findActivePage();

    return (ViewContentOutline)activePage.findView( ViewContentOutline.ID );
  }

  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    return m_widgetFace.createControl( toolkit, parent );
  }

  @Override
  public void disposeControl( )
  {
    m_widgetFace.clear();
  }

  @Override
  public void finish( )
  {
    super.finish();

    if( m_selectionProvider != null )
    {
      m_selectionProvider.removeSelectionChangedListener( m_selectionListener );
      m_selectionProvider = null;
    }
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    final List<IKalypsoTheme> themes = new ArrayList<>();
    final IStructuredSelection sel = (IStructuredSelection)selection;
    final Object[] selectedElements = sel.toArray();
    for( final Object object : selectedElements )
    {
      final IKalypsoTheme theme = findTheme( object );
      if( theme != null )
        themes.add( theme );
    }

    m_themesAct = themes.toArray( new IKalypsoTheme[themes.size()] );

    if( ArrayUtils.isEmpty( m_themesAct ) )
    {
      m_tooltip.setTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.11" ) ); //$NON-NLS-1$
    }
    else
    {
      m_tooltip.setTooltip( StringUtils.EMPTY );
    }
  }

  private IKalypsoTheme findTheme( final Object object )
  {
    if( object instanceof IKalypsoTheme )
      return (IKalypsoTheme)object;

    if( object instanceof IAdaptable )
    {
      final IAdaptable adapable = (IAdaptable)object;
      final IKalypsoTheme theme = (IKalypsoTheme)adapable.getAdapter( IKalypsoTheme.class );
      if( theme != null )
        return theme;
    }

    return null;
  }

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    final Point p = e.getPoint();

    final GM_Point currentPoint = MapUtilities.transform( mapPanel, p );
    updateTooltip( currentPoint );

    repaintMap();
  }

  private void updateTooltip( final GM_Point currentPoint )
  {
    final String tooltip = getTooltip( currentPoint );
    m_tooltip.setTooltip( tooltip );
  }

  private String getTooltip( final GM_Point currentPoint )
  {
    final Set<Feature> featureList = m_widgetFace.getFeatures();

    if( featureList == null || featureList.isEmpty() )
      return StringUtils.EMPTY;

    for( final Object element : featureList )
    {
      final Feature lActFeature = (Feature)element;
      if( lActFeature != null )
      {
        // FIXME: bad and ugly: 0.9 constant: use converted pixel distance instead!
        final GM_Object geometry = lActFeature.getDefaultGeometryPropertyValue();
        final boolean isPointNear = currentPoint.isWithinDistance( geometry, 0.9 );

        // FIXME: snd check should not be necessary -> probably first check is bad
        if( isPointNear || geometry.contains( currentPoint.getPosition() ) )
        {
          return lActFeature.getId() + "\n" + getSimpleFeatureInfo( lActFeature, lActFeature.getFeatureType() ); //$NON-NLS-1$
        }
      }
    }

    return StringUtils.EMPTY;
  }

  // FIXME: ugly! use already existing theme info or similar mechanism
  private String getSimpleFeatureInfo( final Object featureObj, final Object featureType )
  {
    String lStrInfo = ""; //$NON-NLS-1$
    if( featureObj instanceof Feature )
    {
      final Feature feature = (Feature)featureObj;
      final IFeatureType lPropType = (IFeatureType)featureType;

      for( int i = 0; i < feature.getProperties().length; i++ )
      {
        final Object prop = feature.getProperties()[i];
        if( !(prop instanceof String) && prop instanceof List )
        {
          lStrInfo += getSimpleFeatureInfo( prop, featureType == null ? null : lPropType.getProperties()[i] ); //$NON-NLS-1$
        }
        else if( prop != null )
        {
          String lStrPrefix = ""; //$NON-NLS-1$
          if( featureType != null )
          {
            lStrPrefix = lPropType.getProperties()[i].getQName().getLocalPart() + ": "; //$NON-NLS-1$
          }
          final String lPropTrim = ("" + prop).trim(); //$NON-NLS-1$
          if( !"".equals( lPropTrim ) ) { //$NON-NLS-1$ //$NON-NLS-2$
            if( lPropTrim.length() > m_maxStrLen )
            {
              lStrInfo += lStrPrefix + lPropTrim.substring( 0, m_maxStrLen ) + " ...\n"; //$NON-NLS-1$
            }
            else
            {
              lStrInfo += lStrPrefix + lPropTrim + "\n"; //$NON-NLS-1$
            }
          }

        }
      }
    }
    else if( featureObj instanceof List )
    {
      final List< ? > featureList = (List< ? >)featureObj;
      String lStrSuffix = ""; //$NON-NLS-1$
      String lStrPrefix = ""; //$NON-NLS-1$
      if( featureType != null )
      {
        final IPropertyType propertyType = (IPropertyType)featureType;
        lStrPrefix = propertyType.getQName().getLocalPart() + ": "; //$NON-NLS-1$
      }
      for( int i = 0; i < featureList.size(); i++ )
      {
        final Object prop = featureList.get( i );
        if( !"".equals( ("" + prop).trim() ) ) //$NON-NLS-1$ //$NON-NLS-2$
          if( ("" + prop).trim().length() > m_maxStrLen ) //$NON-NLS-1$
          {
            lStrSuffix += ("" + prop).trim().substring( 0, m_maxStrLen ) + " ... , "; //$NON-NLS-1$ //$NON-NLS-2$
          }
          else
          {
            lStrSuffix += prop + ", "; //$NON-NLS-1$
          }
      }
      if( !"".equals( lStrSuffix.trim() ) ) { //$NON-NLS-1$
        lStrInfo += lStrPrefix + lStrSuffix + "\n"; //$NON-NLS-1$
      }
    }
    else if( featureObj != null )
    {
      lStrInfo += featureObj + "\n"; //$NON-NLS-1$
    }
    return lStrInfo;
  }

  void reset( )
  {
    m_widgetFace.clear();

    m_tooltip.setTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.11" ) ); //$NON-NLS-1$

    repaintMap();
  }

  @Override
  public void paint( final Graphics g )
  {
    final Feature feature = m_widgetFace.getFeature();
    if( feature == null )
      return;

    final Set<Feature> featureList = m_widgetFace.getFeatures();

    for( final Feature element : featureList )
    {
      try
      {
        final GM_Object geometryObjectValue = element.getDefaultGeometryPropertyValue();
        final GM_Envelope envelope = geometryObjectValue.getEnvelope();
        GM_Object geometryObjectToShow = geometryObjectValue;

        double scaledFactor = getMapPanel().getCurrentScale();

        // FIXME: mega ugly!
        if( envelope.getMaxX() == envelope.getMinX() && envelope.getMaxY() == envelope.getMinY() )
        {
          scaledFactor *= 0.00093;
        }
        else
        {
          scaledFactor *= 0.0000093;
        }

        geometryObjectToShow = geometryObjectValue.getBuffer( scaledFactor );
        final PolygonSymbolizer symb = new PolygonSymbolizer_Impl();

        final Stroke stroke = new Stroke_Impl( new HashMap<String, CssParameter>(), null, null );
        stroke.setWidth( 5 );
        stroke.setStroke( new Color( 255, 0, 0 ) );
        symb.setStroke( stroke );

        // FIXME:; why m_feature and not element?!
        final DisplayElement de = DisplayElementFactory.buildPolygonDisplayElement( feature, geometryObjectToShow, symb );
        de.paint( g, getMapPanel().getProjection(), new NullProgressMonitor() );
      }
      catch( final Exception e )
      {
      }
    }

    // FIXME: only painted, if m_feature != null
    m_tooltip.paintToolTip( getMapPanel().getScreenBounds().getLocation(), g, getMapPanel().getScreenBounds() );
  }

  @Override
  public String getPartName( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" ); //$NON-NLS-1$
  }

  @Override
  public synchronized boolean canBeActivated( final ISelection selection, final IMapPanel mapPanel )
  {
    try
    {
      final IWorkbenchPage activePage = findActivePage();
      final MapWidgetView widgetView = (MapWidgetView)activePage.findView( MapWidgetView.ID );

      if( widgetView != null )
        return false;

      return super.canBeActivated( selection, mapPanel );
    }
    catch( final Exception e )
    {
      return false;
    }
  }

  IKalypsoTheme[] getCurrentThemes( )
  {
    return m_themesAct;
  }

  IMapPanel getPanel( )
  {
    return m_mapPanel;
  }
}