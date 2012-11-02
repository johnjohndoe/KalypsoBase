/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.internal.layoutwizard.map;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.ogc.gml.GisTemplateFeatureTheme;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.RestoreSelectionHelper;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.MapPanelSourceProvider;
import org.kalypso.ogc.gml.map.widgets.SelectSingleFeatureWidget;
import org.kalypso.ogc.gml.mapmodel.IMapPanelProvider;
import org.kalypso.ogc.gml.mapmodel.WaitForMapJob;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.widgets.IWidget;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.mapeditor.MapPartHelper;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.ui.layoutwizard.AbstractWizardLayoutPart;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.xml.sax.SAXException;

/**
 * @author Gernot Belger
 */
public class MapLayoutPart extends AbstractWizardLayoutPart implements IMapPanelProvider
{
  private final IFeatureSelectionManager m_selectionManager = new FeatureSelectionManager2();

  /**
   * Argument: if present, a toolbar will be shown above the map <br>
   */
  private static final String PROP_MAP_TOOLBAR = "mapToolbar"; //$NON-NLS-1$

  /**
   * Sub-Argument (prefix) in mapToolbar: id of a menu-contribution that will be added to the map-toolbar <br>
   * Multiple {@value} arguments will be added in the given order.
   */
  private static final String PROP_MAP_TOOLBAR_URI = "uri"; //$NON-NLS-1$

  /** Argument: Pfad auf Vorlage für die Karte (.gmt Datei) */
  private static final String PROP_MAPTEMPLATE = "mapTemplate"; //$NON-NLS-1$

  /**
   * Argument: Full class name of the map-widget to use. Default is: {@link org.kalypso.ogc.gml.map.widgets.SelectSingleFeatureWidget}. If empty, no widget will be selected initially.
   */
  private static final String PROP_MAP_WIDGETCLASS = "mapWidgetClass"; //$NON-NLS-1$

  /**
   * Argument: Plug-in id (symbolic name) of the plug.in containing the given widget class. If none if given,
   * 'org.kalypso.ui' is assumed.
   */
  private static final String PROP_MAP_WIDGETPLUGIN = "mapWidgetPlugin"; //$NON-NLS-1$

  /**
   * Argument: Falls true, wird der Context der Karte stets auf den CalcCaseFolder gesetzt. Ansonsten wie üblich auf die
   * .gtt Datei.
   */
  public static final String PROP_MAPTEMPLATEISCALC = "mapTemplateContextIsCalcCase"; //$NON-NLS-1$

  /**
   * Argument: Falls true, wird die Karte auf den FullExtent maximiert, sonst wird {@link #m_wishBoundingBox}angesetzt
   */
  private static final String PROP_MAXIMIZEMAP = "maximizeMap"; //$NON-NLS-1$

  /**
   * Argument: Falls gesetzt, wird das Feature mit dieser ID selektiert, nachdem die Karte geladen wurde. Ansonsten das
   * erste Feature
   */
  private static final String PROP_FEATURE_TO_SELECT_ID = "selectFeatureID"; //$NON-NLS-1$

  /**
   * Argument: feature with this id will be in the center of the map
   */
  private static final String PROP_PAN_TO_FEATURE_ID = "pantoFeatureId"; //$NON-NLS-1$

  private GisTemplateMapModell m_mapModell;

  private GM_Envelope m_wishBoundingBox;

  private IMapPanel m_mapPanel;

  private RestoreSelectionHelper m_selectionRestorer;

  public MapLayoutPart( final String id, final ILayoutPageContext context )
  {
    super( id, context );
  }

  @Override
  public void dispose( )
  {
    super.dispose();

    if( m_mapModell != null )
      m_mapModell.dispose();

    if( m_mapPanel != null )
      m_mapPanel.dispose();
  }

  @Override
  protected ISourceProvider createSourceProvider( final IServiceLocator context )
  {
    if( m_mapPanel == null )
      return null;

    return new MapPanelSourceProvider( context, m_mapPanel );
  }

  @Override
  public void init( ) throws CoreException
  {
    try
    {
      final ILayoutPageContext context = getContext();
      final Arguments arguments = context.getArguments();
      final String mapFileName = arguments.getProperty( PROP_MAPTEMPLATE );
      if( StringUtils.isBlank( mapFileName ) )
        throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), String.format( Messages.getString( "MapLayoutPart_8" ), PROP_MAPTEMPLATE ) ) ); //$NON-NLS-1$

      final URL mapURL = context.resolveURI( mapFileName );
      if( mapURL == null )
        throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "MapLayoutPart_9" ) + mapFileName ) ); //$NON-NLS-1$

      final Gismapview gisview = GisTemplateHelper.loadGisMapView( mapURL );
      final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

      final boolean mapContextIsCalcCase = arguments.getBoolean( PROP_MAPTEMPLATEISCALC, true );

      final URL mapContext = mapContextIsCalcCase ? context.getContext() : mapURL;

      m_mapModell = new GisTemplateMapModell( mapContext, crs, m_selectionManager );
      m_mapModell.createFromTemplate( gisview );

      m_wishBoundingBox = GisTemplateHelper.getBoundingBox( gisview );
      if( arguments.getBoolean( PROP_MAXIMIZEMAP, false ) )
        m_wishBoundingBox = null;
    }
    catch( final MalformedURLException e )
    {
      throwMapLoadException( e );
    }
    catch( final JAXBException e )
    {
      throwMapLoadException( e );
    }
    catch( final SAXException e )
    {
      throwMapLoadException( e );
    }
    catch( final ParserConfigurationException e )
    {
      throwMapLoadException( e );
    }
    catch( final IOException e )
    {
      throwMapLoadException( e );
    }
  }

  private void throwMapLoadException( final Exception e ) throws CoreException
  {
    final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "MapLayoutPart_10" ), e ); //$NON-NLS-1$
    throw new CoreException( status );
  }

  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final Composite mapAndToolbar = toolkit.createComposite( parent, getStyle() );
    final GridLayout mapAndToolbarLayout = new GridLayout();
    mapAndToolbarLayout.marginHeight = 0;
    mapAndToolbarLayout.marginWidth = 0;
    mapAndToolbarLayout.horizontalSpacing = 0;
    mapAndToolbarLayout.verticalSpacing = 0;
    mapAndToolbar.setLayout( mapAndToolbarLayout );

    buildToolbar( toolkit, mapAndToolbar );

    final ILayoutPageContext context = getContext();

    m_mapPanel = MapPartHelper.createMapPanel( mapAndToolbar, SWT.BORDER, new GridData( SWT.FILL, SWT.FILL, true, true ), context, m_selectionManager );
    m_mapPanel.setMapModell( m_mapModell );

    final IWidget widget = createWidget();
    final Arguments arguments = getContext().getArguments();
    final String selectFid = arguments.getProperty( PROP_FEATURE_TO_SELECT_ID, null );
    final String panFid = arguments.getProperty( PROP_PAN_TO_FEATURE_ID, null );

    final WaitForMapJob waitForMapJob = new WaitForMapJob( m_mapPanel );
    waitForMapJob.addJobChangeListener( new MapLoadedJobListener( m_mapPanel, m_wishBoundingBox, selectFid, panFid, widget ) );
    waitForMapJob.setUser( true );
    waitForMapJob.schedule();

    return mapAndToolbar;
  }

  private void buildToolbar( final FormToolkit toolkit, final Composite mapAndToolbar )
  {
    final ILayoutPageContext context = getContext();
    final Arguments arguments = context.getArguments();
    final Arguments toolbarArguments = arguments.getArguments( PROP_MAP_TOOLBAR );
    if( toolbarArguments != null )
    {
      final IContributionManager manager = createToolbar( toolkit, mapAndToolbar, toolbarArguments );
      fillMapToolbar( manager, toolbarArguments );
    }
  }

  private IWidget createWidget( )
  {
    try
    {
      final ILayoutPageContext context = getContext();
      final Arguments arguments = context.getArguments();
      final String widgetClass = arguments.getProperty( PROP_MAP_WIDGETCLASS, SelectSingleFeatureWidget.class.getName() );
      final String widgetPlugin = arguments.getProperty( PROP_MAP_WIDGETPLUGIN, KalypsoGisPlugin.getId() );

      if( widgetClass.isEmpty() )
        return null;

      final Class<IWidget> widgetCls = PluginUtilities.findClass( widgetClass, widgetPlugin );
      return ClassUtilities.newInstance( widgetCls );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates the toolbar if the mapToolbar argument is present. Else <code>null</code> is returned.
   */
  private IContributionManager createToolbar( final FormToolkit toolkit, final Composite mapAndToolbar, final Arguments toolbarArguments )
  {
    if( toolbarArguments == null )
      return null;

    final ToolBar toolbar = new ToolBar( mapAndToolbar, SWT.HORIZONTAL | SWT.FLAT );
    toolkit.adapt( toolbar );
    toolbar.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    return new ToolBarManager( toolbar );
  }

  /**
   * Fills the toolbar according to the configuration.
   */
  private void fillMapToolbar( final IContributionManager manager, final Arguments toolbarArguments )
  {
    final String[] urisKeys = toolbarArguments.findAllKeys( PROP_MAP_TOOLBAR_URI );

    for( final String key : urisKeys )
    {
      final String uri = toolbarArguments.getProperty( key );

      // REMARK: we use a general eclipse mechanism to populate the toolbar here
      // we only use 'toolbar'-uris at the moment, maybe we can later add popup and menu.
      if( uri.startsWith( "toolbar" ) ) //$NON-NLS-1$
        ContributionUtils.populateContributionManager( getContext(), manager, uri );
    }

    manager.update( true );
  }

  @Override
  public void saveSelection( ) throws CoreException
  {
    if( m_mapModell != null )
    {
      final IKalypsoTheme activeTheme = m_mapModell.getActiveTheme();
      if( activeTheme instanceof GisTemplateFeatureTheme )
      {
        final GisTemplateFeatureTheme gtft = (GisTemplateFeatureTheme)activeTheme;
        if( gtft.getStatus().isOK() )
          m_selectionRestorer = new RestoreSelectionHelper( gtft.getLayerKey(), m_selectionManager );
      }
    }
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#restoreSelection(boolean)
   */
  @Override
  public void restoreSelection( final boolean clearState ) throws CoreException
  {
    if( m_selectionRestorer != null )
      m_selectionRestorer.restoreSelection();

    if( clearState )
      m_selectionRestorer = null;
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#saveData(boolean)
   */
  @Override
  public void saveData( final boolean doSaveGml )
  {
  }

  @Override
  public ISelectionProvider getSelectionProvider( )
  {
    return m_mapPanel;
  }

  @Override
  public IMapPanel getMapPanel( )
  {
    return m_mapPanel;
  }
}