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
package org.kalypso.simulation.ui.wizards.calculation.modelpages;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreExceptionRunnable;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.view.WizardView;
import org.kalypso.contribs.eclipse.ui.actions.CommandContributionItem;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.GisTemplateFeatureTheme;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.KalypsoFeatureThemeSelection;
import org.kalypso.ogc.gml.RestoreSelectionHelper;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.MapPanelSourceProvider;
import org.kalypso.ogc.gml.map.widgets.SelectSingleFeatureWidget;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.util.GisTemplateLoadedThread;
import org.kalypso.ogc.gml.widgets.IWidget;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.jfreechart.ChartFactory;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewUtils;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.ogc.sensor.timeseries.TimeserieFeatureProps;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.wizards.calculation.CalcWizard;
import org.kalypso.simulation.ui.wizards.calculation.CalcWizardHelper;
import org.kalypso.simulation.ui.wizards.calculation.IModelWizardPage;
import org.kalypso.simulation.ui.wizards.calculation.TSLinkWithName;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.template.obsdiagview.Obsdiagview;
import org.kalypso.template.obstableview.Obstableview;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;

/**
 * @author Gernot Belger
 */
public abstract class AbstractCalcWizardPage extends WizardPage implements IModelWizardPage, ICommandTarget, ISelectionChangedListener
{
  /**
   * Argument: If this page is asked for the next page and this arugment is set, it will try to find the next-page by
   * this name. Else, the normal behaviour is taken,i.e. the wizard determines the order (normally just the order in the
   * calcWizard.xml)
   */
  private static final String PROP_NEXTPAGE = "nextPage";

  /**
   * Argument: If this page is asked for the next page and this arugment is set, it will try to find the next-page by
   * this name. Else, the normal behaviour is taken,i.e. the wizard determines the order (normally just the order in the
   * calcWizard.xml)
   */
  private static final String PROP_PREVPAGE = "prevPage";

  /**
   * Argument: If set, this value is taken as the page-name (page-id)
   */
  private static final String PROP_PAGE_ID = "pageID";

  /** Argument: Name der modelspec datei, die verwendet wird, falls nicht gesetzt wird der Standard benutzt. */
  private final static String PROP_MODELSPEC = "modelspec";

  /**
   * Argument: Falls diese Argumente konfiguriert sind, wird unter dem Diagramm eine Auswahl in Form von Radio-Buttons
   * eingeblendet, mit welcher zwischen zwei verschiedenen Diasgramm Ansichten umgeschaltet wird. Jeder Radio-Button
   * stellt in Wirklichkeit eineen Filter dar, mit dem bestimmte Datentypen im diagramm unterdrückt werden. Damit kann
   * zum Beispiel eine Umschaltung zwischen W und Q bei W/Q-Zeitreihen erreicht werden.
   * <ul>
   * <li>ignoreType1: Unterdrückter Typ des ersten Knopfs, eine oder mehrere (;-getrennt) der Konstanten aus
   * {@link org.kalypso.ogc.sensor.timeseries.TimeserieConstants}</li>
   * <li>ignoreType2: Unterdrückter Typ des zweiten Knopfs</li>
   * <li>ignoreLabel1: Label des ersten Knopfes</li>
   * <li>ignoreLabel2: Label des zweiten Knopfes</li>
   * </ul>
   */
  private static final String PROP_IGNORETYPE1 = "ignoreType1";

  /** @see #PROP_IGNORETYPE1 */
  private static final String PROP_IGNORETYPE2 = "ignoreType2";

  /** @see #PROP_IGNORETYPE1 */
  private static final String PROP_IGNORELABEL1 = "ignoreLabel1";

  /** @see #PROP_IGNORETYPE1 */
  private static final String PROP_IGNORELABEL2 = "ignoreLabel2";

  /**
   * Argument: diese Argumente dienen dazu, einen weiteren Konfigurierbaren Knopf unterhalb des Diagramms anzubringen.
   * Der Knopf löst einen Ant-Launch aus. Falls gesetzt, wird ein evtl. vorhandener interner Knopf überschrieben.
   * <ul>
   * <li>buttonText: Beschriftung des Knopfs</li>
   * <li>buttonLaunch: Name des Ant-Launchs, welcher im .model/lunach Vrzeichnis liegen muss.</li>
   * <li>buttonTooltip: Tooltip des Knopfs</li>
   * <li>buttonProperties: Zusätzliche Properties, welche an das Ant-Skript weitergegeben werden. Inhalt muss eine
   * Argumentliste sein.</li>
   * </ul>
   */
  private static final String PROP_BUTTON_TEXT = "buttonText";

  private static final String PROP_BUTTON_LAUNCH = "buttonLaunch";

  private static final String PROP_BUTTON_TOOLTIP = "buttonTooltip";

  private static final String PROP_BUTTON_PROPERTIES = "buttonProperties";

  /**
   * Argument: If set, the argument is interpetated as an file relative to the current project. The file will be shown
   * as html viewed inside the calculation wizard for nice navigation.
   * <p>
   * The path must be project relative
   * </p>
   * <p>
   * Supported protocols are 'http(s)' and 'file'
   * </p>
   */
  private static final String PROP_HTML_PATH = "htmlFilePath";

  /**
   * Argument: If set, pressing the help-buttons results in a jump to this context of the eclipse help-system.
   * <p>
   * The 'helpId' argument of the wizard itself must be non null, in order to show the help button.
   * </p>
   */
  private static final String PROP_HELP_ID = "helpId";

  /**
   * Argument: a string indicating, how the Timeseries-Table is filled with timeseries.
   * <ul>
   * <li>all: the timeseries of all features of the active theme are shown</li>
   * <li>selected: only the timeseries of the selected features are shown</li>
   * <li>template: no dynamic behaviiour, a template is loaded (@see #PROP_ZMLTABLE_TEMPLATE)</li>
   * </ul>
   * One of 'selected' or 'all'. If 'selected', only Timeseries of selectede features will be shown, else, timeseries of
   * all features will be shown (in zmlTable)
   */
  private final static String PROP_ZMLTABLE_SHOW = "zmlTableShow";

  /**
   * Argument: applies only, if {@link #PROP_ZMLTABLE_SHOW}is set to 'template'
   */
  private final static String PROP_ZMLTABLE_TEMPLATE = "zmlTableTemplate";

  /**
   * Argument: (optional) a boolean which indicates whether the table should be sorted according to the alphabetical
   * order of the columns' name or not.
   */
  private final static String PROP_ZMLTABLE_ALPHA_SORT = "zmlTableAlphaSort";

  /** Argument: Pfad auf Vorlage für die Gis-Tabelle (.gtt Datei) */
  private final static String PROP_TABLETEMPLATE = "tableTemplate";

  /** Argument: Flag (true or false) if the legend of the diagram is visible */
  private final static String PROP_DIAG_SHOW_LEGEND = "diagShowLegend";

  /** Argument: Pfad auf die Vorlage für das Diagramm (.odt Datei) */
  private final static String PROP_DIAGTEMPLATE = "diagTemplate";

  /** Argument: Pfad auf Vorlage für die Karte (.gmt Datei) */
  private final static String PROP_MAPTEMPLATE = "mapTemplate";

  /**
   * Argument: Full class name of the map-widget to use. Default is:
   * {@link org.kalypso.ogc.gml.map.widgets.SelectSingleFeatureWidget}. If empty, no widget will be selected initially.
   */
  private static final String PROP_MAP_WIDGETCLASS = "mapWidgetClass";

  /**
   * Argument: Plug-in id (symbolic name) of the plug.in containing the given widget class. If none if given,
   * 'org.kalypso.ui' is assumed.
   */
  private static final String PROP_MAP_WIDGETPLUGIN = "mapWidgetPlugin";

  /**
   * Argument: Falls true, wird der Context der Karte stets auf den CalcCaseFolder gesetzt. Ansonsten wie üblich auf die
   * .gtt Datei.
   */
  public final static String PROP_MAPTEMPLATEISCALC = "mapTemplateContextIsCalcCase";

  /**
   * Argument: Falls gesetzt, wird das Feature mit dieser ID selektiert, nachdem die Karte geladen wurde. Ansonsten das
   * erste Feature
   */
  private static final String PROP_FEATURE_TO_SELECT_ID = "selectFeatureID";

  /**
   * Argument: Falls true, wird die Karte auf den FullExtent maximiert, sonst wird {@link #m_wishBoundingBox}angesetzt
   */
  private static final String PROP_MAXIMIZEMAP = "maximizeMap";

  /**
   * Argument: feature with this id will be in the center of the map
   */
  private static final String PROP_PAN_TO_FEATURE_ID = "pantoFeatureId";

  /**
   * Argument: select first feature of active layer by default ?<br>
   * default is "true" <br>
   * valid values are "true" or "false"
   */
  private static final String PROP_SELECT_FIRST_FEATURE_BY_DEFAULT = "selectFirstFeatureByDefault";

  /**
   * Argument: if present, a toolbar will be shown above the map <br>
   */
  private static final String PROP_MAP_TOOLBAR = "mapToolbar";

  /**
   * Sub-Argument in mapToolbar: id of a command that will be added to the map-toolbar <br>
   */
  private static final String PROP_MAP_TOOLBAR_CMD = "command";

  public static final int SELECT_FROM_MAPVIEW = 0;

  public static final int SELECT_FROM_TABLEVIEW = 1;

  public static final int SELECT_FROM_FEATUREVIEW = 2;

  final ICommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  private Arguments m_arguments = null;

  private IProject m_project = null;

  private IFolder m_calcFolder = null;

  private final Properties m_replaceProperties = new Properties();

  private GisTemplateMapModell m_mapModell = null;

  private MapPanel m_mapPanel;

  GM_Envelope m_wishBoundingBox;

  private Frame m_diagFrame = null;

  private ObservationChart m_obsChart = null;

  private DiagView m_diagView = null;

  private Frame m_tableFrame = null;

  private TableView m_tableView = null;

  private Obstableview m_tableTemplate = null;

  private ObservationTable m_table = null;

  private TimeserieFeatureProps[] m_tsProps;

  private LayerTableViewer m_gisTableViewer;

  private final ControlAdapter m_controlAdapter = new ControlAdapter()
  {
    // WARNING: this adapter causes the map to bee painted twice, each time it is resized
    // the reason ist, that the map-panel itself is also a resize-listener
    // moreover, the order in which the two resize events are called is random!
    @Override
    public void controlResized( final ControlEvent e )
    {
      maximizeMap();
    }
  };

  private String m_showZmlTable = "selected";

  private int m_selectSource;

  private String m_ignoreType;

  private URL m_htmlURL = null;

  /** the one and only one selection manager for this page */
  private final IFeatureSelectionManager m_selectionManager = new FeatureSelectionManager2();

  private RestoreSelectionHelper m_selectionRestorer = null;

  private Obsdiagview m_obsdiagviewType;

  /**
   * This listeners reports failure of command executions within this page <br>
   * TODO: may now be moved to a more central place.
   */
  private final IExecutionListener m_cmdExecutionListener = new IExecutionListener()
  {
    public void notHandled( final String commandId, final org.eclipse.core.commands.NotHandledException exception )
    {
      handleExecutionFailure( exception );
    }

    public void postExecuteFailure( final String commandId, final ExecutionException exception )
    {
      handleExecutionFailure( exception );
    }

    public void postExecuteSuccess( final String commandId, final Object returnValue )
    {
    }

    public void preExecute( final String commandId, final ExecutionEvent event )
    {
    }
  };

  private final IPageChangingListener m_pageChangingListener = new IPageChangingListener()
  {
    @Override
    public void handlePageChanging( final PageChangingEvent event )
    {
      if( event.getCurrentPage() == AbstractCalcWizardPage.this )
        event.doit = AbstractCalcWizardPage.this.handlePageChanging();
    }
  };

  private final IPageChangedListener m_pageChangedListener = new IPageChangedListener()
  {
    @Override
    public void pageChanged( final PageChangedEvent event )
    {
      if( event.getSelectedPage() == AbstractCalcWizardPage.this )
        handlePageSelected();
    }
  };

  private MapPanelSourceProvider m_sourceProvider;

  public AbstractCalcWizardPage( final String name, final int selectSource )
  {
    super( name );

    m_selectSource = selectSource;
  }

  /**
   * @see org.eclipse.jface.wizard.WizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
   */
  @Override
  public void setWizard( final IWizard newWizard )
  {
    disposeWizard();

    super.setWizard( newWizard );

    if( newWizard != null )
    {
      final IWizardContainer container = newWizard.getContainer();
      if( container instanceof WizardView )
      {
        final WizardView view = (WizardView) container;
        final IWorkbenchPartSite site = view.getSite();
        final ICommandService cmdService = (ICommandService) site.getService( ICommandService.class );
        cmdService.addExecutionListener( m_cmdExecutionListener );

        view.addPageChangingListener( m_pageChangingListener );
        view.addPageChangedListener( m_pageChangedListener );
      }
    }

  }

  private void disposeWizard( )
  {
    final IWizard oldWizard = getWizard();
    if( oldWizard != null )
    {
      final IWizardContainer container = oldWizard.getContainer();
      if( container instanceof WizardView )
      {
        final WizardView view = (WizardView) container;
        final IWorkbenchPartSite site = view.getSite();
        final ICommandService cmdService = (ICommandService) site.getService( ICommandService.class );
        cmdService.removeExecutionListener( m_cmdExecutionListener );

        view.removePageChangingListener( m_pageChangingListener );
        view.removePageChangedListener( m_pageChangedListener );
      }

      destroyContext( container );
    }
  }

  private void destroyContext( final IWizardContainer container )
  {
    if( container instanceof WizardView )
    {
      if( m_sourceProvider != null )
      {
        m_sourceProvider.dispose();
        m_sourceProvider = null;
      }
    }
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  @Override
  public void dispose( )
  {
    disposeWizard();

    if( m_mapModell != null )
      m_mapModell.dispose();

    if( m_gisTableViewer != null )
      m_gisTableViewer.dispose();

    if( m_table != null )
      m_table.dispose();

    if( m_tableView != null )
      m_tableView.dispose();

    if( m_obsChart != null )
      m_obsChart.dispose();

    if( m_diagView != null )
      m_diagView.dispose();

    if( m_mapPanel != null )
    {
      m_mapPanel.removeSelectionChangedListener( this );
      m_mapPanel.dispose();
    }

    super.dispose();
  }

  public Arguments getArguments( )
  {
    return m_arguments;
  }

  public IProject getProject( )
  {
    return m_project;
  }

  public IFolder getCalcFolder( )
  {
    return m_calcFolder;
  }

  public URL getContext( )
  {
    try
    {
      return ResourceUtilities.createURL( getCalcFolder() );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      return null;
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.IModelWizardPage#init(IProject, String, ImageDescriptor,
   *      Arguments, IFolder)
   */
  public void init( final IProject project, final String pagetitle, final ImageDescriptor imagedesc, final Arguments arguments, final IFolder calcFolder )
  {
    setTitle( pagetitle );
    setImageDescriptor( imagedesc );
    m_project = project;
    m_arguments = arguments;
    m_tsProps = CalcWizardHelper.parseTimeserieFeatureProps( arguments );

    m_calcFolder = calcFolder;

    try
    {
      final URL calcURL = ResourceUtilities.createURL( calcFolder );
      m_replaceProperties.setProperty( "calcdir:", calcURL.toString() );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
    }

    // load HTML
    final String htmlPath = arguments.getProperty( PROP_HTML_PATH, null );
    if( htmlPath != null )
    {
      final IFile htmlFile = getProject().getFile( new Path( htmlPath ) );
      final File htmlLocation = htmlFile.getLocation().toFile();
      try
      {
        m_htmlURL = htmlLocation.toURI().toURL();
      }
      catch( final MalformedURLException e )
      {
        m_htmlURL = null;

        e.printStackTrace();

        final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, "Siehe Details", e );
        ErrorDialog.openError( getShell(), "title", "msg", status );
      }
    }
  }

  /**
   * @see org.kalypso.commons.command.ICommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  /**
   * Diese Properties werden benutzt, um die Vorlagendateien zu parsen
   * 
   * @return properties
   */
  protected Properties getReplaceProperties( )
  {
    return m_replaceProperties;
  }

  /**
   * Erzeugt die Karte und alle Daten die dranhängen und gibt die enthaltende Control zurück
   */
  protected Control initMap( final Composite parent ) throws Exception
  {
    final String mapFileName = getArguments().getProperty( PROP_MAPTEMPLATE );
    final boolean mapContextIsCalcCase = Boolean.valueOf( getArguments().getProperty( PROP_MAPTEMPLATEISCALC, Boolean.TRUE.toString() ) ).booleanValue();
    final IFile mapFile = (IFile) getProject().findMember( mapFileName );
    if( mapFile == null )
      throw new CoreException( StatusUtilities.createErrorStatus( "Vorlagendatei existiert nicht: " + mapFileName ) );

    final Gismapview gisview = GisTemplateHelper.loadGisMapView( mapFile, getReplaceProperties() );
    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    final URL context = mapContextIsCalcCase ? getContext() : ResourceUtilities.createURL( mapFile );
    m_mapModell = new GisTemplateMapModell( context, crs, mapFile.getProject(), m_selectionManager );
    m_mapModell.createFromTemplate( gisview );
    m_mapPanel = new MapPanel( this, m_selectionManager );

    m_mapPanel.addSelectionChangedListener( this );

    m_wishBoundingBox = GisTemplateHelper.getBoundingBox( gisview );

    if( "true".equals( getArguments().getProperty( PROP_MAXIMIZEMAP, "false" ) ) )
      m_wishBoundingBox = null;

    final Composite mapAndToolbar = new Composite( parent, SWT.NONE );
    final GridLayout mapAndToolbarLayout = new GridLayout();
    mapAndToolbarLayout.marginHeight = 0;
    mapAndToolbarLayout.marginWidth = 0;
    mapAndToolbarLayout.horizontalSpacing = 0;
    mapAndToolbarLayout.verticalSpacing = 0;
    mapAndToolbar.setLayout( mapAndToolbarLayout );

    final ToolBarManager manager = createToolbar( mapAndToolbar );
    if( manager != null )
      manager.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Composite mapComposite = new Composite( mapAndToolbar, SWT.BORDER | SWT.RIGHT | SWT.EMBEDDED );
    mapComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    final Frame virtualFrame = SWT_AWT.new_Frame( mapComposite );

    virtualFrame.setVisible( true );
    m_mapPanel.setVisible( true );
    virtualFrame.add( m_mapPanel );

    m_mapPanel.setMapModell( m_mapModell );
    m_mapPanel.repaint();

    if( manager != null )
      fillMapToolbar( manager );

    return mapAndToolbar;
  }

  protected IWidget createWidget( )
  {
    try
    {
      final String widgetClass = getArguments().getProperty( PROP_MAP_WIDGETCLASS, SelectSingleFeatureWidget.class.getName() );
      final String widgetPlugin = getArguments().getProperty( PROP_MAP_WIDGETPLUGIN, KalypsoGisPlugin.getId() );

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
  private ToolBarManager createToolbar( final Composite mapAndToolbar )
  {
    final Arguments arguments = getArguments().getArguments( PROP_MAP_TOOLBAR );
    if( arguments == null )
      return null;

    final ToolBar toolbar = new ToolBar( mapAndToolbar, SWT.HORIZONTAL | SWT.SHADOW_OUT | SWT.FLAT );
    return new ToolBarManager( toolbar );
  }

  /**
   * Fills the toolbar according to the configuration.
   */
  private void fillMapToolbar( final ToolBarManager manager )
  {
    final Arguments arguments = getArguments().getArguments( PROP_MAP_TOOLBAR );
    for( final Entry<String, Object> entry : arguments.entrySet() )
    {
      final String key = entry.getKey();
      final Object value = entry.getValue();

      if( value != null && key.startsWith( PROP_MAP_TOOLBAR_CMD ) )
      {
        final String commandId = value.toString();

        final WizardView wizardView = (WizardView) getWizard().getContainer();
        final IWorkbenchPartSite site = wizardView.getSite();

        final CommandContributionItem item = new CommandContributionItem( site, ObjectUtils.identityToString( this ) + commandId, commandId, new HashMap<String, String>(), null, null, null, null, null, null, SWT.PUSH );

        manager.add( item );
      }
    }

    manager.update( true );
  }

  protected IMapModell getMapModell( )
  {
    return m_mapModell;
  }

  // TODO: why object?
  protected Object getMapPanel( )
  {
    return m_mapPanel;
  }

  public final void maximizeMap( )
  {
    if( m_wishBoundingBox == null )
    {
      final GM_Envelope fullExtentBoundingBox = m_mapPanel.getMapModell().getFullExtentBoundingBox();
      if( fullExtentBoundingBox != null )
      {
        final double buffer = Math.max( fullExtentBoundingBox.getWidth(), fullExtentBoundingBox.getHeight() ) * 0.025;

        final GM_Envelope bufferedExtent = fullExtentBoundingBox.getBuffer( buffer );
        m_mapPanel.setBoundingBox( bufferedExtent );
      }
    }
    else
      m_mapPanel.setBoundingBox( m_wishBoundingBox );
  }

  protected Control initDiagram( final Composite parent )
  {
    /* Load template if defined */
    final String diagFileName = getArguments().getProperty( PROP_DIAGTEMPLATE );
    if( diagFileName != null )
    {
      final IFile diagFile = (IFile) getProject().findMember( diagFileName );
      InputStream is = null;
      try
      {
        is = diagFile.getContents();
        m_obsdiagviewType = DiagViewUtils.loadDiagramTemplateXML( is );
        is.close();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        IOUtils.closeQuietly( is );
      }
    }

    final String ignoreType = m_arguments.getProperty( PROP_IGNORETYPE1, null );
    m_ignoreType = ignoreType;

    try
    {
      final String showLegendString = getArguments().getProperty( PROP_DIAG_SHOW_LEGEND, "true" );
      final boolean showLegend = Boolean.valueOf( showLegendString ).booleanValue();

      // actually creates the template
      m_diagView = new DiagView( true );

      m_obsChart = new ObservationChart( m_diagView );
      m_obsChart.setBackgroundPaint( Color.WHITE );

      final Composite composite = new Composite( parent, SWT.BORDER | SWT.RIGHT | SWT.EMBEDDED );
      m_diagFrame = SWT_AWT.new_Frame( composite );

      m_diagFrame.add( ChartFactory.createChartPanel( m_obsChart ) );
      m_diagFrame.setVisible( true );

      if( m_obsdiagviewType == null )
        m_diagView.setShowLegend( showLegend );
      else
        DiagViewUtils.applyXMLTemplate( m_diagView, m_obsdiagviewType, getContext(), false, null );

      return composite;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final Text text = new Text( parent, SWT.CENTER );
      text.setText( "Kein Diagram vorhanden" );

      return text;
    }
  }

  protected ControlAdapter getControlAdapter( )
  {
    return m_controlAdapter;
  }

  public void refreshDiagram( )
  {
    final TSLinkWithName[] obs = getObservations( true );
    refreshDiagramForContext( obs, getContext() );
  }

  public void refreshZMLTable( )
  {
    if( "template".equals( m_showZmlTable ) && m_tableTemplate != null )
    {
      // each time, we apply the template, so we can switch between ignore types
      m_tableView.setIgnoreTypes( getIgnoreTypes() );

      final IStatus stati = TableViewUtils.applyXMLTemplate( m_tableView, m_tableTemplate, getContext(), false, null );
      if( !stati.isOK() )
        KalypsoSimulationUIPlugin.getDefault().getLog().log( stati );

      // set alphasort flag according to template (NOTE: might be overriden by
      // argument value of the calcwizard)
      m_table.setAlphaSortActivated( m_tableView.isAlphaSort() );

      return;
    }

    final TSLinkWithName[] links = getObservations( "selected".equalsIgnoreCase( m_showZmlTable ) );
    refreshZmlTableForContext( links, getContext() );
  }

  protected void refreshDiagramForContext( final TSLinkWithName[] links, final URL context )
  {
    if( m_diagView != null && m_obsdiagviewType == null )
      CalcWizardHelper.updateZMLView( m_diagView, links, context, true, getIgnoreTypes() );
  }

  protected void refreshZmlTableForContext( final TSLinkWithName[] links, final URL context )
  {
    if( m_tableView != null )
      CalcWizardHelper.updateZMLView( m_tableView, links, context, true, getIgnoreTypes() );
  }

  protected Control initFeatureTable( final Composite parent )
  {
    try
    {
      final IFeatureChangeListener fcl = new IFeatureChangeListener()
      {
        public void featureChanged( final ICommand changeCommand )
        {
          // do nothing in wizard modus
        }

        public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
        {
          // do nothing in wizard modus
        }
      };
      m_gisTableViewer = new LayerTableViewer( parent, SWT.BORDER, this, KalypsoGisPlugin.getDefault().createFeatureTypeCellEditorFactory(), m_selectionManager, fcl );

      final String templateFileName = (String) getArguments().get( PROP_TABLETEMPLATE );
      if( templateFileName != null )
      {
        final IFile templateFile = (IFile) getProject().findMember( templateFileName );
        final Gistableview template = GisTemplateHelper.loadGisTableview( templateFile, getReplaceProperties() );
        m_gisTableViewer.applyTableTemplate( template, getContext() );
      }

      return m_gisTableViewer.getControl();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final Text text = new Text( parent, SWT.NONE );
      text.setText( "Fehler beim Laden des TableTemplate" );

      return null;
    }
  }

  protected Control initZmlTable( final Composite parent )
  {
    try
    {
      m_showZmlTable = m_arguments.getProperty( PROP_ZMLTABLE_SHOW, "selected" );

      if( "template".equalsIgnoreCase( m_showZmlTable ) )
      {
        final String templateFileName = m_arguments.getProperty( PROP_ZMLTABLE_TEMPLATE, null );
        if( templateFileName == null )
          throw new CoreException( StatusUtilities.createErrorStatus( "Keine Vorlagendatei definiert. Die Property " + PROP_ZMLTABLE_TEMPLATE + " muss definiert sein." ) );

        final IFile templateFile = (IFile) getProject().findMember( templateFileName );
        if( templateFile == null )
          throw new CoreException( StatusUtilities.createErrorStatus( "Vorlagendatei existiert nicht: " + templateFileName ) );

        final InputStream templateStream = templateFile.getContents();
        m_tableTemplate = TableViewUtils.loadTableTemplateXML( templateStream );
        templateStream.close();
      }

      m_tableView = new TableView();

      m_table = new ObservationTable( m_tableView );

      // set alphasort flag according to arguments (NOTE: might override setting of the template)
      final String alphaSort = m_arguments.getProperty( PROP_ZMLTABLE_ALPHA_SORT, null );
      if( alphaSort != null )
        m_table.setAlphaSortActivated( Boolean.valueOf( alphaSort ).booleanValue() );

      final String ignoreType = m_arguments.getProperty( PROP_IGNORETYPE1, null );
      m_ignoreType = ignoreType;

      final Composite composite = new Composite( parent, SWT.RIGHT | SWT.EMBEDDED );
      m_tableFrame = SWT_AWT.new_Frame( composite );

      m_tableFrame.add( m_table );

      m_table.setVisible( true );
      m_tableFrame.setVisible( true );

      return composite;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      if( e instanceof CoreException )
      {
        final IStatus status = ((CoreException) e).getStatus();
        ErrorDialog.openError( parent.getShell(), "Tabellenvorlage laden", "Fehler beim Laden der Tabellenvorlage", status );
      }

      final StringWriter sw = new StringWriter();
      e.printStackTrace( new PrintWriter( sw ) );

      final Text text = new Text( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );

      text.setText( "Fehler beim Anzeigen der Tabelle:\n\n" + e.getLocalizedMessage() + "\n\n" + sw.toString() );

      return text;
    }
  }

  public TSLinkWithName[] getObservations( final boolean onlySelected )
  {
    final List< ? > selectedFeatures = onlySelected ? getSelectedFeatures() : getFeatures();

    final Collection<TSLinkWithName> foundObservations = new ArrayList<TSLinkWithName>( selectedFeatures.size() );

    for( final Object name2 : selectedFeatures )
    {
      final Feature kf = (Feature) name2;

      for( final TimeserieFeatureProps tsprop : m_tsProps )
      {
        final String nameColumn = tsprop.getNameColumn();
        String name = tsprop.getNameString();
        if( nameColumn != null )
        {
          final Object fname = kf.getProperty( tsprop.getNameColumn() );
          if( fname != null )
            name = name.replaceAll( "%featureprop%", fname.toString() );
        }
        else
          name = tsprop.getNameString();

        final String linkColumn = tsprop.getLinkColumn();
        final TimeseriesLinkType obsLink = getTimeseriesLink( kf, linkColumn );
        if( obsLink != null )
        {
          final TSLinkWithName linkWithName = new TSLinkWithName( name, obsLink.getLinktype(), obsLink.getHref(), tsprop.getFilter(), tsprop.getColor(), tsprop.getLineWidth(), tsprop.getLineDash() );
          foundObservations.add( linkWithName );
        }
      }
    }

    return foundObservations.toArray( new TSLinkWithName[foundObservations.size()] );
  }

  private TimeseriesLinkType getTimeseriesLink( final Feature feature, final String propertyName )
  {
    final IPropertyType pt = feature.getFeatureType().getProperty( propertyName );
    if( pt == null )
      return null;

    return (TimeseriesLinkType) feature.getProperty( propertyName );
  }

  protected FeatureList getSelectedFeatures( )
  {
    return getFeatures( true );
  }

  protected FeatureList getFeatures( )
  {
    return getFeatures( false );
  }

  private FeatureList getFeatures( final boolean selected )
  {
    final IKalypsoTheme activeTheme;
    switch( m_selectSource )
    {
      case SELECT_FROM_MAPVIEW:
        final IMapModell mapModell = getMapModell();
        if( mapModell == null )
          return FeatureFactory.createFeatureList( null, null );
        activeTheme = mapModell.getActiveTheme();
        break;
      case SELECT_FROM_TABLEVIEW:
        activeTheme = m_gisTableViewer.getTheme();
        break;
      case SELECT_FROM_FEATUREVIEW:
        activeTheme = null;
        break;
      default:
        activeTheme = null;
    }
    if( !(activeTheme instanceof IKalypsoFeatureTheme) )
      return FeatureFactory.createFeatureList( null, null );

    final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme) activeTheme;
    final FeatureList featureList = kft.getFeatureListVisible( null );
    if( featureList == null )
      return FeatureFactory.createFeatureList( null, null );

    if( selected )
    {
      final IStructuredSelection selection = KalypsoFeatureThemeSelection.filter( m_selectionManager.toList(), kft );
      return FeatureFactory.createFeatureList( null, null, selection.toList() );
    }

    return featureList;
  }

  /**
   * We override setControl to set the help context-id on the control. It is the best place since we know here which
   * context-id to use, and subclasses must call this method with their own controls.
   * 
   * @see org.eclipse.jface.dialogs.DialogPage#setControl(org.eclipse.swt.widgets.Control)
   */
  @Override
  protected void setControl( final Control newControl )
  {
    final String helpId = getHelpId();

    // this is where we hook the help id with the given control
    if( helpId != null )
    {
      final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
      helpSystem.setHelp( newControl, helpId );
    }

    super.setControl( newControl );
  }

  protected void setObsIgnoreType( final String ignoreType )
  {
    m_ignoreType = ignoreType;

    // save observations else changes are lost
    saveDirtyObservations( new NullProgressMonitor() );

    refreshDiagram();
    refreshZMLTable();
  }

  protected Composite createIgnoreButtonPanel( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 3, false ) );

    // properties lesen
    final String ignoreType1 = m_arguments.getProperty( PROP_IGNORETYPE1, null );
    final String ignoreType2 = m_arguments.getProperty( PROP_IGNORETYPE2, null );

    final String ignoreLabel1 = m_arguments.getProperty( PROP_IGNORELABEL1, ignoreType2 );
    final String ignoreLabel2 = m_arguments.getProperty( PROP_IGNORELABEL2, ignoreType1 );

    if( ignoreType1 == null || ignoreType2 == null )
    {
      panel.dispose();
      return null;
    }

    final Label label = new Label( panel, SWT.NONE );
    label.setText( "Diagrammanzeige:" );
    final GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = GridData.END;
    label.setLayoutData( gridData );

    final Button radioQ = new Button( panel, SWT.RADIO );
    radioQ.setText( ignoreLabel1 );

    final Button radioW = new Button( panel, SWT.RADIO );
    radioW.setText( ignoreLabel2 );

    radioQ.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        if( radioQ.getSelection() )
          setObsIgnoreType( ignoreType1 );
      }
    } );

    radioW.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        if( radioW.getSelection() )
          setObsIgnoreType( ignoreType2 );
      }
    } );

    radioQ.setSelection( true );

    return panel;
  }

  /**
   * Saves the dirty observations that were edited in the table.
   */
  protected void saveDirtyObservations( final IProgressMonitor monitor )
  {
    if( m_table == null )
      return;

    TableViewUtils.saveDirtyObservations( Arrays.asList( m_tableView.getItems() ), monitor );
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.IModelWizardPage#saveState(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void saveState( final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( "Zustand wird gespeichert", 2000 );

    try
    {
      if( m_mapModell != null )
      {
        final IKalypsoTheme activeTheme = m_mapModell.getActiveTheme();
        if( activeTheme instanceof GisTemplateFeatureTheme )
        {
          final GisTemplateFeatureTheme gtft = (GisTemplateFeatureTheme) activeTheme;
          if( gtft.getStatus().isOK() )
            m_selectionRestorer = new RestoreSelectionHelper( gtft.getLayerKey(), m_selectionManager );
        }
      }

      saveDirtyObservations( new SubProgressMonitor( monitor, 1000 ) );

      if( m_gisTableViewer != null )
        m_gisTableViewer.saveData( new SubProgressMonitor( monitor, 1000 ) );
      else
        monitor.worked( 1000 );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.IModelWizardPage#restoreState(boolean)
   */
  public void restoreState( final boolean clearState ) throws CoreException
  {
    if( m_selectionRestorer != null )
      m_selectionRestorer.restoreSelection();

    if( clearState )
      m_selectionRestorer = null;
  }

  /**
   * @deprecated use ant-stuff instead
   */
  @Deprecated
  protected void runCalculation( )
  {
    runSomething( new ICoreExceptionRunnable()
    {
      public IStatus run( final IProgressMonitor monitor ) throws CoreException
      {
        final ModelNature nature = (ModelNature) getCalcFolder().getProject().getNature( ModelNature.ID );
        final String modelspec = getArguments().getProperty( PROP_MODELSPEC, null );
        return nature.runCalculation( getCalcFolder(), monitor, modelspec );
      }
    }, "Berechnung wird durchgeführt", "Modellrechnung", "Modellrechnung fehlgeschlagen" );
  }

  /**
   * Run an ant skript ore something. It runs as follows:
   * <ul>
   * <li>clear and remeber the selection</li>
   * <li>save alle pages</li>
   * <li>run the runnable</li>
   * <li>refresh all pages</li>
   * <li>reset the selection</li>
   * </ul>
   */
  protected void runSomething( final ICoreExceptionRunnable runnable, final String taskName, final String title, final String errorMessage )
  {
    final IWizard wizard = getWizard();
    if( !(wizard instanceof CalcWizard) )
      return;

    final CalcWizard calcWizard = (CalcWizard) wizard;

    final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        final SubMonitor progress = SubMonitor.convert( monitor, taskName, 3000 );

        try
        {
          final IStatus saveStatus = calcWizard.saveAllPages( progress.newChild( 500, SubMonitor.SUPPRESS_NONE ) );
          if( !saveStatus.isOK() )
            throw new CoreException( saveStatus );

          final IStatus status = runnable.run( progress.newChild( 2500, SubMonitor.SUPPRESS_NONE ) );
          throw new CoreException( status );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, op );

    StatusUtilities.openSpecialErrorDialog( getShell(), title, errorMessage, status, true );

    final Job job = new Job( "Stelle Wizard-Seiten wieder her" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        return calcWizard.refreshAllPages( monitor );
      }
    };
    job.addJobChangeListener( new HandleDoneJobChangeAdapter( getShell(), "Seiten Wiederherstellen", "Fehler beim Wiederherstellen der Seiten", true, StatusUtilities.ALL_STATUS_MASK, false ) );
    // TRICKY: this should only happen, if the workspaces are really reloaded.
    // This HACK just gives one second to do this. Is this always enough?
    // Better would be some code, which is called, after all Worspace-Modification events of
    // operation 1 are handled. Is this possible?
    job.schedule( 1000 );
  }

  protected void selectFeaturesInMap( final String[] fids )
  {
    selectFeaturesInMap( fids, false, true );
  }

  /**
   * @param forceSelectFeatureFromMap
   *          BUGFIX: if this option is set, a feature of the map will be selected. If it is false, so such thing
   *          happens. This is needed to distinguish beetween deploy in Sachsen and Sachsen-Anhalt. This prevents bugs
   *          in the MapAndFeatureWizardPage concerning display of the .gft.
   *          <p>
   *          Option is true: used in Sachsen-Anhalt: Saale
   *          </p>
   *          <p>
   *          Option is false: used in Sachsen: Weisse-Elster
   *          </p>
   */
  @SuppressWarnings("deprecation")
  protected void selectFeaturesInMap( final String[] fids, final boolean noDefaultSelection, final boolean forceSelectFeatureFromMap )
  {
    final IFeatureSelectionManager selectionManager = m_selectionManager;
    final MapPanel mapPanel = m_mapPanel;
    final IMapModell mapModell = getMapModell();

    // this may happen, if the page was never created
    if( mapPanel == null || mapModell == null )
      return;

    final String showZmlTable = m_showZmlTable;

    new GisTemplateLoadedThread( mapModell, new Runnable()
    {
      public void run( )
      {
        boolean refreshDiagramHack = true;

        // erstes feature des aktiven themas selektieren
        final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
        if( activeTheme instanceof IKalypsoFeatureTheme )
        {
          final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme) activeTheme;

          final String panToFid = getArguments().getProperty( PROP_PAN_TO_FEATURE_ID, null );

          if( kft != null && panToFid != null )
          {
            final CommandableWorkspace workspace = kft.getWorkspace();
            final Feature feature = workspace.getFeature( panToFid );
            if( feature != null )
            {
              final GM_Object defaultGeometryProperty = feature.getDefaultGeometryProperty();
              final GM_Point centroid = defaultGeometryProperty.getCentroid();
              m_wishBoundingBox = m_wishBoundingBox.getPaned( centroid );

              if( forceSelectFeatureFromMap )
              {
                // FIX: damit überhaupt was selektiert wird, sonst passiert nichts
                // see also comment on parameter 'forceSelectFeatureFromMap'
                final Feature[] featuresToRemove = FeatureSelectionHelper.getFeatures( selectionManager );
                selectionManager.changeSelection( featuresToRemove, new EasyFeatureWrapper[] { new EasyFeatureWrapper( workspace, feature, null, null ) } );
              }
            }
          }

          if( !noDefaultSelection )
          {
            final CommandableWorkspace workspace = kft.getWorkspace();

            final FeatureList featureList = kft.getFeatureListVisible( null );
            if( featureList != null && featureList.size() != 0 )
            {
              final List<EasyFeatureWrapper> easyFeatures = new ArrayList<EasyFeatureWrapper>( fids.length );
              for( final String fid : fids )
              {
                if( fid != null )
                {
                  final Feature feature = workspace.getFeature( fid );
                  if( feature != null )
                    easyFeatures.add( new EasyFeatureWrapper( workspace, feature, null, null ) );
                }
              }
              final EasyFeatureWrapper[] easyArray;
              if( easyFeatures.isEmpty() && selectFirstFeatureByDefault() )
                easyArray = new EasyFeatureWrapper[] { new EasyFeatureWrapper( workspace, (Feature) featureList.get( 0 ), null, null ) };
              else
                easyArray = easyFeatures.toArray( new EasyFeatureWrapper[easyFeatures.size()] );

              final Feature[] featuresToRemove = FeatureSelectionHelper.getFeatures( selectionManager );
              selectionManager.changeSelection( featuresToRemove, easyArray );

              // only refresh diagram explicitly, if we dont send an selection event
              refreshDiagramHack = false;
            }
          }
        }

        if( !noDefaultSelection )
        {
          // maybe redundant?
          if( refreshDiagramHack )
            refreshDiagram();

          // timing problem: manschmal wird die Tabelle doch nicht aktualisiert
          if( !"selected".equalsIgnoreCase( showZmlTable ) || refreshDiagramHack )
            refreshZMLTable();
        }

        maximizeMap();

        final IWidget widget = createWidget();
        mapPanel.getWidgetManager().setActualWidget( widget );
      }
    } ).start();
  }

  protected String[] getFeaturesToSelect( )
  {
    final String fid = getArguments().getProperty( PROP_FEATURE_TO_SELECT_ID, null );
    return fid == null ? new String[] {} : new String[] { fid };
  }

  protected boolean selectFirstFeatureByDefault( )
  {
    return "true".equals( getArguments().getProperty( PROP_SELECT_FIRST_FEATURE_BY_DEFAULT, "true" ) );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IHtmlWizardPage#getHtmlURL()
   */
  public URL getHtmlURL( )
  {
    return m_htmlURL;
  }

  protected void initButtons( final Composite parent, final String buttonText, final String tooltipText, final SelectionListener buttonListener )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

    final Composite radioPanel = createIgnoreButtonPanel( panel );
    if( radioPanel != null )
    {
      final GridData ignoreData = new GridData( GridData.FILL_HORIZONTAL );
      ignoreData.horizontalAlignment = GridData.BEGINNING;
      radioPanel.setLayoutData( ignoreData );
    }

    // first try ant button
    final String antLaunch = getArguments().getProperty( PROP_BUTTON_LAUNCH, null );
    final String buttonTextOwn = getArguments().getProperty( PROP_BUTTON_TEXT, null );
    final String buttonTooltipOwn = getArguments().getProperty( PROP_BUTTON_TOOLTIP, null );
    final Arguments buttonProperties = getArguments().getArguments( PROP_BUTTON_PROPERTIES );
    Button button = createButton( panel, buttonTextOwn, buttonTooltipOwn, new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        launchAnt( antLaunch, buttonTextOwn, buttonTextOwn, buttonTooltipOwn, buttonProperties );
      }
    } );

    // else, try internal button
    if( button == null )
      button = createButton( panel, buttonText, tooltipText, buttonListener );

    if( button != null )
    {
      final GridData buttonData = new GridData();
      buttonData.horizontalAlignment = SWT.END;
      buttonData.grabExcessHorizontalSpace = true;
      button.setLayoutData( buttonData );
    }

    if( radioPanel != null && button != null )
      panel.setLayout( new GridLayout( 2, false ) );
    else
      panel.setLayout( new GridLayout() );

    // if nothing was created, dispose the panel, if not it will still use some place
    if( radioPanel == null && button == null )
      panel.dispose();
    else
      panel.layout();
  }

  protected void launchAnt( final String antLaunch, final String taskName, final String title, final String errorMessage, final Map<String, Object> antProps )
  {
    runSomething( new ICoreExceptionRunnable()
    {
      public IStatus run( final IProgressMonitor monitor ) throws CoreException
      {
        final ModelNature nature = (ModelNature) getProject().getNature( ModelNature.ID );
        return nature.launchAnt( taskName, antLaunch, antProps, getCalcFolder(), new SubProgressMonitor( monitor, 2000 ) );
      }
    }, taskName, title, errorMessage );
  }

  private Button createButton( final Composite parent, final String text, final String tooltip, final SelectionListener listener )
  {
    if( text == null || listener == null )
      return null;

    final Button button = new Button( parent, SWT.NONE | SWT.PUSH );
    button.setText( text );
    button.setToolTipText( tooltip );
    button.addSelectionListener( listener );
    return button;
  }

  /**
   * Overriden in order to make it configurable by the calcWizard.xml.
   * 
   * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
   */
  @Override
  public IWizardPage getNextPage( )
  {
    final IWizardPage page = getPageFromProperty( PROP_NEXTPAGE );
    if( page != null )
      return page;

    return super.getNextPage();
  }

  /**
   * Overriden in order to make it configurable by the calcWizard.xml.
   * 
   * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
   */
  @Override
  public IWizardPage getPreviousPage( )
  {
    final IWizardPage page = getPageFromProperty( PROP_PREVPAGE );
    if( page != null )
      return page;

    return super.getPreviousPage();
  }

  private IWizardPage getPageFromProperty( final String prop )
  {
    final String nextPageID = getArguments().getProperty( prop, null );
    if( nextPageID != null )
    {
      final IWizard wizard = getWizard();
      return wizard == null ? null : wizard.getPage( nextPageID );
    }

    return null;
  }

  /**
   * Overriden in order to make it configurable by the calcWizard.xml.
   * 
   * @see org.eclipse.jface.wizard.IWizardPage#getName()
   */
  @Override
  public String getName( )
  {
    return getArguments().getProperty( PROP_PAGE_ID, super.getName() );
  }

  public void selectionChanged( final SelectionChangedEvent event )
  {
    refreshDiagram();
    if( "selected".equalsIgnoreCase( m_showZmlTable ) )
    {
      // Save the observations before refreshing else changes are lost
      saveDirtyObservations( new NullProgressMonitor() );

      refreshZMLTable();
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.ICalcWizardPage#getHelpId()
   */
  public String getHelpId( )
  {
    return getArguments().getProperty( PROP_HELP_ID, null );
  }

  private final String[] getIgnoreTypes( )
  {
    return m_ignoreType == null ? new String[0] : m_ignoreType.split( ";" );
  }

  public void setSelectSource( final int selectSource )
  {
    m_selectSource = selectSource;
  }

  public DiagView getDiagView( )
  {
    return m_diagView;
  }

  protected void handleExecutionFailure( final Exception exception )
  {
    if( AbstractCalcWizardPage.this.isCurrentPage() )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( exception );
      ErrorDialog.openError( getShell(), "", exception.getMessage(), status );
    }
  }

  /**
   * Will be called if this page is about to be closed (i.e another page is selected).<br>
   * Default behaviour is to unhook any registered context-activations.<br>
   * Client who want to override should call the super implementation, if they return <code>true</code>.
   * 
   * @return <code>false</code>, if the the page shall not be exited now.
   */
  protected boolean handlePageChanging( )
  {
    final IWizardContainer container = getContainer();
    destroyContext( container );

    return true;
  }

  /**
   * Will be called after this page has been selected.<br>
   * Default behaviour is to regsiter context-activations for this page if necessary (for example the map-context is
   * registered here, if the the page has any map.<br>
   * Client who want to override should always call the super implementation.
   */
  protected void handlePageSelected( )
  {
    final IWizardContainer container = getContainer();
    if( container instanceof WizardView && m_mapPanel != null )
    {
      final WizardView view = (WizardView) container;

      final IWorkbenchPartSite site = view.getSite();
      m_sourceProvider = new MapPanelSourceProvider( site, m_mapPanel );
    }
  }

}