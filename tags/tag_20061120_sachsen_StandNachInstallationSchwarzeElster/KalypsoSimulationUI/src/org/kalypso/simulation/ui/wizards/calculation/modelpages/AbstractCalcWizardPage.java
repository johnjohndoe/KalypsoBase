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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizard;
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
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreExceptionRunnable;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.GisTemplateFeatureTheme;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.KalypsoFeatureThemeSelection;
import org.kalypso.ogc.gml.RestoreSelectionHelper;
import org.kalypso.ogc.gml.featureview.FeatureChange;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.widgets.WidgetHelper;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.util.GisTemplateLoadedThread;
import org.kalypso.ogc.sensor.diagview.DiagView;
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
import org.kalypso.template.obstableview.ObstableviewType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypso.zml.obslink.TimeseriesLink;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureTypeProperty;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * @author Belger
 */
public abstract class AbstractCalcWizardPage extends WizardPage implements IModelWizardPage, ICommandTarget,
    ISelectionChangedListener
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
   * 
   * <ul>
   * <li>all: the timeseries of all features of the active theme are shown</li>
   * <li>selected: only the timeseries of the selected features are shown</li>
   * <li>template: no dynamic behaviiour, a template is loaded (@see #PROP_ZMLTABLE_TEMPLATE)</li>
   * </ul>
   * 
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

  /** Argument: Pfad auf Vorlage für die Karte (.gmt Datei) */
  private final static String PROP_MAPTEMPLATE = "mapTemplate";

  /** Argument: ID of the map-widget to use. Default is: {@link org.kalypso.ogc.gml.map.MapPanel#WIDGET_SINGLE_SELECT} */
  private static final String PROP_MAP_WIDGETID = "mapWidgetID";

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

  public static final int SELECT_FROM_MAPVIEW = 0;

  public static final int SELECT_FROM_TABLEVIEW = 1;

  public static final int SELECT_FROM_FEATUREVIEW = 2;

  final ICommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  private Arguments m_arguments = null;

  private IProject m_project = null;

  private IFolder m_calcFolder = null;

  private Properties m_replaceProperties = new Properties();

  private IMapModell m_mapModell = null;

  private MapPanel m_mapPanel;

  GM_Envelope m_wishBoundingBox;

  private Frame m_diagFrame = null;

  private ObservationChart m_obsChart = null;

  protected DiagView m_diagView = null;

  private Frame m_tableFrame = null;

  private TableView m_tableView = null;

  private ObstableviewType m_tableTemplate = null;

  private ObservationTable m_table = null;

  private TimeserieFeatureProps[] m_tsProps;

  private LayerTableViewer m_gisTableViewer;

  private final ControlAdapter m_controlAdapter = new ControlAdapter()
  {
    // WARNING: this adapter causes the map to bee painted twice, each time it is resized
    // the reason ist, that the map-panel itself is also a resize-listener
    // moreover, the order in which the two resize events are called is random!
    public void controlResized( final ControlEvent e )
    {
      maximizeMap();
    }
  };

  private String m_showZmlTable = "selected";

  private final int m_selectSource;

  private String m_ignoreType;

  private URL m_htmlURL = null;

  /** the one and only one selection manager for this page */
  private final IFeatureSelectionManager m_selectionManager = new FeatureSelectionManager2();

  private RestoreSelectionHelper m_selectionRestorer = null;

  public AbstractCalcWizardPage( final String name, final int selectSource )
  {
    super( name );
    m_selectSource = selectSource;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  public void dispose()
  {
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
      m_mapPanel.removeSelectionChangedListener( this );

    super.dispose();
  }

  public Arguments getArguments()
  {
    return m_arguments;
  }

  public IProject getProject()
  {
    return m_project;
  }

  public IFolder getCalcFolder()
  {
    return m_calcFolder;
  }

  public URL getContext()
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
  public void init( final IProject project, final String pagetitle, final ImageDescriptor imagedesc,
      final Arguments arguments, final IFolder calcFolder )
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
        m_htmlURL = htmlLocation.toURL();
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
  protected Properties getReplaceProperties()
  {
    return m_replaceProperties;
  }

  /**
   * Erzeugt die Karte und alle Daten die dranhängen und gibt die enthaltende Control zurück
   */
  protected Control initMap( final Composite parent ) throws IOException, JAXBException, CoreException
  {
    final String mapFileName = getArguments().getProperty( PROP_MAPTEMPLATE );
    final boolean mapContextIsCalcCase = Boolean.valueOf(
        getArguments().getProperty( PROP_MAPTEMPLATEISCALC, Boolean.TRUE.toString() ) ).booleanValue();
    final IFile mapFile = (IFile)getProject().findMember( mapFileName );
    if( mapFile == null )
      throw new CoreException( StatusUtilities.createErrorStatus( "Vorlagendatei existiert nicht: " + mapFileName ) );

    final Gismapview gisview = GisTemplateHelper.loadGisMapView( mapFile, getReplaceProperties() );
    final CS_CoordinateSystem crs = KalypsoGisPlugin.getDefault().getCoordinatesSystem();
    final URL context = mapContextIsCalcCase ? getContext() : ResourceUtilities.createURL( mapFile );
    m_mapModell = new GisTemplateMapModell( gisview, context, crs, mapFile.getProject(), m_selectionManager );
    m_mapPanel = new MapPanel( this, crs, m_selectionManager );

    m_mapPanel.addSelectionChangedListener( this );

    m_wishBoundingBox = GisTemplateHelper.getBoundingBox( gisview );

    if( "true".equals( getArguments().getProperty( PROP_MAXIMIZEMAP, "false" ) ) )
      m_wishBoundingBox = null;

    final Composite mapComposite = new Composite( parent, SWT.BORDER | SWT.RIGHT | SWT.EMBEDDED );
    final Frame virtualFrame = SWT_AWT.new_Frame( mapComposite );

    virtualFrame.setVisible( true );
    m_mapPanel.setVisible( true );
    virtualFrame.add( m_mapPanel );

    m_mapPanel.setMapModell( m_mapModell );
    m_mapPanel.onModellChange( new ModellEvent( null, ModellEvent.THEME_ADDED ) );

    final String widgetID = getArguments().getProperty( PROP_MAP_WIDGETID, MapPanel.WIDGET_SINGLE_SELECT );
    m_mapPanel.getWidgetManager().setActualWidget( WidgetHelper.createWidget( widgetID ) );

    // only do this, when map has loaded
    // m_mapPanel.setBoundingBox( m_wishBoundingBox );

    return mapComposite;
  }

  protected IMapModell getMapModell()
  {
    return m_mapModell;
  }

  public final void maximizeMap()
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
    try
    {
      final String showLegendString = getArguments().getProperty( PROP_DIAG_SHOW_LEGEND, "true" );
      final boolean showLegend = Boolean.valueOf( showLegendString ).booleanValue();

      final String ignoreType = m_arguments.getProperty( PROP_IGNORETYPE1, null );
      m_ignoreType = ignoreType;

      // actually creates the template
      m_diagView = new DiagView( true );
      m_diagView.setShowLegend( showLegend );

      m_obsChart = new ObservationChart( m_diagView );
      m_obsChart.setBackgroundPaint( Color.WHITE );

      final Composite composite = new Composite( parent, SWT.BORDER | SWT.RIGHT | SWT.EMBEDDED );
      m_diagFrame = SWT_AWT.new_Frame( composite );

      m_diagFrame.add( ChartFactory.createChartPanel( m_obsChart ) );
      m_diagFrame.setVisible( true );

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

  protected ControlAdapter getControlAdapter()
  {
    return m_controlAdapter;
  }

  public void refreshDiagram()
  {
    final TSLinkWithName[] obs = getObservations( true );
    refreshDiagramForContext( obs, getContext() );
  }

  public void refreshZMLTable()
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
    if( m_diagView != null )
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
        public void featureChanged( final FeatureChange change )
        {
        // do nothing in wizard modus
        }

        public void openFeatureRequested( final Feature feature, final FeatureTypeProperty ftp )
        {
        // do nothing in wizard modus
        }
      };
      m_gisTableViewer = new LayerTableViewer( parent, SWT.BORDER, this, KalypsoGisPlugin.getDefault()
          .createFeatureTypeCellEditorFactory(), m_selectionManager, fcl );

      final String templateFileName = (String)getArguments().get( PROP_TABLETEMPLATE );
      if( templateFileName != null )
      {
        final IFile templateFile = (IFile)getProject().findMember( templateFileName );
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
          throw new CoreException( StatusUtilities.createErrorStatus( "Keine Vorlagendatei definiert. Die Property "
              + PROP_ZMLTABLE_TEMPLATE + " muss definiert sein." ) );

        final IFile templateFile = (IFile)getProject().findMember( templateFileName );
        if( templateFile == null )
          throw new CoreException( StatusUtilities.createErrorStatus( "Vorlagendatei existiert nicht: "
              + templateFileName ) );

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

      m_table.setVisible( true );
      m_tableFrame.setVisible( true );
      m_tableFrame.add( m_table );

      return composite;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      if( e instanceof CoreException )
      {
        final IStatus status = ( (CoreException)e ).getStatus();
        ErrorDialog.openError( parent.getShell(), "Tabellenvorlage laden", "Fehler beim Laden der Tabellenvorlage",
            status );
      }

      final StringWriter sw = new StringWriter();
      e.printStackTrace( new PrintWriter( sw ) );

      final Text text = new Text( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );

      text.setText( "Fehler beim Anzeigen der Tabelle:\n\n" + e.getLocalizedMessage() + "\n\n" + sw.toString() );

      return text;
    }
  }

  public TSLinkWithName[] getObservations( boolean onlySelected )
  {
    final List selectedFeatures = onlySelected ? getSelectedFeatures() : getFeatures();

    final Collection foundObservations = new ArrayList( selectedFeatures.size() );

    for( final Iterator it = selectedFeatures.iterator(); it.hasNext(); )
    {
      final Feature kf = (Feature)it.next();

      for( int i = 0; i < m_tsProps.length; i++ )
      {
        final TimeserieFeatureProps tsprop = m_tsProps[i];

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

        final TimeseriesLink obsLink = (TimeseriesLink)kf.getProperty( tsprop.getLinkColumn() );
        if( obsLink != null )
        {
          final TSLinkWithName linkWithName = new TSLinkWithName( name, obsLink.getLinktype(), obsLink.getHref(),
              tsprop.getFilter(), tsprop.getColor(), tsprop.getLineWidth(), tsprop.getLineDash() );
          foundObservations.add( linkWithName );
        }
      }
    }

    return (TSLinkWithName[])foundObservations.toArray( new TSLinkWithName[foundObservations.size()] );
  }

  protected FeatureList getSelectedFeatures()
  {
    return getFeatures( true );
  }

  protected FeatureList getFeatures()
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
    if( activeTheme == null )
      return FeatureFactory.createFeatureList( null, null );

    final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;
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
  protected void setControl( final Control newControl )
  {
    final String helpId = getHelpId();

    // this is where we hook the help id with the given control
    if( helpId != null )
      WorkbenchHelp.setHelp( newControl, helpId );

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

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
   */
  public void performHelp()
  {
  // todo get helpid, show help
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
      public void widgetSelected( SelectionEvent e )
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
        final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;
        if( kft instanceof GisTemplateFeatureTheme )
        {
          final GisTemplateFeatureTheme gtft = (GisTemplateFeatureTheme)kft;
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
   * @throws CoreException
   * @see org.kalypso.simulation.ui.wizards.calculation.IModelWizardPage#restoreState()
   */
  public void restoreState() throws CoreException
  {
    if( m_selectionRestorer != null )
    {
      m_selectionRestorer.restoreSelection();
      m_selectionRestorer = null;
    }
  }

  /**
   * @deprecated use ant-stuff instead
   */
  protected void runCalculation()
  {
    runSomething( new ICoreExceptionRunnable()
    {

      public IStatus run( IProgressMonitor monitor ) throws CoreException
      {
        final ModelNature nature = (ModelNature)getCalcFolder().getProject().getNature( ModelNature.ID );
        final String modelspec = getArguments().getProperty( PROP_MODELSPEC, null );
        return nature.runCalculation( getCalcFolder(), new SubProgressMonitor( monitor, 2000 ), modelspec );
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
  protected void runSomething( final ICoreExceptionRunnable runnable, final String taskName, final String title,
      final String errorMessage )
  {
    final IWizard wizard = getWizard();
    if( !( wizard instanceof CalcWizard ) )
      return;

    final CalcWizard calcWizard = (CalcWizard)wizard;

    final WorkspaceModifyOperation op = new WorkspaceModifyOperation()
    {
      public void execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( taskName, 3000 );

        try
        {
          final IStatus saveStatus = calcWizard.saveAllPages( monitor );
          if( !saveStatus.isOK() )
            throw new CoreException( saveStatus );

          final IStatus status = runnable.run( monitor );

          throw new CoreException( status );
        }
        finally
        {
          monitor.done();
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, op );
    if( !status.isOK() )
      ErrorDialog.openError( getShell(), title, errorMessage, status );

    //    final WorkspaceModifyOperation op2 = new WorkspaceModifyOperation() {
    //
    //      protected void execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException,
    // InterruptedException
    //      {
    //        final IStatus result = calcWizard.refreshAllPages( monitor );
    //        if( !result.isOK() )
    //          throw new CoreException( result );
    //        
    //      }};
    //      final IStatus status2 = RunnableContextHelper.execute( getContainer(), true, true, op2 );
    //      if( !status2.isOK() )
    //        ErrorDialog.openError( getShell(), title, errorMessage, status2 );

    final Job job = new Job( "Stelle Wizard-Seiten wieder her" )
    {
      protected IStatus run( final IProgressMonitor monitor )
      {
        return calcWizard.refreshAllPages( monitor );
      }
    };
    job.addJobChangeListener( new HandleDoneJobChangeAdapter( getShell(), "Seiten Wiederherstellen",
        "Fehler beim Wiederherstellen der Seiten", true, StatusUtilities.ALL_STATUS_MASK ) );
    // TRICKY: this should only happen, if the workspaces are realy reloaded.
    // This HACK just gives one second to do this. Is this always enough?
    // Better would be some code, which is called, after all Worspace-Modification events of
    // operastion 1 are handled. Is this possible?
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
  protected void selectFeaturesInMap( final String[] fids, final boolean noDefaultSelection,
      final boolean forceSelectFeatureFromMap )
  {
    final IFeatureSelectionManager selectionManager = m_selectionManager;
    final IMapModell mapModell = getMapModell();

    // this may happen, if the page was never created
    if( mapModell == null )
      return;

    final String showZmlTable = m_showZmlTable;

    new GisTemplateLoadedThread( mapModell, new Runnable()
    {
      public void run()
      {
        //        final IKalypsoTheme activeTheme = m_mapModell.getActiveTheme();
        //        final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;

        boolean refreshDiagramHack = true;

        // erstes feature des aktiven themas selektieren
        final IKalypsoTheme activeTheme = mapModell.getActiveTheme();
        if( activeTheme instanceof IKalypsoFeatureTheme )
        {
          final IKalypsoFeatureTheme kft = (IKalypsoFeatureTheme)activeTheme;

          final String panToFid = getArguments().getProperty( PROP_PAN_TO_FEATURE_ID, null );

          if( kft != null && panToFid != null )
          {
            final CommandableWorkspace workspace = kft.getWorkspace();
            final Feature feature = workspace.getFeature( panToFid );
            if( feature != null )
            {
              GM_Object defaultGeometryProperty = feature.getDefaultGeometryProperty();
              GM_Point centroid = defaultGeometryProperty.getCentroid();
              m_wishBoundingBox = m_wishBoundingBox.getPaned( centroid );

              if( forceSelectFeatureFromMap )
              {
                // FIX: damit überhaupt was selektiert wird, sonst passiert nichts
                // see also comment on parameter 'forceSelectFeatureFromMap'
                final Feature[] featuresToRemove = FeatureSelectionHelper.getFeatures( selectionManager );
                selectionManager.changeSelection( featuresToRemove, new EasyFeatureWrapper[]
                { new EasyFeatureWrapper( workspace, feature, null, null ) } );
              }
            }
          }

          if( !noDefaultSelection )
          {
            final CommandableWorkspace workspace = kft.getWorkspace();

            final FeatureList featureList = kft.getFeatureListVisible( null );
            if( featureList != null && featureList.size() != 0 )
            {
              final List easyFeatures = new ArrayList( fids.length );
              for( int i = 0; i < fids.length; i++ )
              {
                final String fid = fids[i];
                if( fid != null )
                {
                  final Feature feature = workspace.getFeature( fid );
                  if( feature != null )
                    easyFeatures.add( new EasyFeatureWrapper( workspace, feature, null, null ) );
                }
              }
              final EasyFeatureWrapper[] easyArray;
              if( easyFeatures.isEmpty() && selectFirstFeatureByDefault() )
                easyArray = new EasyFeatureWrapper[]
                { new EasyFeatureWrapper( workspace, (Feature)featureList.get( 0 ), null, null ) };
              else
                easyArray = (EasyFeatureWrapper[])easyFeatures.toArray( new EasyFeatureWrapper[easyFeatures.size()] );

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
      }
    } ).start();
  }

  protected String[] getFeaturesToSelect()
  {
    final String fid = getArguments().getProperty( PROP_FEATURE_TO_SELECT_ID, null );
    return fid == null ? new String[] {} : new String[]
    { fid };
  }

  protected boolean selectFirstFeatureByDefault()
  {
    return "true".equals( getArguments().getProperty( PROP_SELECT_FIRST_FEATURE_BY_DEFAULT, "true" ) );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.view.IHtmlWizardPage#getHtmlURL()
   */
  public URL getHtmlURL()
  {
    return m_htmlURL;
  }

  protected void initButtons( final Composite parent, final String buttonText, final String tooltipText,
      final SelectionListener buttonListener )
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
      public void widgetSelected( final SelectionEvent e )
      {
        launchAnt( antLaunch, buttonTooltipOwn, buttonTextOwn, buttonTooltipOwn, buttonProperties );
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

  protected void launchAnt( final String antLaunch, final String taskName, final String title,
      final String errorMessage, final Map antProps )
  {
    runSomething( new ICoreExceptionRunnable()
    {
      public IStatus run( final IProgressMonitor monitor ) throws CoreException
      {
        final ModelNature nature = (ModelNature)getProject().getNature( ModelNature.ID );
        return nature
            .launchAnt( taskName, antLaunch, antProps, getCalcFolder(), new SubProgressMonitor( monitor, 2000 ) );
      }
    }, taskName, title, errorMessage );
  }

  private Button createButton( final Composite parent, final String text, final String tooltip,
      final SelectionListener listener )
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
  public IWizardPage getNextPage()
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
  public IWizardPage getPreviousPage()
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
  public String getName()
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
  public String getHelpId()
  {
    return getArguments().getProperty( PROP_HELP_ID, null );
  }

  private final String[] getIgnoreTypes()
  {
    return m_ignoreType == null ? new String[0] : m_ignoreType.split( ";" );
  }
}