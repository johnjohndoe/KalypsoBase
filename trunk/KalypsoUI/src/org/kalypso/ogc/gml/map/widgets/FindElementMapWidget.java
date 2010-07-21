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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.handlers.IHandlerService;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.ChangeExtentCommand;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.outline.MapOutline;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.sld.PolygonSymbolizer_Impl;
import org.kalypsodeegree_impl.graphics.sld.Stroke_Impl;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This tool helps to find elements on the map. The elements will be searched only in selected layers. Searching will be
 * done for all inserted values, last element found will be centered on the map, in case of filled "X" and "Y" fields
 * the shown position will be centered always according to this coordinates.
 * 
 * @author ig
 */
@SuppressWarnings("unchecked")
public class FindElementMapWidget extends AbstractWidget implements IWidgetWithOptions
{
  private final String m_defaultCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

  private FindElementWidgetFace m_widgetFace;

  protected boolean m_boolFound;

  private Map<String, Feature> m_mapCacheFound;

  private final ToolTipRenderer m_tooltip = new ToolTipRenderer();

  protected IMapPanel m_mapPanel = null;

  protected Feature m_feature;

  protected Set<Feature> m_featureList;

  protected Text m_name;

  protected Text m_gmlId;

  protected Text m_id;

  protected Text m_posX;

  protected Text m_posY;

  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private ISelectionProvider m_selectionProvider = null;

  protected List<IKalypsoTheme> m_themesAct;

  private QName m_resultIdQName = new QName( "http://www.tu-harburg.de/wb/kalypso/schemata/1d2dResults", "calcId" ); //$NON-NLS-1$  //$NON-NLS-2$

  private QName m_nameQName = new QName( NS.GML3, "name" ); //$NON-NLS-1$

  private int m_cacheSize = 1024;

  private boolean m_boolIsSimple = true;

  private String m_wildCChar = "*"; //$NON-NLS-1$

  private int m_maxStrLen = 500;

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    final IWorkbenchPage page = window.getActivePage();
    final MapOutline outlineView = (MapOutline) page.findView( MapOutline.ID );
    m_mapPanel = getMapPanel();
    if( outlineView == null )
    {
      m_themesAct = Arrays.asList( m_mapPanel.getMapModell().getAllThemes() );
      m_mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" ) ); //$NON-NLS-1$
      return;
    }

    final IMapPanel outlineMapPanel = outlineView.getMapPanel();
    if( outlineMapPanel != mapPanel )
    {
      m_mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.2" ) ); //$NON-NLS-1$
      return;
    }

    m_selectionProvider = outlineView.getSite().getSelectionProvider();
    m_selectionProvider.addSelectionChangedListener( m_selectionListener );

    handleSelectionChanged( m_selectionProvider.getSelection() );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#createControl(org.eclipse.swt.widgets.Composite)
   */
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    return m_widgetFace.createControl( parent );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#disposeControl()
   */
  public void disposeControl( )
  {
    if( m_widgetFace != null )
    {
      m_widgetFace.disposeControl();
      m_widgetFace.disposeParent();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#finish()
   */
  @Override
  public void finish( )
  {
    super.finish();

    if( m_selectionProvider != null )
    {
      m_selectionProvider.removeSelectionChangedListener( m_selectionListener );
      m_selectionProvider = null;
    }
    if( m_mapCacheFound.size() > m_cacheSize )
      m_mapCacheFound.clear();
    disposeControl();
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    final List<IKalypsoTheme> themes = new ArrayList<IKalypsoTheme>();

    final IStructuredSelection sel = (IStructuredSelection) selection;
    final Object[] selectedElements = sel.toArray();
    for( final Object object : selectedElements )
    {
      final IKalypsoTheme theme = findTheme( object );
      if( theme != null )
        themes.add( theme );
    }
    m_themesAct = themes;
    if( m_themesAct == null || m_themesAct.size() == 0 )
    {
      m_tooltip.setTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.11" ) ); //$NON-NLS-1$
    }
    else
    {
      m_tooltip.setTooltip( "" ); //$NON-NLS-1$
    }
  }

  private IKalypsoTheme findTheme( final Object object )
  {
    if( object instanceof IKalypsoTheme )
      return (IKalypsoTheme) object;

    if( object instanceof IAdaptable )
    {
      final IAdaptable adapable = (IAdaptable) object;
      final IKalypsoTheme theme = (IKalypsoTheme) adapable.getAdapter( IKalypsoTheme.class );
      if( theme != null )
        return theme;
    }

    return null;
  }

  public FindElementMapWidget( )
  {
    this( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" ), Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public FindElementMapWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
    m_widgetFace = new FindElementWidgetFace();

    m_mapCacheFound = new HashMap<String, Feature>();

    m_featureList = new HashSet<Feature>();

  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#moved(java.awt.Point)
   */
  @Override
  public void moved( final Point p )
  {
    try
    {
      final IMapPanel mapPanel = getMapPanel();
      if( mapPanel == null )
        return;

      final GM_Point currentPoint = MapUtilities.transform( mapPanel, p );
      updateTooltip( currentPoint );

      mapPanel.repaintMap();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }

  private void updateTooltip( final GM_Point currentPoint ) throws Exception
  {
    if( m_featureList != null && m_featureList.size() > 0 )
    {
      for( Iterator iterator = m_featureList.iterator(); iterator.hasNext(); )
      {
        Feature lActFeature = (Feature) iterator.next();
        if( lActFeature != null
            && (currentPoint.isWithinDistance( lActFeature.getDefaultGeometryPropertyValue(), 0.9 ) || lActFeature.getDefaultGeometryPropertyValue().contains( currentPoint.getPosition() )) )
        {
          m_tooltip.setTooltip( lActFeature.getId() + "\n" + getSimpleFeatureInfo( lActFeature, lActFeature.getFeatureType() ) ); //$NON-NLS-1$
          break;
        }
        else
        {
          m_tooltip.setTooltip( "" ); //$NON-NLS-1$
        }
      }
    }
    else
    {
      m_tooltip.setTooltip( "" ); //$NON-NLS-1$
    }
  }

  private String getSimpleFeatureInfo( final Object featureObj, final Object featureType )
  {
    String lStrInfo = ""; //$NON-NLS-1$
    if( featureObj instanceof Feature )
    {
      Feature feature = (Feature) featureObj;
      IFeatureType lPropType = (IFeatureType) featureType;

      for( int i = 0; i < feature.getProperties().length; i++ )
      {
        Object prop = feature.getProperties()[i];
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
          String lPropTrim = ("" + prop).trim();
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
      List featureList = (List) featureObj;
      String lStrSuffix = ""; //$NON-NLS-1$
      String lStrPrefix = ""; //$NON-NLS-1$
      if( featureType != null )
      {
        IPropertyType propertyType = (IPropertyType) featureType;
        lStrPrefix = propertyType.getQName().getLocalPart() + ": "; //$NON-NLS-1$
      }
      for( int i = 0; i < featureList.size(); i++ )
      {
        Object prop = featureList.get( i );
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

  protected void reset( )
  {
    m_feature = null;
    m_featureList.clear();
    getMapPanel().repaintMap();
    m_tooltip.setTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.11" ) ); //$NON-NLS-1$
  }

  private class FindElementWidgetFace
  {
    private Composite rootPanel;

    private FormToolkit toolkit;

    public FindElementWidgetFace( )
    {
    }

    public void disposeParent( )
    {

    }

    public Control createControl( final Composite parent )
    {
      parent.setLayout( new FillLayout() );
      rootPanel = new Composite( parent, SWT.FILL );
      rootPanel.setLayout( new FillLayout() );
      toolkit = new FormToolkit( parent.getDisplay() );

      final GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      rootPanel.setLayout( gridLayout );
      createGUI( rootPanel );
      final Button lButtonSearch = toolkit.createButton( rootPanel, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.4" ), SWT.PUSH ); //$NON-NLS-1$  
      lButtonSearch.setSize( 30, 15 );
      lButtonSearch.addSelectionListener( new SelectionAdapter()
      {
        /**
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected( final SelectionEvent e )
        {
          doSearchOperation();
        }

      } );

      final Button lButtonReset = toolkit.createButton( rootPanel, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.5" ), SWT.PUSH ); //$NON-NLS-1$  
      lButtonReset.setSize( 30, 15 );
      lButtonReset.addSelectionListener( new SelectionAdapter()
      {
        /**
         * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
        public void widgetSelected( final SelectionEvent e )
        {
          m_gmlId.setText( "" );
          if( m_id != null && m_id.isVisible() )
            m_id.setText( "" );
          m_name.setText( "" );
          m_posX.setText( "" );
          m_posY.setText( "" );
          reset();
        }

      } );

      return rootPanel;
    }

    private void createGUI( final Composite parent )
    {
      toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.6" ) ); //$NON-NLS-1$
      m_gmlId = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
      m_gmlId.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
      m_gmlId.addKeyListener( keyListnerEnter() );

      boolean is1d2dModule = isIn1d2dModule();
      if( is1d2dModule )
      {
        toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.7" ) ); //$NON-NLS-1$
        m_id = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
        m_id.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        m_id.addKeyListener( keyListnerEnter() );
      }

      toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.3" ) ); //$NON-NLS-1$
      m_name = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
      m_name.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
      m_name.addKeyListener( keyListnerEnter() );

      toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.8" ) ); //$NON-NLS-1$
      toolkit.createLabel( parent, Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.9" ) ); //$NON-NLS-1$

      m_posX = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
      m_posX.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
      m_posX.addKeyListener( keyListnerEnter() );

      m_posY = toolkit.createText( parent, "", SWT.SINGLE | SWT.BORDER ); //$NON-NLS-1$
      m_posY.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
      m_posY.addKeyListener( keyListnerEnter() );
    }

    /**
     * provides the check for activated 1d2d plugin or context. otherwise the search for result id will not be shown
     */
    private boolean isIn1d2dModule( )
    {
      boolean is1d2dModule = false;
      try
      {
        String lTmp = ""; //$NON-NLS-1$
        if( PlatformUI.isWorkbenchRunning() )
        {
          final IWorkbench workbench = PlatformUI.getWorkbench();
          final IHandlerService service = (IHandlerService) workbench.getService( IHandlerService.class );
          final IEvaluationContext currentState = service.getCurrentState();
          lTmp = currentState.getVariable( "activeCaseDataProvider" ).toString(); //$NON-NLS-1$
        }
        is1d2dModule = lTmp.contains( "kalypso1d2d" ); //$NON-NLS-1$
      }
      catch( Exception e )
      {
      }
      return is1d2dModule;
    }

    private KeyListener keyListnerEnter( )
    {
      return new KeyListener()
      {

        @Override
        public void keyReleased( org.eclipse.swt.events.KeyEvent e )
        {
        }

        @Override
        public void keyPressed( org.eclipse.swt.events.KeyEvent e )
        {
          if( e.keyCode == 16777296 || e.character == '\n' || e.character == '\r' ) // KeyEvent.VK_ENTER )
          {
            doSearchOperation();
            return;
          }
        }
      };
    }

    public void disposeControl( )
    {
      if( rootPanel == null )
      {
        return;
      }
      if( !rootPanel.isDisposed() )
      {
        rootPanel.getParent().setEnabled( true );
        rootPanel.setVisible( false );
        rootPanel.dispose();
        toolkit.dispose();
      }
    }
  }

  /**
   * This function changes the extent of the map panel, so that it centers the centroid if the first geometry of the
   * given feature or to the X,Y position.
   */
  protected void showFound( ) throws ExecutionException
  {
    GM_Point centroid = null;
    if( m_feature == null && m_boolFound || (m_posX != null && m_posY != null && !"".equals( m_posX.getText() ) && !"".equals( m_posY.getText() )) )
    {
      centroid = GeometryFactory.createGM_Point( NumberUtils.parseQuietDouble( m_posX.getText() ), NumberUtils.parseQuietDouble( m_posY.getText() ), m_defaultCrs );
    }
    else
    {
      if( m_feature == null )
        return;
      final GM_Object[] geometries = m_feature.getGeometryPropertyValues();

      if( geometries.length == 0 )
        return;

      final GM_Object geometry = geometries[0];
      centroid = geometry.getCentroid();
    }

    final GM_Envelope boundingBox = m_mapPanel.getBoundingBox();
    if( boundingBox == null )
      return;

    /* Get the new paned bounding box to the centroid of the geometry. */
    final GM_Envelope paned = boundingBox.getPaned( centroid );
    /* Finally set the bounding box. */
    MapHandlerUtils.postMapCommandChecked( m_mapPanel, new ChangeExtentCommand( m_mapPanel, paned ), null );
    getMapPanel().repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    if( m_feature == null )
      return;

    for( Iterator<Feature> iterator = m_featureList.iterator(); iterator.hasNext(); )
    {
      Feature element = iterator.next();

      try
      {
        GM_Object geometryObjectValue = element.getDefaultGeometryPropertyValue();
        GM_Envelope envelope = geometryObjectValue.getEnvelope();
        GM_Object geometryObjectToShow = null;

        double scaledFactor = getMapPanel().getCurrentScale();

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
        final Stroke stroke = new Stroke_Impl( new HashMap<Object, Object>(), null, null );
        stroke.setWidth( 3 );
        stroke.setStroke( new Color( 255, 0, 0 ) );
        symb.setStroke( stroke );

        final DisplayElement de = DisplayElementFactory.buildPolygonDisplayElement( m_feature, geometryObjectToShow.getConvexHull(), symb );
        de.paint( g, getMapPanel().getProjection(), new NullProgressMonitor() );
      }
      catch( final Exception e )
      {
      }
    }
    m_tooltip.paintToolTip( getMapPanel().getScreenBounds().getLocation(), g, getMapPanel().getScreenBounds() );
  }

  protected boolean findFeature( final FeatureList featureList )
  {
    int lIntCountFounds = 0;

    if( m_gmlId != null && !"".equals( m_gmlId.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findFeatureForGmlId( m_gmlId.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_id != null && !"".equals( m_id.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findResultFeatureForPropertyId( m_resultIdQName, m_id.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_name != null && !"".equals( m_name.getText() ) ) //$NON-NLS-1$
    {
      m_feature = findResultFeatureForPropertyId( m_nameQName, m_name.getText().toLowerCase(), featureList );
      m_featureList.add( m_feature );
      lIntCountFounds += m_feature == null ? 0 : 1;
    }
    if( m_posX != null && !"".equals( m_posX.getText() ) && m_posY != null && !"".equals( m_posY.getText() ) ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      m_feature = null;
      m_boolFound = true;
      try
      {
        showFound();
      }
      catch( ExecutionException e )
      {
        e.printStackTrace();
      }
    }

    return lIntCountFounds > 0 ? true : false;
  }

  private Feature findResultFeatureForPropertyId( final QName propertyName, final String propertyValue, final FeatureList featureList )
  {
    Feature lFeature = m_mapCacheFound.get( propertyName + propertyValue );
    if( lFeature != null )
    {
      return lFeature;
    }
    for( final Iterator<Feature> iterator = featureList.iterator(); iterator.hasNext(); lFeature = iterator.next() )
    {
      Object lPropertyValue = null;
      try
      {
        lPropertyValue = lFeature.getProperty( propertyName );
      }
      catch( Exception e )
      {
        continue;
      }
      if( checkEquals( ("" + lPropertyValue).toLowerCase(), propertyValue ) ) //$NON-NLS-1$
      {
        m_mapCacheFound.put( propertyName + propertyValue, lFeature );

        if( m_boolIsSimple )
          return lFeature;

        m_featureList.add( lFeature );
        m_feature = lFeature;
      }
    }
    return null;
  }

  private Feature findFeatureForGmlId( final String gmlId, final FeatureList featureList )
  {
    Feature lFeature = m_mapCacheFound.get( gmlId );
    if( lFeature != null )
    {
      return lFeature;
    }
    for( Iterator<Feature> iterator = featureList.iterator(); iterator.hasNext(); )
    {
      lFeature = iterator.next();
      if( checkEquals( lFeature.getId().toLowerCase(), gmlId ) )
      {
        m_mapCacheFound.put( gmlId, lFeature );
        if( m_boolIsSimple )
          return lFeature;

        m_featureList.add( lFeature );
        m_feature = lFeature;
      }
    }
    return null;
  }

  private boolean checkEquals( final String idToCheck, final String pIdPattern )
  {
    if( !pIdPattern.contains( m_wildCChar ) )
    {
      return pIdPattern.toLowerCase().equals( idToCheck.toLowerCase() );
    }
    String idPattern = pIdPattern;
    if( idPattern.startsWith( "*" ) ) //$NON-NLS-1$
    {
      idPattern = idPattern.substring( 1 );
    }
    StringTokenizer lStrTokenizer = new StringTokenizer( idPattern, m_wildCChar );
    String lStrRest = idToCheck;
    while( lStrTokenizer.hasMoreTokens() )
    {
      String lStrToken = lStrTokenizer.nextToken();
      int indexOfToken = lStrRest.indexOf( lStrToken );
      if( indexOfToken > -1 )
      {
        lStrRest = lStrRest.substring( indexOfToken + lStrToken.length() );
      }
      else
      {
        return false;
      }
    }
    return true;

  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#getPartName()
   */
  @Override
  public String getPartName( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.map.widgets.FindElementMapWidget.1" );
  }

  protected void doSearchOperation( )
  {
    m_boolIsSimple = checkInputs();
    if( m_themesAct != null )
      for( final IKalypsoTheme lTheme : m_themesAct )
      {
        if( lTheme instanceof IKalypsoCascadingTheme )
        {
          IKalypsoCascadingTheme lThemes = (IKalypsoCascadingTheme) lTheme;
          for( int i = 0; i < lThemes.getAllThemes().length; i++ )
          {
            try
            {
              findInTheme( lThemes.getAllThemes()[i] );
            }
            catch( Exception e )
            {
            }

            if( m_boolFound && m_boolIsSimple )
              break;
          }
        }
        else
        {
          findInTheme( lTheme );
          if( m_boolFound && m_boolIsSimple )
            break;
        }
      }
    try
    {
      showFound();
    }
    catch( ExecutionException e1 )
    {
      e1.printStackTrace();
    }
  }

  private boolean checkInputs( )
  {
    int lIntCountInputs = 0;
    if( m_gmlId != null )
    {
      m_gmlId.setText( m_gmlId.getText().trim() );
      if( m_gmlId.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_gmlId.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }
    if( m_id != null )
    {
      m_id.setText( m_id.getText().trim() );
      if( m_id.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_id.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }
    if( m_name != null )
    {
      m_name.setText( m_name.getText().trim() );
      if( m_name.getText().contains( m_wildCChar ) )
      {
        m_boolIsSimple = false;
      }
      if( !"".equals( m_name.getText() ) ) //$NON-NLS-1$
      {
        lIntCountInputs++;
      }
    }

    if( m_posX != null && m_posY != null )
    {
      m_posX.setText( m_posX.getText().trim() );
      m_posY.setText( m_posY.getText().trim() );
      if( m_posX.getText().contains( m_wildCChar ) || m_posY.getText().contains( m_wildCChar ) )
      {
        throw new NumberFormatException( "Invalid Double!" ); //$NON-NLS-1$
      }
      if( !"".equals( m_posX.getText() ) && !"".equals( m_posY.getText() ) ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        lIntCountInputs++;
      }
    }
    return m_boolIsSimple ? lIntCountInputs > 0 : false;
  }

  private void findInTheme( final IKalypsoTheme lTheme )
  {
    IKalypsoFeatureTheme lActTheme = (IKalypsoFeatureTheme) lTheme;
    FeatureList featureList = lActTheme.getFeatureList();
    m_boolFound = findFeature( featureList );
  }
}